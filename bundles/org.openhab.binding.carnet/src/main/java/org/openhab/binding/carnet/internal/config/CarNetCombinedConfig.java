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
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetOidcConfig;
import org.openhab.binding.carnet.internal.config.CarNetVehicleConfiguration.CarNetUserInfo;

/**
 * {@link CarNetCombinedConfig} combines account and vehicle config on the api level
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetCombinedConfig {
    public CarNetOidcConfig oidcConfig = new CarNetOidcConfig();
    public String oidcDate = ""; // Date in getOIDC http response header
    public String oidcConfigUrl = "";
    public String clientId = "";
    public String xClientId = "";
    public String authScope = "";
    public String redirect_uri = "";
    public String xrequest = "";
    public String responseType = "";
    public String xappName = "";
    public String xappVersion = "";

    public String tokenSetId = "";
    public CarNetAccountConfiguration account = new CarNetAccountConfiguration();
    public CarNetVehicleConfiguration vehicle = new CarNetVehicleConfiguration();
    public CarNetUserInfo user = new CarNetUserInfo();
}
