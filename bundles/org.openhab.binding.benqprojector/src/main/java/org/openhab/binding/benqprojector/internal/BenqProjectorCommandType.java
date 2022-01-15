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
package org.openhab.binding.benqprojector.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;

/**
 * Represents all valid command types which could be processed by this
 * binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public enum BenqProjectorCommandType {
    POWER("power", SwitchItem.class),
    SOURCE("source", StringItem.class),
    PICTURE_MODE("picturemode", StringItem.class),
    ASPECT_RATIO("aspectratio", StringItem.class),
    FREEZE("freeze", SwitchItem.class),
    BLANK("blank", SwitchItem.class),
    DIRECTCMD("directcmd", StringItem.class),
    LAMP_TIME("lamptime", NumberItem.class);

    private final String text;
    private Class<? extends Item> itemClass;

    private BenqProjectorCommandType(final String text, Class<? extends Item> itemClass) {
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
    public static BenqProjectorCommandType getCommandType(String commandTypeText) throws IllegalArgumentException {
        for (BenqProjectorCommandType c : BenqProjectorCommandType.values()) {
            if (c.text.equals(commandTypeText)) {
                return c;
            }
        }

        throw new IllegalArgumentException("Not valid command type: " + commandTypeText);
    }
}
