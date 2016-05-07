package com.github.nighturs.twittermatrix.topology;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.github.nighturs.twittermatrix.domain.Tweet;
import com.vdurmont.emoji.EmojiParser;

import java.util.Collections;
import java.util.regex.Pattern;

import static com.github.nighturs.twittermatrix.topology.TweetProcessorTopology.TWEET_FIELD;

class TweetCleaningBolt extends BaseBasicBolt {

    private static final Pattern TWITTER_URL_PATTERN = Pattern.compile("https://t\\.co/[^ ]+(?= )?");

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        Tweet tweet = (Tweet) input.getValueByField(TWEET_FIELD);
        collector.emit(Collections.singletonList(tweet.withText(urlToMark(emojiToAliases(tweet.getText())))));
    }

    private static String emojiToAliases(String text) {
        return EmojiParser.parseToAliases(text);
    }

    static String urlToMark(String text) {
        return TWITTER_URL_PATTERN.matcher(text).replaceAll(":link:");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(TWEET_FIELD));
    }
}
