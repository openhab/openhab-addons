/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaReceiverRequest;
import org.openhab.binding.freebox.internal.api.model.FreeboxAuthorizationStatus;
import org.openhab.binding.freebox.internal.api.model.FreeboxAuthorizeRequest;
import org.openhab.binding.freebox.internal.api.model.FreeboxAuthorizeResponse;
import org.openhab.binding.freebox.internal.api.model.FreeboxCallEntry;
import org.openhab.binding.freebox.internal.api.model.FreeboxConnectionStatus;
import org.openhab.binding.freebox.internal.api.model.FreeboxDiscoveryResponse;
import org.openhab.binding.freebox.internal.api.model.FreeboxFtpConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHost;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanInterface;
import org.openhab.binding.freebox.internal.api.model.FreeboxLcdConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxLoginResponse;
import org.openhab.binding.freebox.internal.api.model.FreeboxOpenSessionRequest;
import org.openhab.binding.freebox.internal.api.model.FreeboxOpenSessionResponse;
import org.openhab.binding.freebox.internal.api.model.FreeboxPhoneStatus;
import org.openhab.binding.freebox.internal.api.model.FreeboxResponse;
import org.openhab.binding.freebox.internal.api.model.FreeboxSambaConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxSystemConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxUPnPAVConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxWifiGlobalConfig;
import org.openhab.binding.freebox.internal.api.model.FreeboxXdslStatus;
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
public class FreeboxApiManager {

    private final Logger logger = LoggerFactory.getLogger(FreeboxApiManager.class);

    private static final int HTTP_CALL_DEFAULT_TIMEOUT_MS = 10000;
    private static final String AUTH_HEADER = "X-Fbx-App-Auth";
    private static final String HTTP_CALL_CONTENT_TYPE = "application/json; charset=utf-8";

    private static class AuthorizeResponse extends FreeboxResponse<FreeboxAuthorizeResponse> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if ((getResult().getAppToken() == null) || getResult().getAppToken().isEmpty()) {
                throw new FreeboxException("No app token in response", this);
            }
            if (getResult().getTrackId() == null) {
                throw new FreeboxException("No track id in response", this);
            }
        }
    }

    private static class AuthorizationStatusResponse extends FreeboxResponse<FreeboxAuthorizationStatus> {
    }

    private static class LoginResponse extends FreeboxResponse<FreeboxLoginResponse> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if ((getResult().getChallenge() == null) || getResult().getChallenge().isEmpty()) {
                throw new FreeboxException("No challenge in response", this);
            }
        }
    }

    private static class OpenSessionResponse extends FreeboxResponse<FreeboxOpenSessionResponse> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if ((getResult().getSessionToken() == null) || getResult().getSessionToken().isEmpty()) {
                throw new FreeboxException("No session token in response", this);
            }
        }
    }

    public static class EmptyResponse extends FreeboxResponse<Object> {
    }

    private static class ConnectionStatusResponse extends FreeboxResponse<FreeboxConnectionStatus> {
    }

    private static class XdslStatusResponse extends FreeboxResponse<FreeboxXdslStatus> {
    }

    private static class WifiGlobalConfigResponse extends FreeboxResponse<FreeboxWifiGlobalConfig> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if (getResult().isEnabled() == null) {
                throw new FreeboxException("No Wifi status in response", this);
            }
        }
    }

    private static class FtpConfigResponse extends FreeboxResponse<FreeboxFtpConfig> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if (getResult().isEnabled() == null) {
                throw new FreeboxException("No FTP status in response", this);
            }
        }
    }

    private static class AirMediaConfigResponse extends FreeboxResponse<FreeboxAirMediaConfig> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if (getResult().isEnabled() == null) {
                throw new FreeboxException("No AirMedia status in response", this);
            }
        }
    }

    private static class UPnPAVConfigResponse extends FreeboxResponse<FreeboxUPnPAVConfig> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if (getResult().isEnabled() == null) {
                throw new FreeboxException("No UPnP AV status in response", this);
            }
        }
    }

    private static class SambaConfigResponse extends FreeboxResponse<FreeboxSambaConfig> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if (getResult().isFileShareEnabled() == null) {
                throw new FreeboxException("No file sharing status in response", this);
            }
            if (getResult().isPrintShareEnabled() == null) {
                throw new FreeboxException("No printer sharing status in response", this);
            }
        }
    }

    private static class LcdConfigResponse extends FreeboxResponse<FreeboxLcdConfig> {
    }

    private static class SystemConfigResponse extends FreeboxResponse<FreeboxSystemConfig> {
    }

    private static class LanConfigResponse extends FreeboxResponse<FreeboxLanConfig> {
    }

    private static class LanHostsResponse extends FreeboxResponse<List<FreeboxLanHost>> {
    }

    private static class LanInterfacesResponse extends FreeboxResponse<List<FreeboxLanInterface>> {
    }

    private static class PhoneStatusResponse extends FreeboxResponse<List<FreeboxPhoneStatus>> {
        @Override
        public void evaluate() throws FreeboxException {
            super.evaluate();
            if (getResult() == null || getResult().size() == 0) {
                throw new FreeboxException("No phone status in response", this);
            }
        }
    }

    private static class CallEntryResponse extends FreeboxResponse<List<FreeboxCallEntry>> {
    }

    private static class AirMediaReceiversResponse extends FreeboxResponse<List<FreeboxAirMediaReceiver>> {
    }

    private String appId;
    private String appName;
    private String appVersion;
    private String deviceName;
    private String baseAddress;
    private String appToken;
    private String sessionToken;
    private Gson gson;

    public FreeboxApiManager(String appId, String appName, String appVersion, String deviceName) {
        this.appId = appId;
        this.appName = appName;
        this.appVersion = appVersion;
        this.deviceName = deviceName;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    public FreeboxDiscoveryResponse checkApi(String fqdn, boolean secureHttp) {
        String url = String.format("%s://%s/api_version", secureHttp ? "https" : "http", fqdn);
        try {
            String jsonResponse = HttpUtil.executeUrl("GET", url, HTTP_CALL_DEFAULT_TIMEOUT_MS);
            return gson.fromJson(jsonResponse, FreeboxDiscoveryResponse.class);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("checkApi with {} failed: {}", url, e.getMessage());
            return null;
        }
    }

    public boolean authorize(boolean useHttps, String fqdn, String apiBaseUrl, String apiVersion, String appToken) {
        String[] versionSplit = apiVersion.split(".");
        String majorVersion = "5";
        if (versionSplit.length > 0) {
            majorVersion = versionSplit[0];
        }
        this.baseAddress = (useHttps ? "https://" : "http://") + fqdn + apiBaseUrl + "v" + majorVersion + "/";

        String authorizeStatus = FreeboxAuthorizationStatus.AUTHORIZATION_STATUS_UNKNOWN;
        try {
            String token = appToken;
            if (StringUtils.isEmpty(token)) {
                FreeboxAuthorizeRequest request = new FreeboxAuthorizeRequest(appId, appName, appVersion, deviceName);
                FreeboxAuthorizeResponse response = executeUrl("POST", "login/authorize/", gson.toJson(request),
                        AuthorizeResponse.class, false);
                token = response.getAppToken();
                int trackId = response.getTrackId();

                logger.info("####################################################################");
                logger.info("# Please accept activation request directly on your freebox        #");
                logger.info("# Once done, record Apptoken in the Freebox Item configuration     #");
                logger.info("# {} #", token);
                logger.info("####################################################################");

                do {
                    Thread.sleep(2000);
                    authorizeStatus = executeUrl("GET", "login/authorize/" + trackId, null,
                            AuthorizationStatusResponse.class, false).getStatus();
                } while (FreeboxAuthorizationStatus.AUTHORIZATION_STATUS_PENDING.equalsIgnoreCase(authorizeStatus));
            } else {
                authorizeStatus = FreeboxAuthorizationStatus.AUTHORIZATION_STATUS_GRANTED;
            }

            if (!FreeboxAuthorizationStatus.AUTHORIZATION_STATUS_GRANTED.equalsIgnoreCase(authorizeStatus)) {
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

    private synchronized void openSession() throws FreeboxException {
        if (appToken == null) {
            throw new FreeboxException("No app token to open a new session");
        }
        sessionToken = null;
        String challenge = executeUrl("GET", "login/", null, LoginResponse.class, false).getChallenge();
        FreeboxOpenSessionRequest request = new FreeboxOpenSessionRequest(appId, hmacSha1(appToken, challenge));
        sessionToken = executeUrl("POST", "login/session/", gson.toJson(request), OpenSessionResponse.class, false,
                false, true).getSessionToken();
    }

    public synchronized void closeSession() {
        if (sessionToken != null) {
            try {
                executeUrl("POST", "login/logout/", null, EmptyResponse.class, false);
            } catch (FreeboxException e) {
            }
            sessionToken = null;
        }
    }

    public synchronized String getSessionToken() {
        return sessionToken;
    }

    public FreeboxConnectionStatus getConnectionStatus() throws FreeboxException {
        return executeUrl("GET", "connection/", null, ConnectionStatusResponse.class);
    }

    public String getxDslStatus() throws FreeboxException {
        return executeUrl("GET", "connection/xdsl/", null, XdslStatusResponse.class).getStatus();
    }

    public boolean isWifiEnabled() throws FreeboxException {
        return executeUrl("GET", "wifi/config/", null, WifiGlobalConfigResponse.class).isEnabled();
    }

    public boolean enableWifi(boolean enable) throws FreeboxException {
        FreeboxWifiGlobalConfig config = new FreeboxWifiGlobalConfig();
        config.setEnabled(enable);
        return executeUrl("PUT", "wifi/config/", gson.toJson(config), WifiGlobalConfigResponse.class).isEnabled();
    }

    public boolean isFtpEnabled() throws FreeboxException {
        return executeUrl("GET", "ftp/config/", null, FtpConfigResponse.class).isEnabled();
    }

    public boolean enableFtp(boolean enable) throws FreeboxException {
        FreeboxFtpConfig config = new FreeboxFtpConfig();
        config.setEnabled(enable);
        return executeUrl("PUT", "ftp/config/", gson.toJson(config), FtpConfigResponse.class).isEnabled();
    }

    public boolean isAirMediaEnabled() throws FreeboxException {
        return executeUrl("GET", "airmedia/config/", null, AirMediaConfigResponse.class).isEnabled();
    }

    public boolean enableAirMedia(boolean enable) throws FreeboxException {
        FreeboxAirMediaConfig config = new FreeboxAirMediaConfig();
        config.setEnabled(enable);
        return executeUrl("PUT", "airmedia/config/", gson.toJson(config), AirMediaConfigResponse.class).isEnabled();
    }

    public boolean isUPnPAVEnabled() throws FreeboxException {
        return executeUrl("GET", "upnpav/config/", null, UPnPAVConfigResponse.class).isEnabled();
    }

    public boolean enableUPnPAV(boolean enable) throws FreeboxException {
        FreeboxUPnPAVConfig config = new FreeboxUPnPAVConfig();
        config.setEnabled(enable);
        return executeUrl("PUT", "upnpav/config/", gson.toJson(config), UPnPAVConfigResponse.class).isEnabled();
    }

    public FreeboxSambaConfig getSambaConfig() throws FreeboxException {
        return executeUrl("GET", "netshare/samba/", null, SambaConfigResponse.class);
    }

    public boolean enableSambaFileShare(boolean enable) throws FreeboxException {
        FreeboxSambaConfig config = new FreeboxSambaConfig();
        config.setFileShareEnabled(enable);
        return executeUrl("PUT", "netshare/samba/", gson.toJson(config), SambaConfigResponse.class)
                .isFileShareEnabled();
    }

    public boolean enableSambaPrintShare(boolean enable) throws FreeboxException {
        FreeboxSambaConfig config = new FreeboxSambaConfig();
        config.setPrintShareEnabled(enable);
        return executeUrl("PUT", "netshare/samba/", gson.toJson(config), SambaConfigResponse.class)
                .isPrintShareEnabled();
    }

    public FreeboxLcdConfig getLcdConfig() throws FreeboxException {
        return executeUrl("GET", "lcd/config/", null, LcdConfigResponse.class);
    }

    public int setLcdBrightness(int brightness) throws FreeboxException {
        FreeboxLcdConfig config = getLcdConfig();
        int newValue = Math.min(100, brightness);
        newValue = Math.max(newValue, 0);
        config.setBrightness(newValue);
        return executeUrl("PUT", "lcd/config/", gson.toJson(config), LcdConfigResponse.class).getBrightness();
    }

    public int increaseLcdBrightness() throws FreeboxException {
        FreeboxLcdConfig config = getLcdConfig();
        config.setBrightness(Math.min(100, config.getBrightness() + 1));
        return executeUrl("PUT", "lcd/config/", gson.toJson(config), LcdConfigResponse.class).getBrightness();
    }

    public int decreaseLcdBrightness() throws FreeboxException {
        FreeboxLcdConfig config = getLcdConfig();
        config.setBrightness(Math.max(0, config.getBrightness() - 1));
        return executeUrl("PUT", "lcd/config/", gson.toJson(config), LcdConfigResponse.class).getBrightness();
    }

    public FreeboxLcdConfig setLcdOrientation(int orientation) throws FreeboxException {
        FreeboxLcdConfig config = getLcdConfig();
        int newValue = Math.min(360, orientation);
        newValue = Math.max(newValue, 0);
        config.setOrientation(newValue);
        config.setOrientationForced(true);
        return executeUrl("PUT", "lcd/config/", gson.toJson(config), LcdConfigResponse.class);
    }

    public boolean setLcdOrientationForced(boolean forced) throws FreeboxException {
        FreeboxLcdConfig config = getLcdConfig();
        config.setOrientationForced(forced);
        return executeUrl("PUT", "lcd/config/", gson.toJson(config), LcdConfigResponse.class).isOrientationForced();
    }

    public FreeboxSystemConfig getSystemConfig() throws FreeboxException {
        return executeUrl("GET", "system/", null, SystemConfigResponse.class);
    }

    public boolean isInLanBridgeMode() throws FreeboxException {
        return executeUrl("GET", "lan/config/", null, LanConfigResponse.class).isInBridgeMode();
    }

    public List<FreeboxLanHost> getLanHosts() throws FreeboxException {
        List<FreeboxLanHost> hosts = new ArrayList<>();
        List<FreeboxLanInterface> interfaces = executeUrl("GET", "lan/browser/interfaces/", null,
                LanInterfacesResponse.class);
        if (interfaces != null) {
            for (FreeboxLanInterface lanInterface : interfaces) {
                if (lanInterface.getHostCount() > 0) {
                    List<FreeboxLanHost> lanHosts = getLanHostsFromInterface(lanInterface.getName());
                    if (lanHosts != null) {
                        hosts.addAll(lanHosts);
                    }
                }
            }
        }
        return hosts;
    }

    private List<FreeboxLanHost> getLanHostsFromInterface(String lanInterface) throws FreeboxException {
        return executeUrl("GET", "lan/browser/" + encodeUrl(lanInterface) + "/", null, LanHostsResponse.class);
    }

    public FreeboxPhoneStatus getPhoneStatus() throws FreeboxException {
        return executeUrl("GET", "phone/?_dc=1415032391207", null, PhoneStatusResponse.class).get(0);
    }

    public List<FreeboxCallEntry> getCallEntries() throws FreeboxException {
        return executeUrl("GET", "call/log/", null, CallEntryResponse.class);
    }

    public List<FreeboxAirMediaReceiver> getAirMediaReceivers() throws FreeboxException {
        return executeUrl("GET", "airmedia/receivers/", null, AirMediaReceiversResponse.class, true, true, false);
    }

    public void playMedia(String url, String airPlayName, String airPlayPassword) throws FreeboxException {
        FreeboxAirMediaReceiverRequest request = new FreeboxAirMediaReceiverRequest();
        request.setStartAction();
        request.setVideoMediaType();
        if (StringUtils.isNotEmpty(airPlayPassword)) {
            request.setPassword(airPlayPassword);
        }
        request.setMedia(url);
        executeUrl("POST", "airmedia/receivers/" + encodeUrl(airPlayName) + "/", gson.toJson(request),
                EmptyResponse.class, true, false, true);
    }

    public void stopMedia(String airPlayName, String airPlayPassword) throws FreeboxException {
        FreeboxAirMediaReceiverRequest request = new FreeboxAirMediaReceiverRequest();
        request.setStopAction();
        request.setVideoMediaType();
        if (StringUtils.isNotEmpty(airPlayPassword)) {
            request.setPassword(airPlayPassword);
        }
        executeUrl("POST", "airmedia/receivers/" + encodeUrl(airPlayName) + "/", gson.toJson(request),
                EmptyResponse.class, true, false, true);
    }

    public void reboot() throws FreeboxException {
        executeUrl("POST", "system/reboot/", null, EmptyResponse.class);
    }

    private <T extends FreeboxResponse<F>, F> F executeUrl(String httpMethod, String relativeUrl, String requestContent,
            Class<T> responseClass) throws FreeboxException {
        return executeUrl(httpMethod, relativeUrl, requestContent, responseClass, true, false, false);
    }

    private <T extends FreeboxResponse<F>, F> F executeUrl(String httpMethod, String relativeUrl, String requestContent,
            Class<T> responseClass, boolean retryAuth) throws FreeboxException {
        return executeUrl(httpMethod, relativeUrl, requestContent, responseClass, retryAuth, false, false);
    }

    private <T extends FreeboxResponse<F>, F> F executeUrl(String httpMethod, String relativeUrl, String requestContent,
            Class<T> responseClass, boolean retryAuth, boolean patchTableReponse, boolean doNotLogData)
            throws FreeboxException {
        try {
            Properties headers = null;
            String token = getSessionToken();
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

            if (patchTableReponse) {
                // Replace empty result by an empty table result
                jsonResponse = jsonResponse.replace("\"result\":{}", "\"result\":[]");
            }

            return evaluateJsonReesponse(jsonResponse, responseClass, doNotLogData);
        } catch (FreeboxException e) {
            if (retryAuth && e.isAuthRequired()) {
                logger.debug("Authentication required: open a new session and retry the request");
                openSession();
                return executeUrl(httpMethod, relativeUrl, requestContent, responseClass, false, patchTableReponse,
                        doNotLogData);
            }
            throw e;
        } catch (IOException e) {
            throw new FreeboxException(
                    httpMethod + " request " + relativeUrl + ": execution failed: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new FreeboxException(
                    httpMethod + " request " + relativeUrl + ": response parsing failed: " + e.getMessage());
        }
    }

    private <T extends FreeboxResponse<F>, F> F evaluateJsonReesponse(String jsonResponse, Class<T> responseClass,
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
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new FreeboxException("Encoding the URL \"" + url + "\" in UTF-8 failed");
        }
    }

    private static String hmacSha1(String key, String value) throws FreeboxException {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value.getBytes());

            // Convert raw bytes to Hex
            byte[] hexBytes = new Hex().encode(rawHmac);

            // Covert array of Hex bytes to a String
            return new String(hexBytes, "UTF-8");
        } catch (Exception e) {
            throw new FreeboxException("Computing the hmac-sha1 of the challenge and the app token failed");
        }
    }

}
