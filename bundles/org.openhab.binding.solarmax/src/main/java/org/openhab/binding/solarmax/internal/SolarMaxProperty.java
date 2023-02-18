/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solarmax.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarmax.internal.connector.SolarMaxCommandKey;

/**
 * The {@link SolarMaxProperty} Enum defines common constants, which are
 * used across the whole binding.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public enum SolarMaxProperty {

    PROPERTY_SOFTWARE_VERSION(SolarMaxCommandKey.softwareVersion.name()),
    PROPERTY_BUILD_NUMBER(SolarMaxCommandKey.buildNumber.name());

    private final String propertyId;

    private SolarMaxProperty(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyId() {
        return propertyId;
    }
}
