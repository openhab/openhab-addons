/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Map;

/**
 * This class implements the PollyTTS configuration.
 *
 * @author Robert Hillman - Initial contribution
 */
public class PollyTTSConfig {

    private final String accessKey;
    private final String secretKey;
    private final String serviceRegion;
    private final int expireDate;
    private String audioFormat;
    private long lastDelete;

    public PollyTTSConfig(Map<String, Object> config) {
        accessKey = config.get("accessKey").toString();
        secretKey = config.get("secretKey").toString();
        serviceRegion = config.get("serviceRegion").toString();
        audioFormat = config.get("audioFormat").toString();
        expireDate = (int) Double.parseDouble(config.get("cacheExpiration").toString());
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

}
