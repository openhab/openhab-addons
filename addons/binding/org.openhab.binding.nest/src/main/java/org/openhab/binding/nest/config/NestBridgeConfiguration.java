/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.config;

/**
 * This has the configuration for the nest bridge, allowing it to talk to nest.
 *
 * @author David Bennett - initial contribution
 */
public class NestBridgeConfiguration {
    /** Client id from the nest product page. */
    public String clientId;
    /** Client secret from the nest product page. */
    public String clientSecret;
    /** Client secret from the auth page. */
    public String pincode;
    /** The access token to use once retrieved from nest. */
    public String accessToken;
    /** How often to refresh data from nest. */
    public int refreshInterval;
}
