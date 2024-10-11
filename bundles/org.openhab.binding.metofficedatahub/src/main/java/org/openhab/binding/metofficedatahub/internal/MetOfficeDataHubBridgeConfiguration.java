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
package org.openhab.binding.metofficedatahub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MetOfficeDataHubBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDataHubBridgeConfiguration {

    /**
     * Site Specific API Subscription - API Key
     */
    public String siteApiKey = "";

    /**
     * Rate limit of API call's in 24 hour period starting from 0000 (Free is capped at 360 - this allows 110 due to
     * reboots)
     */
    public int siteRateDailyLimit = 250;
}
