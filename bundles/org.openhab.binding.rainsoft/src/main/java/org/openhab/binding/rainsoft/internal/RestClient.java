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
import org.openhab.binding.rainsoft.internal.data.DataFactory;
import org.openhab.binding.rainsoft.internal.data.ParamBuilder;
import org.openhab.binding.rainsoft.internal.data.Profile;
import org.openhab.binding.rainsoft.internal.data.RainSoftDevices;
import org.openhab.binding.rainsoft.internal.data.RainSoftEvent;
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
        logger.info("Creating RainSoft client for API version {} on endPoint {}", ApiConstants.API_VERSION,
                ApiConstants.API_BASE);
    }

    /**
     * Post data to given url
     *
     * @param url
     * @param data
     * @param unamePassword username:password if applicable, otherwise null
     * @return the servers response
     * @throws AuthenticationException
     *
     */

    private String postRequest(String resourceUrl, String data, String oauth_token) throws AuthenticationException {
        String result = null;
        logger.trace("RestClient - postRequest: {} - {} - {}", resourceUrl, data, oauth_token);
        try {
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            StringBuilder output = new StringBuilder();
            URL url = new URL(resourceUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", ApiConstants.API_USER_AGENT);
            conn.setRequestProperty("Authorization", "Bearer " + oauth_token);
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

            // conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset: UTF-8");
            conn.setRequestProperty("X-API-LANG", "en");
            conn.setRequestProperty("Content-length", "gzip, deflate");
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);

            OutputStream out = conn.getOutputStream();
            out.write(postData);
            logger.debug("RestApi postRequest: {}, response code: {}, message {}.", resourceUrl, conn.getResponseCode(),
                    conn.getResponseMessage());
            switch (conn.getResponseCode()) {
                case 200:
                case 201:
                    break;
                case 400:
                case 401:
                    throw new AuthenticationException("Invalid username or password");
                default:
                    logger.error("Unhandled http response code: {}", conn.getResponseCode());
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            conn.disconnect();
            result = output.toString();
            logger.trace("RestApi postRequest response: {}.", result);
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException ex) {
            logger.error("RestApi error in postRequest!", ex);
            // ex.printStackTrace();
        }
        return result;
    }

    /**
     * Get data from given url
     *
     * @param url
     * @param data
     * @return the servers response
     * @throws AuthenticationException
     */
    private String getRequest(String resourceUrl, Profile profile) throws AuthenticationException {
        String result = null;
        logger.trace("RestClient - getRequest: {}", resourceUrl);
        try {
            StringBuilder output = new StringBuilder();
            URL url = new URL(resourceUrl);// + "?" + data);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", ApiConstants.API_USER_AGENT);
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

            conn.setRequestProperty("cache-control", "no-cache");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("authorization", "Bearer " + profile.getAccessToken());

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
            if (!result.startsWith("[{\"id\"")) { // Ignore ding results
                logger.trace("RestApi getRequest response: {}.", result);
            }
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
    public Profile getAuthenticatedProfile(String username, String password, String refreshToken, String twofactorCode,
            String hardwareId) throws AuthenticationException, ParseException {

        String refToken = refreshToken;

        logger.debug("RestClient - getAuthenticatedProfile U:{} - P:{} - R:{} - 2:{} - H:{}",
                RainSoftUtils.sanitizeData(username), RainSoftUtils.sanitizeData(password),
                RainSoftUtils.sanitizeData(refreshToken), RainSoftUtils.sanitizeData(twofactorCode),
                RainSoftUtils.sanitizeData(hardwareId));

        if ((twofactorCode != null) && (!twofactorCode.equals(""))) {
            logger.debug("RestClient - getAuthenticatedProfile - valid 2fa - run getAuthCode");
            refToken = getAuthCode(twofactorCode, username, password, hardwareId);
        }

        JSONObject oauthToken = get_oauth_token(username, password, refToken);
        String jsonResult = postRequest(ApiConstants.URL_SESSION, DataFactory.getSessionParams(hardwareId),
                oauthToken.get("access_token").toString());
        JSONObject obj = (JSONObject) new JSONParser().parse(jsonResult);
        return new Profile((JSONObject) obj.get("profile"), oauthToken.get("refresh_token").toString(),
                oauthToken.get("access_token").toString());
    }

    /**
     * Get a (new) oAuth token.
     *
     * @param username the username of the RainSoft account.
     * @param password the password for the RainSoft account.
     * @return a JSONObject with the available data stored in it (access_token, refresh_token)
     * @throws AuthenticationException
     * @throws ParseException
     */
    private JSONObject get_oauth_token(String username, String password, String refreshToken)
            throws AuthenticationException, ParseException {

        logger.debug("RestClient - get_oauth_token {} - {} - {}", RainSoftUtils.sanitizeData(username),
                RainSoftUtils.sanitizeData(password), RainSoftUtils.sanitizeData(refreshToken));

        String result = null;
        JSONObject oauth_token = null;
        String resourceUrl = ApiConstants.API_OAUTH_ENDPOINT;
        try {
            Map<String, String> map = new HashMap<String, String>();

            map.put("client_id", "ring_official_android");
            map.put("scope", "client");
            if (refreshToken == null || refreshToken.equals("")) {
                logger.debug("RestClient - get_oauth_token - refreshToken null or empty {}",
                        RainSoftUtils.sanitizeData(refreshToken));
                map.put("grant_type", "password");
                map.put("username", username);
                map.put("password", password);
            } else {
                logger.debug("RestClient - get_oauth_token - refreshToken NOT null or empty {}",
                        RainSoftUtils.sanitizeData(refreshToken));
                map.put("grant_type", "refresh_token");
                map.put("refresh_token", refreshToken);
            }
            URL url = new URL(resourceUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", ApiConstants.API_USER_AGENT);
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

            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset: UTF-8");
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

            logger.debug("RestClient get_oauth_token: {}, response code: {}, message {}.", resourceUrl,
                    conn.getResponseCode(), conn.getResponseMessage());

            switch (conn.getResponseCode()) {
                case 200:
                case 201:
                    break;
                case 400:
                    throw new AuthenticationException("Two factor authentication enabled, enter code");
                case 412:
                    if (conn.getResponseMessage().startsWith("Precondition")) {
                        throw new AuthenticationException("Two factor authentication enabled, enter code");
                    }
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

    public Boolean refresh_session(String refreshToken) {
        logger.debug("RestClient - refresh_session {}", RainSoftUtils.sanitizeData(refreshToken));
        String result = null;
        String resourceUrl = ApiConstants.API_OAUTH_ENDPOINT;
        try {
            Map<String, String> map = new HashMap<String, String>();

            map.put("grant_type", "refresh_token");
            map.put("refresh_token", refreshToken);

            URL url = new URL(resourceUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", ApiConstants.API_USER_AGENT);
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

            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset: UTF-8");
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

            switch (conn.getResponseCode()) {
                case 200:
                case 201:
                    break;
                case 400:
                case 401:
                    return false;
                default:
                    logger.error("Unhandled http response code: {}", conn.getResponseCode());
                    return false;
            }
            logger.debug("RestApi resource: {}, response code: {}.", resourceUrl, conn.getResponseCode());

            result = readFullyAsString(conn.getInputStream(), "UTF-8");
            conn.disconnect();

            logger.debug("RestApi response: {}.", result);
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException ex) {
            logger.error("ERROR!", ex);
            // ex.printStackTrace();
        }
        return true;
    }

    /**
     * Post data to given url
     *
     * @param url
     * @param data
     * @param unamePassword username:password if applicable, otherwise null
     * @return the servers response
     * @throws AuthenticationException
     *
     */

    private String getAuthCode(String authCode, String username, String password, String hardwareId)
            throws AuthenticationException {
        logger.debug("RestClient - getAuthCode A:{} - U:{} - P:{} - H:{}", RainSoftUtils.sanitizeData(authCode),
                RainSoftUtils.sanitizeData(username), RainSoftUtils.sanitizeData(password), RainSoftUtils.sanitizeData(hardwareId));

        String result = "";

        String resourceUrl = ApiConstants.API_OAUTH_ENDPOINT;
        try {
            ParamBuilder pb = new ParamBuilder(false);
            pb.add("client_id", "ring_official_android");
            pb.add("scope", "client");
            pb.add("grant_type", "password");
            pb.add("password", password);
            pb.add("username", username);

            URL url = new URL(resourceUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("X-API-LANG", "en");
            conn.setRequestProperty("Content-length", "gzip, deflate");
            conn.setRequestProperty("2fa-support", "true");
            conn.setRequestProperty("2fa-code", authCode);
            conn.setRequestProperty("hardware_id", hardwareId);
            conn.setRequestProperty("User-Agent", ApiConstants.API_USER_AGENT);
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

            byte[] out = pb.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(out);

            logger.info("RestApi getAuthCode: {}, response code: {}, message {}.", resourceUrl, conn.getResponseCode(),
                    conn.getResponseMessage());
            String tmp = conn.getResponseMessage();

            switch (conn.getResponseCode()) {
                case 200:
                case 201:
                    break;
                case 400:
                    throw new AuthenticationException("2 factor enabled, enter code");
                case 412:
                    if (conn.getResponseMessage().startsWith("Verification Code")) {
                        throw new AuthenticationException("2 factor enabled, enter code");
                    }
                case 401:
                    throw new AuthenticationException("Invalid username or password.");
                default:
                    logger.error("Unhandled http response code: {}", conn.getResponseCode());
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            result = readFullyAsString(conn.getInputStream(), "UTF-8");
            conn.disconnect();

            JSONObject refToken = (JSONObject) new JSONParser().parse(result);
            result = refToken.get("refresh_token").toString();
            logger.debug("RestClient - getAuthCode response: {}.", RainSoftUtils.sanitizeData(result));
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException ex) {
            logger.error("Error getting auth code!", ex);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            logger.error("Error parsing refToken", e);
        }
        return result;
    }

    /**
     * Get the RainSoftDevices instance, given the authenticated Profile.
     *
     * @param profile the Profile previously retrieved when authenticating.
     * @return the RainSoftDevices instance filled with all available data.
     * @throws AuthenticationException when request is invalid.
     * @throws ParseException when response is invalid JSON.
     */
    public RainSoftDevices getRainSoftDevices(Profile profile, RainSoftAccount rainSoftAccount)
            throws ParseException, AuthenticationException {
        String jsonResult = getRequest(ApiConstants.URL_DEVICES, profile);// DataFactory.getDevicesParams(profile));
        JSONObject obj = (JSONObject) new JSONParser().parse(jsonResult);
        return new RainSoftDevices(obj, rainSoftAccount);
    }

    /**
     * Get a List with the last recorded events, newest on top.
     *
     * @param profile the Profile previously retrieved when authenticating.
     * @param limit the maximum number of events.
     * @return
     * @throws AuthenticationException
     * @throws ParseException
     */
    public synchronized List<RainSoftEvent> getHistory(Profile profile, int limit)
            throws AuthenticationException, ParseException {

        String jsonResult = getRequest(ApiConstants.URL_HISTORY + "?limit=" + limit, profile);
        if (jsonResult != null) {
            JSONArray obj = (JSONArray) new JSONParser().parse(jsonResult);
            List<RainSoftEvent> result = new ArrayList<>(limit);
            for (Object jsonEvent : obj.toArray()) {
                result.add(new RainSoftEvent((JSONObject) jsonEvent));
            }
            return result;
        } else {
            return new ArrayList<>(0);
        }
    }

}
