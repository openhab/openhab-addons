/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hpprinter.internal.api;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;
import org.eclipse.smarthome.io.net.http.TrustAllTrustMananger;

/**
 * The {@link HPEWSTrustManager} is responsible for ignoring invalid HTTPS certificates of the Embedded Web Interface
 *
 * @author Stewart Cossey - Initial contribution
 */
public class HPEWSTrustManager implements TlsTrustManagerProvider {

    private String hostName;
    public HPEWSTrustManager(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String getHostName() {
        return this.hostName;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
		return TrustAllTrustMananger.getInstance();
	}

}