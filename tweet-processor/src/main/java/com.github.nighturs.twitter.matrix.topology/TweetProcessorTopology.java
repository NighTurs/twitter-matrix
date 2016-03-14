package com.github.nighturs.twitter.matrix.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.contrib.jms.JmsMessageProducer;
import backtype.storm.contrib.jms.bolt.JmsBolt;
import backtype.storm.topology.TopologyBuilder;
import org.aeonbits.owner.ConfigFactory;

public final class TweetProcessorTopology {
    public static final String TWITTER_PUBLIC_STREAM_SPOUT = "TWITTER_PUBLIC_STREAM_SPOUT";
    public static final String JMS_TWEET_TOPIC_BOLT = "JMS_TWEET_TOPIC_BOLT";
    public static final String TWEET_PROCESSOR = "TWEET_PROCESSOR";
    public static final String TWEET_TOPIC = "TWEET_TOPIC";

    private TweetProcessorTopology() {
    }

    public static void main(String[] args) {
        TwitterApiConfig apiConfig = ConfigFactory.create(TwitterApiConfig.class);
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(TWITTER_PUBLIC_STREAM_SPOUT, new TwitterPublicStreamSpout());

        JmsBolt tweetJmsBolt = new JmsBolt();
        tweetJmsBolt.setJmsProvider(new LocalActiveMQJmsProvider(TWEET_TOPIC));
        tweetJmsBolt.setJmsMessageProducer((JmsMessageProducer) (session, input) -> session.createTextMessage(input.getStringByField(
                "TWEET_TEXT")));
        builder.setBolt(JMS_TWEET_TOPIC_BOLT, tweetJmsBolt).shuffleGrouping(TWITTER_PUBLIC_STREAM_SPOUT);

        Config config = new Config();
        config.put(TwitterPublicStreamSpout.API_CONSUMER_KEY_PROP, apiConfig.twitterApiConsumerKey());
        config.put(TwitterPublicStreamSpout.API_CONSUMER_SECRET_PROP, apiConfig.twitterApiConsumerSecret());
        config.put(TwitterPublicStreamSpout.API_TOKEN_PROP, apiConfig.twitterApiToken());
        config.put(TwitterPublicStreamSpout.API_TOKEN_SECRET_PROP, apiConfig.twitterApiTokenSecret());

        new LocalCluster().submitTopology(TWEET_PROCESSOR, config, builder.createTopology());
    }
}