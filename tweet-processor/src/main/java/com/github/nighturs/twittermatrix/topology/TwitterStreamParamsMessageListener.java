package com.github.nighturs.twittermatrix.topology;

import com.github.nighturs.twittermatrix.ActiveMqConfig;
import com.github.nighturs.twittermatrix.TwitterStreamParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.function.Consumer;

class TwitterStreamParamsMessageListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(TwitterStreamParamsMessageListener.class);
    private Consumer<TwitterStreamParams> paramsConsumer;
    private Gson gson;

    TwitterStreamParamsMessageListener(Consumer<TwitterStreamParams> paramsConsumer) {
        this.paramsConsumer = paramsConsumer;
        this.gson = new GsonBuilder().create();
    }

    void listenStreamParamChanges(ActiveMqConfig activeMqConfig) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory(activeMqConfig.activeMqUsername(),
                    activeMqConfig.activeMqPassword(),
                    activeMqConfig.activeMqUrl());
            Destination dest = new ActiveMQTopic(activeMqConfig.activeMqTwitterStreamParamsTopic());
            Connection connection = cf.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(dest);
            consumer.setMessageListener(this);
            connection.start();
        } catch (Exception e) {
            logger.warn("Error creating JMS connection", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        logger.info("Received stream param message, Message={}", message);
        try {
            String json = ((TextMessage) message).getText();
            TwitterStreamParams params = gson.fromJson(json, TwitterStreamParams.class);
            paramsConsumer.accept(params);
        } catch (JMSException e) {
            logger.error("Failed to handle message, Message={}", message, e);
        }
    }
}
