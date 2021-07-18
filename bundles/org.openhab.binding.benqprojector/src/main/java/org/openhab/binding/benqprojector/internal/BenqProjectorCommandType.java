/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.InvalidClassException;

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
    POWER("Power", SwitchItem.class),
    SOURCE("Source", StringItem.class),
    PICTURE_MODE("PictureMode", StringItem.class),
    ASPECT_RATIO("AspectRatio", StringItem.class),
    FREEZE("Freeze", SwitchItem.class),
    BLANK("Blank", SwitchItem.class),
    DIRECTCMD("DirectCmd", StringItem.class),
    LAMP_TIME("LampTime", NumberItem.class);

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
     * Procedure to validate command type string.
     *
     * @param commandTypeText
     *            command string e.g. RawData, Command, Brightness
     * @return true if item is valid.
     * @throws IllegalArgumentException
     *             Not valid command type.
     * @throws InvalidClassException
     *             Not valid class for command type.
     */
    public static boolean validateBinding(String commandTypeText, Class<? extends Item> itemClass)
            throws IllegalArgumentException, InvalidClassException {
        for (BenqProjectorCommandType c : BenqProjectorCommandType.values()) {
            if (c.text.equalsIgnoreCase(commandTypeText)) {
                if (c.getItemClass().equals(itemClass)) {
                    return true;
                } else {
                    throw new InvalidClassException("Not valid class for command type");
                }
            }
        }

        throw new IllegalArgumentException("Not valid command type");
    }

    /**
     * Procedure to convert command type string to command type class.
     *
     * @param commandTypeText
     *            command string e.g. RawData, Command, Brightness
     * @return corresponding command type.
     * @throws InvalidClassException
     *             Not valid class for command type.
     */
    public static BenqProjectorCommandType getCommandType(String commandTypeText) throws IllegalArgumentException {
        for (BenqProjectorCommandType c : BenqProjectorCommandType.values()) {
            if (c.text.equalsIgnoreCase(commandTypeText)) {
                return c;
            }
        }

        throw new IllegalArgumentException("Not valid command type");
    }
}
