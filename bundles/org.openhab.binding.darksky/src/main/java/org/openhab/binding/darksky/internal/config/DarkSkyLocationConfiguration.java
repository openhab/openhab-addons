/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.darksky.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.darksky.internal.handler.AbstractDarkSkyHandler;

/**
 * The {@link DarkSkyLocationConfiguration} is the class used to match the {@link AbstractDarkSkyHandler}s
 * configuration.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DarkSkyLocationConfiguration {

    private @NonNullByDefault({}) String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
