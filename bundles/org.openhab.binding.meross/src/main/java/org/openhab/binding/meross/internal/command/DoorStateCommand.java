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
 * The {@link DoorStateCommand} class is responsible for the concrete implementation of door state commands with garage
 * doors
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class DoorStateCommand {

    abstract static class Base implements Command {
        protected int deviceChannel;
        protected final int openValue;

        protected Base(int openValue, int deviceChannel) {
            this.openValue = openValue;
            this.deviceChannel = deviceChannel;
        }

        @Override
        public byte[] commandType(String type) {
            Map<String, Object> payload = Map.of("state", Map.of("open", openValue, "channel", deviceChannel));
            return MqttMessageBuilder.buildMqttMessage("SET", MerossEnum.Namespace.GARAGE_DOOR_STATE.value(), payload);
        }
    }

    /**
     * defines command up
     */
    public static class Up extends Base {
        public Up(int deviceChannel) {
            super(1, deviceChannel);
        }
    }

    /**
     * defines command down
     */
    public static class Down extends Base {
        public Down(int deviceChannel) {
            super(0, deviceChannel);
        }
    }
}
