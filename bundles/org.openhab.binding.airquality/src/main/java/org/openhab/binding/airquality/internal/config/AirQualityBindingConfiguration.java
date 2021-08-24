/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.binding.airquality.internal.AirQualityException;

/**
 * The {@link AirQualityBindingConfiguration} is responsible for holding configuration
 * informations needed to access Netatmo API and general binding behavior setup
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirQualityBindingConfiguration {
    public String apiKey = "";

    public void update(AirQualityBindingConfiguration newConfiguration) throws AirQualityException {
        newConfiguration.checkValid();
        this.apiKey = newConfiguration.apiKey;
    }

    public void checkValid() throws AirQualityException {
        if (apiKey.isEmpty()) {
            throw new AirQualityException("@text/emptyApiKey");
        }
    }
}
