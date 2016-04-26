package com.github.nighturs.twittermatrix.topology;

import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.github.nighturs.twittermatrix.domain.TwitterStreamParams;
import com.github.nighturs.twittermatrix.topology.TweetPhraseMatcherBolt.TrackPhrases;
import com.google.common.collect.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

import static com.github.nighturs.twittermatrix.topology.TestUtils.ph;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TweetPhraseMatcherBoltTest {

    @Test
    public void testOnApiParamsUpdate() throws Exception {
        TweetPhraseMatcherBolt bolt = newTweetPhraseMatcherBolt();
        bolt.onApiParamsUpdate(new TwitterStreamParams(Lists.newArrayList(ph("OneWord"), ph("Multiple Words")),
                Collections.emptyList()));
        assertEquals(sharedTermsCountPerPhrase(), bolt.trackPhrases.get().getTermsCountPerPhrase());
        assertEquals(sharedPhrasesPerTerm(), bolt.trackPhrases.get().getPhrasesPerTerm());
    }

    @Test
    public void testOnApiParamsUpdateExtraSpacesIgnored() throws Exception {
        TweetPhraseMatcherBolt bolt = newTweetPhraseMatcherBolt();
        bolt.onApiParamsUpdate(new TwitterStreamParams(Lists.newArrayList(ph("   two    terms   ")),
                Collections.emptyList()));
        assertEquals(Integer.valueOf(2),
                bolt.trackPhrases.get().getTermsCountPerPhrase().get(ph("   two    terms   ")));
    }

    @Test
    public void testFindMatchedPhrases() throws Exception {
        TweetPhraseMatcherBolt bolt = newTweetPhraseMatcherBolt();
        bolt.trackPhrases.set(sharedTrackPhrases());
        assertThat(bolt.findMatchedPhrases("There are, mmm, multiple words."), hasItem(ph("Multiple Words")));
        assertThat(bolt.findMatchedPhrases("There are multiple words and oneWord."),
                allOf(hasItem(ph("Multiple Words")), hasItems(ph("OneWord"))));
        assertTrue(bolt.findMatchedPhrases("No word for you").isEmpty());
    }

    @Test
    public void testFindMatchedPhrasesWordsDelimitedOnlyByPunctuation() throws Exception {
        TweetPhraseMatcherBolt bolt = newTweetPhraseMatcherBolt();
        bolt.onApiParamsUpdate(singlePhraseParams("python"));
        assertThat(bolt.findMatchedPhrases("bandwidth monitor: my first pip/python package"),
                hasItem(ph("python")));
    }

    private Multimap<String, TweetPhrase> sharedPhrasesPerTerm() {
        return HashMultimap.create(ImmutableMultimap.<String, TweetPhrase>builder().put("oneword", ph("OneWord"))
                .put("multiple", ph("Multiple Words"))
                .put("words", ph("Multiple Words"))
                .build());
    }

    private TweetPhraseMatcherBolt newTweetPhraseMatcherBolt() {
        return new TweetPhraseMatcherBolt(Mockito.mock(RabbitMqConfig.class));
    }

    private Map<TweetPhrase, Integer> sharedTermsCountPerPhrase() {
        return ImmutableMap.<TweetPhrase, Integer>builder().put(ph("OneWord"), 1).put(ph("Multiple Words"), 2).build();
    }

    private TrackPhrases sharedTrackPhrases() {
        return new TrackPhrases(sharedTermsCountPerPhrase(), sharedPhrasesPerTerm());
    }

    private TwitterStreamParams singlePhraseParams(String phrase) {
        return new TwitterStreamParams(Collections.singletonList(ph(phrase)), Collections.emptyList());
    }
}