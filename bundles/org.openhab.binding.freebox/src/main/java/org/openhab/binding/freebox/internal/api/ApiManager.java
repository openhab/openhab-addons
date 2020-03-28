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
package org.openhab.binding.freebox.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.freebox.internal.api.model.AuthorizationStatus;
import org.openhab.binding.freebox.internal.api.model.AuthorizationStatus.Status;
import org.openhab.binding.freebox.internal.api.model.AuthorizationStatusResponse;
import org.openhab.binding.freebox.internal.api.model.AuthorizeRequest;
import org.openhab.binding.freebox.internal.api.model.AuthorizeResponse;
import org.openhab.binding.freebox.internal.api.model.AuthorizeResult;
import org.openhab.binding.freebox.internal.api.model.DiscoveryResponse;
import org.openhab.binding.freebox.internal.api.model.EmptyResponse;
import org.openhab.binding.freebox.internal.api.model.FreeboxResponse;
import org.openhab.binding.freebox.internal.api.model.LoginResponse;
import org.openhab.binding.freebox.internal.api.model.LogoutResponse;
import org.openhab.binding.freebox.internal.api.model.OpenSessionRequest;
import org.openhab.binding.freebox.internal.api.model.OpenSessionResult;
import org.openhab.binding.freebox.internal.config.ServerConfiguration;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link FreeboxApiManager} is responsible for the communication with the Freebox.
 * It implements the different HTTP API calls provided by the Freebox
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class ApiManager {
    private final Logger logger = LoggerFactory.getLogger(ApiManager.class);
    private static final String APP_ID = FrameworkUtil.getBundle(ApiManager.class).getSymbolicName();

    private static final int HTTP_CALL_DEFAULT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(10);

    private static final String HTTP_CALL_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String AUTH_HEADER = "X-Fbx-App-Auth";

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private @Nullable String baseAddress;
    private @NonNullByDefault({}) String appToken;
    private @Nullable String sessionToken;

    public ApiManager(ServerConfiguration configuration/* String appName, String appVersion, String deviceName */)
            throws FreeboxException {

        logger.debug("Authorize job...");
        String hostAddress = String.format("%s:%d", configuration.hostAddress, configuration.remoteHttpsPort);
        boolean httpsRequestOk = false;
        DiscoveryResponse discovery = null;
        if (configuration.httpsAvailable) {
            discovery = checkApi(hostAddress, true);
            httpsRequestOk = (discovery != null);
        }
        if (!httpsRequestOk) {
            discovery = checkApi(hostAddress, false);
        }
        boolean useHttps = false;
        if (discovery == null) {
            throw new FreeboxException("Can't connect to " + hostAddress);
        } else if (discovery.getApiBaseUrl().isEmpty()) {
            throw new FreeboxException(hostAddress + " does not deliver any API base URL");
        } else if (discovery.getApiVersion().isEmpty()) {
            throw new FreeboxException(" does not deliver any API version");
        } else if (discovery.isHttpsAvailable()) {
            if (discovery.getHttpsPort() == -1 || discovery.getApiDomain().isEmpty()) {
                if (httpsRequestOk) {
                    useHttps = true;
                } else {
                    logger.debug("{} does not deliver API domain or HTTPS port; use HTTP API", hostAddress);
                }
            } else if (checkApi(String.format("%s:%d", discovery.getApiDomain(), discovery.getHttpsPort()),
                    true) != null) {
                useHttps = true;
                hostAddress = String.format("%s:%d", discovery.getApiDomain(), discovery.getHttpsPort());
            }
        }

        if (!authorize2(useHttps, hostAddress, discovery.getApiBaseUrl(), discovery.getApiVersion(),
                configuration.appToken)) {
            if (configuration.appToken.isEmpty()) {
                throw new FreeboxException("App token not set in the thing configuration");
            } else {
                throw new FreeboxException("Check your app token in the thing configuration; opening session with "
                        + hostAddress + " using " + (useHttps ? "HTTPS" : "HTTP") + " API version "
                        + discovery.getApiVersion() + " failed");
            }
        } else {
            logger.debug("Freebox bridge : session opened with {} using {} API version {}", hostAddress,
                    (useHttps ? "HTTPS" : "HTTP"), discovery.getApiVersion());
        }
    }

    public boolean authorize2(boolean useHttps, String fqdn, String apiBaseUrl, String apiVersion, String appToken) {
        String[] versionSplit = apiVersion.split("\\.");
        String majorVersion = "5";
        if (versionSplit.length > 0) {
            majorVersion = versionSplit[0];
        }
        this.baseAddress = (useHttps ? "https://" : "http://") + fqdn + apiBaseUrl + "v" + majorVersion + "/";

        boolean granted = false;
        try {
            String token = appToken;
            if (token.isEmpty()) {
                AuthorizeRequest request = new AuthorizeRequest(APP_ID, FrameworkUtil.getBundle(getClass()));
                AuthorizeResult response = executePost(AuthorizeResponse.class, null, request);
                token = response.getAppToken();

                logger.info("####################################################################");
                logger.info("# Please accept activation request directly on your freebox        #");
                logger.info("# Once done, record Apptoken in the Freebox thing configuration    #");
                logger.info("# {} #", token);
                logger.info("####################################################################");

                AuthorizationStatus result;
                do {
                    Thread.sleep(2000);
                    result = executeGet(AuthorizationStatusResponse.class, response.getTrackId().toString());
                } while (result.getStatus() == Status.PENDING);
                granted = result.getStatus() == Status.GRANTED;
            } else {
                granted = true;
            }
            if (!granted) {
                return false;
            }

            this.appToken = token;
            openSession();
            return true;
        } catch (FreeboxException | InterruptedException e) {
            logger.debug("Error while opening a session", e);
            return false;
        }
    }

    public @Nullable DiscoveryResponse checkApi(String fqdn, boolean secureHttp) {
        String url = String.format("%s://%s/api_version", secureHttp ? "https" : "http", fqdn);
        try {
            String jsonResponse = HttpUtil.executeUrl("GET", url, HTTP_CALL_DEFAULT_TIMEOUT_MS);
            return gson.fromJson(jsonResponse, DiscoveryResponse.class);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("checkApi with {} failed: {}", url, e.getMessage());
            return null;
        }
    }

    private synchronized void openSession() throws FreeboxException {
        String challenge = executeGet(LoginResponse.class, null).getChallenge();
        OpenSessionResult loginResult = execute(new OpenSessionRequest(APP_ID, appToken, challenge), null);
        sessionToken = loginResult.getSessionToken();
    }

    public synchronized void closeSession() {
        if (sessionToken != null) {
            try {
                executePost(LogoutResponse.class, null, null);
            } catch (FreeboxException e) {
                logger.warn("Error closing session : {}", e.getMessage());
            }
            sessionToken = null;
        }
    }

    public synchronized @Nullable String getSessionToken() {
        return sessionToken;
    }

    private <T extends FreeboxResponse<F>, F> F executeUrl(String httpMethod, @Nullable String relativeUrl,
            @Nullable String requestContent, Class<T> responseClass, boolean retryAuth, boolean patchTableReponse,
            boolean doNotLogData) throws FreeboxException {
        try {
            Properties headers = null;
            String token = sessionToken;
            if (token != null) {
                headers = new Properties();
                headers.setProperty(AUTH_HEADER, token);
            }
            InputStream stream = null;
            String contentType = null;
            if (requestContent != null) {
                stream = new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8));
                contentType = HTTP_CALL_CONTENT_TYPE;
            }
            logger.debug("executeUrl {} {} requestContent {}", httpMethod, relativeUrl,
                    doNotLogData ? "***" : requestContent);
            String jsonResponse = HttpUtil.executeUrl(httpMethod, baseAddress + relativeUrl, headers, stream,
                    contentType, HTTP_CALL_DEFAULT_TIMEOUT_MS);
            if (stream != null) {
                stream.close();
                stream = null;
            }

            if (patchTableReponse) {
                // Replace empty result by an empty table result
                jsonResponse = jsonResponse.replace("\"result\":{}", "\"result\":[]");
            }

            return evaluateJsonResponse(jsonResponse, responseClass, doNotLogData);
        } catch (FreeboxException e) {
            if (retryAuth && e.isAuthRequired()) {
                logger.debug("Authentication required: open a new session and retry the request");
                openSession();
                return executeUrl(httpMethod, relativeUrl, requestContent, responseClass, false, patchTableReponse,
                        doNotLogData);
            }
            throw e;
        } catch (IOException e) {
            throw new FreeboxException(httpMethod + " request " + relativeUrl + ": execution failed: " + e.getMessage(),
                    e);
        } catch (JsonSyntaxException e) {
            throw new FreeboxException(
                    httpMethod + " request " + relativeUrl + ": response parsing failed: " + e.getMessage(), e);
        }
    }

    private <T extends FreeboxResponse<F>, F> F evaluateJsonResponse(String jsonResponse, Class<T> responseClass,
            boolean doNotLogData) throws JsonSyntaxException, FreeboxException {
        logger.debug("evaluateJsonReesponse Json {}", doNotLogData ? "***" : jsonResponse);
        // First check only if the result is successful
        FreeboxResponse<Object> partialResponse = gson.fromJson(jsonResponse, EmptyResponse.class);
        partialResponse.evaluate();
        // Parse the full response in case of success
        T fullResponse = gson.fromJson(jsonResponse, responseClass);
        fullResponse.evaluate();
        F result = fullResponse.getResult();
        return result;
    }

    private String encodeUrl(String url) throws FreeboxException {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new FreeboxException("Encoding the URL \"" + url + "\" in UTF-8 failed", e);
        }
    }

    public <T extends FreeboxResponse<F>, F> F executeGet(Class<T> responseClass, @Nullable String request)
            throws FreeboxException {
        Annotation[] annotations = responseClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof RelativePath) {
                RelativePath myAnnotation = (RelativePath) annotation;
                String relativeUrl = myAnnotation.relativeUrl();
                if (request != null) {
                    relativeUrl += encodeUrl(request) + "/";
                }
                return executeUrl("GET", relativeUrl, null, responseClass, myAnnotation.retryAuth(), false, false);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends FreeboxResponse<F>, F> F execute(Object request, @Nullable String requestUrl)
            throws FreeboxException {
        Annotation[] annotations = request.getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof RequestAnnotation) {
                RequestAnnotation myAnnotation = (RequestAnnotation) annotation;
                Class<T> answerClass = (Class<T>) myAnnotation.responseClass();
                String relativeUrl = myAnnotation.relativeUrl();
                if (requestUrl != null) {
                    relativeUrl += encodeUrl(requestUrl) + "/";
                }
                return executeUrl(myAnnotation.method(), relativeUrl, gson.toJson(request), answerClass,
                        myAnnotation.retryAuth(), false, false);
            }
        }
        return null;
    }

    public <T extends FreeboxResponse<F>, F> F executePost(Class<T> responseClass, @Nullable String request,
            @Nullable Object content) throws FreeboxException {
        Annotation[] annotations = responseClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof RelativePath) {
                RelativePath myAnnotation = (RelativePath) annotation;
                String relativeUrl = myAnnotation.relativeUrl();
                if (request != null) {
                    relativeUrl += encodeUrl(request) + "/";
                }
                return executeUrl("POST", relativeUrl, content != null ? gson.toJson(content) : null, responseClass,
                        myAnnotation.retryAuth(), false, false);
            }
        }
        return null;
    }
}
