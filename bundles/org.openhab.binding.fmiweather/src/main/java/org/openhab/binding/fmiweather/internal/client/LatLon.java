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
package org.openhab.binding.fmiweather.internal.client;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * QueryParameter implementation for latlon
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class LatLon implements QueryParameter {

    private final String latlon;

    public LatLon(BigDecimal latitude, BigDecimal longitude) {
        this.latlon = String.format("%s,%s", latitude.toPlainString(), longitude.toPlainString());
    }

    @Override
    public List<Map.Entry<String, String>> toRequestParameters() {
        return List.of(new AbstractMap.SimpleImmutableEntry<>("latlon", latlon));
    }
}
