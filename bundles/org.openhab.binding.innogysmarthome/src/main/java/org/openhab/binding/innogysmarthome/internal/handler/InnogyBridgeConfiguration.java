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
package org.openhab.binding.innogysmarthome.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants;

/**
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class InnogyBridgeConfiguration {

    public String brand = InnogyBindingConstants.DEFAULT_BRAND;
    public String authcode = "";
    public int websocketidletimeout = 900;

    public String clientId = InnogyBindingConstants.CLIENT_ID_INNOGY_SMARTHOME;
    public String clientSecret = InnogyBindingConstants.CLIENT_SECRET_INNOGY_SMARTHOME;
    public String redirectUrl = InnogyBindingConstants.REDIRECT_URL_INNOGY_SMARTHOME;
}
