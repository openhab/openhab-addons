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
package org.openhab.binding.freeboxos.internal.api;

import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsCertificateProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a CertificateManager for the Freebox SSL certificate
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component
@NonNullByDefault
public class FreeboxTlsCertificateProvider implements TlsCertificateProvider {

    private static final String CERTIFICATE_NAME = "freeboxECCRootCA.crt";

    public static final String DEFAULT_NAME = "mafreebox.freebox.fr";

    @Override
    public String getHostName() {
        return DEFAULT_NAME;
    }

    @Override
    public URL getCertificate() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(CERTIFICATE_NAME);
        if (resource != null) {
            return resource;
        }
        throw new IllegalStateException("Certificate '%s' not found or not accessible".formatted(CERTIFICATE_NAME));
    }
}
