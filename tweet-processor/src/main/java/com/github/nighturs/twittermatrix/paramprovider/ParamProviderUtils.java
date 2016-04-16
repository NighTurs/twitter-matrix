package com.github.nighturs.twittermatrix.paramprovider;

import com.github.nighturs.twittermatrix.config.ActiveMqConfig;
import com.github.nighturs.twittermatrix.domain.TwitterStreamParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

final class ParamProviderUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParamProviderUtils.class);
    private static final Gson gson = new GsonBuilder().create();

    private ParamProviderUtils() {
        throw new UnsupportedOperationException("Instance not supported");
    }

    static void publishParams(ActiveMqConfig activeMqConfig, TwitterStreamParams streamParams) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory(activeMqConfig.activeMqUsername(),
                    activeMqConfig.activeMqPassword(),
                    activeMqConfig.activeMqUrl());
            Destination dest = new ActiveMQTopic(activeMqConfig.activeMqTwitterStreamParamsTopic());
            Connection connection = cf.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(dest);
            producer.send(session.createTextMessage(gson.toJson(streamParams)));
            logger.info("Sent stream parameters update, StreamParams={}", streamParams);
            session.close();
            connection.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
