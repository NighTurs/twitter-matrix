package com.github.nighturs.twittermatrix.domain;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Wither;
import twitter4j.Status;

import java.util.List;

@Data
@Wither
public final class Tweet {

    @NonNull
    private final long id;
    @NonNull
    private final long originId;
    @NonNull
    private final String originUserScreenName;
    @NonNull
    private final String text;
    private final List<TweetPhrase> matchedPhrases;

    public String getUrl() {
        return String.format("https://twitter.com/%s/status/%s", originUserScreenName, originId);
    }

    public static Tweet of(Status status) {
        Status actualStatus = status.getRetweetedStatus() == null ? status : status.getRetweetedStatus();
        return new Tweet(status.getId(),
                actualStatus.getId(),
                actualStatus.getUser().getScreenName(),
                actualStatus.getText(),
                null);
    }

    public boolean isRetweet() {
        return id != originId;
    }
}
