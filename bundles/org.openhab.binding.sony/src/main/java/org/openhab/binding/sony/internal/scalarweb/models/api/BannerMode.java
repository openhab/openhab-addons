/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the banner mode and is for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class BannerMode {

    /** The current value of the banner mode */
    private @Nullable String currentValue;

    /**
     * Constructor used for deserialization only
     */
    public BannerMode() {
    }

    /**
     * Gets the current value of the banner mode
     *
     * @return the current value of the banner mode
     */
    public @Nullable String getCurrentValue() {
        return currentValue;
    }

    @Override
    public String toString() {
        return "BannerMode [currentValue=" + currentValue + "]";
    }
}
