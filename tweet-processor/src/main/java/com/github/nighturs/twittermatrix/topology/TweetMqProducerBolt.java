package com.github.nighturs.twittermatrix.topology;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.RabbitMqUtil;
import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.domain.Tweet;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

class TweetMqProducerBolt extends BaseBasicBolt {

    private static final Logger logger = LoggerFactory.getLogger(TweetMqProducerBolt.class);
    private static final Gson gson = new GsonBuilder().create();
    private Connection mqConn;
    private Channel mqChannel;
    private final RabbitMqConfig mqConfig;

    TweetMqProducerBolt(RabbitMqConfig mqConfig) {
        this.mqConfig = mqConfig;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        ConnectionFactory factory = RabbitMqUtil.connectionFactory(mqConfig);
        try {
            mqConn = factory.newConnection();
            mqChannel = mqConn.createChannel();
            mqChannel.exchangeDeclare(mqConfig.rabbitMqTwitterTweetExchange(), "direct");
        } catch (TimeoutException | IOException e) {
            throw new RuntimeException("Error while establishing queue connection", e);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        try {
            mqChannel.close();
            mqConn.close();
        } catch (TimeoutException | IOException e) {
            logger.warn("Error while closing queue connection", e);
        }
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TweetProcessorTopology.TWEET_FIELD);
        String json = gson.toJson(TweetView.of(tweet));
        try {
            for (TweetPhrase phrase : tweet.getMatchedPhrases()) {
                mqChannel.basicPublish(mqConfig.rabbitMqTwitterTweetExchange(),
                        phrase.id(),
                        null,
                        json.getBytes(Charsets.UTF_8));
            }
            logger.info("Published tweet, Tweet={}", tweet);
        } catch (IOException e) {
            logger.error("Failed to publish tweet, Tweet={}", tweet, e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Value
    private static class TweetView {

        @NonNull
        private final Long id;
        @NonNull
        private final String url;
        @NonNull
        private final String text;
        @NonNull
        private final List<TweetPhraseView> phrases;

        private static TweetView of(Tweet tweet) {
            return new TweetView(tweet.getId(),
                    tweet.getUrl(),
                    tweet.getText(),
                    tweet.getMatchedPhrases().stream().map(TweetPhraseView::of).collect(Collectors.toList()));
        }

        @Value
        private static class TweetPhraseView {

            @NonNull
            private final String phrase;
            @NonNull
            private final String id;

            static TweetPhraseView of(TweetPhrase tweetPhrase) {
                return new TweetPhraseView(tweetPhrase.getPhrase(), tweetPhrase.id());
            }
        }
    }
}
