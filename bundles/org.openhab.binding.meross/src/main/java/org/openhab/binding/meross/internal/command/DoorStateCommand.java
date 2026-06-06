/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * The {@link DoorStateCommand} class is responsible for the concrete implementation of door state commands with garage
 * doors
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class DoorStateCommand {

    abstract static class Base implements MerossCommand {
        protected int deviceChannel;
        protected final int openValue;

        protected Base(int deviceChannel, int openValue) {
            this.deviceChannel = deviceChannel;
            this.openValue = openValue;
        }

        @Override
        public byte[] command(String deviceUUID) {
            Map<String, Object> payload = Map.of("state", Map.of("open", openValue, "channel", deviceChannel));
            return MqttMessageBuilder.buildMqttMessage("SET", MerossEnum.Namespace.GARAGE_DOOR_STATE.value(),
                    deviceUUID, payload);
        }
    }

    /**
     * defines command up
     */
    public static class Up extends Base {
        public Up(int deviceChannel) {
            super(deviceChannel, 1);
        }
    }

    /**
     * defines command down
     */
    public static class Down extends Base {
        public Down(int deviceChannel) {
            super(deviceChannel, 0);
        }
    }
}
