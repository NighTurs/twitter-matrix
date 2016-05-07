package com.github.nighturs.twittermatrix.domain;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Wither;

@Data
@Wither
public final class TweetPhrase {

    @NonNull
    private final String phrase;
    private final TweetPhraseStats stats;

    public String id() {
        return phrase;
    }

    public static TweetPhrase create(String phrase) {
        return new TweetPhrase(phrase, null);
    }
}
