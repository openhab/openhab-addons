/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.servletservices.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.openhab.io.neeo.internal.models.NeeoThingUID;

/**
 * The route path information class. This class will evaluate the route path into it's logical components.
 *
 * @author Tim Roberts - Initial contribution
 */
public class PathInfo {

    /** The thing uid */
    private final NeeoThingUID thingUid;

    /** The item name */
    private final String itemName;

    /** The channel number */
    private final int channelNbr;

    /** The component type */
    private final String componentType;

    /** The component sub type */
    private final String componentSubType;

    /** The action value */
    private final String actionValue;

    /**
     * Creates the path info object from the route path
     *
     * @param paths the non-null, non-empty route paths
     */
    public PathInfo(String[] paths) throws IllegalArgumentException {
        Objects.requireNonNull(paths, "paths cannot be null");

        // Note - the following check ensures that the path contains exactly what we check
        // and will fail if not (in the case of when a firmware update changes what we expect)
        if (paths.length < 7 || paths.length > 8) {
            throw new IllegalArgumentException(
                    "Path length invalid (must be between 7 and 8): " + String.join(",", paths));
        }

        // new ThingUID can throw illegal argument exception as well
        thingUid = new NeeoThingUID(paths[1]);

        itemName = paths[2];
        try {
            channelNbr = Integer.parseInt(paths[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channelNbr is not a valid integer: " + paths[3]);
        }

        // button/textlabel/slider/etc
        componentType = paths[4];

        // actor/sensor/buttoncmd
        if (StringUtils.equalsIgnoreCase("button", componentType)) {
            componentSubType = "actor";
            actionValue = paths[5];
        } else {
            componentSubType = paths[5];
            actionValue = paths.length == 8 ? paths[7] : null;
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
    public String getActionValue() {
        return actionValue;
    }

    @Override
    public String toString() {
        return "PathInfo [thingUid=" + thingUid + ", itemName=" + itemName + ", channelNbr=" + channelNbr
                + ", componentType=" + componentType + ", componentSubType=" + componentSubType + ", actionValue="
                + actionValue + "]";
    }

}
