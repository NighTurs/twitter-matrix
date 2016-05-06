package com.github.nighturs.twittermatrix.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("classpath:topologyConfig.properties")
public interface TopologyConfig extends Config {

    @Key("topology.retweet.max.frequency.seconds")
    int topologyRetweetMaxFrequencySeconds();
}
