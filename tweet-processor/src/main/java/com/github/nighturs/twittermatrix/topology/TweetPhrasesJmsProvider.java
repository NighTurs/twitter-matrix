package com.github.nighturs.twittermatrix.topology;

import backtype.storm.contrib.jms.JmsProvider;
import com.github.nighturs.twittermatrix.config.ActiveMqConfig;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

class TweetPhrasesJmsProvider implements JmsProvider {
    private final ConnectionFactory connectionFactory;
    private final Destination destination;

    TweetPhrasesJmsProvider(ActiveMqConfig config) {
        this.connectionFactory = new ActiveMQConnectionFactory(config.activeMqUsername(),
                config.activeMqPassword(),
                config.activeMqUrl());
        this.destination = new ActiveMQTopic(config.activeMqTwitterTweetPhrasesTopic());
    }

    @Override
    public ConnectionFactory connectionFactory() throws Exception {
        return connectionFactory;
    }

    @Override
    public Destination destination() throws Exception {
        return destination;
    }

}
