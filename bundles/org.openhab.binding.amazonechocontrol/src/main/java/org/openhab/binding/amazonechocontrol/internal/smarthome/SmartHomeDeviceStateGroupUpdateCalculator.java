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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeCapabilities.SmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.DriverIdentity;
import org.openhab.binding.amazonechocontrol.internal.smarthome.JsonSmartHomeDevices.SmartHomeDevice;

/**
 * Handles the update interval calculation
 *
 * @author Michael Geramb
 */
@NonNullByDefault
public class SmartHomeDeviceStateGroupUpdateCalculator {
    static final boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()
            .toString().indexOf("-agentlib:jdwp") > 0;

    static final Integer updateIntervalPrivateSkillsInSeconds = isDebug ? 10 : 600;
    static final Integer updateIntervalAcousticEventsInSeconds = 10;
    Integer updateIntervalAmazonInSeconds;
    Integer updateIntervalSkillsInSeconds;

    class UpdateGroup {
        final int intervalInSeconds;
        Date lastUpdated;

        public UpdateGroup(int intervalInSeconds) {
            this.intervalInSeconds = intervalInSeconds;
            this.lastUpdated = new Date(0);
        }
    }

    Map<Integer, UpdateGroup> updateGroups = new HashMap<>();

    public SmartHomeDeviceStateGroupUpdateCalculator(int updateIntervalAmazonInSeconds,
            int updateIntervalSkillsInSeconds) {
        this.updateIntervalAmazonInSeconds = updateIntervalAmazonInSeconds;
        this.updateIntervalSkillsInSeconds = updateIntervalSkillsInSeconds;
    }

    Integer GetUpdateIntervalInSeconds(SmartHomeDevice shd) {
        Integer updateIntervalInSeconds = shd.UpdateIntervalInSeconds;
        if (updateIntervalInSeconds != null) {
            return updateIntervalInSeconds;
        }
        SmartHomeCapability[] capabilities = shd.capabilities;
        if (capabilities != null) {
            for (SmartHomeCapability capability : capabilities) {
                if (capability != null && HandlerAcousticEventSensor.INTERFACE.equals(capability.interfaceName)) {
                    updateIntervalInSeconds = updateIntervalAcousticEventsInSeconds;
                    break;
                }
            }
        }
        if (updateIntervalInSeconds == null) {
            if ("openHAB".equalsIgnoreCase(shd.manufacturerName)
                    || StringUtils.startsWithIgnoreCase(shd.manufacturerName, "ioBroker")) {
                // OpenHAB or ioBroker skill
                updateIntervalInSeconds = updateIntervalPrivateSkillsInSeconds;

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
        shd.UpdateIntervalInSeconds = updateIntervalInSeconds;
        return updateIntervalInSeconds;

    }

    public void RemoveDevicesWithNoUpdate(List<SmartHomeDevice> devices) {
        Date updateTimeStamp = new Date();
        // check if new group is needed
        boolean syncAllGroups = false;
        for (SmartHomeDevice device : devices) {
            int updateIntervalInSeconds = GetUpdateIntervalInSeconds(device);
            if (!updateGroups.containsKey(updateIntervalInSeconds)) {
                UpdateGroup newGroup = new UpdateGroup(updateIntervalInSeconds);
                updateGroups.put(updateIntervalInSeconds, newGroup);
                syncAllGroups = true;
            }
        }
        // check which groups needs an update
        Set<Integer> groupsToUpdate = new HashSet<Integer>();
        for (UpdateGroup group : updateGroups.values()) {
            long millisecondsSinceLastUpdate = updateTimeStamp.getTime() - group.lastUpdated.getTime();
            if (syncAllGroups || millisecondsSinceLastUpdate >= group.intervalInSeconds * 1000) {
                group.lastUpdated = updateTimeStamp;
                groupsToUpdate.add(group.intervalInSeconds);
            }
        }
        // remove unused devices
        for (int i = devices.size() - 1; i >= 0; i--) {
            if (!groupsToUpdate.contains(GetUpdateIntervalInSeconds(devices.get(i)))) {
                devices.remove(i);
            }
        }

    }
}
