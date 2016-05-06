package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.config.TopologyConfig;
import com.github.nighturs.twittermatrix.domain.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class RetweetFilterBoltTest {

    private RetweetFilterBolt bolt;

    @Before
    public void setUp() throws Exception {
        bolt = new RetweetFilterBolt(topologyConfigMock());
    }

    @Test
    public void testExecuteOriginalTweet() throws Exception {
        verifyPass(originalTweet());
        verifyPass(originalTweet());
    }

    @Test
    public void testExecuteRetweet() throws Exception {
        setFixedClock(0, 0);
        verifyPass(retweet(2));
        verifyPass(retweet(3));
        verifyFiltered(retweet(2));
        setFixedClock(1, 0);
        verifyFiltered(retweet(2));
        setFixedClock(1, 1);
        verifyPass(retweet(2));
        verifyPass(retweet(3));
    }

    private void setFixedClock(int min, int sec) {
        bolt.systemClock = TestUtils.fixedClock(min, sec);
    }

    private void verifyFiltered(Tweet tweet) {
        verifyTimes(tweet, 0);
    }

    private void verifyPass(Tweet tweet) {
        verifyTimes(tweet, 1);
    }

    private void verifyTimes(Tweet tweet, int times) {
        BasicOutputCollector basicOutputCollector = basicOutputCollectorMock();
        bolt.execute(tweetTupleMock(tweet), basicOutputCollector);
        verify(basicOutputCollector, times(times)).emit(Collections.singletonList(tweet));
    }

    private BasicOutputCollector basicOutputCollectorMock() {
        return Mockito.mock(BasicOutputCollector.class);
    }

    private Tuple tweetTupleMock(Tweet tweet) {
        Tuple tweetTuple = Mockito.mock(Tuple.class);
        when(tweetTuple.getValueByField(TweetProcessorTopology.TWEET_FIELD)).thenReturn(tweet);
        return tweetTuple;
    }

    private Tweet originalTweet() {
        return new Tweet(1, 1, "name", "tweet", null);
    }

    private Tweet retweet(int id) {
        return new Tweet(1, id, "name", "tweet", null);
    }

    private TopologyConfig topologyConfigMock() {
        TopologyConfig topologyConfig = Mockito.mock(TopologyConfig.class);
        when(topologyConfig.topologyRetweetMaxFrequencySeconds()).thenReturn(60);
        return topologyConfig;
    }
}