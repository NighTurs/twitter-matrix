package com.github.nighturs.twitter.matrix.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.contrib.jms.JmsMessageProducer;
import backtype.storm.contrib.jms.bolt.JmsBolt;
import backtype.storm.topology.TopologyBuilder;
import org.aeonbits.owner.ConfigFactory;

public final class TweetProcessorTopology {
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
        config.put(TwitterPublicStreamSpout.API_CONSUMER_KEY_PROP, apiConfig.twitterApiConsumerKey());
        config.put(TwitterPublicStreamSpout.API_CONSUMER_SECRET_PROP, apiConfig.twitterApiConsumerSecret());
        config.put(TwitterPublicStreamSpout.API_TOKEN_PROP, apiConfig.twitterApiToken());
        config.put(TwitterPublicStreamSpout.API_TOKEN_SECRET_PROP, apiConfig.twitterApiTokenSecret());
        config.put(TwitterPublicStreamSpout.API_PARAMETERS_TRACK, apiConfig.twitterApiParametersTrack());
        config.put(TwitterPublicStreamSpout.API_PARAMETERS_LANGUAGE, apiConfig.twitterApiParametersLanguage());

        new LocalCluster().submitTopology(TWEET_PROCESSOR, config, builder.createTopology());
    }
}