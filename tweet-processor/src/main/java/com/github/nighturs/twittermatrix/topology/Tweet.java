package com.github.nighturs.twittermatrix.topology;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;

class Tweet implements Serializable {

    private final long id;
    private final String text;
    private final List<String> matchedPhrases;

    Tweet(long id, String text) {
        this.id = id;
        this.text = text;
        this.matchedPhrases = null;
    }

    Tweet(long id, String text, List<String> matchedPhrases) {
        this.id = id;
        this.text = text;
        this.matchedPhrases = matchedPhrases;
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    Tweet withMatchedPhrases(List<String> matchedPhrases) {
        return new Tweet(this.id, this.text, matchedPhrases);
    }

    long getId() {
        return id;
    }

    String getText() {
        return text;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("text", text).toString();
    }

    private static final long serialVersionUID = -8474868949029295001L;
}
