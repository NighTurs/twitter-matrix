package com.github.nighturs.twittermatrix.topology;

import com.github.nighturs.twittermatrix.domain.Tweet;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

final class TestUtils {

    private TestUtils() {
        throw new UnsupportedOperationException("Instance not supported");
    }

    static TweetPhrase ph(String phrase) {
        return TweetPhrase.create(phrase);
    }

    static Tweet fakeTweet() {
        return new Tweet(1234L, "dude", "some text", null);
    }

    static Clock fixedClock(int min, int sec) {
        return Clock.fixed(LocalDateTime.of(2016, 10, 20, min, sec).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }
}
