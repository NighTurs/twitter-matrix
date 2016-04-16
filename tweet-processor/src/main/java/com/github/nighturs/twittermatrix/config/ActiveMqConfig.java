package com.github.nighturs.twittermatrix.config;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:activeMqConfig.properties")
public interface ActiveMqConfig extends Accessible {

    @Key("activemq.url")
    String activeMqUrl();

    @Key("activemq.username")
    String activeMqUsername();

    @Key("activemq.password")
    String activeMqPassword();

    @Key("activemq.twitter.tweet.topic")
    String activeMqTwitterTweetTopic();

    @Key("activemq.twitter.stream.params.topic")
    String activeMqTwitterStreamParamsTopic();

    @Key("activemq.twitter.tweet.phrases.topic")
    String activeMqTwitterTweetPhrasesTopic();
}
