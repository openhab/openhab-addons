/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;

/**
 * Java Bean to represent a JSON part of message describing a Netatmo device
 * properties
 *
 * @author Andreas Brenk
 * @author Gaël L'hopital
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetatmoDevice extends AbstractDevice {

    private static final int[] WIFI_STATUS_THRESHOLDS = { 56, 71, 86 };

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlacePart extends AbstractMessage {

        public Integer getAltitude() {
            return altitude;
        }

        public String getCountry() {
            return country;
        }

        public List<Double> getLocation() {
            return location;
        }

        public String getTimezone() {
            return timezone;
        }

        public Boolean getTrustedLocation() {
            return trust_location;
        }

        protected Integer altitude;
        protected String country;
        protected List<Double> location;
        protected String timezone;
        protected Boolean trust_location;

        @Override
        public String toString() {
            final ToStringBuilder builder = createToStringBuilder();
            builder.appendSuper(super.toString());

            builder.append("altitude", getAltitude());
            builder.append("country", getCountry());
            builder.append("location", getLocation());
            builder.append("timezone", getTimezone());
            builder.append("trustedLocation", getTrustedLocation());

            return builder.toString();
        }
    }

    protected String ip;
    protected Date last_fw_update;
    protected Date last_radio_store;
    protected Date last_status_store;
    protected Date last_upgrade;
    protected List<String> modules;
    protected PlacePart place;
    protected String station_name;
    protected List<String> user_owner;
    protected Integer wifi_status;

    private int getWifiStatus() {
        return this.wifi_status;
    }

    public Integer getAltitude() {
        return this.place.altitude;
    }

    public Double getLatitude() {
        return this.place.location.get(1);
    }

    public Double getLongitude() {
        return this.place.location.get(0);
    }

    public int getWifiStatusAsPercent() {
        int level = 100;
        int step = 100 / (WIFI_STATUS_THRESHOLDS.length + 1);
        for (int i : WIFI_STATUS_THRESHOLDS) {
            if (getWifiStatus() < i)
                break;
            level -= step;
        }
        return level;
    }

    public BigDecimal getwifistatus() {
        return new BigDecimal(getWifiStatusAsPercent());
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = createToStringBuilder();
        builder.appendSuper(super.toString());

        builder.append("ip", this.ip);
        builder.append("lastFirmwareUpdate", getLastFirmwareUpdate());
        builder.append("lastRadioStore", getLastRadioStore());
        builder.append("lastStatusStore", getLastStatusStore());
        builder.append("lastUpgrade", getLastUpgrade());
        builder.append("modules", getModules());
        builder.append("place", getPlace());
        builder.append("stationName", getStationName());
        builder.append("owner", getOwner());
        builder.append("wifiStatus", this.getWifiStatus());

        return builder.toString();
    }

    private List<String> getOwner() {
        return this.user_owner;
    }

    private String getStationName() {
        return this.station_name;
    }

    private PlacePart getPlace() {
        return this.place;
    }

    private List<String> getModules() {
        return this.modules;
    }

    private Date getLastUpgrade() {
        return this.last_upgrade;
    }

    private Date getLastStatusStore() {
        return this.last_status_store;
    }

    private Date getLastRadioStore() {
        return this.last_radio_store;
    }

    private Date getLastFirmwareUpdate() {
        return this.last_fw_update;
    }

    public PointType getlocation() {
        return new PointType(new DecimalType(getLatitude()), new DecimalType(getLongitude()),
                new DecimalType(getAltitude()));
    }

}
