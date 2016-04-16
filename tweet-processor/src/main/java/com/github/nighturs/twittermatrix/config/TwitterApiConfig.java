package com.github.nighturs.twittermatrix.config;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:twitterApiConfig.properties")
public interface TwitterApiConfig extends Accessible {

    @Key("twitter.api.consumer.key")
    String twitterApiConsumerKey();

    @Key("twitter.api.consumer.secret")
    String twitterApiConsumerSecret();

    @Key("twitter.api.token")
    String twitterApiToken();

    @Key("twitter.api.token.secret")
    String twitterApiTokenSecret();
}
