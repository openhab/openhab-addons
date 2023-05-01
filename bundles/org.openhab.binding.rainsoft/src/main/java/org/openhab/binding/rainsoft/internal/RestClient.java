/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rainsoft.internal;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openhab.binding.rainsoft.internal.data.ParamBuilder;
import org.openhab.binding.rainsoft.internal.data.RainSoftDevices;
import org.openhab.binding.rainsoft.internal.errors.AuthenticationException;
import org.openhab.binding.rainsoft.internal.utils.RainSoftUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ben Rosenblum - Initial contribution
 */

public class RestClient {

    private static final int CONNECTION_TIMEOUT = 12000;
    private final Logger logger = LoggerFactory.getLogger(RestClient.class);

    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";

    // The factory to create data elements
    // private DataElementFactory factory;

    /**
     * Create a new client with the given server and port address.
     *
     * @param endPoint
     */
    public RestClient() {
        logger.info("Creating RainSoft client for API on endPoint {}",
                ApiConstants.API_BASE);
    }

    /**
     * Get data from given url
     *
     * @param url
     * @param data
     * @return the servers response
     * @throws AuthenticationException
     */
    private String getRequest(String resourceUrl, String authToken) throws AuthenticationException {
        String result = null;
        logger.trace("RestClient - getRequest: {}", resourceUrl);
        try {
            StringBuilder output = new StringBuilder();
            URL url = new URL(resourceUrl);// + "?" + data);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", ApiConstants.API_ACCEPT_JSON);
            conn.setRequestProperty("Content-Type", ApiConstants.API_CONTENT_TYPE);
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            // SSL setting
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new javax.net.ssl.X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            } }, null);
            conn.setSSLSocketFactory(context.getSocketFactory());
            conn.setRequestMethod(METHOD_GET);

            conn.setRequestProperty("X-Remind-Auth-Token", authToken);

            conn.setDoOutput(true);
            conn.setConnectTimeout(12000);

            switch (conn.getResponseCode()) {
                case 200:
                case 201:
                    break;
                case 400:
                case 401:
                    // break;
                    throw new AuthenticationException("Invalid request");
                default:
                    logger.error("Unhandled http response code: {}", conn.getResponseCode());
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            if (conn.getResponseCode() != 200) {
                logger.debug("RestApi getRequest: {}, response code: {}, message {}.", resourceUrl,
                        conn.getResponseCode(), conn.getResponseMessage());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            conn.disconnect();
            result = output.toString();
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException ex) {
            logger.debug("RestApi error in getRequest!", ex);
            // ex.printStackTrace();
        }
        return result;
    }

    /**
     * Get a (new) authenticated profile.
     *
     * @param username the username of the RainSoft account.
     * @param password the password for the RainSoft account.
     * @param hardwareId a hardware ID (must be unique for every piece of hardware used).
     * @return a Profile instance with available data stored in it.
     * @throws AuthenticationException
     * @throws ParseException
     */
    public String getAuthenticatedProfile(String username, String password) throws AuthenticationException, ParseException {

        logger.debug("RestClient - getAuthenticatedProfile U:{} - P:{}",
                RainSoftUtils.sanitizeData(username), RainSoftUtils.sanitizeData(password));

        JSONObject authToken = get_auth_token(username, password);
        String token = authToken.get("authentication_token").toString();
        logger.debug("RestClient - getAuthenticatedProfile T:{}",RainSoftUtils.sanitizeData(token));
        return token;
    }

    /**
     * Get a (new) auth token.
     *
     * @param username the username of the RainSoft account.
     * @param password the password for the RainSoft account.
     * @return a JSONObject with the available data stored in it (access_token, refresh_token)
     * @throws AuthenticationException
     * @throws ParseException
     */
    private JSONObject get_auth_token(String username, String password)
            throws AuthenticationException, ParseException {

        logger.debug("RestClient - get_auth_token {} - {}", RainSoftUtils.sanitizeData(username),
                RainSoftUtils.sanitizeData(password));

        String result = null;
        JSONObject oauth_token = null;
        String resourceUrl = ApiConstants.URL_LOGIN;
        try {
            Map<String, String> map = new HashMap<String, String>();

                map.put("email", username);
                map.put("password", password);
            URL url = new URL(resourceUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", ApiConstants.API_ACCEPT_JSON);
            conn.setRequestProperty("Content-Type", ApiConstants.API_CONTENT_TYPE);
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            // SSL setting
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new javax.net.ssl.X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            } }, null);
            conn.setSSLSocketFactory(context.getSocketFactory());
            conn.setRequestMethod(METHOD_POST);

            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);

            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            conn.setFixedLengthStreamingMode(length);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(out);

            logger.debug("RestClient get_auth_token: {}, response code: {}, message {}.", resourceUrl,
                    conn.getResponseCode(), conn.getResponseMessage());

            switch (conn.getResponseCode()) {
                case 200:
                case 201:
                    break;
                case 401:
                    throw new AuthenticationException("Invalid username or password.");
                default:
                    logger.error("Unhandled http response code: {}", conn.getResponseCode());
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            result = readFullyAsString(conn.getInputStream(), "UTF-8");
            conn.disconnect();

            oauth_token = (JSONObject) new JSONParser().parse(result);
            logger.debug("RestClient response: {}.", RainSoftUtils.sanitizeData(result));
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException ex) {
            logger.error("RestApi: Error in get_oauth_token!", ex);
            // ex.printStackTrace();
        }
        return oauth_token;
    }

    public String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

    public String getCustomerId(String authToken) throws AuthenticationException, ParseException {
        String jsonResult = getRequest(ApiConstants.URL_CUSTOMER, authToken);
        JSONObject obj = (JSONObject) new JSONParser().parse(jsonResult);
        String customerId = obj.get("id").toString();
        logger.debug("RestClient - getCustomerId ID:{}",customerId);
        return customerId;
    }

    public String getLocations(String customerId, String authToken) throws AuthenticationException, ParseException {
        String jsonResult = getRequest(ApiConstants.URL_LOCATIONS + "/" + customerId, authToken);
        logger.debug("RestClient - getLocations ID:{}", jsonResult);
        return jsonResult;
    }

    public String getDevice(String deviceId, String authToken) throws AuthenticationException, ParseException {
        String jsonResult = getRequest(ApiConstants.URL_DEVICE + "/" + deviceId, authToken);
        logger.debug("RestClient - getDevice ID:{}", jsonResult);
        return jsonResult;
    }

    public String getWaterUsage(String deviceId, String authToken) throws AuthenticationException, ParseException {
        String jsonResult = getRequest(ApiConstants.URL_DEVICE + "/" + deviceId + "/water_usage", authToken);
        logger.debug("RestClient - getWaterUsage ID:{}", jsonResult);
        return jsonResult;
    }

    public String getSaltUsage(String deviceId, String authToken) throws AuthenticationException, ParseException {
        String jsonResult = getRequest(ApiConstants.URL_DEVICE + "/" + deviceId + "/salt_usage", authToken);
        logger.debug("RestClient - getSaltUsage ID:{}", jsonResult);
        return jsonResult;
    }

    /**
     * Get the RainSoftDevices instance, given the authenticated Profile.
     *
     * @param profile the Profile previously retrieved when authenticating.
     * @return the RainSoftDevices instance filled with all available data.
     * @throws AuthenticationException when request is invalid.
     * @throws ParseException when response is invalid JSON.
     */
    public RainSoftDevices getRainSoftDevices(String authToken, RainSoftAccount rainSoftAccount)
            throws ParseException, AuthenticationException {
        String jsonResult = getRequest(ApiConstants.URL_DEVICE, authToken);
        JSONObject obj = (JSONObject) new JSONParser().parse(jsonResult);
        return new RainSoftDevices(obj, rainSoftAccount);
    }

}
