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
package org.openhab.binding.tado.internal.handler;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.ControlDevice;
import org.openhab.binding.tado.internal.api.model.Zone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TadoBatteryChecker} checks the battery state of Tado control
 * devices.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class TadoBatteryChecker {
    private final Logger logger = LoggerFactory.getLogger(TadoBatteryChecker.class);

    private List<Zone> zoneList = null;
    private Date refreshTime = new Date();
    private TadoHomeHandler homeHandler;

    public TadoBatteryChecker(TadoHomeHandler homeHandler) {
        this.homeHandler = homeHandler;
    }

    private synchronized void refreshZoneList() {
        Date now = new Date();
        if (homeHandler != null && (now.after(refreshTime) || zoneList == null)) {
            // be frugal, we only need to refresh the battery state hourly
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR, 1);
            refreshTime = calendar.getTime();

            Long homeId = homeHandler.getHomeId();
            if (homeId != null) {
                logger.debug("Fetching (battery state) zone list for HomeId {}", homeId);
                try {
                    zoneList = homeHandler.getApi().listZones(homeId);
                } catch (IOException | ApiException e) {
                    zoneList = null;
                    logger.debug("Fetch (battery state) zone list exception", e);
                }
            }
        }
    }

    public State getBatteryLowAlarm(long zoneId) {
        refreshZoneList();
        boolean hasBatteryStateValue = false;
        if (zoneList != null) {
            // logger.debug("Fetching battery state for ZoneId {}", zoneId);
            for (Zone zone : zoneList) {
                if (zoneId == zone.getId()) {
                    for (ControlDevice device : zone.getDevices()) {
                        String batteryState = device.getBatteryState();
                        if (batteryState != null) {
                            if (!batteryState.equals("NORMAL")) {    
                                return OnOffType.ON;
                            }
                            hasBatteryStateValue = true;
                        }
                    }
                    break;
                }
            }
        }
        return hasBatteryStateValue ? OnOffType.OFF : UnDefType.UNDEF;
    }

}
