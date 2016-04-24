package com.github.nighturs.twittermatrix.topology;

import com.github.nighturs.twittermatrix.config.RabbitMqConfig;
import com.github.nighturs.twittermatrix.domain.TwitterStreamParams;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

class TwitterStreamParamsMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(TwitterStreamParamsMessageListener.class);
    private Consumer<TwitterStreamParams> paramsConsumer;
    private Gson gson;

    TwitterStreamParamsMessageListener(Consumer<TwitterStreamParams> paramsConsumer) {
        this.paramsConsumer = paramsConsumer;
        this.gson = new GsonBuilder().create();
    }

    void listenStreamParamChanges(RabbitMqConfig mqConfig) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(mqConfig.rabbitMqHost());
            factory.setPort(mqConfig.rabbitMqPort());
            factory.setUsername(mqConfig.rabbitMqUsername());
            factory.setPassword(mqConfig.rabbitMqPassword());
            factory.setAutomaticRecoveryEnabled(true);
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.exchangeDeclare(mqConfig.rabbitMqTwitterStreamParamsExchange(), "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, mqConfig.rabbitMqTwitterStreamParamsExchange(), "");
            DefaultConsumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) {
                    String message = new String(body, Charsets.UTF_8);
                    TwitterStreamParams params = gson.fromJson(message, TwitterStreamParams.class);
                    paramsConsumer.accept(params);
                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Error while establishing queue connection", e);
        }
    }
}
