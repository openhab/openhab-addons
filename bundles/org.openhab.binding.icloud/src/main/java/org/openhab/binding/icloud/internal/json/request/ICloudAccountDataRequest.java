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
package org.openhab.binding.icloud.internal.json.request;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Serializable request for icloud device data.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
@NonNullByDefault
public class ICloudAccountDataRequest {
    @SuppressWarnings("unused")
    private ClientContext clientContext;

    private ICloudAccountDataRequest() {
        this.clientContext = ClientContext.defaultInstance();
    }

    public static ICloudAccountDataRequest defaultInstance() {
        return new ICloudAccountDataRequest();
    }

    public static class ClientContext {
        @SuppressWarnings("unused")
        private String appName = "FindMyiPhone";
        @SuppressWarnings("unused")
        private boolean fmly = true;
        @SuppressWarnings("unused")
        private String appVersion = "5.0";
        @SuppressWarnings("unused")
        private String buildVersion = "376";
        @SuppressWarnings("unused")
        private int clientTimestamp = 0;
        @SuppressWarnings("unused")
        private String deviceUDID = "";
        @SuppressWarnings("unused")
        private int inactiveTime = 1;
        @SuppressWarnings("unused")
        private String osVersion = "14.0";
        @SuppressWarnings("unused")
        private String productType = "iPhone14,2";

        private ClientContext() {
            // empty to hide constructor
        }

        public static ClientContext defaultInstance() {
            return new ClientContext();
        }
    }
}
