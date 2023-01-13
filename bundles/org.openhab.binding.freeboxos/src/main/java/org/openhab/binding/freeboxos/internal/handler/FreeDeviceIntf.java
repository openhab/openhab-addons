/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

@NonNullByDefault
public interface FreeDeviceIntf {
    public ChannelUID getEventChannelUID();

    public void triggerChannel(ChannelUID channelUID, String event);

    public Map<String, String> editProperties();

    public void updateProperties(@Nullable Map<String, String> properties);

    default long controlUptimeAndFirmware(long newUptime, long oldUptime, String firmwareVersion) {
        if (newUptime < oldUptime) {
            triggerChannel(getEventChannelUID(), "restarted");
            Map<String, String> properties = editProperties();
            if (!firmwareVersion.equals(properties.get(Thing.PROPERTY_FIRMWARE_VERSION))) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
                updateProperties(properties);
                triggerChannel(getEventChannelUID(), "firmware_updated");
            }
        }
        return newUptime;
    }

}
