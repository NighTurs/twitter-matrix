package com.github.nighturs.twittermatrix;

import com.google.common.base.MoreObjects;

import java.util.List;

public class TwitterStreamParams {
    private final List<String> trackPhrases;
    private final List<String> languages;

    public TwitterStreamParams(List<String> trackPhrases, List<String> languages) {
        this.trackPhrases = trackPhrases;
        this.languages = languages;
    }

    public List<String> getTrackPhrases() {
        return trackPhrases;
    }

    public List<String> getLanguages() {
        return languages;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("trackPhrases", trackPhrases)
                .add("languages", languages)
                .toString();
    }
}