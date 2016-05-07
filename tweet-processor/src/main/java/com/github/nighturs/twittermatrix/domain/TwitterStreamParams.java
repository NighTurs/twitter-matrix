package com.github.nighturs.twittermatrix.domain;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
public class TwitterStreamParams {

    @NonNull
    private final List<TweetPhrase> trackPhrases;
    @NonNull
    private final List<String> languages;

    public TwitterStreamParams(List<TweetPhrase> trackPhrases, List<String> languages) {
        Preconditions.checkArgument(isValidTrackPhrases(trackPhrases));
        Preconditions.checkArgument(isValidLanguages(languages));
        this.trackPhrases = trackPhrases;
        this.languages = languages;
    }

    public static boolean isValidTrackPhrases(List<TweetPhrase> trackPhrases) {
        return !trackPhrases.isEmpty() && trackPhrases.size() == trackPhrases.stream().map(TweetPhrase::id).distinct().count();
    }

    public static boolean isValidLanguages(List<String> languages) {
        return !languages.isEmpty() && languages.size() == languages.stream().distinct().count();
    }
}