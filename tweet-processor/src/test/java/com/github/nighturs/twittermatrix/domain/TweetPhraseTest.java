package com.github.nighturs.twittermatrix.domain;

import org.junit.Assert;
import org.junit.Test;

public class TweetPhraseTest {

    @Test
    public void testId() throws Exception {
        Assert.assertEquals("bananas_are_the_best", new TweetPhrase("bananas are the best", null).id());
    }
}