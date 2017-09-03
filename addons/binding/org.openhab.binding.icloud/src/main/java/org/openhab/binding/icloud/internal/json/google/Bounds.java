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
public class Bounds {
    private Northeast northeast;

    public Northeast getNortheast() {
        return this.northeast;
    }

    public void setNortheast(Northeast northeast) {
        this.northeast = northeast;
    }

    private Southwest southwest;

    public Southwest getSouthwest() {
        return this.southwest;
    }

    public void setSouthwest(Southwest southwest) {
        this.southwest = southwest;
    }
}
