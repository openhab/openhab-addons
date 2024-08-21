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
package org.openhab.binding.freebox.internal.api;

import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsCertificateProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a TrustManager for the Freebox SSL certificate
 *
 * @author Laurent Garnier - Initial Contribution
 */
@Component
@NonNullByDefault
public class FreeboxTlsCertificateProvider implements TlsCertificateProvider {

    @Override
    public String getHostName() {
        return "mafreebox.freebox.fr";
    }

    @Override
    public URL getCertificate() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("freeboxECCRootCA.crt");
        if (resource != null) {
            return resource;
        } else {
            throw new IllegalStateException("Certifcate resource not found or not accessible");
        }
    }
}
