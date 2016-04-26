package com.github.nighturs.twittermatrix.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:rabbitMqConfig.properties")
public interface RabbitMqConfig extends Config {

    @Key("rabbitmq.host")
    String rabbitMqHost();

    @Key("rabbitmq.port")
    Integer rabbitMqPort();

    @Key("rabbitmq.username")
    String rabbitMqUsername();

    @Key("rabbitmq.password")
    String rabbitMqPassword();

    @Key("rabbitmq.twitter.tweet.exchange")
    String rabbitMqTwitterTweetExchange();

    @Key("rabbitmq.twitter.stream.params.exchange")
    String rabbitMqTwitterStreamParamsExchange();

    @Key("rabbitmq.twitter.tweet.phrases.exchange")
    String rabbitMqTwitterTweetPhrasesExchange();
}
