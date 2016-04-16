package com.github.nighturs.twittermatrix.topology;

import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.github.nighturs.twittermatrix.domain.TweetPhraseStats;
import com.google.common.collect.Lists;
import org.junit.Test;

import static com.github.nighturs.twittermatrix.topology.TestUtils.*;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;

public class TweetPhraseStatisticsBoltTest {

    @Test
    public void updateMatchedPhrases() throws Exception {
        TweetPhraseStatisticsBolt bolt = new TweetPhraseStatisticsBolt();
        bolt.systemClock = fixedClock(9, 30);
        TweetPhrase ph1 = ph("Phrase one");
        TweetPhrase ph2 = ph("Phrase two");
        assertThat(bolt.enrichWithStats(Lists.newArrayList(ph1, ph2)),
                allOf(iterableWithSize(2), hasItem(ph1.withStats(stats(0))), hasItem(ph2.withStats(stats(0)))));
        bolt.updateMatchedPhrases(fakeTweet().withMatchedPhrases(singletonList(ph1)));
        bolt.updateMatchedPhrases(fakeTweet().withMatchedPhrases(singletonList(ph1)));
        bolt.updateMatchedPhrases(fakeTweet().withMatchedPhrases(singletonList(ph2)));
        assertThat(bolt.enrichWithStats(Lists.newArrayList(ph1, ph2)),
                allOf(iterableWithSize(2), hasItem(ph1.withStats(stats(2))), hasItem(ph2.withStats(stats(1)))));
        bolt.systemClock = fixedClock(9, 32);
        assertThat(bolt.enrichWithStats(Lists.newArrayList(ph1, ph2)),
                allOf(iterableWithSize(2), hasItem(ph1.withStats(stats(0))), hasItem(ph2.withStats(stats(0)))));
    }

    private TweetPhraseStats stats(int freqMinute) {
        return new TweetPhraseStats(freqMinute);
    }
}