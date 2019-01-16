/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api;

import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.TlsCertificateProvider;
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
