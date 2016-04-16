package com.github.nighturs.twittermatrix.paramprovider;

import com.github.nighturs.twittermatrix.ActiveMqConfig;
import com.github.nighturs.twittermatrix.TweetPhrase;
import com.github.nighturs.twittermatrix.TwitterStreamParams;
import com.google.common.base.Splitter;
import org.aeonbits.owner.ConfigFactory;

import java.util.stream.Collectors;

public final class ParamFromArgsProvider {

    private ParamFromArgsProvider() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Should be called with two arguments, languages and track phrases");
        }
        ActiveMqConfig activeMqConfig = ConfigFactory.create(ActiveMqConfig.class);
        TwitterStreamParams streamParams = new TwitterStreamParams(Splitter.on(",")
                .splitToList(args[1])
                .stream()
                .map(TweetPhrase::create)
                .collect(Collectors.toList()), Splitter.on(",").splitToList(args[0]));
        ParamProviderUtils.publishParams(activeMqConfig, streamParams);
    }
}
