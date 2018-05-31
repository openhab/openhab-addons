/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.request;

/**
 * Serializable request for icloud device data.
 *
 * @author Patrik Gfeller
 *
 */
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
        private String appName = "iCloud Find (Web)";
        @SuppressWarnings("unused")
        private boolean fmly = true;
        @SuppressWarnings("unused")
        private String appVersion = "2.0";
        @SuppressWarnings("unused")
        private String timezone = "US/Eastern";
        @SuppressWarnings("unused")
        private int inactiveTime = 2255;
        @SuppressWarnings("unused")
        private String apiVersion = "3.0";
        @SuppressWarnings("unused")
        private String webStats = "0:15";

        private ClientContext() {
            // empty to hide constructor
        }

        public static ClientContext defaultInstance() {
            return new ClientContext();
        }
    }
}
