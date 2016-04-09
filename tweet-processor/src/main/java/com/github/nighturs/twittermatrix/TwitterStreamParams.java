package com.github.nighturs.twittermatrix;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class TwitterStreamParams {
    @NonNull
    private final List<TweetPhrase> trackPhrases;
    @NonNull
    private final List<String> languages;
}