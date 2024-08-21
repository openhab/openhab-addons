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
package org.openhab.binding.pulseaudio.internal.items;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract root class for all items in a pulseaudio server. Every item in a
 * pulseaudio server has a name and a unique id which can be inherited by this
 * class.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDeviceConfig {

    protected int id;
    protected String name;

    public AbstractDeviceConfig(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * returns the internal id of this device
     *
     * @return
     */
    public int getId() {
        return id;
    }

    public String getUIDName() {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }

    public String getPaName() {
        return name;
    }
}
