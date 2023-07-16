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
package org.openhab.binding.connectedcar.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiBrandProperties;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNOperationList.CarNetOperationList;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNPairingInfo.CarNetPairingInfo;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetMbbStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetOidcConfig;

/**
 * {@link CombinedConfig} combines account and vehicle config on the api level
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
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
    public ApiBrandProperties api = new ApiBrandProperties();
    public CarNetOidcConfig oidcConfig = new CarNetOidcConfig();
    public @Nullable BrandAuthenticator authenticator;
    public AccountConfiguration account = new AccountConfiguration();
    public ThingConfiguration vehicle = new ThingConfiguration();
    public VehicleConfig vstatus = new VehicleConfig();
    public UserConfig user = new UserConfig();
    @Nullable
    public CombinedConfig previousConfig;

    public CombinedConfig() {
    }

    public CombinedConfig(CombinedConfig aconfig, ThingConfiguration vconfig) {
        this(vconfig);
        this.api = aconfig.api;
        this.account = aconfig.account;
        this.tokenSetId = aconfig.tokenSetId;
        this.authenticator = aconfig.authenticator;
        this.oidcConfig = new CarNetOidcConfig();
        this.user = new UserConfig();
    }

    public CombinedConfig(CombinedConfig aconfig) {
        this.api = aconfig.api;
        this.account = aconfig.account;
        this.tokenSetId = aconfig.tokenSetId;
        this.authenticator = aconfig.authenticator;
        this.oidcConfig = new CarNetOidcConfig();
        this.user = new UserConfig();
    }

    public CombinedConfig(ThingConfiguration vconfig) {
        this.vehicle = vconfig;
    }

    public String getLogId() {
        return !vehicle.vin.isEmpty() ? vehicle.vin : !api.brand.isEmpty() ? api.brand : account.brand;
    }
}
