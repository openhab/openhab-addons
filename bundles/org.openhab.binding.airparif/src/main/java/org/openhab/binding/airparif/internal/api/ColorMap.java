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
package org.openhab.binding.airparif.internal.api;

import java.util.HashMap;
import java.util.Objects;

import org.openhab.binding.airparif.internal.api.AirParifApi.Appreciation;

/**
 * Class association between air quality appreciation and its color
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class ColorMap extends HashMap<Appreciation, String> {
    private static final long serialVersionUID = -605462873565278453L;

    private static Appreciation fromApiName(String searched) {
        return Objects.requireNonNull(Appreciation.AS_SET.stream().filter(mt -> searched.equals(mt.apiName)).findFirst()
                .orElse(Appreciation.UNKNOWN));
    }

    public String put(String key, String value) {
        return super.put(fromApiName(key), value);
    }
}
