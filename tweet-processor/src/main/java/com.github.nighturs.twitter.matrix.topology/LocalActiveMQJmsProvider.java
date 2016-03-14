package com.github.nighturs.twitter.matrix.topology;

import backtype.storm.contrib.jms.JmsProvider;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

class LocalActiveMQJmsProvider implements JmsProvider {
    private ConnectionFactory connectionFactory;
    private Destination destination;

    public LocalActiveMQJmsProvider(String destination) {
        this.connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        this.destination = new ActiveMQTopic(destination);
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
