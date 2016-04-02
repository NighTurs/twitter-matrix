package com.github.nighturs.twittermatrix.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.contrib.jms.bolt.JmsBolt;
import backtype.storm.topology.TopologyBuilder;
import com.github.nighturs.twittermatrix.ActiveMqConfig;
import org.aeonbits.owner.ConfigFactory;

final class TweetProcessorTopology {

    private static final String TWITTER_PUBLIC_STREAM_SPOUT = "TWITTER_PUBLIC_STREAM_SPOUT";
    private static final String JMS_TWEET_TOPIC_BOLT = "JMS_TWEET_TOPIC_BOLT";
    private static final String TWEET_PROCESSOR = "TWEET_PROCESSOR";
    private static final String TWEET_TO_JSON_BOLT = "TWEET_TO_JSON_BOLT";

    private TweetProcessorTopology() {
    }

    public static void main(String[] args) {
        TwitterApiConfig apiConfig = ConfigFactory.create(TwitterApiConfig.class);
        ActiveMqConfig mqConfig = ConfigFactory.create(ActiveMqConfig.class);
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(TWITTER_PUBLIC_STREAM_SPOUT, new TwitterPublicStreamSpout());
        builder.setBolt(TWEET_TO_JSON_BOLT, new TweetToJsonBolt()).shuffleGrouping(TWITTER_PUBLIC_STREAM_SPOUT);

        JmsBolt tweetJmsBolt = new JmsBolt();
        tweetJmsBolt.setJmsProvider(new TweetJmsProvider(mqConfig));
        tweetJmsBolt.setJmsMessageProducer(new TweetMessageProducer());
        builder.setBolt(JMS_TWEET_TOPIC_BOLT, tweetJmsBolt).shuffleGrouping(TWEET_TO_JSON_BOLT);

        Config config = new Config();
        config.registerSerialization(Tweet.class);
        for (String propName : apiConfig.propertyNames()) {
            config.put(propName, apiConfig.getProperty(propName));
        }
        for (String propName : mqConfig.propertyNames()) {
            config.put(propName, apiConfig.getProperty(propName));
        }

        new LocalCluster().submitTopology(TWEET_PROCESSOR, config, builder.createTopology());
    }
}