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
package org.openhab.binding.ecovacs.internal.api.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public final class EcovacsApiUrlFactory {

    private EcovacsApiUrlFactory() {
        // Prevent instantiation
    }

    private static final String MAIN_URL_LOGIN_PATH = "/user/login";

    private static final String PORTAL_USERS_PATH = "/users/user.do";
    private static final String PORTAL_IOT_PRODUCT_PATH = "/pim/product/getProductIotMap";
    private static final String PORTAL_IOT_DEVMANAGER_PATH = "/iot/devmanager.do";
    private static final String PORTAL_LOG_PATH = "/lg/log.do";

    public static String getLoginUrl(EcovacsApiConfiguration config) {
        return getMainUrl(config) + MAIN_URL_LOGIN_PATH;
    }

    public static String getAuthUrl(EcovacsApiConfiguration config) {
        return String.format("https://gl-%1$s-openapi.ecovacs.%2$s/v1/global/auth/getAuthCode", config.getCountry(),
                getApiUrlTld(config));
    }

    public static String getPortalUsersUrl(EcovacsApiConfiguration config) {
        return getPortalUrl(config) + PORTAL_USERS_PATH;
    }

    public static String getPortalProductIotMapUrl(EcovacsApiConfiguration config) {
        return getPortalUrl(config) + PORTAL_IOT_PRODUCT_PATH;
    }

    public static String getPortalIotDeviceManagerUrl(EcovacsApiConfiguration config) {
        return getPortalUrl(config) + PORTAL_IOT_DEVMANAGER_PATH;
    }

    public static String getPortalLogUrl(EcovacsApiConfiguration config) {
        return getPortalUrl(config) + PORTAL_LOG_PATH;
    }

    private static String getPortalUrl(EcovacsApiConfiguration config) {
        String continentSuffix = "cn".equalsIgnoreCase(config.getCountry()) ? "" : "-" + config.getContinent();
        return String.format("https://portal%1$s.ecouser.net/api", continentSuffix);
    }

    private static String getMainUrl(EcovacsApiConfiguration config) {
        return String.format("https://gl-%1$s-api.ecovacs.%2$s/v1/private/%1$s/%3$s/%4$s/%5$s/%6$s/%7$s/%8$s",
                config.getCountry(), getApiUrlTld(config), config.getLanguage(), config.getDeviceId(),
                config.getAppCode(), config.getAppVersion(), config.getChannel(), config.getDeviceType());
    }

    private static String getApiUrlTld(EcovacsApiConfiguration config) {
        return "cn".equalsIgnoreCase(config.getCountry()) ? "cn" : "com";
    }
}
