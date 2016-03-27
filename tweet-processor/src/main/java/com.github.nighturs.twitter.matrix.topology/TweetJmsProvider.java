package com.github.nighturs.twitter.matrix.topology;

import backtype.storm.contrib.jms.JmsProvider;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

class TweetJmsProvider implements JmsProvider {
    private final ConnectionFactory connectionFactory;
    private final Destination destination;

    TweetJmsProvider(ActiveMqConfig config) {
        this.connectionFactory = new ActiveMQConnectionFactory(config.activeMqUsername(),
                config.activeMqPassword(),
                config.activeMqUrl());
        this.destination = new ActiveMQTopic(config.activeMqTwitterTweetTopic());
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
