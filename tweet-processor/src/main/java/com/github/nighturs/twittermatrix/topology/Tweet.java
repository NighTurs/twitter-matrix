package com.github.nighturs.twittermatrix.topology;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

class Tweet implements Serializable {

    private final long id;
    private final String text;

    Tweet(long id, String text) {
        this.id = id;
        this.text = text;
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
