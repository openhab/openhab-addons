/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.energenie.internal.ssl.SSLContextBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link RestClient} interface that is using Jetty HttpClient to perform the requests
 * and is loading a {@link Certificate} from the OSGi bundle
 *
 * @author Svilen Valkanov - Initial contribution
 *
 */
@Component(service = RestClient.class, immediate = true, configurationPid = "binding.energenie")
public class RestClientImpl implements RestClient {

    private final Logger logger = LoggerFactory.getLogger(RestClientImpl.class);

    private HttpClient httpClient;
    private BundleContext bundleContext;
    private String baseURL;
    private int timeout;

    @Activate
    public void activate(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;

        this.httpClient = new HttpClient(new SslContextFactory());

        loadCertificate(CERTIFICATE_DIR, CERTIFICATE_FILE_NAME);
        setBaseURL(ENERGENIE_REST_API_URL);
        setConnectionTimeout(DEFAULT_REQUEST_TIMEOUT);
        startHttpClient();
    }

    public void deactivate(BundleContext context) throws Exception {
        stopHttpClient();
    }

    @Override
    public synchronized ContentResponse sendRequest(String requestPath, HttpMethod httpMethod, Properties httpHeaders,
            InputStream content, String contentType) throws IOException {

        String url = getBaseURL() + "/" + requestPath;

        Request request = null;
        ContentResponse response = null;

        if (this.httpClient.isStarted()) {
            request = this.httpClient.newRequest(url).method(httpMethod).timeout(getConnectionTimeout(),
                    TimeUnit.MILLISECONDS);
        }

        if (request != null) {
            if (httpHeaders != null) {
                for (String httpHeaderKey : httpHeaders.stringPropertyNames()) {
                    request.header(httpHeaderKey, httpHeaders.getProperty(httpHeaderKey));
                }
            }

            // add content if a valid method is given ...
            if (content != null && (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT)) {
                request.content(new InputStreamContentProvider(content), contentType);
            }

            logger.debug("About to execute {}", request.getURI());

            try {
                response = request.send();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (TimeoutException | ExecutionException e) {
                logger.debug("Response cannot be processed. Something went wrong: {}, ", e.getMessage(), e);
                throw new IOException(e);
            }
        }

        return response;
    }

    private void loadCertificate(String relativePath, String file) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
        SSLContextBuilder sslContext = SSLContextBuilder.create(bundleContext);
        try {
            sslContext.withTrustedCertificate(relativePath, file);
            logger.info("Certificate successfully loaded from bundle");
        } catch (CertificateExpiredException e) {
            logger.error("Root certificate has expired {}", file);
        } finally {
            httpClient.getSslContextFactory().setSslContext(sslContext.build());
        }

    }

    private synchronized void startHttpClient() throws Exception {
        if (!httpClient.isStarted()) {
            httpClient.start();
        }
    }

    private synchronized void stopHttpClient() throws Exception {
        if (!httpClient.isStopped()) {
            httpClient.stop();
        }
    }

    @Override
    public String getBaseURL() {
        return baseURL;
    }

    @Override
    public void setBaseURL(String baseUrl) {
        this.baseURL = baseUrl;
    }

    @Override
    public int getConnectionTimeout() {
        return timeout;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
