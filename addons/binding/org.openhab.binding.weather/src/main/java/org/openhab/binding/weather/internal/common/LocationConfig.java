/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weather.internal.common;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openhab.binding.weather.internal.model.ProviderName;

/**
 * Holds a location configuration for the bridge.
 *
 * @author Gerhard Riegler
 * @since 1.6.0
 */
public class LocationConfig {
    private static final int DEFAULT_UPDATE_INTERVAL = 240;

    private String providerName;
    private String language = "en";
    private Double latitude;
    private Double longitude;
    private String woeid;
    private Integer updateInterval = DEFAULT_UPDATE_INTERVAL;
    private String name;
    private String units = "si";
    private String apikey;
    private String apikey2;

    /**
     * Returns the language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Returns the latitude.
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude.
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude.
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude.
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the woeid.
     */
    public String getWoeid() {
        return woeid;
    }

    /**
     * Sets the woeid.
     */
    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    /**
     * Returns the updateInterval in minutes.
     */
    public Integer getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Sets the updateInterval in minutes.
     */
    public void setUpdateInterval(Integer updateInterval) {
        this.updateInterval = updateInterval;
    }

    /**
     * Returns the providerName.
     */
    public ProviderName getProviderName() {
        if (providerName != null) {
            return ProviderName.valueOf(providerName);
        }
        return null;
    }

    /**
     * Sets the providerName.
     */
    public void setProviderName(ProviderName providerName) {
        this.providerName = providerName.toString();
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the units of measurement.
     */
    public String getMeasurementUnits() {
        return units;
    }

    /**
     * Sets the units of measurement.
     */
    public void setMeasurementUnits(String u) {
        units = u;
    }

    /**
     * Returns the apikey.
     */
    public String getApiKey() {
        return apikey;
    }

    /**
     * Sets the apikey.
     */
    public void setApiKey(String apiKey) {
        this.apikey = apiKey;
    }

    /**
     * Returns the apikey2.
     */
    public String getApiKey2() {
        return apikey2;
    }

    /**
     * Sets the apikey2.
     */
    public void setApiKey2(String apiKey2) {
        this.apikey2 = apiKey2;
    }

    /**
     * Returns true, if this config is valid.
     */
    public boolean isValid() {
        boolean valid = providerName != null && language != null && updateInterval != null;
        if (!valid) {
            return false;
        }

        if (providerName != null) {
            ProviderName name = ProviderName.valueOf(providerName);
            if (apikey != null && (name != ProviderName.HamWeather)
                    || (name == ProviderName.HamWeather && apikey2 != null)) {
                if (name == ProviderName.Yahoo) {
                    return woeid != null;
                } else {
                    return latitude != null && longitude != null;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("providerName", providerName).append("apiKey", apikey);
        if (apikey2 != null) {
            tsb.append("apiKey2", apikey2);
        }
        return tsb.append("language", language).append("updateInterval", updateInterval).append("latitude", latitude)
                .append("longitude", longitude).append("woeid", woeid).append("name", name).toString();
    }

}
