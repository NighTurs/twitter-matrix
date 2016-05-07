package com.github.nighturs.twittermatrix.domain;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.regex.Pattern;

@Value
@Wither
public final class TweetPhrase {

    private static final int SPACE_CODEPOINT = Character.codePointAt(" ", 0);
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(" ");
    public static final int PHRASE_BYTE_LENGTH_LIMIT = 60;
    @NonNull
    private final String phrase;
    private final TweetPhraseStats stats;

    public TweetPhrase(String phrase, TweetPhraseStats stats) {
        Preconditions.checkArgument(isValidPhrase(phrase));
        this.phrase = phrase;
        this.stats = stats;
    }

    public String id() {
        return WHITESPACE_PATTERN.matcher(phrase).replaceAll("_");
    }

    public static TweetPhrase create(String phrase) {
        return new TweetPhrase(phrase, null);
    }

    public static boolean isValidPhrase(String phrase) {
        if (phrase.isEmpty()) {
            return false;
        }
        boolean validSize = phrase.getBytes().length <= PHRASE_BYTE_LENGTH_LIMIT;
        boolean hasValidCharacters = phrase.chars()
                .allMatch(x -> (Character.isAlphabetic(x) && Character.isLowerCase(x)) || Character.isDigit(x) ||
                        x == SPACE_CODEPOINT);
        boolean noTrailingSpaces = phrase.charAt(0) != SPACE_CODEPOINT;
        boolean noLeadingSpaces = phrase.charAt(phrase.length() - 1) != SPACE_CODEPOINT;
        boolean worksSpearatedBySincleSpaces = !phrase.contains("  ");
        return validSize && hasValidCharacters && noTrailingSpaces && noLeadingSpaces && worksSpearatedBySincleSpaces;
    }
}
