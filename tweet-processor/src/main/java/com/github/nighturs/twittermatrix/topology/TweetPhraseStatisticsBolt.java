package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.TweetPhrase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;
import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_PHRASES_FIELD;

class TweetPhraseStatisticsBolt extends BaseBasicBolt {
    private final Map<TweetPhrase, LinkedList<Instant>> matchesByPhrase = new HashMap<>();
    Clock systemClock = Clock.systemDefaultZone();

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        if (input.contains(TWEET_FIELD)) {
            Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
            updateMatchedPhrases(tweet);
        } else if (input.contains(TWEET_PHRASES_FIELD)) {
            List<TweetPhrase> tweetPhrases = (List<TweetPhrase>) input.getValueByField(TWEET_PHRASES_FIELD);
            List<TweetPhrase> enrichedPhrases = enrichWithStats(tweetPhrases);
            collector.emit(Collections.singletonList(enrichedPhrases));
        } else {
            throw new RuntimeException(String.format("Unknown tuple type, %s", input));
        }
    }

    void updateMatchedPhrases(Tweet tweet) {
        Instant now = Instant.now();
        for (TweetPhrase phrase : tweet.getMatchedPhrases()) {
            if (!matchesByPhrase.containsKey(phrase)) {
                matchesByPhrase.put(phrase, new LinkedList<>());
            }
            LinkedList<Instant> matches = matchesByPhrase.get(phrase);
            removeOldData(matches);
            matches.add(now);
        }
    }

    List<TweetPhrase> enrichWithStats(List<TweetPhrase> tweetPhrases) {
        tweetPhrases.stream().map(matchesByPhrase::get).filter(x -> x != null).forEach(this::removeOldData);
        return tweetPhrases.stream().map(x -> {
            LinkedList<Instant> matches = matchesByPhrase.get(x);
            if (matches == null) {
                return x;
            } else {
                return x.withStats(matches.size());
            }
        }).collect(Collectors.toList());
    }

    private void removeOldData(LinkedList<Instant> instants) {
        Instant minuteAgo = Instant.now().minus(Duration.ofMinutes(1));
        while (!instants.isEmpty() && instants.getFirst().isBefore(minuteAgo)) {
            instants.removeFirst();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_PHRASES_FIELD));
    }
}
