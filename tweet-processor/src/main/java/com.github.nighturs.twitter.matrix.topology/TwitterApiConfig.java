package com.github.nighturs.twitter.matrix.topology;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:twitterApiConfig.properties")
interface TwitterApiConfig extends Accessible {
    String API_CONSUMER_KEY = "twitter.api.consumer.key";
    String API_CONSUMER_SECRET = "twitter.api.consumer.secret";
    String API_TOKEN = "twitter.api.token";
    String API_TOKEN_SECRET = "twitter.api.token.secret";
    String API_PARAMETERS_TRACK = "twitter.api.parameters.track";
    String API_PARAMETERS_LANGUAGE = "twitter.api.parameters.language";

    @Key(API_CONSUMER_KEY)
    String twitterApiConsumerKey();

    @Key(API_CONSUMER_SECRET)
    String twitterApiConsumerSecret();

    @Key(API_TOKEN)
    String twitterApiToken();

    @Key(API_TOKEN_SECRET)
    String twitterApiTokenSecret();

    @Key(API_PARAMETERS_TRACK)
    String twitterApiParametersTrack();

    @Key(API_PARAMETERS_LANGUAGE)
    String twitterApiParametersLanguage();
}
