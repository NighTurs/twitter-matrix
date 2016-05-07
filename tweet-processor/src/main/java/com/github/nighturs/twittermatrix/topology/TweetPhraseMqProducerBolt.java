package com.github.nighturs.twittermatrix.topology;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.RabbitMqUtil;
import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.github.nighturs.twittermatrix.domain.TweetPhraseStats;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_PHRASES_FIELD;

class TweetPhraseMqProducerBolt extends BaseBasicBolt {

    private static final Logger logger = LoggerFactory.getLogger(TweetPhraseMqProducerBolt.class);
    private static final Gson gson = new GsonBuilder().create();
    private Connection mqConn;
    private Channel mqChannel;
    private final RabbitMqConfig mqConfig;

    TweetPhraseMqProducerBolt(RabbitMqConfig mqConfig) {
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
            mqChannel.exchangeDeclare(mqConfig.rabbitMqTwitterTweetPhrasesExchange(), "fanout");
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
        @SuppressWarnings("unchecked")
        List<TweetPhrase> tweetPhrases = (List<TweetPhrase>) input.getValueByField(TWEET_PHRASES_FIELD);
        String json = gson.toJson(new TweetPhrasesView(tweetPhrases.stream()
                .map(TweetPhraseView::of)
                .collect(Collectors.toList())));
        try {
            mqChannel.basicPublish(mqConfig.rabbitMqTwitterTweetPhrasesExchange(),
                    "",
                    null,
                    json.getBytes(Charsets.UTF_8));
            logger.info("Published tweet phrase, TweetPhrases={}", tweetPhrases);
        } catch (IOException e) {
            logger.error("Failed to publish tweetPhrase, TweetPhrases={}", tweetPhrases, e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Data
    private static class TweetPhrasesView {

        @NonNull
        private final List<TweetPhraseView> phrases;
    }

    @Data
    private static class TweetPhraseView {
        @NonNull
        private final String phrase;
        @NonNull
        private final String id;
        private final TweetPhraseStats stats;

        static TweetPhraseView of(TweetPhrase tweetPhrase) {
            return new TweetPhraseView(tweetPhrase.getPhrase(), tweetPhrase.id(), tweetPhrase.getStats());
        }
    }
}
