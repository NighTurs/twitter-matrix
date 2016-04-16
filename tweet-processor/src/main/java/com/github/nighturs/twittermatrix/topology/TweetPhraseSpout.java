package com.github.nighturs.twittermatrix.topology;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import com.github.nighturs.twittermatrix.config.ActiveMqConfig;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.github.nighturs.twittermatrix.domain.TwitterStreamParams;
import org.aeonbits.owner.ConfigFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class TweetPhraseSpout extends BaseRichSpout {

    private static final long TUPLE_RATE_MILLIS = 5000;
    @SuppressWarnings("FieldCanBeLocal")
    private TwitterStreamParamsMessageListener paramsMessageListener;
    private AtomicReference<List<TweetPhrase>> trackPhrases;
    private SpoutOutputCollector spoutOutputCollector;

    @SuppressWarnings("rawtypes")
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        ActiveMqConfig activeMqConfig = ConfigFactory.create(ActiveMqConfig.class);
        paramsMessageListener = new TwitterStreamParamsMessageListener(this::onApiParamsUpdate);
        paramsMessageListener.listenStreamParamChanges(activeMqConfig);
        spoutOutputCollector = collector;
        trackPhrases = new AtomicReference<>();
    }

    @Override
    public void nextTuple() {
        try {
            Thread.sleep(TUPLE_RATE_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        final List<TweetPhrase> phrases = trackPhrases.get();
        if (phrases != null) {
            spoutOutputCollector.emit(Collections.singletonList(phrases));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TweetProcessorTopology.TWEET_PHRASES_FIELD));
    }

    private void onApiParamsUpdate(TwitterStreamParams params) {
        trackPhrases.set(params.getTrackPhrases());
    }
}
