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
    private Logger logger = LoggerFactory.getLogger(TadoBatteryChecker.class);

    private List<Zone> zoneList = null;
    private long homeId = -1;
    private Date refreshTime = new Date();

    public TadoBatteryChecker() {
    }

    private synchronized void initializeZoneList(TadoZoneHandler callerZone) {
        Date now = new Date();
        if (now.after(refreshTime) || homeId != callerZone.getHomeId() || zoneList == null) {
            // be frugal, we only need to refresh the battery state hourly
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR, 1);
            refreshTime = calendar.getTime();

            // note: (to do) we would require multiple zoneLists or multiple
            // TadoBatteryChecker instances if simultaneously monitoring multiple homes, but
            // this is highly unlikely so we won't bother writing code for it yet ;)
            homeId = callerZone.getHomeId();
            logger.debug("Fetching (battery state) zone list for HomeId {}", homeId);
            try {
                zoneList = callerZone.getApi().listZones(homeId);
            } catch (IOException | ApiException e) {
                zoneList = null;
                logger.debug("Fetch (battery state) zone list exception {}", e);
            }
        }
    }

    public OnOffType getBatteryLowAlarm(TadoZoneHandler callerZone) {
        initializeZoneList(callerZone);
        if (zoneList != null) {
            for (Zone thisZone : zoneList) {
                if (thisZone.getId() == callerZone.getZoneId()) {
                    Boolean alarm = false;
                    for (ControlDevice thisDevice : thisZone.getDevices()) {
                        String batteryState = thisDevice.getBatteryState();
                        alarm = alarm || (batteryState != null && !batteryState.equals("NORMAL"));
//                        logger.trace("HomeId {}, ZoneId {}, Device SerialNo {}, Battery State {}",
//                                callerZone.getHomeId(), callerZone.getZoneId(), thisDevice.getSerialNo(),
//                                thisDevice.getBatteryState());
                    }
                    return OnOffType.from(alarm);
                }
            }
        }
        return OnOffType.from(false);
    }

}
