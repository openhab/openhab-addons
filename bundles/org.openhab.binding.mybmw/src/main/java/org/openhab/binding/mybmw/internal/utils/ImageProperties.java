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
package org.openhab.binding.mybmw.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ImageProperties} Properties of current Vehicle Image
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - fix default viewport as "default" is not available anymore
 */
@NonNullByDefault
public class ImageProperties {
    public static final int RETRY_COUNTER = 5;
    public int failCounter = 0;
    public String viewport = "VehicleStatus"; // default view

    public ImageProperties(String viewport) {
        this.viewport = viewport;
    }

    public ImageProperties() {
    }

    public void failed() {
        failCounter++;
    }

    public boolean failLimitReached() {
        return failCounter > RETRY_COUNTER;
    }

    @Override
    public String toString() {
        return viewport;
    }
}
