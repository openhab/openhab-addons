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
package org.openhab.binding.ihc.internal.ws.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcFatalExecption;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcTlsExecption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom HTTP connection pool, which install all-trusting trust manager.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcConnectionPool {

    private final Logger logger = LoggerFactory.getLogger(IhcConnectionPool.class);
    private static final String DEFAULT_TLS_VER = "TLSv1";

    /**
     * Controller TLS certificate is self signed, which means that certificate
     * need to be manually added to java key store as a trusted certificate.
     * This is special SSL context which will be configured to trust all
     * certificates and manual work is not required.
     */
    private SSLContext sslContext;

    /** Holds and share cookie information (session id) from authentication procedure */
    private CookieStore cookieStore;

    private HttpClientBuilder httpClientBuilder;
    private HttpClientContext localContext;
    private String tlsVersion = DEFAULT_TLS_VER;

    public IhcConnectionPool() throws IhcFatalExecption {
        this(DEFAULT_TLS_VER);
    }

    public IhcConnectionPool(String tlsVersion) throws IhcFatalExecption {
        if (!tlsVersion.isEmpty()) {
            this.tlsVersion = tlsVersion;
        }
        init();
    }

    private void init() throws IhcFatalExecption {
        try {

            // Create a local instance of cookie store
            cookieStore = new BasicCookieStore();

            // Create local HTTP context
            localContext = HttpClientContext.create();

            // Bind custom cookie store to the local context
            localContext.setCookieStore(cookieStore);

            httpClientBuilder = HttpClientBuilder.create();

            // Setup a Trust Strategy that allows all certificates.

            logger.debug("Initialize SSL context");

            // Create a trust manager that does not validate certificate chains, but accept all.
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    logger.trace("Trusting server cert: {}", certs[0].getIssuerDN());
                }
            } };

            // Install the all-trusting trust manager

            // Old controller FW supports only SSLv3 and TLSv1, never controller TLSv1.2
            sslContext = SSLContext.getInstance(tlsVersion);
            logger.debug("Using TLS version {}", sslContext.getProtocol());
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Controller accepts only HTTPS connections and because normally IP address are used on home network rather
            // than DNS names, create custom host name verifier.
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    logger.trace("HostnameVerifier: arg0 = {}, arg1 = {}", arg0, arg1);
                    return true;
                }
            };

            // Create an SSL Socket Factory, to use our weakened "trust strategy"
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                    new String[] { tlsVersion }, null, hostnameVerifier);

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                    .register("https", sslSocketFactory).build();

            // Create connection-manager using our Registry. Allows multi-threaded use
            PoolingHttpClientConnectionManager connMngr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            // Increase max connection counts
            connMngr.setMaxTotal(20);
            connMngr.setDefaultMaxPerRoute(6);

            httpClientBuilder.setConnectionManager(connMngr);
        } catch (KeyManagementException e) {
            throw new IhcFatalExecption(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IhcTlsExecption(e);
        }
    }

    public HttpClient getHttpClient() {
        return httpClientBuilder.build();
    }

    public HttpClientContext getHttpContext() {
        return localContext;
    }
}
