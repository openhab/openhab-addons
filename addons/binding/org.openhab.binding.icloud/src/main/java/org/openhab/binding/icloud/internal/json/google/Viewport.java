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
 * @author Patrik Gfeller
 *
 */
public class Viewport {
    private Northeast2 northeast;

    public Northeast2 getNortheast() {
        return this.northeast;
    }

    public void setNortheast(Northeast2 northeast) {
        this.northeast = northeast;
    }

    private Southwest2 southwest;

    public Southwest2 getSouthwest() {
        return this.southwest;
    }

    public void setSouthwest(Southwest2 southwest) {
        this.southwest = southwest;
    }
}
