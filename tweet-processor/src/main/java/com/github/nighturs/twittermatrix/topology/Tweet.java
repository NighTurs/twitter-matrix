package com.github.nighturs.twittermatrix.topology;

import com.google.common.base.MoreObjects;
import twitter4j.Status;

import java.io.Serializable;
import java.util.List;

final class Tweet implements Serializable {

    private final long id;
    private final String userScreenName;
    private final String text;
    private final List<String> matchedPhrases;

    Tweet(long id, String userScreenName, String text) {
        this.userScreenName = userScreenName;
        this.id = id;
        this.text = text;
        this.matchedPhrases = null;
    }

    private Tweet(Tweet tweet, List<String> matchedPhrases) {
        this.userScreenName = tweet.getUserScreenName();
        this.id = tweet.getId();
        this.text = tweet.getText();
        this.matchedPhrases = matchedPhrases;
    }

    static Tweet of(Status status) {
        Status actualStatus = status.getRetweetedStatus() == null ? status : status.getRetweetedStatus();
        return new Tweet(actualStatus.getId(), actualStatus.getUser().getScreenName(), actualStatus.getText());
    }

    String getUrl() {
        return String.format("https://twitter.com/%s/status/%s", userScreenName, id);
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    Tweet withMatchedPhrases(List<String> matchedPhrases) {
        return new Tweet(this, matchedPhrases);
    }

    long getId() {
        return id;
    }

    String getText() {
        return text;
    }

    private String getUserScreenName() {
        return userScreenName;
    }

    public List<String> getMatchedPhrases() {
        return matchedPhrases;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("userScreenName", userScreenName)
                .add("text", text)
                .add("matchedPhrases", matchedPhrases)
                .toString();
    }

    private static final long serialVersionUID = -8474868949029295001L;
}
