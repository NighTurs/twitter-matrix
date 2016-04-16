package com.github.nighturs.twittermatrix.domain;

import com.github.nighturs.twittermatrix.domain.TweetPhrase;
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