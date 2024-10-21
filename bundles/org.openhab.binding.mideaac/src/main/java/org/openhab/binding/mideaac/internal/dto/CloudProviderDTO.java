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
package org.openhab.binding.mideaac.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CloudProvider} class contains the information
 * to allow encryption and decryption for the supported Cloud Providers
 *
 * @author Jacek Dobrowolski - Initial Contribution
 * @author Bob Eckhoff - JavaDoc
 */
@NonNullByDefault
public class CloudProviderDTO {

    private String name;
    private String appkey;
    private String appid;
    private String apiurl;
    private String signkey;
    private String proxied;

    private String iotkey;
    private String hmackey;

    /**
     * Cloud provider Name
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Cloud provider application key
     * 
     * @return appkey
     */
    public String getLoginKey() {
        return appkey;
    }

    /**
     * Cloud provider application ID
     * 
     * @return appid
     */
    public String getAppId() {
        return appid;
    }

    /**
     * Cloud provider url
     * 
     * @return apiurl
     */
    public String getApiUrl() {
        return apiurl;
    }

    /**
     * Cloud Provider Sign Key
     * 
     * @return signkey
     */
    public String getSignKey() {
        return signkey;
    }

    /**
     * Cloud provider proxy - MSmarthome only
     * 
     * @return proxied
     */
    public String getProxied() {
        return proxied;
    }

    /**
     * Cloud provider iot key - MSmarthome only
     * 
     * @return iotkey
     */
    public String getIotKey() {
        return iotkey;
    }

    /**
     * Cloud provider hmackey - MSmarthome only
     * 
     * @return hmackey
     */
    public String getHmacKey() {
        return hmackey;
    }

    /**
     * Cloud provider data
     * 
     * @param name Cloud provider
     * @param appkey application key
     * @param appid application id
     * @param apiurl application url
     * @param signkey sign key for AES
     * @param iotkey iot key - MSmarthome only
     * @param hmackey hmac key - MSmarthome only
     * @param proxied proxy - MSmarthome only
     */
    public CloudProviderDTO(String name, String appkey, String appid, String apiurl, String signkey, String iotkey,
            String hmackey, String proxied) {
        super();
        this.name = name;
        this.appkey = appkey;
        this.appid = appid;
        this.apiurl = apiurl;
        this.signkey = signkey;
        this.iotkey = iotkey;
        this.hmackey = hmackey;
        this.proxied = proxied;
    }

    /**
     * Cloud provider information
     * All providers use the same signkey for AES encryption and Decryption.
     * V2 Devices do not require a Cloud Provider entry as they only use AES
     * 
     * @param name Cloud provider
     * @return Cloud provider information
     */
    public static CloudProviderDTO getCloudProvider(String name) {
        switch (name) {
            case "NetHome Plus":
                return new CloudProviderDTO("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "1017",
                        "https://mapp.appsmb.com", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");
            case "Midea Air":
                return new CloudProviderDTO("Midea Air", "ff0cf6f5f0c3471de36341cab3f7a9af", "1117",
                        "https://mapp.appsmb.com", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");
            case "MSmartHome":
                return new CloudProviderDTO("MSmartHome", "ac21b9f9cbfe4ca5a88562ef25e2b768", "1010",
                        "https://mp-prod.appsmb.com/mas/v5/app/proxy?alias=", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S",
                        "meicloud", "PROD_VnoClJI9aikS8dyy", "v5");
        }
        return new CloudProviderDTO("", "", "", "", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");
    }
}
