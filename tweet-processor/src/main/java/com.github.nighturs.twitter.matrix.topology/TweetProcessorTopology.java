package com.github.nighturs.twitter.matrix.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.contrib.jms.JmsMessageProducer;
import backtype.storm.contrib.jms.bolt.JmsBolt;
import backtype.storm.topology.TopologyBuilder;
import org.aeonbits.owner.ConfigFactory;

final class TweetProcessorTopology {

    private static final String TWITTER_PUBLIC_STREAM_SPOUT = "TWITTER_PUBLIC_STREAM_SPOUT";
    private static final String JMS_TWEET_TOPIC_BOLT = "JMS_TWEET_TOPIC_BOLT";
    private static final String TWEET_PROCESSOR = "TWEET_PROCESSOR";

    private TweetProcessorTopology() {
    }

    public static void main(String[] args) {
        TwitterApiConfig apiConfig = ConfigFactory.create(TwitterApiConfig.class);
        ActiveMqConfig mqConfig = ConfigFactory.create(ActiveMqConfig.class);
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(TWITTER_PUBLIC_STREAM_SPOUT, new TwitterPublicStreamSpout());

        JmsBolt tweetJmsBolt = new JmsBolt();
        tweetJmsBolt.setJmsProvider(new TweetJmsProvider(mqConfig));
        tweetJmsBolt.setJmsMessageProducer((JmsMessageProducer) (session, input) -> session.createTextMessage(input.getStringByField(
                "TWEET_TEXT")));
        builder.setBolt(JMS_TWEET_TOPIC_BOLT, tweetJmsBolt).shuffleGrouping(TWITTER_PUBLIC_STREAM_SPOUT);

        Config config = new Config();
        for (String propName : apiConfig.propertyNames()) {
            config.put(propName, apiConfig.getProperty(propName));
        }

        new LocalCluster().submitTopology(TWEET_PROCESSOR, config, builder.createTopology());
    }
}