/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.OkHttpClient;

/**
 * {@link TrustingOkHttpClient} is a OkHttpClient subclass
 * that does clears positively every certificate request
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class TrustingOkHttpClient extends OkHttpClient {
    private final Logger logger = LoggerFactory.getLogger(TrustingOkHttpClient.class);

    private final TrustManager[] certs = new TrustManager[] { new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        }
    } };

    public TrustingOkHttpClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, certs, new SecureRandom());

            this.setHostnameVerifier((hostname, session) -> true);
            this.setSslSocketFactory(ctx.getSocketFactory());
        } catch (GeneralSecurityException ex) {
            logger.trace("Ignoring security exception: ", ex);
        }
    }
}
