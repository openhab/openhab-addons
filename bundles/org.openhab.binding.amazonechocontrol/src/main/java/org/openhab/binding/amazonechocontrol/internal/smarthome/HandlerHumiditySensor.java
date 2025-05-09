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

import static org.openhab.binding.amazonechocontrol.internal.smarthome.Constants.CHANNEL_TYPE_AIR_QUALITY_HUMIDITY;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeCapability;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HandlerHumiditySensor} is responsible for the Alexa.HumiditySensor interface
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HandlerHumiditySensor extends AbstractInterfaceHandler {
    private final Logger logger = LoggerFactory.getLogger(HandlerHumiditySensor.class);
    public static final String INTERFACE = "Alexa.HumiditySensor";

    private static final ChannelInfo HUMIDITY = new ChannelInfo("relativeHumidity", "relativeHumidity",
            CHANNEL_TYPE_AIR_QUALITY_HUMIDITY);

    public HandlerHumiditySensor(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        super(smartHomeDeviceHandler, List.of(INTERFACE));
    }

    @Override
    protected Set<ChannelInfo> findChannelInfos(JsonSmartHomeCapability capability, @Nullable String property) {
        if (HUMIDITY.propertyName.equals(property)) {
            return Set.of(HUMIDITY);
        }
        return Set.of();
    }

    @Override
    public void updateChannels(String interfaceName, List<JsonObject> stateList, UpdateChannelResult result) {
        QuantityType<Dimensionless> humidityValue = null;
        for (JsonObject state : stateList) {
            if (HUMIDITY.propertyName.equals(state.get("name").getAsString()) && humidityValue == null) {
                JsonElement value = state.get("value");
                BigDecimal humidity;
                if (value.isJsonObject()) {
                    humidity = value.getAsJsonObject().getAsBigDecimal();
                } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                    humidity = value.getAsJsonPrimitive().getAsBigDecimal();
                } else {
                    logger.warn("Could not properly convert {}", state);
                    continue;
                }
                humidityValue = new QuantityType<>(humidity, Units.PERCENT);
            }
        }
        smartHomeDeviceHandler.updateState(HUMIDITY.channelId, humidityValue == null ? UnDefType.UNDEF : humidityValue);
    }

    @Override
    public boolean handleCommand(Connection connection, JsonSmartHomeDevice shd, String entityId,
            List<JsonSmartHomeCapability> capabilities, String channelId, Command command) throws IOException {
        return false;
    }
}
