/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

import org.osgi.framework.Bundle;

/**
 * The {@link AuthorizeData} holds and handle data needed to
 * be sent to API in order to get authorization
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class AuthorizeData {
    protected String appId;
    protected String appName;
    protected String appVersion;
    protected String deviceName;

    public AuthorizeData(String appId, Bundle bundle) {
        this.appId = appId;
        this.appName = bundle.getHeaders().get("Bundle-Name");
        this.appVersion = String.format("%d.%d", bundle.getVersion().getMajor(), bundle.getVersion().getMinor());
        this.deviceName = bundle.getHeaders().get("Bundle-Vendor");
    }
}
