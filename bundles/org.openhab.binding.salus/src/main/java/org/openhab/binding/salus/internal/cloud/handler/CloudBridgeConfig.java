/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal.cloud.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.handler.AbstractBridgeConfig;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class CloudBridgeConfig extends AbstractBridgeConfig {

    public CloudBridgeConfig() {
    }

    public CloudBridgeConfig(String username, String password, String url, long refreshInterval,
            long propertiesRefreshInterval, int maxHttpRetries) {
        super(username, password, url, refreshInterval, propertiesRefreshInterval, maxHttpRetries);
    }

    @Override
    public String toString() {
        return "CloudBridgeConfig{" + "username='" + username + '\'' + ", password='<SECRET>'" + ", url='" + url + '\''
                + ", refreshInterval=" + refreshInterval + ", propertiesRefreshInterval=" + propertiesRefreshInterval
                + '}';
    }
}
