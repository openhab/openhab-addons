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
package org.openhab.binding.freeboxos.internal.handler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link FreeDeviceIntf} defines some common methods for various devices (server, player, repeater) not belonging
 * to the same class hierarchy
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface FreeDeviceIntf extends ApiConsumerIntf {
    public ChannelUID getEventChannelUID();

    public void triggerChannel(ChannelUID channelUID, String event);

    default long checkUptimeAndFirmware(long newUptime, long oldUptime, String firmwareVersion) {
        if (newUptime < oldUptime) {
            triggerChannel(getEventChannelUID(), "restarted");
        }
        if (oldUptime < 0 || newUptime < oldUptime) {
            Map<String, String> properties = editProperties();
            String property = properties.get(Thing.PROPERTY_FIRMWARE_VERSION);
            if (!firmwareVersion.equals(property)) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
                updateProperties(properties);
                if (property != null) {
                    triggerChannel(getEventChannelUID(), "firmware_updated");
                }
            }
        }
        return newUptime;
    }

    default void processReboot(Runnable actualReboot) {
        triggerChannel(getEventChannelUID(), "reboot_requested");
        actualReboot.run();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "System rebooting...");
        stopJobs();
        addJob("Initialize", this::initialize, 30, TimeUnit.SECONDS);
    }
}
