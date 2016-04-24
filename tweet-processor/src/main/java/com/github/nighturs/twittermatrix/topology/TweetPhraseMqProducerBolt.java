package com.github.nighturs.twittermatrix.topology;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.RabbitMqUtil;
import com.github.nighturs.twittermatrix.config.ConfigUtils;
import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.google.common.base.Charsets;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.github.nighturs.twittermatrix.topology.TweetPhrasesToJsonBolt.JSON_TWEET_PHRASES_FIELD;

class TweetPhraseMqProducerBolt extends BaseBasicBolt {

    private static final Logger logger = LoggerFactory.getLogger(TweetPhraseMqProducerBolt.class);
    private Connection mqConn;
    private Channel mqChannel;
    private RabbitMqConfig mqConfig;

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        mqConfig = ConfigUtils.createFromStormConf(RabbitMqConfig.class, stormConf);
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
        String tweetPhrase = input.getStringByField(JSON_TWEET_PHRASES_FIELD);
        try {
            mqChannel.basicPublish(mqConfig.rabbitMqTwitterTweetPhrasesExchange(),
                    "",
                    null,
                    tweetPhrase.getBytes(Charsets.UTF_8));
            logger.info("Published tweet phrase, TweetPhrase={}", tweetPhrase);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to publish tweetPhrase, TweetPhrase=%s", tweetPhrase), e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
