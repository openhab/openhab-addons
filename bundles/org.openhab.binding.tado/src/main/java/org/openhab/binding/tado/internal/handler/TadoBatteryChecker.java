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
package org.openhab.binding.tado.internal.handler;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.ControlDevice;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    private Map<Long, State> zoneList = new HashMap<>();
    private Date refreshTime = new Date();
    private TadoHomeHandler homeHandler;

    public TadoBatteryChecker(TadoHomeHandler homeHandler) {
        this.homeHandler = homeHandler;
    }

    private synchronized void refreshZoneList() {
        Date now = new Date();
        if (homeHandler != null && (now.after(refreshTime) || zoneList.isEmpty())) {
            // be frugal, we only need to refresh the battery state hourly
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.HOUR, 1);
            refreshTime = calendar.getTime();

            Long homeId = homeHandler.getHomeId();
            if (homeId != null) {
                logger.debug("Fetching (battery state) zone list for HomeId {}", homeId);
                zoneList.clear();
                try {
                    homeHandler.getApi().listZones(homeId).forEach(zone -> {
                        boolean batteryLow = !zone.getDevices().stream().map(ControlDevice::getBatteryState)
                                .filter(Objects::nonNull).allMatch(s -> s.equals("NORMAL"));
                        zoneList.put(Long.valueOf(zone.getId()), OnOffType.from(batteryLow));
                    });
                } catch (IOException | ApiException e) {
                    logger.debug("Fetch (battery state) zone list exception");
                }
            }
        }
    }

    public State getBatteryLowAlarm(long zoneId) {
        refreshZoneList();
        return zoneList.getOrDefault(zoneId, UnDefType.UNDEF);
    }
}
