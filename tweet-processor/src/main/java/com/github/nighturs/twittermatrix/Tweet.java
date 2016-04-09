package com.github.nighturs.twittermatrix;

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
    private final String userScreenName;
    @NonNull
    private final String text;
    private final List<TweetPhrase> matchedPhrases;

    public String getUrl() {
        return String.format("https://twitter.com/%s/status/%s", userScreenName, id);
    }

    public static Tweet of(Status status) {
        Status actualStatus = status.getRetweetedStatus() == null ? status : status.getRetweetedStatus();
        return new Tweet(actualStatus.getId(), actualStatus.getUser().getScreenName(), actualStatus.getText(), null);
    }
}
