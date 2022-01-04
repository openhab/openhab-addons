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
package org.openhab.binding.unifi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifi.internal.handler.UniFiClientThingHandler;

/**
 * The {@link UniFiClientThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiClientThingHandler}.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiClientThingConfig {

    private String cid = "";

    private String site = "";

    private int considerHome = 180;

    public String getClientID() {
        return cid;
    }

    public String getSite() {
        return site;
    }

    public int getConsiderHome() {
        return considerHome;
    }

    public boolean isValid() {
        return !cid.isBlank();
    }

    @Override
    public String toString() {
        return String.format("UniFiClientConfig{cid: '%s', site: '%s', considerHome: %d}", cid, site, considerHome);
    }
}
