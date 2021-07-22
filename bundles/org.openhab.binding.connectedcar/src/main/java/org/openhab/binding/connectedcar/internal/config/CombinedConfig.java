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
package org.openhab.binding.connectedcar.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.brand.BrandApiProperties;
import org.openhab.binding.connectedcar.internal.api.brand.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNOperationList.CarNetOperationList;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNPairingInfo.CarNetPairingInfo;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetMbbStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetOidcConfig;

/**
 * {@link CombinedConfig} combines account and vehicle config on the api level
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CombinedConfig {

    public static class UserConfig {
        public String id = "";
        public String identity = "";
        public String oauthId = "";
        public String role = "";
        public String status = "";
        public String securityLevel = "";
        public String profileUrl = "";
    }

    public static class VehicleConfig {
        public @Nullable CarNetOperationList operationList;
        public String rolesRightsUrl = "";
        public String homeRegionUrl = "";
        public String apiUrlPrefix = "";
        public String[] imageUrls = new String[0];

        public CarNetMbbStatus mbb = new CarNetMbbStatus();
        public CarNetPairingInfo pairingInfo = new CarNetPairingInfo();
    }

    public String tokenSetId = "";
    public BrandApiProperties api = new BrandApiProperties();
    public CarNetOidcConfig oidcConfig = new CarNetOidcConfig();
    public @Nullable BrandAuthenticator authenticator;
    public AccountConfiguration account = new AccountConfiguration();
    public VehicleConfiguration vehicle = new VehicleConfiguration();
    public VehicleConfig vstatus = new VehicleConfig();
    public UserConfig user = new UserConfig();
}
