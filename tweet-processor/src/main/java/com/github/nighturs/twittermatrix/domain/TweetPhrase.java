package com.github.nighturs.twittermatrix.domain;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.regex.Pattern;

@Value
@Wither
public final class TweetPhrase {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(" ");
    @NonNull
    private final String phrase;
    private final TweetPhraseStats stats;

    public String id() {
        return WHITESPACE_PATTERN.matcher(phrase).replaceAll("_");
    }

    public static TweetPhrase create(String phrase) {
        return new TweetPhrase(phrase, null);
    }
}
