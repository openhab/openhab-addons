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
package org.openhab.binding.carnet.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNPairingInfo.CarNetPairingInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetOidcConfig;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;
import org.openhab.binding.carnet.internal.api.CarNetBrandAuthenticator;

/**
 * {@link CarNetCombinedConfig} combines account and vehicle config on the api level
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetCombinedConfig {

    public static class CarNetUserInfo {
        public String id = "";
        public String oauthId = "";
        public String role = "";
        public String status = "";
        public String securityLevel = "";
        public String profileUrl = "";
    }

    public String tokenSetId = "";
    public CarNetApiProperties api = new CarNetApiProperties();
    public CarNetOidcConfig oidcConfig = new CarNetOidcConfig();
    public CarNetAccountConfiguration account = new CarNetAccountConfiguration();
    public CarNetVehicleConfiguration vehicle = new CarNetVehicleConfiguration();
    public CarNetUserInfo user = new CarNetUserInfo();
    public CarNetPairingInfo pairingInfo = new CarNetPairingInfo();
    public @Nullable CarNetBrandAuthenticator authenticator;
}
