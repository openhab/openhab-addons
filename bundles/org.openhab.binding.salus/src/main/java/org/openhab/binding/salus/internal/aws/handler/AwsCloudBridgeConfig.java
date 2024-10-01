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
package org.openhab.binding.salus.internal.aws.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.handler.AbstractBridgeConfig;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class AwsCloudBridgeConfig extends AbstractBridgeConfig {
    private String userPoolId = "eu-central-1_XGRz3CgoY";
    private String identityPoolId = "60912c00-287d-413b-a2c9-ece3ccef9230";
    private String clientId = "4pk5efh3v84g5dav43imsv4fbj";
    private String region = "eu-central-1";
    private String companyCode = "salus-eu";
    private String awsService = "a24u3z7zzwrtdl-ats";

    public AwsCloudBridgeConfig() {
        setUrl("https://service-api.eu.premium.salusconnect.io");
    }

    public AwsCloudBridgeConfig(String username, String password, String url, long refreshInterval,
            long propertiesRefreshInterval, int maxHttpRetries, String userPoolId, String identityPoolId,
            String clientId, String region, String companyCode, String awsService) {
        super(username, password, url, refreshInterval, propertiesRefreshInterval, maxHttpRetries);
        this.userPoolId = userPoolId;
        this.identityPoolId = identityPoolId;
        this.clientId = clientId;
        this.region = region;
        this.companyCode = companyCode;
        this.awsService = awsService;
        if (url.isBlank()) {
            setUrl("https://service-api.eu.premium.salusconnect.io");
        }
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public void setUserPoolId(String userPoolId) {
        this.userPoolId = userPoolId;
    }

    public String getIdentityPoolId() {
        return identityPoolId;
    }

    public void setIdentityPoolId(String identityPoolId) {
        this.identityPoolId = identityPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getAwsService() {
        return awsService;
    }

    public void setAwsService(String awsService) {
        this.awsService = awsService;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && !userPoolId.isBlank() && !identityPoolId.isBlank() && !clientId.isBlank()
                && !region.isBlank() && !companyCode.isBlank() && !awsService.isBlank();
    }

    @Override
    public String toString() {
        return "AwsCloudBridgeConfig{" + //
                "userPoolId='" + userPoolId + '\'' + //
                "identityPoolId='" + identityPoolId + '\'' + //
                ", clientId='" + clientId + '\'' + //
                ", region='" + region + '\'' + //
                ", companyCode='" + companyCode + '\'' + //
                ", awsService='" + awsService + '\'' + //
                ", username='" + username + '\'' + //
                ", password='<SECRET>'" + //
                ", url='" + url + '\'' + //
                ", refreshInterval=" + refreshInterval + //
                ", propertiesRefreshInterval=" + propertiesRefreshInterval + //
                ", maxHttpRetries=" + maxHttpRetries + //
                '}';
    }
}
