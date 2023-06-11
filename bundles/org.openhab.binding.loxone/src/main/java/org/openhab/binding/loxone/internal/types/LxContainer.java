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
package org.openhab.binding.loxone.internal.types;

import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.loxone.internal.controls.LxControl;

/**
 * Container on Loxone Miniserver that groups {@link LxControl} objects.
 * <p>
 * Examples of containers are rooms and categories.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxContainer {
    private LxUuid uuid; // set by JSON deserialization
    private String name; // set by JSON deserialization
    private final Set<LxControl> controls;

    /**
     * Create a new container with given uuid and name
     */
    LxContainer() {
        controls = new HashSet<>();
    }

    /**
     * Obtain container's current name
     *
     * @return
     *         container's current name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtain container's UUID (assigned by Loxone Miniserver)
     *
     * @return
     *         container's UUID
     */
    public LxUuid getUuid() {
        return uuid;
    }

    /**
     * Add a new control to this container
     *
     * @param control control to be added
     */
    public void addControl(LxControl control) {
        controls.add(control);
    }

    /**
     * Removes a control from the container
     *
     * @param control control object to remove from the container
     * @return true if control object existed in the container and was removed
     */
    public boolean removeControl(LxControl control) {
        return controls.remove(control);
    }
}
