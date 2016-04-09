package com.github.nighturs.twittermatrix.topology;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import java.util.Map;
import java.util.stream.Collectors;

final class ConfigUtils {

    private ConfigUtils() {
        throw new UnsupportedOperationException("Instance not supported");
    }

    @SuppressWarnings("rawtypes")
    static <T extends Config> T createFromStormConf(Class<? extends T> clazz, Map conf) {
        //noinspection unchecked
        Map<String, String> nonNullValuesConf = ((Map<String, String>) conf).entrySet()
                .stream()
                .filter(x -> x.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ConfigFactory.create(clazz, nonNullValuesConf);
    }
}
