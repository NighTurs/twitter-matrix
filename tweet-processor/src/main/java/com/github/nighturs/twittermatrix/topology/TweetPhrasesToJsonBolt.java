package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_PHRASES_FIELD;

class TweetPhrasesToJsonBolt extends BaseBasicBolt {

    static final String JSON_TWEET_PHRASES_FIELD = "tweetPhrasesJson";
    private static final Gson gson = new GsonBuilder().create();

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        @SuppressWarnings("unchecked")
        List<TweetPhrase> tweetPhrases = (List<TweetPhrase>) input.getValueByField(TWEET_PHRASES_FIELD);
        String json = gson.toJson(new TweetPhrasesView(tweetPhrases));
        collector.emit(Collections.singletonList(json));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(JSON_TWEET_PHRASES_FIELD));
    }

    @Data
    private static class TweetPhrasesView {

        @NonNull
        private final List<TweetPhrase> phrases;
    }
}
