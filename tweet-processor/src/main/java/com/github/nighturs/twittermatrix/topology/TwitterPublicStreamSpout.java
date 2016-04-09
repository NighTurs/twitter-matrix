package com.github.nighturs.twittermatrix.topology;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import com.github.nighturs.twittermatrix.ActiveMqConfig;
import com.github.nighturs.twittermatrix.Tweet;
import com.github.nighturs.twittermatrix.TweetPhrase;
import com.github.nighturs.twittermatrix.TwitterStreamParams;
import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;

class TwitterPublicStreamSpout extends BaseRichSpout {

    private static final Logger logger = LoggerFactory.getLogger(TwitterPublicStreamSpout.class);
    private static final int MSG_QUEUE_CAPACITY = 100000;
    private TwitterApiConfig twitterApiConfig;
    private BlockingQueue<Status> statusQueue;
    private SpoutOutputCollector spoutOutputCollector;
    private Twitter4jStatusClient t4jClient;
    @SuppressWarnings("FieldCanBeLocal")
    private TwitterStreamParamsMessageListener paramsMessageListener;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_FIELD));
    }

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.twitterApiConfig = ConfigUtils.createFromStormConf(TwitterApiConfig.class, conf);
        ActiveMqConfig activeMqConfig = ConfigUtils.createFromStormConf(ActiveMqConfig.class, conf);
        this.spoutOutputCollector = collector;
        this.statusQueue = new LinkedBlockingQueue<>();
        this.paramsMessageListener = new TwitterStreamParamsMessageListener(this::onApiParamsUpdate);
        paramsMessageListener.listenStreamParamChanges(activeMqConfig);
    }

    private void restartApiClient(TwitterStreamParams params) {
        if (t4jClient != null && !t4jClient.isDone()) {
            logger.info("Stopping current twitter client");
            t4jClient.stop();
        }

        logger.info("Staring new twitter client, Params={}", params);
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(MSG_QUEUE_CAPACITY);

        Hosts apiHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint apiEndpoint = new StatusesFilterEndpoint();
        apiEndpoint.languages(params.getLanguages());
        apiEndpoint.trackTerms(params.getTrackPhrases()
                .stream()
                .map(TweetPhrase::getPhrase)
                .collect(Collectors.toList()));

        Authentication apiAuth = new OAuth1(twitterApiConfig.twitterApiConsumerKey(),
                twitterApiConfig.twitterApiConsumerSecret(),
                twitterApiConfig.twitterApiToken(),
                twitterApiConfig.twitterApiTokenSecret());

        ClientBuilder builder = new ClientBuilder().name("twitter-public-stream-client-01")
                .hosts(apiHosts)
                .authentication(apiAuth)
                .endpoint(apiEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client apiClient = builder.build();

        t4jClient = new Twitter4jStatusClient(apiClient,
                msgQueue,
                Lists.newArrayList(new BasicStatusListener(statusQueue)),
                Executors.newSingleThreadExecutor());
        t4jClient.connect();
        t4jClient.process();
    }

    @Override
    public void nextTuple() {
        Status status = statusQueue.poll();
        if (status != null) {
            Tweet tweet = Tweet.of(status);
            spoutOutputCollector.emit(Lists.newArrayList(tweet));
            logger.info("Produced, Tweet={}, Status={}", tweet, status);
        }
    }

    private void onApiParamsUpdate(TwitterStreamParams params) {
        restartApiClient(params);
    }

    private static class BasicStatusListener implements StatusListener {

        private BlockingQueue<Status> statusQueue;

        BasicStatusListener(BlockingQueue<Status> statusQueue) {
            this.statusQueue = statusQueue;
        }

        @Override
        public void onException(Exception ex) {

        }

        @Override
        public void onStatus(Status status) {
            if (!statusQueue.offer(status)) {
                logger.warn("Status queue capacity reached, Status={}", status);
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {

        }

        @Override
        public void onStallWarning(StallWarning warning) {

        }
    }
}
