package com.github.nighturs.twittermatrix.topology;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.domain.Tweet;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.github.nighturs.twittermatrix.domain.TwitterStreamParams;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.Data;
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
    private final RabbitMqConfig mqConfig;

    TweetPhraseMatcherBolt(RabbitMqConfig mqConfig) {
        this.mqConfig = mqConfig;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        trackPhrases.set(new TrackPhrases(new HashMap<>(), HashMultimap.create()));
        paramsMessageListener = new TwitterStreamParamsMessageListener(this::onApiParamsUpdate);
        paramsMessageListener.listenStreamParamChanges(mqConfig);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
        List<TweetPhrase> matchedPhrases = findMatchedPhrases(tweet.getText());
        if (matchedPhrases.isEmpty()) {
            logger.debug("No phrases matched, Tweet={}", tweet);
        } else {
            logger.debug("Matched phrases, Tweet={}, Phrases={}", tweet, matchedPhrases);
        }
        collector.emit(Lists.newArrayList(tweet.withMatchedPhrases(matchedPhrases)));
    }

    List<TweetPhrase> findMatchedPhrases(String tweetText) {
        StringBuilder sb = tweetText.toLowerCase(Locale.ROOT)
                .chars()
                .map(x -> Character.isWhitespace(x) || !Character.isAlphabetic(x) ? SPACE_CODEPOINT : x)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append);
        TrackPhrases tp = trackPhrases.get();
        Map<TweetPhrase, Integer> termsCountPerPhrase = Maps.newHashMap(tp.termsCountPerPhrase);
        List<TweetPhrase> matchedPhrases = new ArrayList<>();
        for (String term : Splitter.on(" ").split(sb)) {
            for (TweetPhrase phrase : tp.phrasesPerTerm.get(term)) {
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
        Map<TweetPhrase, Integer> termsCountPerPhrase = new HashMap<>();
        Multimap<String, TweetPhrase> prhasesPerTerm = HashMultimap.create();

        for (TweetPhrase phrase : params.getTrackPhrases()) {
            List<String> terms = Splitter.on(" ")
                    .splitToList(phrase.getPhrase().toLowerCase(Locale.ROOT))
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

    @Data
    static class TrackPhrases {
        private final Map<TweetPhrase, Integer> termsCountPerPhrase;
        private final Multimap<String, TweetPhrase> phrasesPerTerm;
    }
}