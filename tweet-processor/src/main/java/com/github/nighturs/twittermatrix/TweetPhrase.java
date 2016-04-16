package com.github.nighturs.twittermatrix;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Wither;

@Data
@Wither
public final class TweetPhrase {

    @NonNull
    private final String phrase;
    private final int freqMinute;

    public static TweetPhrase create(String phrase) {
        return new TweetPhrase(phrase, 0);
    }
}