package com.github.nighturs.twittermatrix.paramprovider;

import com.github.nighturs.twittermatrix.RabbitMqUtil;
import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.domain.TwitterStreamParams;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

final class ParamProviderUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParamProviderUtils.class);
    private static final Gson gson = new GsonBuilder().create();

    private ParamProviderUtils() {
        throw new UnsupportedOperationException("Instance not supported");
    }

    static void publishParams(RabbitMqConfig mqConfig, TwitterStreamParams streamParams) {
        ConnectionFactory factory = RabbitMqUtil.connectionFactory(mqConfig);
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(mqConfig.rabbitMqTwitterStreamParamsExchange(), "fanout");
            channel.basicPublish(mqConfig.rabbitMqTwitterStreamParamsExchange(),
                    "",
                    null,
                    gson.toJson(streamParams).getBytes(Charsets.UTF_8));
            logger.info("Sent stream parameters update, StreamParams={}", streamParams);
            channel.close();
            connection.close();
        } catch (TimeoutException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
