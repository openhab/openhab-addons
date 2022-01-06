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
package org.openhab.binding.dwdpollenflug.internal.dto;

/**
 * The {@link DWDPollenflug} class is internal DWD data structure.
 *
 * @author Johannes Ott - Initial contribution
 */
public enum DWDPollenflugPollen {
    AMBROSIA("ambrosia"),
    BEIFUSS("mugwort"),
    BIRKE("birch"),
    ERLE("alder"),
    ESCHE("ash"),
    GRAESER("grasses"),
    HASEL("hazel"),
    ROGGEN("rye");

    private final String channelName;

    private DWDPollenflugPollen(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}
