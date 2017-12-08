/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.google;

/**
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class Geometry {
    private Bounds bounds;

    private Location location;

    private String location_type;

    private Viewport viewport;

    public Bounds getBounds() {
        return this.bounds;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getLocationType() {
        return this.location_type;
    }

    public Viewport getViewport() {
        return this.viewport;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocationType(String location_type) {
        this.location_type = location_type;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}
