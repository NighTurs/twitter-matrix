package com.github.nighturs.twittermatrix.topology;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.ActiveMqConfig;
import com.github.nighturs.twittermatrix.TwitterStreamParams;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;

class TweetPhraseMatcherBolt extends BaseBasicBolt {

    private static final Logger logger = LoggerFactory.getLogger(TweetPhraseMatcherBolt.class);
    private static final int SPACE_CODEPOINT = Character.codePointAt(" ", 0);
    AtomicReference<TrackPhrases> trackPhrases = new AtomicReference<>();
    @SuppressWarnings("FieldCanBeLocal")
    private TwitterStreamParamsMessageListener paramsMessageListener;

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        ActiveMqConfig activeMqConfig = ConfigFactory.create(ActiveMqConfig.class);
        paramsMessageListener = new TwitterStreamParamsMessageListener(this::onApiParamsUpdate);
        paramsMessageListener.listenStreamParamChanges(activeMqConfig);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
        List<String> matchedPhrases = findMatchedPhrases(tweet.getText());
        if (matchedPhrases.isEmpty()) {
            logger.warn("No phrases matched, Tweet={}", tweet);
        } else {
            logger.info("Matched phrases, Tweet={}, Phrases={}", tweet, matchedPhrases);
        }
        collector.emit(Lists.newArrayList(tweet.withMatchedPhrases(matchedPhrases)));
    }

    List<String> findMatchedPhrases(String tweetText) {
        StringBuilder sb = tweetText
                .toLowerCase(Locale.ROOT)
                .chars()
                .map(x -> Character.isWhitespace(x) ? SPACE_CODEPOINT : x)
                .filter(x -> Character.isAlphabetic(x) || Character.isWhitespace(x))
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append);
        TrackPhrases tp = trackPhrases.get();
        Map<String, Integer> termsCountPerPhrase = Maps.newHashMap(tp.termsCountPerPhrase);
        List<String> matchedPhrases = new ArrayList<>();
        for (String term : Splitter.on(" ").split(sb)) {
            for (String phrase : tp.phrasesPerTerm.get(term)) {
                Integer matches = termsCountPerPhrase.get(phrase) - 1;
                if (matches.equals(0)) {
                    matchedPhrases.add(phrase);
                }
                termsCountPerPhrase.put(phrase, matches);
            }
        }
        return matchedPhrases;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_FIELD));
    }

    void onApiParamsUpdate(TwitterStreamParams params) {
        Map<String, Integer> termsCountPerPhrase = new HashMap<>();
        Multimap<String, String> prhasesPerTerm = HashMultimap.create();

        for (String phrase : params.getTrackPhrases()) {
            List<String> terms = Splitter.on(" ")
                    .splitToList(phrase.toLowerCase(Locale.ROOT))
                    .stream()
                    .filter(x -> !x.isEmpty())
                    .collect(Collectors.toList());
            termsCountPerPhrase.put(phrase, terms.size());
            for (String term : terms) {
                prhasesPerTerm.put(term, phrase);
            }
        }
        TrackPhrases newTrackPhrases = new TrackPhrases(termsCountPerPhrase, prhasesPerTerm);
        logger.info("New twitter stream params, Params={}, TrackPhrases={}", params, newTrackPhrases);
        trackPhrases.set(newTrackPhrases);
    }

    @SuppressWarnings("WeakerAccess")
    static class TrackPhrases {
        Map<String, Integer> termsCountPerPhrase;
        Multimap<String, String> phrasesPerTerm;

        TrackPhrases(Map<String, Integer> termsCountPerPhrase, Multimap<String, String> phrasesPerTerm) {
            this.termsCountPerPhrase = termsCountPerPhrase;
            this.phrasesPerTerm = phrasesPerTerm;
        }
    }
}