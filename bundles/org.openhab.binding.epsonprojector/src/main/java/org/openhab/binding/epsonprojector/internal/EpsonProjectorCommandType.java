/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.epsonprojector.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;

/**
 * Represents all valid command types which could be processed by this
 * binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public enum EpsonProjectorCommandType {
    POWER("power", SwitchItem.class),
    POWER_STATE("powerstate", StringItem.class),
    LAMP_TIME("lamptime", NumberItem.class),
    KEY_CODE("keycode", StringItem.class),
    VKEYSTONE("verticalkeystone", NumberItem.class),
    HKEYSTONE("horizontalkeystone", NumberItem.class),
    AKEYSTONE("autokeystone", SwitchItem.class),
    FREEZE("freeze", SwitchItem.class),
    ASPECT_RATIO("aspectratio", StringItem.class),
    LUMINANCE("luminance", StringItem.class),
    SOURCE("source", StringItem.class),
    BRIGHTNESS("brightness", NumberItem.class),
    CONTRAST("contrast", NumberItem.class),
    DENSITY("density", NumberItem.class),
    TINT("tint", NumberItem.class),
    COLOR_TEMP("colortemperature", NumberItem.class),
    FLESH_TEMP("fleshtemperature", NumberItem.class),
    COLOR_MODE("colormode", StringItem.class),
    HPOSITION("horizontalposition", NumberItem.class),
    VPOSITION("verticalposition", NumberItem.class),
    GAMMA("gamma", StringItem.class),
    VOLUME("volume", DimmerItem.class),
    MUTE("mute", SwitchItem.class),
    HREVERSE("horizontalreverse", SwitchItem.class),
    VREVERSE("verticalreverse", SwitchItem.class),
    BACKGROUND("background", StringItem.class),
    ERR_CODE("errcode", NumberItem.class),
    ERR_MESSAGE("errmessage", StringItem.class);

    private final String text;
    private Class<? extends Item> itemClass;

    private EpsonProjectorCommandType(final String text, Class<? extends Item> itemClass) {
        this.text = text;
        this.itemClass = itemClass;
    }

    @Override
    public String toString() {
        return text;
    }

    public Class<? extends Item> getItemClass() {
        return itemClass;
    }

    /**
     * Procedure to convert command type string to command type class.
     *
     * @param commandTypeText
     *            command string e.g. RawData, Command, Brightness
     * @return corresponding command type.
     * @throws IllegalArgumentException
     *             No valid class for command type.
     */
    public static EpsonProjectorCommandType getCommandType(String commandTypeText) throws IllegalArgumentException {
        for (EpsonProjectorCommandType c : EpsonProjectorCommandType.values()) {
            if (c.text.equals(commandTypeText)) {
                return c;
            }
        }

        throw new IllegalArgumentException("Not valid command type: " + commandTypeText);
    }
}
