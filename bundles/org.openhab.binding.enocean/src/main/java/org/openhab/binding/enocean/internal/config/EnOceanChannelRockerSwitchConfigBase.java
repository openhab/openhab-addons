/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.config;

import java.security.InvalidParameterException;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanChannelRockerSwitchConfigBase {

    public String switchMode;
    public String channel;

    public enum SwitchMode {
        Unkown(""),
        RockerSwitch("rockerSwitch"),
        ToggleDir1("toggleButtonDir1"),
        ToggleDir2("toggleButtonDir2");

        private String value;

        SwitchMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SwitchMode getSwitchMode(String value) {
            if (value == null) {
                return SwitchMode.Unkown;
            }

            for (SwitchMode t : SwitchMode.values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }

            throw new InvalidParameterException("Unknown SwitchMode");
        }
    }

    public enum Channel {
        Unkown(""),
        ChannelA("channelA"),
        ChannelB("channelB");

        private String value;

        Channel(String value) {
            this.value = value;
        }

        public static Channel getChannel(String value) {
            if (value == null) {
                return Channel.Unkown;
            }

            for (Channel t : Channel.values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }

            throw new InvalidParameterException("Unknown Channel");
        }
    }

    public EnOceanChannelRockerSwitchConfigBase() {
        switchMode = "rockerSwitch";
        channel = "channelA";
    }

    public SwitchMode getSwitchMode() {
        return SwitchMode.getSwitchMode(switchMode);
    }

    public Channel getChannel() {
        return Channel.getChannel(channel);
    }
}
