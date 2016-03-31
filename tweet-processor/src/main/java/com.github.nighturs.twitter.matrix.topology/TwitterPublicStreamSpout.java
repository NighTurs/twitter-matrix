package com.github.nighturs.twitter.matrix.topology;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import com.google.common.base.Splitter;
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
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

class TwitterPublicStreamSpout extends BaseRichSpout {

    private static final Logger logger = LoggerFactory.getLogger(TwitterPublicStreamSpout.class);
    private static final int MSG_QUEUE_CAPACITY = 100000;
    private TwitterApiConfig twitterApiConfig;
    private BlockingQueue<Status> statusQueue;
    private SpoutOutputCollector spoutOutputCollector;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("TWEET_TEXT"));
    }

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context, SpoutOutputCollector collector) {
        //noinspection unchecked
        Map<String, String> nonNullValuesConf = ((Map<String, String>) conf).entrySet()
                .stream()
                .filter(x -> x.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        twitterApiConfig = ConfigFactory.create(TwitterApiConfig.class, nonNullValuesConf);
        this.spoutOutputCollector = collector;
        this.statusQueue = new LinkedBlockingQueue<>();
        setupApiClient();
    }

    private void setupApiClient() {
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(MSG_QUEUE_CAPACITY);

        Hosts apiHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint apiEndpoint = new StatusesFilterEndpoint();
        apiEndpoint.languages(Collections.singletonList(twitterApiConfig.twitterApiParametersLanguage()));
        apiEndpoint.trackTerms(Lists.newArrayList(Splitter.on(",").split(twitterApiConfig.twitterApiParametersTrack())));

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

        Twitter4jStatusClient t4jClient =
                new Twitter4jStatusClient(apiClient, msgQueue, Lists.newArrayList(new StatusListener() {
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
                }), Executors.newSingleThreadExecutor());
        t4jClient.connect();
        t4jClient.process();
    }

    @Override
    public void nextTuple() {
        Status status = statusQueue.poll();
        if (status != null) {
            String tweetJson = String.format(
                    "{\"tweetUrl\":\"https://twitter.com/user/status/%s\",\"tweetText\":\"%s\"}",
                    status.getId(),
                    status.getText());
            spoutOutputCollector.emit(Lists.newArrayList(tweetJson));
            logger.info("Produced, Text={}", tweetJson);
        }
    }
}
