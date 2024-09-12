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
package org.openhab.binding.airquality.internal.api.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AirQualityCity} is responsible for storing
 * the "city" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class AirQualityCity {
    private String name = "";
    private @Nullable String url;
    private List<Double> geo = List.of();

    public String getName() {
        return name;
    }

    public @Nullable String getUrl() {
        return url;
    }

    public String getGeo() {
        return geo.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
