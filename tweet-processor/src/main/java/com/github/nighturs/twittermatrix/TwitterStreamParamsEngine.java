package com.github.nighturs.twittermatrix;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.aeonbits.owner.ConfigFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.*;
import java.util.stream.Collectors;

public final class TwitterStreamParamsEngine {

    private TwitterStreamParamsEngine() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Should be called with two arguments, languages and track phrases");
        }
        TwitterStreamParams streamParams = new TwitterStreamParams(Splitter.on(",")
                .splitToList(args[1])
                .stream()
                .map(TweetPhrase::create)
                .collect(Collectors.toList()), Splitter.on(",").splitToList(args[0]));
        ActiveMqConfig activeMqConfig = ConfigFactory.create(ActiveMqConfig.class);
        Gson gson = new GsonBuilder().create();

        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory(activeMqConfig.activeMqUsername(),
                    activeMqConfig.activeMqPassword(),
                    activeMqConfig.activeMqUrl());
            Destination dest = new ActiveMQTopic(activeMqConfig.activeMqTwitterStreamParamsTopic());
            Connection connection = cf.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(dest);
            producer.send(session.createTextMessage(gson.toJson(streamParams)));
            session.close();
            connection.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
