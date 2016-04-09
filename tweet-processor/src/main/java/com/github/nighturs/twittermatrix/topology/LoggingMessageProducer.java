package com.github.nighturs.twittermatrix.topology;

import backtype.storm.contrib.jms.JmsMessageProducer;
import backtype.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

class LoggingMessageProducer implements JmsMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMessageProducer.class);

    @Override
    public Message toMessage(Session session, Tuple input) throws JMSException {
        String message = input.getString(0);
        logger.info("Published message, Message={}", message);
        return session.createTextMessage(message);
    }
}
