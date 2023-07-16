/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_NULL;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link BrandNull} providesan empty implementation of the Brand interface
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class BrandNull extends ApiBase {

    public BrandNull() {
    }

    @Override
    public ApiBrandProperties getProperties() {
        ApiBrandProperties properties = new ApiBrandProperties();
        properties.brand = API_BRAND_NULL;
        return properties;
    }
}
