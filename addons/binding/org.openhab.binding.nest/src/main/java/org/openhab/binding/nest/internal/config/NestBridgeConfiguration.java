/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The configuration for the Nest bridge, allowing it to talk to Nest.
 *
 * @author David Bennett - Initial contribution
 */
@NonNullByDefault
public class NestBridgeConfiguration {
    public static final String PRODUCT_ID = "productId";
    /** Product ID from the Nest product page. */
    public String productId = "";

    public static final String PRODUCT_SECRET = "productSecret";
    /** Product secret from the Nest product page. */
    public String productSecret = "";

    public static final String PINCODE = "pincode";
    /** Product pincode from the Nest authorization page. */
    public @Nullable String pincode;

    public static final String ACCESS_TOKEN = "accessToken";
    /** The access token to use once retrieved from Nest. */
    public @Nullable String accessToken;
}
