/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.enocean.internal.config;

import java.security.InvalidParameterException;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanChannelVirtualRockerSwitchConfig {

    public Integer duration;
    public String switchMode;

    public enum SwitchMode {
        Unkown(""),
        RockerSwitch("rockerSwitch"),
        ToggleDir1("toggleButtonDir1"),
        ToggleDir2("toggleButtonDir2");

        private String value;

        SwitchMode(String value) {
            this.value = value;
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

    public EnOceanChannelVirtualRockerSwitchConfig() {
        duration = 350;
        switchMode = "rockerSwitch";
    }

    public SwitchMode getSwitchMode() {
        return SwitchMode.getSwitchMode(switchMode);
    }
}
