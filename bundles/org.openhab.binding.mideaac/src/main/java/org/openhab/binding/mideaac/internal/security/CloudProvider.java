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
package org.openhab.binding.mideaac.internal.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CloudProvider} class contains the information
 * to allow encryption and decryption for the supported Cloud Providers
 *
 * @author Jacek Dobrowolski - Initial Contribution
 */
@NonNullByDefault
public class CloudProvider {

    private String name;
    private String appkey;
    private String appid;
    private String apiurl;
    private String signkey;
    private String proxied;

    private String iotkey;
    private String hmackey;

    public String getName() {
        return name;
    }

    public String getAppKey() {
        return appkey;
    }

    public String getLoginKey() {
        return appkey;
    }

    public String getAppId() {
        return appid;
    }

    public String getSrc() {
        return appid;
    }

    public String getApiUrl() {
        return apiurl;
    }

    public String getSignKey() {
        return signkey;
    }

    public String getProxied() {
        return proxied;
    }

    public String getIotKey() {
        return iotkey;
    }

    public String getHmacKey() {
        return hmackey;
    }

    public CloudProvider(String name, String appkey, String appid, String apiurl, String signkey, String iotkey,
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
     * All providers use the same signkey for AES encryption and Decryption.
     * V2 Devices do not require a Cloud Provider entry as they only use AES
     */
    public static CloudProvider getCloudProvider(String name) {
        switch (name) {
            case "NetHome Plus":
                return new CloudProvider("NetHome Plus", "3742e9e5842d4ad59c2db887e12449f9", "1017",
                        "https://mapp.appsmb.com", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");
            case "Midea Air":
                return new CloudProvider("Midea Air", "ff0cf6f5f0c3471de36341cab3f7a9af", "1117",
                        "https://mapp.appsmb.com", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");
            case "MSmartHome":
                return new CloudProvider("MSmartHome", "ac21b9f9cbfe4ca5a88562ef25e2b768", "1010",
                        "https://mp-prod.appsmb.com/mas/v5/app/proxy?alias=", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S",
                        "meicloud", "PROD_VnoClJI9aikS8dyy", "v5");
        }
        return new CloudProvider("", "", "", "", "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S", "", "", "");
    }
}
