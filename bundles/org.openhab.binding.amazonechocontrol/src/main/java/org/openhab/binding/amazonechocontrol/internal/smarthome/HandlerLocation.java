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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HandlerLocation} is responsible for the Alexa.Location interface
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HandlerLocation extends AbstractInterfaceHandler {
    public static final String INTERFACE = "Alexa.Location";

    private static final ChannelInfo GEOLOCATION_STATE = new ChannelInfo("geolocation", "geoLocation",
            Constants.CHANNEL_TYPE_GEOLOCATION);

    public HandlerLocation(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        if (GEOLOCATION_STATE.propertyName.equals(property)) {
            return Set.of(GEOLOCATION_STATE);
        }
        return Set.of();
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        for (JsonObject state : stateList) {
            if (GEOLOCATION_STATE.propertyName.equals(state.get("name").getAsString())) {
                JsonElement coordinateElement = state.get("value").getAsJsonObject().get("coordinate");
                if (coordinateElement != null) {
                    JsonObject coordinate = coordinateElement.getAsJsonObject();
                    Double latitude = coordinate.has("latitudeInDegrees")
                            ? coordinate.get("latitudeInDegrees").getAsDouble()
                            : null;
                    Double longitude = coordinate.has("longitudeInDegrees")
                            ? coordinate.get("longitudeInDegrees").getAsDouble()
                            : null;
                    if (latitude != null && longitude != null) {
                        updateState(GEOLOCATION_STATE.channelId,
                                new PointType(new DecimalType(latitude), new DecimalType(longitude)));
                    } else {
                        updateState(GEOLOCATION_STATE.channelId, UnDefType.UNDEF);
                    }
                }
            }
        }
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command)
            throws IOException, InterruptedException {
        return false;
    }
}
