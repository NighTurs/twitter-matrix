package com.github.nighturs.twittermatrix.paramprovider;

import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.domain.TweetPhrase;
import com.github.nighturs.twittermatrix.domain.TwitterStreamParams;
import com.google.common.collect.Lists;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
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
        RabbitMqConfig rabbitMqConfig = ConfigFactory.create(RabbitMqConfig.class);
        executor.scheduleAtFixedRate(() -> updateParamsWithChanges(csvUrl, rabbitMqConfig), 0, 1, TimeUnit.MINUTES);
    }

    private static void updateParamsWithChanges(URL csvUrl, RabbitMqConfig rabbitMqConfig) {
        try {
            List<TweetPhrase> phrases = new ArrayList<>();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(csvUrl.openStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (TweetPhrase.isValidPhrase(inputLine)) {
                        phrases.add(TweetPhrase.create(inputLine));
                    } else {
                        logger.error("Skip update. Invalid track phrase in csv, Phrase={}", inputLine);
                        return;
                    }
                }
                logger.info("Got phrases, Phrases={}", phrases);
            }
            if (curPhrases.get() == null || !curPhrases.get().equals(phrases)) {
                if (!TwitterStreamParams.isValidTrackPhrases(phrases)) {
                    logger.error("Skip update. Invalid track phrases, Phrases={}", phrases);
                    return;
                }
                TwitterStreamParams streamParams = new TwitterStreamParams(phrases, Lists.newArrayList("en", "ru"));
                ParamProviderUtils.publishParams(rabbitMqConfig, streamParams);
                curPhrases.set(phrases);
            }
        } catch (Exception e) {
            logger.error("Failed to get or publish phrases", e);
        }
    }
}
