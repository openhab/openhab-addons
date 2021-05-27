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
package org.openhab.binding.carnet.internal.api.brand;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.CNAPI_BRAND_NULL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;

/**
 * {@link CarNetBrandApiNull} providesan empty implementation of the Brand interface
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetBrandApiNull extends CarNetApiBase {

    public CarNetBrandApiNull() {
    }

    @Override
    public CarNetApiProperties getProperties() {
        CarNetApiProperties properties = new CarNetApiProperties();
        properties.brand = CNAPI_BRAND_NULL;
        return properties;
    }
}
