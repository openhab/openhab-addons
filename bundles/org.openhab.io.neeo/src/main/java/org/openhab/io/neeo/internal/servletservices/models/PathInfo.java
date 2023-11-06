/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.servletservices.models;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.models.ItemSubType;
import org.openhab.io.neeo.internal.models.NeeoThingUID;

/**
 * The route path information class. This class will evaluate the route path into it's logical components.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class PathInfo {

    /** The thing uid */
    private final NeeoThingUID thingUid;

    /** The item name */
    private final String itemName;

    /** The channel number */
    private final ItemSubType subType;

    /** The channel number */
    private final int channelNbr;

    /** The component type */
    private final String componentType;

    /** The component sub type */
    private final String componentSubType;

    /** The action value */
    private final @Nullable String actionValue;

    /**
     * Creates the path info object from the route path
     *
     * @param paths the non-null, non-empty route paths
     */
    public PathInfo(String[] paths) {
        Objects.requireNonNull(paths, "paths cannot be null");

        // Note - the following check ensures that the path contains exactly what we check
        // and will fail if not (in the case of when a firmware update changes what we expect)
        if (paths.length < 7 || paths.length > 9) {
            throw new IllegalArgumentException(
                    "Path length invalid (must be between 7 and 9): " + String.join(",", paths));
        }

        // The path can be one of the following formats:
        // 1. /device/{thingUID}/{itemName}/{channelNbr}/button/on/default
        // 2. /device/{thingUID}/{itemName}/{channelNbr}/{component}/{componentsubtype}/default/[value]
        // 3. /device/{thingUID}/{itemName}/{itemSubType}/{channelNbr}/button/on/default
        // 4. /device/{thingUID}/{itemName}/{itemSubType}/{channelNbr}/{component}/{componentsubtype}/default/[value]

        // new ThingUID can throw illegal argument exception as well
        int idx = 1;
        thingUid = new NeeoThingUID(paths[idx++]);

        itemName = paths[idx++];

        subType = ItemSubType.isValid(paths[idx]) ? ItemSubType.parse(paths[idx++]) : ItemSubType.NONE;

        try {
            channelNbr = Integer.parseInt(paths[idx++]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channelNbr is not a valid integer: " + paths[idx - 1]);
        }

        // button/textlabel/slider/etc
        componentType = paths[idx++];

        // actor/sensor/buttoncmd
        if ("button".equalsIgnoreCase(componentType)) {
            componentSubType = "actor";
            actionValue = paths[idx++];
        } else {
            componentSubType = paths[idx++];
            actionValue = paths.length == 9 ? paths[idx + 1] : null;
        }

        // we don't care about the next one currently (always 'default')
        // componentId = paths[6];
    }

    /**
     * Gets the {@link NeeoThingUID}
     *
     * @return the {@link NeeoThingUID}
     */
    public NeeoThingUID getThingUid() {
        return thingUid;
    }

    /**
     * Gets the item name
     *
     * @return the item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Gets the item subtype
     *
     * @return the item subtype
     */
    public ItemSubType getSubType() {
        return subType;
    }

    /**
     * Gets the channel number
     *
     * @return the channel number
     */
    public int getChannelNbr() {
        return channelNbr;
    }

    /**
     * Gets the component type
     *
     * @return the component type
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * Gets the component sub type
     *
     * @return the component sub type
     */
    public String getComponentSubType() {
        return componentSubType;
    }

    /**
     * Gets the action value
     *
     * @return the action value
     */
    @Nullable
    public String getActionValue() {
        return actionValue;
    }

    @Override
    public String toString() {
        return "PathInfo [thingUid=" + thingUid + ", itemName=" + itemName + ", subType=" + subType + ", channelNbr="
                + channelNbr + ", componentType=" + componentType + ", componentSubType=" + componentSubType
                + ", actionValue=" + actionValue + "]";
    }
}
