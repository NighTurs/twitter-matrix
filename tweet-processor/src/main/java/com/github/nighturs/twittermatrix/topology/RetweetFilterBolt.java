package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.config.TopologyConfig;
import com.github.nighturs.twittermatrix.domain.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;

class RetweetFilterBolt extends BaseBasicBolt {

    private static final Logger logger = LoggerFactory.getLogger(RetweetFilterBolt.class);
    Clock systemClock = Clock.systemDefaultZone();
    private final TopologyConfig topologyConfig;
    private final Map<Long, Instant> lastPassTimeByRetweet = new HashMap<>();
    private final Deque<Map.Entry<Long, Instant>> retentionWindow = new ArrayDeque<>();

    RetweetFilterBolt(TopologyConfig topologyConfig) {
        this.topologyConfig = topologyConfig;
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
        if (tweet.isRetweet()) {
            moveRetentionWindow();
            if (!lastPassTimeByRetweet.containsKey(tweet.getOriginId())) {
                Instant now = Instant.now(systemClock);
                lastPassTimeByRetweet.put(tweet.getOriginId(), now);
                retentionWindow.addLast(new HashMap.SimpleEntry<>(tweet.getOriginId(), now));
                collector.emit(Collections.singletonList(tweet));
            } else {
                logger.info("Filtered retweet, Tweet={}, LastPassTime={}",
                        tweet,
                        lastPassTimeByRetweet.get(tweet.getOriginId()));
            }
        } else {
            collector.emit(Collections.singletonList(tweet));
        }
    }

    private void moveRetentionWindow() {
        Instant leftEdge = Instant.now(systemClock).minusSeconds(topologyConfig.topologyRetweetMaxFrequencySeconds());

        while (!retentionWindow.isEmpty() && retentionWindow.peekFirst().getValue().isBefore(leftEdge)) {
            lastPassTimeByRetweet.remove(retentionWindow.peekFirst().getKey());
            retentionWindow.removeFirst();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_FIELD));
    }
}
