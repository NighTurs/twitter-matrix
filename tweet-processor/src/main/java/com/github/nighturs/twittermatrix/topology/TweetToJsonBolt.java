package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.Tweet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;

class TweetToJsonBolt extends BaseBasicBolt {

    private static final String JSON_TWEET_FIELD = "tweetJson";
    private static final Gson gson = new GsonBuilder().create();

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
        String json = gson.toJson(new TweetView(tweet));
        collector.emit(Lists.newArrayList(json));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(JSON_TWEET_FIELD));
    }

    @SuppressWarnings("unused")
    private static class TweetView {

        private final String tweetUrl;
        private final String tweetText;

        TweetView(Tweet tweet) {
            tweetUrl = tweet.getUrl();
            tweetText = tweet.getText();
        }
    }
}
