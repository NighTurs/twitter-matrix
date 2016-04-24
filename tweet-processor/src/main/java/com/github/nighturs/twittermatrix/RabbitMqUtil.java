package com.github.nighturs.twittermatrix;

import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.rabbitmq.client.ConnectionFactory;

public final class RabbitMqUtil {

    private RabbitMqUtil() {
        throw new UnsupportedOperationException("Instance not supported");
    }

    public static ConnectionFactory connectionFactory(RabbitMqConfig mqConfig) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(mqConfig.rabbitMqHost());
        factory.setPort(mqConfig.rabbitMqPort());
        factory.setUsername(mqConfig.rabbitMqUsername());
        factory.setPassword(mqConfig.rabbitMqPassword());
        factory.setAutomaticRecoveryEnabled(true);
        return factory;
    }
}
