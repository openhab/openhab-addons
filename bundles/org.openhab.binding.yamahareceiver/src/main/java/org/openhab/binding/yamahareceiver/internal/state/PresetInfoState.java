/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The preset state containing the channel names and currently selected channel
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - RX-V3900 compatibility improvements
 */
public class PresetInfoState implements Invalidateable {
    public static class Preset {
        private final String name;
        private final int value;

        public Preset(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Preset preset = (Preset) o;
            return value == preset.value && Objects.equals(name, preset.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }

    public int presetChannel = 0; // Used by NET_RADIO, RADIO, HD_RADIO, iPOD, USB, PC
    public final List<Preset> presetChannelNames = new ArrayList<>();
    public boolean presetChannelNamesChanged = false;

    @Override
    public void invalidate() {
        presetChannel = 0;
        presetChannelNames.clear();
    }
}
