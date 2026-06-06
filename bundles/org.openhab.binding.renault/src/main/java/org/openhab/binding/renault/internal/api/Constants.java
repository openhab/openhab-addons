/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.renault.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for Renault API.
 * 
 * https://github.com/hacf-fr/renault-api/blob/main/src/renault_api/const.py
 * 
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class Constants {

    private static final String GIGYA_KEY_EU = "3_VgdkgtIRH3AdHvJm-cjV2ug2EFE0lxt0IJzMC4MFqZjFpn_GYFXVdNZ19L7wZX0N";
    private static final String GIGYA_URL_EU = "https://accounts.eu1.gigya.com";
    private static final String GIGYA_URL_US = "https://accounts.us1.gigya.com";
    private static final String KAMEREON_URL_EU = "https://api-wired-prod-1-euw1.wrd-aws.com";
    private static final String KAMEREON_URL_US = "https://api-wired-prod-1-usw2.wrd-aws.com";

    private String gigyaApiKey = "gigya-api-key";
    private String gigyaRootUrl = "gigya-root-url";
    private String kamereonRootUrl = "kamereon-root-url";

    public Constants(final String locale) {
        switch (locale) {
            case "bg_BG":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "cs_CZ":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "da_DK":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "de_DE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "de_AT":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "de_CH":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "en_GB":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "en_IE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "es_ES":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "es_MX":
                gigyaRootUrl = GIGYA_URL_US;
                gigyaApiKey = "4_yTFqPSsGxVyRXPZUM7t1Iw";
                kamereonRootUrl = KAMEREON_URL_US;
                break;
            case "fi_FI":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_FR":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_BE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_CH":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_LU":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_zt44Wl_wT9mnqn-BHrR19PvXj3wYRPQKLcPbGWawlatFR837KdxSZZStbBTDaqnb";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "hr_HR":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "hu_HU":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "it_IT":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "it_CH":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "nl_NL":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "nl_BE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "no_NO":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "pl_PL":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "pt_PT":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "ro_RO":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "ru_RU":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "sk_SK":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "sl_SI":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "sv_SE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
            default:
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = GIGYA_KEY_EU;
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
        }
    }

    public String getGigyaApiKey() {
        return gigyaApiKey;
    }

    public String getGigyaRootUrl() {
        return gigyaRootUrl;
    }

    public String getKamereonRootUrl() {
        return kamereonRootUrl;
    }
}
