package com.github.nighturs.twittermatrix.domain;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;

public class TwitterStreamParamsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testTrackPhrasesValidations() {
        new TwitterStreamParams(Lists.newArrayList(TweetPhrase.create("same"), TweetPhrase.create("same")),
                Lists.newArrayList("en", "ru"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLanguagesValidations() {
        new TwitterStreamParams(Collections.singletonList(TweetPhrase.create("ok")),
                Lists.newArrayList("en", "en"));
    }
}