package com.github.nighturs.twittermatrix.topology;

import com.github.nighturs.twittermatrix.TwitterStreamParams;
import com.github.nighturs.twittermatrix.topology.TweetPhraseMatcherBolt.TrackPhrases;
import com.google.common.collect.*;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TweetPhraseMatcherBoltTest {

    @Test
    public void testOnApiParamsUpdate() throws Exception {
        TweetPhraseMatcherBolt bolt = new TweetPhraseMatcherBolt();
        bolt.onApiParamsUpdate(new TwitterStreamParams(Lists.newArrayList("OneWord", "Multiple Words"), null));
        assertEquals(sharedTermsCountPerPhrase(), bolt.trackPhrases.get().termsCountPerPhrase);
        assertEquals(sharedPhrasesPerTerm(), bolt.trackPhrases.get().phrasesPerTerm);
    }

    @Test
    public void testOnApiParamsUpdateExtraSpacesIgnored() throws Exception {
        TweetPhraseMatcherBolt bolt = new TweetPhraseMatcherBolt();
        bolt.onApiParamsUpdate(new TwitterStreamParams(Lists.newArrayList("   two    terms   "), null));
        assertEquals(Integer.valueOf(2), bolt.trackPhrases.get().termsCountPerPhrase.get("   two    terms   "));
    }

    @Test
    public void testFindMatchedPhrases() throws Exception {
        TweetPhraseMatcherBolt bolt = new TweetPhraseMatcherBolt();
        bolt.trackPhrases.set(sharedTrachPhrases());
        assertThat(bolt.findMatchedPhrases("There are, mmm, multiple words."), hasItem("Multiple Words"));
        assertThat(bolt.findMatchedPhrases("There are multiple words and oneWord."),
                allOf(hasItem("Multiple Words"), hasItems("OneWord")));
        assertTrue(bolt.findMatchedPhrases("No word for you").isEmpty());
    }

    private Multimap<String, String> sharedPhrasesPerTerm() {
        return HashMultimap.create(ImmutableMultimap.<String, String>builder().put("oneword", "OneWord")
                .put("multiple", "Multiple Words")
                .put("words", "Multiple Words")
                .build());
    }

    private Map<String, Integer> sharedTermsCountPerPhrase() {
        return ImmutableMap.<String, Integer>builder().put("OneWord", 1).put("Multiple Words", 2).build();
    }

    private TrackPhrases sharedTrachPhrases() {
        return new TrackPhrases(sharedTermsCountPerPhrase(), sharedPhrasesPerTerm());
    }
}