package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.domain.Tweet;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;

class UnmatchedTweetFilterBolt extends BaseBasicBolt {

    private static final Logger logger = LoggerFactory.getLogger(UnmatchedTweetFilterBolt.class);

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
        if (!tweet.getMatchedPhrases().isEmpty()) {
            collector.emit(Lists.newArrayList(tweet));
        } else {
            logger.info("Filter tweet without matched phrases, Tweet={}", tweet);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_FIELD));
    }
}
