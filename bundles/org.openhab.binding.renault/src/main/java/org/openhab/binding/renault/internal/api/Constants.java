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
                gigyaApiKey = "3__3ER_6lFvXEXHTP_faLtq6eEdbKDXd9F5GoKwzRyZq37ZQ-db7mXcLzR1Jtls5sn";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "cs_CZ":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_oRlKr5PCVL_sPWUZdJ8c5NOl5Ej8nIZw7VKG7S9Rg36UkDszFzfHfxCaUAUU5or2";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "da_DK":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_5x-2C8b1R4MJPQXkwTPdIqgBpcw653Dakw_ZaEneQRkTBdg9UW9Qg_5G-tMNrTMc";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "de_DE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_7PLksOyBRkHv126x5WhHb-5pqC1qFR8pQjxSeLB6nhAnPERTUlwnYoznHSxwX668";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "de_AT":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3__B4KghyeUb0GlpU62ZXKrjSfb7CPzwBS368wioftJUL5qXE0Z_sSy0rX69klXuHy";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "de_CH":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_UyiWZs_1UXYCUqK_1n7l7l44UiI_9N9hqwtREV0-UYA_5X7tOV-VKvnGxPBww4q2";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "en_GB":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_e8d4g4SE_Fo8ahyHwwP7ohLGZ79HKNN2T8NjQqoNnk6Epj6ilyYwKdHUyCw3wuxz";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "en_IE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_Xn7tuOnT9raLEXuwSI1_sFFZNEJhSD0lv3gxkwFtGI-RY4AgiePBiJ9EODh8d9yo";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "es_ES":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_DyMiOwEaxLcPdBTu63Gv3hlhvLaLbW3ufvjHLeuU8U5bx3zx19t5rEKq7KMwk9f1";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "es_MX":
                gigyaRootUrl = GIGYA_URL_US;
                gigyaApiKey = "3_BFzR-2wfhMhUs5OCy3R8U8IiQcHS-81vF8bteSe8eFrboMTjEWzbf4pY1aHQ7cW0";
                kamereonRootUrl = KAMEREON_URL_US;
                break;
            case "fi_FI":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_xSRCLDYhk1SwSeYQLI3DmA8t-etfAfu5un51fws125ANOBZHgh8Lcc4ReWSwaqNY";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_FR":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_4LKbCcMMcvjDm3X89LU4z4mNKYKdl_W0oD9w-Jvih21WqgJKtFZAnb9YdUgWT9_a";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_BE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_ZK9x38N8pzEvdiG7ojWHeOAAej43APkeJ5Av6VbTkeoOWR4sdkRc-wyF72HzUB8X";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_CH":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_h3LOcrKZ9mTXxMI9clb2R1VGAWPke6jMNqMw4yYLz4N7PGjYyD0hqRgIFAIHusSn";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "fr_LU":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_zt44Wl_wT9mnqn-BHrR19PvXj3wYRPQKLcPbGWawlatFR837KdxSZZStbBTDaqnb";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "hr_HR":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_HcDC5GGZ89NMP1jORLhYNNCcXt7M3thhZ85eGrcQaM2pRwrgrzcIRWEYi_36cFj9";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "hu_HU":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_nGDWrkSGZovhnVFv5hdIxyuuCuJGZfNmlRGp7-5kEn9yb0bfIfJqoDa2opHOd3Mu";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "it_IT":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_js8th3jdmCWV86fKR3SXQWvXGKbHoWFv8NAgRbH7FnIBsi_XvCpN_rtLcI07uNuq";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "it_CH":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_gHkmHaGACxSLKXqD_uDDx415zdTw7w8HXAFyvh0qIP0WxnHPMF2B9K_nREJVSkGq";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "nl_NL":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_ZIOtjqmP0zaHdEnPK7h1xPuBYgtcOyUxbsTY8Gw31Fzy7i7Ltjfm-hhPh23fpHT5";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "nl_BE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_yachztWczt6i1pIMhLIH9UA6DXK6vXXuCDmcsoA4PYR0g35RvLPDbp49YribFdpC";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "no_NO":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_QrPkEJr69l7rHkdCVls0owC80BB4CGz5xw_b0gBSNdn3pL04wzMBkcwtbeKdl1g9";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "pl_PL":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_2YBjydYRd1shr6bsZdrvA9z7owvSg3W5RHDYDp6AlatXw9hqx7nVoanRn8YGsBN8";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "pt_PT":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3__afxovspi2-Ip1E5kNsAgc4_35lpLAKCF6bq4_xXj2I2bFPjIWxAOAQJlIkreKTD";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "ro_RO":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_WlBp06vVHuHZhiDLIehF8gchqbfegDJADPQ2MtEsrc8dWVuESf2JCITRo5I2CIxs";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "ru_RU":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_N_ecy4iDyoRtX8v5xOxewwZLKXBjRgrEIv85XxI0KJk8AAdYhJIi17LWb086tGXR";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "sk_SK":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_e8d4g4SE_Fo8ahyHwwP7ohLGZ79HKNN2T8NjQqoNnk6Epj6ilyYwKdHUyCw3wuxz";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "sl_SI":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_QKt0ADYxIhgcje4F3fj9oVidHsx3JIIk-GThhdyMMQi8AJR0QoHdA62YArVjbZCt";
                kamereonRootUrl = KAMEREON_URL_EU;
                break;
            case "sv_SE":
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3_EN5Hcnwanu9_Dqot1v1Aky1YelT5QqG4TxveO0EgKFWZYu03WkeB9FKuKKIWUXIS";
                kamereonRootUrl = KAMEREON_URL_EU;
            default:
                gigyaRootUrl = GIGYA_URL_EU;
                gigyaApiKey = "3__B4KghyeUb0GlpU62ZXKrjSfb7CPzwBS368wioftJUL5qXE0Z_sSy0rX69klXuHy";
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
