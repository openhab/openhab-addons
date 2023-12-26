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
package org.openhab.binding.airquality.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.api.Pollutant.SensitiveGroup;

/**
 * The {@link SensitiveGroupConfiguration} is the class used to match the
 * sensitive-group channel configuration.
 *
 * @author Gaël L"hopital - Initial contribution
 */
@NonNullByDefault
public class SensitiveGroupConfiguration {
    private String group = "RESPIRATORY";

    public @Nullable SensitiveGroup asSensitiveGroup() {
        try {
            return SensitiveGroup.valueOf(group);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
