package com.github.nighturs.twittermatrix.domain;

import lombok.Data;
import lombok.experimental.Wither;

@Data
@Wither
public class TweetPhraseStats {

    private final int freqMinute;
}
