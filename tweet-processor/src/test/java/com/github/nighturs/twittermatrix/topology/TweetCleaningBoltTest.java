package com.github.nighturs.twittermatrix.topology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TweetCleaningBoltTest {

    @Test
    public void testUrlToMark() throws Exception {
        assertEquals("Link at the end. :link:",
                TweetCleaningBolt.urlToMark("Link at the end. https://t.co/8evek027Co"));
        assertEquals("Link in the :link: middle.",
                TweetCleaningBolt.urlToMark("Link in the https://t.co/8evek027Co middle."));
        assertEquals("Double :link: link :link:",
                TweetCleaningBolt.urlToMark("Double https://t.co/8evek027Co link https://t.co/3987dfji"));
    }
}