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
package org.openhab.binding.squeezebox.internal.model;

/**
 * Attributes of a Squeezebox Server favorite
 *
 * @author Mark Hilbush - Initial contribution
 *
 */
public class Favorite {
    /**
     * Favorite id is of form xxxxxxxx.nn
     */
    public String id;

    /**
     * Just the nn part of the id
     */
    public String shortId;

    /**
     * The name given to the favorite in the Squeezebox Server.
     */
    public String name;

    /**
     * Creates a preset from the given favorite id
     *
     * @param id Squeezebox Server internal identifier for favorite
     */
    public Favorite(String id) {
        this.id = id;
        this.shortId = id;
        if (id.indexOf(".") != -1) {
            this.shortId = id.substring(id.indexOf(".") + 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Favorite {id=").append(id).append(", shortId=").append(shortId).append(", name=").append(name)
                .append("}");
        return sb.toString();
    }
}
