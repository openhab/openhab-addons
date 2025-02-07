/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice.DriverIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the update interval calculation
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class SmartHomeDeviceStateGroupUpdateCalculator {
    private final Logger logger = LoggerFactory.getLogger(SmartHomeDeviceStateGroupUpdateCalculator.class);

    private static final int UPDATE_INTERVAL_PRIVATE_SKILLS_IN_SECONDS = 300;
    private static final int UPDATE_INTERVAL_PRIVATE_SKILLS_IN_SECONDS_TRACE = 10;
    private static final int UPDATE_INTERVAL_ACOUSTIC_EVENTS_IN_SECONDS = 10;
    private final int updateIntervalAmazonInSeconds;
    private final int updateIntervalSkillsInSeconds;

    private static class UpdateGroup {
        private final int intervalInSeconds;
        private Date lastUpdated;

        public UpdateGroup(int intervalInSeconds) {
            this.intervalInSeconds = intervalInSeconds;
            this.lastUpdated = new Date(0);
        }
    }

    private final Map<Integer, UpdateGroup> updateGroups = new HashMap<>();

    public SmartHomeDeviceStateGroupUpdateCalculator(int updateIntervalAmazonInSeconds,
            int updateIntervalSkillsInSeconds) {
        this.updateIntervalAmazonInSeconds = updateIntervalAmazonInSeconds;
        this.updateIntervalSkillsInSeconds = updateIntervalSkillsInSeconds;
    }

    private Integer getUpdateIntervalInSeconds(JsonSmartHomeDevice shd) {
        Integer updateIntervalInSeconds = shd.updateIntervalInSeconds;
        if (updateIntervalInSeconds != null) {
            return updateIntervalInSeconds;
        }
        if (shd.getCapabilities().stream()
                .anyMatch(capability -> HandlerAcousticEventSensor.INTERFACE.equals(capability.interfaceName))) {
            updateIntervalInSeconds = UPDATE_INTERVAL_ACOUSTIC_EVENTS_IN_SECONDS;
        }

        if (updateIntervalInSeconds == null) {
            String manufacturerName = shd.manufacturerName;
            if (manufacturerName != null && ("openHAB".equalsIgnoreCase(manufacturerName)
                    || manufacturerName.toLowerCase().startsWith("iobroker"))) {
                // OpenHAB or ioBroker skill
                if (logger.isTraceEnabled()) {
                    updateIntervalInSeconds = UPDATE_INTERVAL_PRIVATE_SKILLS_IN_SECONDS_TRACE;
                } else {
                    updateIntervalInSeconds = UPDATE_INTERVAL_PRIVATE_SKILLS_IN_SECONDS;
                }
            } else {
                boolean isSkillDevice = false;
                DriverIdentity driverIdentity = shd.driverIdentity;
                isSkillDevice = driverIdentity != null && "SKILL".equals(driverIdentity.namespace);
                if (isSkillDevice) {
                    updateIntervalInSeconds = updateIntervalSkillsInSeconds;
                } else {
                    updateIntervalInSeconds = updateIntervalAmazonInSeconds;
                }
            }
        }
        shd.updateIntervalInSeconds = updateIntervalInSeconds;
        return updateIntervalInSeconds;
    }

    public void removeDevicesWithNoUpdate(List<JsonSmartHomeDevice> devices) {
        Date updateTimeStamp = new Date();
        // check if new group is needed
        boolean syncAllGroups = false;
        for (JsonSmartHomeDevice device : devices) {
            int updateIntervalInSeconds = getUpdateIntervalInSeconds(device);
            if (!updateGroups.containsKey(updateIntervalInSeconds)) {
                UpdateGroup newGroup = new UpdateGroup(updateIntervalInSeconds);
                updateGroups.put(updateIntervalInSeconds, newGroup);
                syncAllGroups = true;
            }
        }
        // check which groups needs an update
        Set<Integer> groupsToUpdate = new HashSet<>();
        for (UpdateGroup group : updateGroups.values()) {
            long millisecondsSinceLastUpdate = updateTimeStamp.getTime() - group.lastUpdated.getTime();
            if (syncAllGroups || millisecondsSinceLastUpdate >= group.intervalInSeconds * 1000) {
                group.lastUpdated = updateTimeStamp;
                groupsToUpdate.add(group.intervalInSeconds);
            }
        }
        // remove unused devices
        for (int i = devices.size() - 1; i >= 0; i--) {
            if (!groupsToUpdate.contains(getUpdateIntervalInSeconds(devices.get(i)))) {
                devices.remove(i);
            }
        }
    }
}
