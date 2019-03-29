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
package org.openhab.binding.net.internal.handler;

import java.security.cert.CertificateException;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class SecureContextBuilder {

    private static SecureContextBuilder certificateHandler;

    private SslContextBuilder sslContextBuilder;

    private SecureContextBuilder() throws CertificateException {
        SelfSignedCertificate cert = new SelfSignedCertificate();
        sslContextBuilder = SslContextBuilder.forServer(cert.certificate(), cert.privateKey());

    }

    public static synchronized SecureContextBuilder getInstance() throws CertificateException {
        if (certificateHandler == null) {
            certificateHandler = new SecureContextBuilder();
        }
        return certificateHandler;
    }

    public SslContextBuilder getSslContextBuilder() {
        return sslContextBuilder;
    }

}
