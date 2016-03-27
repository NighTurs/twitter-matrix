package com.github.nighturs.twitter.matrix.topology;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:twitterApiConfig.properties")
interface TwitterApiConfig extends Config {

    @Key("twitter.api.consumer.key")
    String twitterApiConsumerKey();

    @Key("twitter.api.consumer.secret")
    String twitterApiConsumerSecret();

    @Key("twitter.api.token")
    String twitterApiToken();

    @Key("twitter.api.token.secret")
    String twitterApiTokenSecret();

    @Key("twitter.api.parameters.track")
    String twitterApiParametersTrack();

    @Key("twitter.api.parameters.language")
    String twitterApiParametersLanguage();
}
