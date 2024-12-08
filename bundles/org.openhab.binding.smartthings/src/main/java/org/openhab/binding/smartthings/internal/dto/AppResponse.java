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
 * Data object for smartthings app creation response
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class AppResponse {

    public class App {
        public String appName;
        public String appId;
        public String appType;
        public String principalType;

        public String[] classifications;

        public String displayName;
        public String description;

        public Boolean singleInstance;

        public record webhookSmartApp(String targetUrl, String targetStatus, String signatureType) {
        }

        public webhookSmartApp webhookSmartApp;
    }

    public App app;
    public String oauthClientId;
    public String oauthClientSecret;
}
