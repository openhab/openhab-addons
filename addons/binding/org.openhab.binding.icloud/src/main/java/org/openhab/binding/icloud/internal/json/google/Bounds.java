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
public class Bounds {
    private Northeast northeast;

    private Southwest southwest;

    public Northeast getNortheast() {
        return this.northeast;
    }

    public Southwest getSouthwest() {
        return this.southwest;
    }

    public void setNortheast(Northeast northeast) {
        this.northeast = northeast;
    }

    public void setSouthwest(Southwest southwest) {
        this.southwest = southwest;
    }
}
