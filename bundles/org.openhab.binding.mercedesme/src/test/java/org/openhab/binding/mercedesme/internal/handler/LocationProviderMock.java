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
package org.openhab.binding.mercedesme.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;

/**
 * {@link LocationProviderMock} to mock possible locations
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LocationProviderMock implements LocationProvider {

    @Override
    public @Nullable PointType getLocation() {
        return new PointType("1.234,9.876");
    }
}
