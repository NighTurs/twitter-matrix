package com.github.nighturs.twittermatrix.topology;

import com.github.nighturs.twittermatrix.domain.Tweet;
import org.junit.Assert;
import org.junit.Test;

public class TweetTest {

    @Test
    public void testGetUrl() throws Exception {
        Tweet tweet = new Tweet(12345L, 54321L, "theman", "I'm the man", null);
        Assert.assertEquals("https://twitter.com/theman/status/54321", tweet.getUrl());
    }
}