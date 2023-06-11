/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.voice.pollytts.internal.cloudapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class implements the PollyTTS configuration.
 *
 * @author Robert Hillman - Initial contribution
 */
public class PollyTTSConfig {

    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final String SERVICE_REGION = "serviceRegion";
    private static final String AUDIO_FORMAT = "audioFormat";
    private static final String CACHE_EXPIRATION = "cacheExpiration";

    private String accessKey = "";
    private String secretKey = "";
    private String serviceRegion = "eu-west-1";
    private int expireDate = 0;
    private String audioFormat = "default";
    private long lastDelete;

    public PollyTTSConfig(Map<String, Object> config) {
        assertValidConfig(config);

        accessKey = config.getOrDefault(ACCESS_KEY, accessKey).toString();
        secretKey = config.getOrDefault(SECRET_KEY, secretKey).toString();
        serviceRegion = config.getOrDefault(SERVICE_REGION, serviceRegion).toString();
        audioFormat = config.getOrDefault(AUDIO_FORMAT, audioFormat).toString();
        expireDate = (int) Double
                .parseDouble(config.getOrDefault(CACHE_EXPIRATION, Double.toString(expireDate)).toString());
    }

    private void assertValidConfig(Map<String, Object> config) {
        List<String> emptyParams = Stream.of(ACCESS_KEY, SECRET_KEY, SERVICE_REGION)
                .filter(param -> config.get(param) == null || config.get(param).toString().isEmpty())
                .collect(Collectors.toList());

        if (!emptyParams.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing configuration parameters: " + emptyParams.stream().collect(Collectors.joining(", ")));
        }
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getServiceRegion() {
        return serviceRegion;
    }

    /**
     * get the life time for cache files
     */
    public int getExpireDate() {
        return expireDate;
    }

    /**
     * returns audio format specified for audio
     */
    public String getAudioFormat() {
        return audioFormat;
    }

    /**
     * get the date when cache was cleaned last
     */
    public long getLastDelete() {
        return lastDelete;
    }

    /**
     * set the date when cache was cleaned last
     */
    public void setLastDelete(long lastDelete) {
        this.lastDelete = lastDelete;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PollyTTSConfig [accessKey=").append(accessKey).append(", secretKey=").append(secretKey)
                .append(", serviceRegion=").append(serviceRegion).append(", expireDate=").append(expireDate)
                .append(", audioFormat=").append(audioFormat).append(", lastDelete=").append(lastDelete).append("]");
        return builder.toString();
    }
}
