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
package org.openhab.binding.zoneminder.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Monitor} represents the attributes of a Zoneminder monitor
 * that are relevant to this binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Monitor {

    private final Logger logger = LoggerFactory.getLogger(Monitor.class);

    private String id;
    private String name;
    private String function;
    private Boolean enabled;
    private String status;
    private Boolean alarm = Boolean.FALSE;
    private MonitorState state = MonitorState.UNKNOWN;
    private Integer hourEvents = 0;
    private Integer dayEvents = 0;
    private Integer weekEvents = 0;
    private Integer monthEvents = 0;
    private Integer totalEvents = 0;
    private String imageUrl = "";
    private String videoUrl = "";
    private @Nullable Event lastEvent = null;

    public Monitor(String monitorId, String name, String function, String enabled, String status) {
        this.id = monitorId;
        this.name = name;
        this.function = function;
        this.enabled = "1".equals(enabled);
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String setId(String id) {
        return this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean isAlarm() {
        return alarm;
    }

    public MonitorState getState() {
        return state;
    }

    public void setState(MonitorState state) {
        this.alarm = (state != MonitorState.IDLE && state != MonitorState.UNKNOWN);
        this.state = state;
    }

    public Integer getHourEvents() {
        return hourEvents;
    }

    public void setHourEvents(@Nullable String hourEvents) {
        if (hourEvents != null) {
            try {
                this.hourEvents = Integer.parseInt(hourEvents);
            } catch (NumberFormatException e) {
                logger.debug("Monitor object contains invalid hourEvents: {}", hourEvents);
            }
        }
    }

    public Integer getDayEvents() {
        return dayEvents;
    }

    public void setDayEvents(@Nullable String dayEvents) {
        if (dayEvents != null) {
            try {
                this.dayEvents = Integer.parseInt(dayEvents);
            } catch (NumberFormatException e) {
                logger.debug("Monitor object contains invalid dayEvents: {}", dayEvents);
            }
        }
    }

    public Integer getWeekEvents() {
        return weekEvents;
    }

    public void setWeekEvents(@Nullable String weekEvents) {
        if (weekEvents != null) {
            try {
                this.weekEvents = Integer.parseInt(weekEvents);
            } catch (NumberFormatException e) {
                logger.debug("Monitor object contains invalid totalEvents: {}", weekEvents);
            }
        }
    }

    public Integer getMonthEvents() {
        return monthEvents;
    }

    public void setMonthEvents(@Nullable String monthEvents) {
        if (monthEvents != null) {
            try {
                this.monthEvents = Integer.parseInt(monthEvents);
            } catch (NumberFormatException e) {
                logger.debug("Monitor object contains invalid monthEvents: {}", monthEvents);
            }
        }
    }

    public Integer getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(@Nullable String totalEvents) {
        if (totalEvents != null) {
            try {
                this.totalEvents = Integer.parseInt(totalEvents);
            } catch (NumberFormatException e) {
                logger.debug("Monitor object contains invalid totalEvents: {}", totalEvents);
            }
        }
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public @Nullable Event getMostRecentCompletedEvent() {
        return lastEvent;
    }

    public void setLastEvent(@Nullable Event lastEvent) {
        this.lastEvent = lastEvent;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", function=").append(function);
        sb.append(", enabled=").append(enabled);
        sb.append(", status=").append(status);
        sb.append(", alarm=").append(alarm);
        sb.append(", state=").append(state);
        sb.append(", events=(").append(hourEvents);
        sb.append(",").append(dayEvents);
        sb.append(",").append(weekEvents);
        sb.append(",").append(monthEvents);
        sb.append(",").append(totalEvents);
        sb.append(")");
        return sb.toString();
    }
}
