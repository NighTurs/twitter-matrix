package com.github.nighturs.twittermatrix.paramprovider;

import com.github.nighturs.twittermatrix.ActiveMqConfig;
import com.github.nighturs.twittermatrix.TweetPhrase;
import com.github.nighturs.twittermatrix.TwitterStreamParams;
import com.google.common.collect.Lists;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class ParamFromCsvUrlProvider {

    private static final Logger logger = LoggerFactory.getLogger(ParamFromCsvUrlProvider.class);
    private static final AtomicReference<List<TweetPhrase>> curPhrases = new AtomicReference<>();

    private ParamFromCsvUrlProvider() {
    }

    public static void main(String[] args) throws MalformedURLException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Should be called with sheet url argument");
        }
        URL csvUrl = new URL(args[0]);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ActiveMqConfig activeMqConfig = ConfigFactory.create(ActiveMqConfig.class);
        executor.scheduleAtFixedRate(() -> updateParamsWithChanges(csvUrl, activeMqConfig), 0, 1, TimeUnit.MINUTES);
    }

    private static void updateParamsWithChanges(URL csvUrl, ActiveMqConfig activeMqConfig) {
        List<TweetPhrase> phrases = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(csvUrl.openStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                phrases.add(TweetPhrase.create(inputLine));
            }
            logger.info("Got phrases, Phrases={}", phrases);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (curPhrases.get() == null || !curPhrases.get().equals(phrases)) {
            TwitterStreamParams streamParams = new TwitterStreamParams(phrases, Lists.newArrayList("en", "ru"));
            ParamProviderUtils.publishParams(activeMqConfig, streamParams);
            curPhrases.set(phrases);
        }
    }
}
