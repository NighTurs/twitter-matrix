package com.github.nighturs.twittermatrix.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import java.util.Map;
import java.util.stream.Collectors;

public final class ConfigUtils {

    private ConfigUtils() {
        throw new UnsupportedOperationException("Instance not supported");
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Config> T createFromStormConf(Class<? extends T> clazz, Map conf) {
        //noinspection unchecked
        Map<String, String> nonNullValuesConf = ((Map<String, String>) conf).entrySet()
                .stream()
                .filter(x -> x.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ConfigFactory.create(clazz, nonNullValuesConf);
    }
}
