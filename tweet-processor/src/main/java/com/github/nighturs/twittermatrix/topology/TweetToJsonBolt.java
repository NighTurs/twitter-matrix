package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.Tweet;
import com.github.nighturs.twittermatrix.TweetPhrase;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;

class TweetToJsonBolt extends BaseBasicBolt {

    private static final String JSON_TWEET_FIELD = "tweetJson";
    private static final Gson gson = new GsonBuilder().create();

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
        String json = gson.toJson(TweetView.of(tweet));
        collector.emit(Lists.newArrayList(json));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(JSON_TWEET_FIELD));
    }

    @Data
    private static class TweetView {

        @NonNull
        private final String tweetUrl;
        @NonNull
        private final String tweetText;
        @NonNull
        private final List<String> phrases;

        private static TweetView of(Tweet tweet) {
            return new TweetView(tweet.getUrl(),
                    tweet.getText(),
                    tweet.getMatchedPhrases().stream().map(TweetPhrase::getPhrase).collect(Collectors.toList()));
        }
    }
}
