package com.github.nighturs.twittermatrix.topology;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:twitterApiConfig.properties")
interface TwitterApiConfig extends Accessible {

    @Key("twitter.api.consumer.key")
    String twitterApiConsumerKey();

    @Key("twitter.api.consumer.secret")
    String twitterApiConsumerSecret();

    @Key("twitter.api.token")
    String twitterApiToken();

    @Key("twitter.api.token.secret")
    String twitterApiTokenSecret();
}
