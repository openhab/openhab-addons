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

package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings app description
 *
 * @author Laurent ARNAL - Initial contribution
 */

public class SmartthingsApp {
    public String appName;
    public String displayName;
    public String description;
    public String appType;
    public String appId;
    public String createdDate;
    public String lastUpdatedDate;

    public String principalType;

    public String[] classifications;

    public Boolean singleInstance;

    public record webhookSmartApp(String targetUrl, String targetStatus, String publicKey, String signatureType) {
    }

    public webhookSmartApp webhookSmartApp;

    public record ui(String publicId, String pluginUri, Boolean dashboardCardsEnabled,
            Boolean preInstallDashboardCardsEnabled) {
    }

    public ui ui;

    public record owner(String ownerType, String ownerId) {
    }

    public owner owner;
}
