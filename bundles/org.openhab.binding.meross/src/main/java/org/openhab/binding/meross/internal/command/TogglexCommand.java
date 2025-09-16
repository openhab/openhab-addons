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
 * @author Mark Herwege - Refactored for extra parameters
 */
@NonNullByDefault
public class TogglexCommand {

    abstract static class Base implements MerossCommand {
        protected final int onOffValue;
        protected int deviceChannel;

        protected Base(int deviceChannel, int onOffValue) {
            this.deviceChannel = deviceChannel;
            this.onOffValue = onOffValue;
        }

        @Override
        public byte[] command(String deviceUUID) {
            Map<String, Object> payload = Map.of("togglex", Map.of("onoff", onOffValue, "channel", deviceChannel));
            return MqttMessageBuilder.buildMqttMessage("SET", MerossEnum.Namespace.CONTROL_TOGGLEX.value(), deviceUUID,
                    payload);
        }
    }

    /**
     * defines command turn on
     */
    public static class TurnOn extends Base {
        public TurnOn(int deviceChannel) {
            super(deviceChannel, 1);
        }
    }

    /**
     * defines command turn off
     */
    public static class TurnOff extends Base {
        public TurnOff(int deviceChannel) {
            super(deviceChannel, 0);
        }
    }
}
