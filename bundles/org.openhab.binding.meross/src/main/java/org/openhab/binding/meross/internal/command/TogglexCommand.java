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
package org.openhab.binding.meross.internal.command;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.dto.MqttMessageBuilder;

/**
 * The {@link TogglexCommand} class is responsible for the concrete implementation of togglex commands which control
 * smart plugs and bulbs
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class TogglexCommand {
    /**
     * defines command turn on
     */
    public static class TurnOn implements Command {
        /**
         * build a togglex command on mode
         * 
         * @param type The command type
         * @return togglex command type, on mode
         */
        @Override
        public byte[] commandType(String type) {
            Map<String, Object> payload = Map.of("togglex", Map.of("onoff", 1, "channel", 0));
            return MqttMessageBuilder.buildMqttMessage("SET", MerossEnum.Namespace.CONTROL_TOGGLEX.value(), payload);
        }
    }

    /**
     * defines command turn off
     */
    public static class TurnOff implements Command {
        /**
         * build a togglex command off mode
         * 
         * @param type The command type
         * @return togglex command type, off mode
         */
        @Override
        public byte[] commandType(String type) {
            Map<String, Object> payload = Map.of("togglex", Map.of("onoff", 0, "channel", 0));
            return MqttMessageBuilder.buildMqttMessage("SET", MerossEnum.Namespace.CONTROL_TOGGLEX.value(), payload);
        }
    }
}
