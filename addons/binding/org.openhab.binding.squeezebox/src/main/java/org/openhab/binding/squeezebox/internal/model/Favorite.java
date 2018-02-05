/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
