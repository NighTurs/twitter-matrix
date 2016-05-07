package com.github.nighturs.twittermatrix.domain;

import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
public class TweetPhraseStats {

    private final int freqMinute;
}
