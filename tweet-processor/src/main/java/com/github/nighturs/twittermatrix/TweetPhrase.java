package com.github.nighturs.twittermatrix;

import java.util.Objects;

public final class TweetPhrase {
    private final String phrase;
    private final int freqMinute;

    private TweetPhrase(String phrase, int freqMinute) {
        this.phrase = phrase;
        this.freqMinute = freqMinute;
    }

    public static TweetPhrase create(String phrase) {
        return new TweetPhrase(phrase, 0);
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    public TweetPhrase withStats(int freqMinute) {
        return new TweetPhrase(phrase, freqMinute);
    }

    public String getPhrase() {
        return phrase;
    }

    public int getFreqMinute() {
        return freqMinute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TweetPhrase that = (TweetPhrase) o;
        return freqMinute == that.freqMinute && Objects.equals(phrase, that.phrase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phrase, freqMinute);
    }
}
