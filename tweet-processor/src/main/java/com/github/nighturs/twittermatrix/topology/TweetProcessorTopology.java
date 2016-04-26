package com.github.nighturs.twittermatrix.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.config.TwitterApiConfig;
import com.github.nighturs.twittermatrix.domain.Tweet;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;

final class TweetProcessorTopology {

    private static final String TWITTER_PUBLIC_STREAM_SPOUT = "TWITTER_PUBLIC_STREAM_SPOUT";
    private static final String TWEET_MQ_PRODUCER_BOLT = "TWEET_MQ_PRODUCER_BOLT";
    private static final String TWEET_PROCESSOR = "TWEET_PROCESSOR";
    private static final String TWEET_TO_JSON_BOLT = "TWEET_TO_JSON_BOLT";
    private static final String TWEET_PHRASE_MATCHER_BOLT = "TWEET_PHRASE_MATCHER_BOLT";
    private static final String TWEET_PHRASE_SPOUT = "TWEET_PHRASE_SPOUT";
    private static final String TWEET_PHRASE_STATISTICS_BOLT = "TWEET_PHRASE_STATISTICS_BOLT";
    private static final String JMS_TWEET_PHRASES_TOPIC_BOLT = "JMS_TWEET_PHRASES_TOPIC_BOLT";
    private static final String TWEET_PHRASE_TO_JSON_BOLT = "TWEET_PHRASE_TO_JSON_BOLT";
    static final String TWEET_FIELD = "tweet";
    static final String TWEET_PHRASES_FIELD = "tweetPhrases";

    private TweetProcessorTopology() {
    }

    public static void main(String[] args) {
        TwitterApiConfig apiConfig = ConfigFactory.create(TwitterApiConfig.class);
        RabbitMqConfig mqConfig = ConfigFactory.create(RabbitMqConfig.class);
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(TWITTER_PUBLIC_STREAM_SPOUT, new TwitterPublicStreamSpout());
        builder.setBolt(TWEET_PHRASE_MATCHER_BOLT, new TweetPhraseMatcherBolt())
                .shuffleGrouping(TWITTER_PUBLIC_STREAM_SPOUT);
        builder.setBolt(TWEET_MQ_PRODUCER_BOLT, new TweetMqProducerBolt()).shuffleGrouping(TWEET_PHRASE_MATCHER_BOLT);

        builder.setSpout(TWEET_PHRASE_SPOUT, new TweetPhraseSpout());
        builder.setBolt(TWEET_PHRASE_STATISTICS_BOLT, new TweetPhraseStatisticsBolt())
                .shuffleGrouping(TWEET_PHRASE_SPOUT)
                .shuffleGrouping(TWEET_PHRASE_MATCHER_BOLT);
        builder.setBolt(JMS_TWEET_PHRASES_TOPIC_BOLT, new TweetPhraseMqProducerBolt())
                .shuffleGrouping(TWEET_PHRASE_STATISTICS_BOLT);

        Config config = new Config();
        config.registerSerialization(Tweet.class);
        config.registerSerialization(List.class);
        config.registerSerialization(TweetPhrase.class);
        for (String propName : apiConfig.propertyNames()) {
            config.put(propName, apiConfig.getProperty(propName));
        }
        for (String propName : mqConfig.propertyNames()) {
            config.put(propName, apiConfig.getProperty(propName));
        }

        new LocalCluster().submitTopology(TWEET_PROCESSOR, config, builder.createTopology());
    }
}