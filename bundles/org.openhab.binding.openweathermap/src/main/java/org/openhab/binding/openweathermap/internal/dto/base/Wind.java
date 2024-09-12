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
package org.openhab.binding.openweathermap.internal.dto.base;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Generated Plain Old Java Objects class for {@link Wind} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Wind {
    private Double speed;
    private Double deg;
    private @Nullable Double gust;

    public Double getSpeed() {
        return speed;
    }

    public Double getDeg() {
        return deg;
    }

    public @Nullable Double getGust() {
        return gust;
    }
}
