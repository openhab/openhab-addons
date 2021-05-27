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
package org.openhab.binding.carnet.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link CarNetApiProperties} defines brand speficfic properties
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetApiProperties {
    public boolean weakSsl = true;
    public String oidcDate = ""; // Date in getOIDC http response header
    public String oidcConfigUrl = "";

    public String brand = "";
    public String apiDefaultUrl = "";
    public String xcountry = "";
    public String baseUrl = "";
    public String clientId = "";
    public String xClientId = "";
    public String authScope = "";
    public String redirect_uri = "";
    public String xrequest = "";
    public String responseType = "";
    public String xappId = "";
    public String xappName = "";
    public String xappVersion = "";
    public String clientName = "";
    public String clientPlatform = "";
}
