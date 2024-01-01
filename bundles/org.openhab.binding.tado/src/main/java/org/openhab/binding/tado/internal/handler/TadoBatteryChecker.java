/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.ControlDevice;
import org.openhab.binding.tado.internal.api.model.Zone;
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
@NonNullByDefault
public class TadoBatteryChecker {
    private final Logger logger = LoggerFactory.getLogger(TadoBatteryChecker.class);

    private final TadoHomeHandler homeHandler;
    private Map<Long, Zone> zones = new HashMap<>();
    private Instant refreshTime = Instant.MIN;

    public TadoBatteryChecker(TadoHomeHandler homeHandler) {
        this.homeHandler = homeHandler;
    }

    private void refreshZoneList() {
        if (refreshTime.isAfter(Instant.now())) {
            return;
        }
        // only refresh the battery state hourly
        refreshTime = Instant.now().plus(1, ChronoUnit.HOURS);
        Long homeId = homeHandler.getHomeId();
        if (homeId != null) {
            logger.debug("Fetching (battery state) zone list for HomeId {}", homeId);
            try {
                Map<Long, Zone> zones = new HashMap<>();
                homeHandler.getApi().listZones(homeId).stream().filter(Objects::nonNull)
                        .forEach(zone -> zones.put((long) zone.getId(), zone));
                this.zones = zones;
            } catch (IOException | ApiException e) {
                logger.debug("Fetch (battery state) zone list exception");
            }
        }
    }

    public synchronized Optional<Zone> getZone(long zoneId) {
        refreshZoneList();
        return Optional.ofNullable(zones.get(zoneId));
    }

    public State getBatteryLowAlarm(long zoneId) {
        Optional<Zone> zone = getZone(zoneId);
        if (zone.isPresent()) {
            boolean batteryOk = zone.get().getDevices().stream().map(ControlDevice::getBatteryState)
                    .filter(Objects::nonNull).allMatch(batteryState -> "NORMAL".equals(batteryState));
            return OnOffType.from(!batteryOk);
        }
        return UnDefType.UNDEF;
    }
}
