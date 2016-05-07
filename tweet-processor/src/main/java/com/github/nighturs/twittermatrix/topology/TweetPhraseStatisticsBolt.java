package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.domain.Tweet;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.github.nighturs.twittermatrix.domain.TweetPhraseStats;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;
import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_PHRASES_FIELD;

class TweetPhraseStatisticsBolt extends BaseBasicBolt {
    private final Map<String, Integer> matchesByPhrase = new HashMap<>();
    private final Deque<Map.Entry<String, Instant>> matchesWindow = new ArrayDeque<>();
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
        Instant now = Instant.now(systemClock);
        for (TweetPhrase phrase : tweet.getMatchedPhrases()) {
            removeOldData();
            Integer currentMatches = matchesByPhrase.get(phrase.id());
            matchesByPhrase.put(phrase.id(), currentMatches == null ? 1 : currentMatches + 1);
            matchesWindow.addLast(new HashMap.SimpleEntry<>(phrase.id(), now));
        }
    }

    List<TweetPhrase> enrichWithStats(List<TweetPhrase> tweetPhrases) {
        removeOldData();
        return tweetPhrases.stream().map(x -> {
            Integer matches = matchesByPhrase.get(x.id());
            if (matches == null) {
                return x.withStats(new TweetPhraseStats(0));
            } else {
                return x.withStats(new TweetPhraseStats(matches));
            }
        }).collect(Collectors.toList());
    }

    private void removeOldData() {
        Instant minuteAgo = Instant.now(systemClock).minus(Duration.ofMinutes(1));
        while (!matchesWindow.isEmpty() && matchesWindow.peekFirst().getValue().isBefore(minuteAgo)) {
            Map.Entry<String, Instant> entry = matchesWindow.pollFirst();
            Integer leftMatches = matchesByPhrase.get(entry.getKey()) - 1;
            if (leftMatches == 0) {
                matchesByPhrase.remove(entry.getKey());
            } else {
                matchesByPhrase.put(entry.getKey(), leftMatches);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_PHRASES_FIELD));
    }
}
