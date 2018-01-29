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
 * @author Mark Hilbush
 *
 */
public class Favorite {
    // id is of form xxxxxxxx.nn
    public String id;

    // just the nn part of the id
    public String shortId;

    // name of the favorite
    public String name;

    // type of favorite (currently unused)
    public String type;

    // indicates if favorite is audio (currently unused)
    public Boolean isaudio;

    // indicates if favorite has sub-items
    public Boolean hasitems;

    // URL of the favorite (currently unused)
    public String url;

    public Favorite(String id) {
        this.id = id;
        this.shortId = id;
        if (id.indexOf(".") != -1) {
            this.shortId = id.substring(id.indexOf(".") + 1);
        }
    }

    @Override
    public String toString() {
        return "Favorite {id=" + id + ", shortId=" + shortId + ", name=" + name + ", type=" + type + ", isaudio="
                + isaudio + ", hasitems=" + hasitems + ", url=" + url + "}";
    }
}
