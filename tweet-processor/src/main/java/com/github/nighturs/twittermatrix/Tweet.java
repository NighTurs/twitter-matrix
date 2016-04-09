package com.github.nighturs.twittermatrix;

import com.google.common.base.MoreObjects;
import twitter4j.Status;

import java.util.List;

public final class Tweet {

    private final long id;
    private final String userScreenName;
    private final String text;
    private final List<TweetPhrase> matchedPhrases;

    public Tweet(long id, String userScreenName, String text) {
        this.userScreenName = userScreenName;
        this.id = id;
        this.text = text;
        this.matchedPhrases = null;
    }

    private Tweet(Tweet tweet, List<TweetPhrase> matchedPhrases) {
        this.userScreenName = tweet.getUserScreenName();
        this.id = tweet.getId();
        this.text = tweet.getText();
        this.matchedPhrases = matchedPhrases;
    }

    public static Tweet of(Status status) {
        Status actualStatus = status.getRetweetedStatus() == null ? status : status.getRetweetedStatus();
        return new Tweet(actualStatus.getId(), actualStatus.getUser().getScreenName(), actualStatus.getText());
    }

    public String getUrl() {
        return String.format("https://twitter.com/%s/status/%s", userScreenName, id);
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    public Tweet withMatchedPhrases(List<TweetPhrase> matchedPhrases) {
        return new Tweet(this, matchedPhrases);
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getUserScreenName() {
        return userScreenName;
    }

    public List<TweetPhrase> getMatchedPhrases() {
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
}
