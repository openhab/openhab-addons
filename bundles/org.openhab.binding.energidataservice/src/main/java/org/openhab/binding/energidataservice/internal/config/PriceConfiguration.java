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
package org.openhab.binding.energidataservice.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PriceConfiguration} class defines common configuration parameters for price
 * channels.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class PriceConfiguration {

    public static final String INCLUDE_VAT = "includeVAT";

    /**
     * Add VAT to amount based on regional settings.
     */
    public boolean includeVAT = false;
}
