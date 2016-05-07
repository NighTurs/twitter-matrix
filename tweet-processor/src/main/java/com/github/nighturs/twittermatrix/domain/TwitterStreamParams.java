package com.github.nighturs.twittermatrix.domain;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
public class TwitterStreamParams {

    @NonNull
    private final List<TweetPhrase> trackPhrases;
    @NonNull
    private final List<String> languages;
}