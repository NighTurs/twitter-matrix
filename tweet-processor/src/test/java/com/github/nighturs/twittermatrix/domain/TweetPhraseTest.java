package com.github.nighturs.twittermatrix.domain;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TweetPhraseTest {

    @Test
    public void testId() throws Exception {
        Assert.assertEquals("bananas_are_the_best", new TweetPhrase("bananas are the best", null).id());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidations() {
        TweetPhrase.create("");
    }

    @Test
    public void testIsValidPhrase() throws Exception {
        assertTrue(TweetPhrase.isValidPhrase("valid phrase"));
        assertTrue(TweetPhrase.isValidPhrase("русский тоже сойдет"));
        assertFalse(TweetPhrase.isValidPhrase(""));
        assertFalse(TweetPhrase.isValidPhrase("too long phrase aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa " +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        assertFalse(TweetPhrase.isValidPhrase(" trailing space"));
        assertFalse(TweetPhrase.isValidPhrase("leading space "));
        assertFalse(TweetPhrase.isValidPhrase("only alphabetic, or digit chars"));
        assertFalse(TweetPhrase.isValidPhrase("only Lower case"));
        assertFalse(TweetPhrase.isValidPhrase("words separated by single  space"));
    }
}