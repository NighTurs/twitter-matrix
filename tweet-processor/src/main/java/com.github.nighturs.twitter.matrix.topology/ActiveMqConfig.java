package com.github.nighturs.twitter.matrix.topology;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:activeMqConfig.properties")
public interface ActiveMqConfig extends Config {

    @Key("activemq.url")
    String activeMqUrl();

    @Key("activemq.username")
    String activeMqUsername();

    @Key("activemq.password")
    String activeMqPassword();

    @Key("activemq.twitter.tweet.topic")
    String activeMqTwitterTweetTopic();
}
