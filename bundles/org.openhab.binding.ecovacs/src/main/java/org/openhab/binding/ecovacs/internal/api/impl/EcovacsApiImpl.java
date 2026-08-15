/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ecovacs.internal.api.EcovacsApi;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiConfiguration;
import org.openhab.binding.ecovacs.internal.api.EcovacsApiException;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.commands.IotDeviceCommand;
import org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal.PortalAuthRequest;
import org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal.PortalAuthRequestParameter;
import org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal.PortalCleanLogsRequest;
import org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal.PortalIotCommandRequest;
import org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal.PortalIotProductRequest;
import org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal.PortalLoginRequest;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.main.AccessData;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.main.AuthCode;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.main.ConfigEntry;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.main.ResponseWrapper;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.main.VerificationRequest;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.Device;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.IotProduct;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalCleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalCleanLogsResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalCleanResultsResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalDeviceResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotProductResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalLoginResponse;
import org.openhab.binding.ecovacs.internal.api.util.HashUtil;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * @author Danny Baumann - Initial contribution
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public final class EcovacsApiImpl implements EcovacsApi {
    private final Logger logger = LoggerFactory.getLogger(EcovacsApiImpl.class);

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final MqttConnection mqttConnection = new MqttConnection();

    private final EcovacsApiConfiguration configuration;
    private @Nullable PublicKey publicKey;
    private @Nullable Credentials credentials;

    public EcovacsApiImpl(HttpClient httpClient, EcovacsApiConfiguration configuration) {
        this.httpClient = httpClient;
        this.configuration = configuration;
    }

    @Override
    public void testAndSetCredentials(Credentials creds) throws EcovacsApiException, InterruptedException {
        // Execute a get devices request to test credentials validity
        PortalAuthRequest data = new PortalAuthRequest(PortalTodo.GET_DEVICE_LIST, createAuthData(creds));
        String userUrl = EcovacsApiUrlFactory.getPortalUsersUrl(configuration);
        executeRequest(createJsonRequest(userUrl, data));
        this.credentials = creds;
    }

    @Override
    public void startLoginAndRequestVerificationCode() throws EcovacsApiException, InterruptedException {
        String encryptedAccount = encryptAccount();
        Map<String, String> params = getBaseLoginRequestParameters();
        params.put("encryptEmail", encryptedAccount);
        params.put("verifyType", "EMAIL_VERIFY_DEVICE");
        params.put("supportChar", "N");
        params.put("isForce", "N");

        Request request = createAuthRequest(
                EcovacsApiUrlFactory.getPrivateApiUrl("user/sendEmailVerifyCode", configuration),
                configuration.getClientKey(), configuration.getClientSecret(), params);
        ContentResponse response = executeRequest(request);
        Type responseType = new TypeToken<ResponseWrapper<VerificationRequest>>() {
        }.getType();
        handleResponseWrapper(gson.fromJson(response.getContentAsString(), responseType));
    }

    @Override
    public Credentials finishLogin(String verificationCode) throws EcovacsApiException, InterruptedException {
        AccessData accessData = verifyDeviceAndLogin(verificationCode);
        AuthCode authCode = getAuthCode(accessData.getUid(), accessData.getAccessToken());
        Credentials creds = portalLogin(authCode, accessData.getUid());
        this.credentials = creds;
        return creds;
    }

    @Override
    public Credentials refreshCredentials() throws EcovacsApiException, InterruptedException {
        Credentials creds = this.credentials;
        if (creds == null) {
            throw new EcovacsApiException("Can not refresh token while not logged in");
        }
        AuthCode authCode = getAuthCode(creds.userId(), creds.token());
        Credentials refreshedCreds = portalLogin(authCode, creds.userId());
        this.credentials = refreshedCreds;
        return refreshedCreds;
    }

    EcovacsApiConfiguration getConfig() {
        return configuration;
    }

    public @Nullable Credentials getCredentials() {
        return this.credentials;
    }

    MqttSubscriptionHandle subscribeForMqttEvents(Device device, MqttEventReceiver receiver)
            throws EcovacsApiException, InterruptedException {
        Credentials creds = this.credentials;
        if (creds == null) {
            throw new EcovacsApiException("Can not subscribe while not logged in");
        }

        mqttConnection.connectIfNeeded(configuration, creds);
        mqttConnection.subscribeDevice(device, receiver);
        return new MqttSubscriptionHandle(mqttConnection, device);
    }

    private Map<String, String> getBaseLoginRequestParameters() {
        Map<String, String> params = new HashMap<>();
        long now = System.currentTimeMillis();
        params.put("country", configuration.getCountry());
        params.put("lang", configuration.getLanguage());
        params.put("deviceId", configuration.getDeviceId());
        params.put("appCode", configuration.getAppCode());
        params.put("appVersion", configuration.getAppVersion());
        params.put("channel", configuration.getChannel());
        params.put("deviceType", configuration.getDeviceType());
        params.put("authTimespan", String.valueOf(now));
        params.put("authTimeZone", configuration.getTimeZone());
        params.put("requestId", HashUtil.getMD5Hash(String.valueOf(now)));
        return params;
    }

    private String encryptAccount() throws EcovacsApiException, InterruptedException {
        PublicKey publicKey = getPublicKey();
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encrypted = cipher.doFinal(configuration.getUsername().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new EcovacsApiException("Failed to encrypt account", e);
        }
    }

    private PublicKey getPublicKey() throws EcovacsApiException, InterruptedException {
        PublicKey publicKey = this.publicKey;
        if (publicKey == null) {
            publicKey = this.publicKey = fetchPublicKey();
        }
        return publicKey;
    }

    private PublicKey fetchPublicKey() throws EcovacsApiException, InterruptedException {
        Map<String, String> params = getBaseLoginRequestParameters();
        params.put("keys", "PUBLIC.KEY.CONFIG");

        Request request = createAuthRequest(EcovacsApiUrlFactory.getPrivateApiUrl("common/getConfig", configuration),
                configuration.getClientKey(), configuration.getClientSecret(), params);
        ContentResponse response = executeRequest(request);
        Type responseType = new TypeToken<ResponseWrapper<List<ConfigEntry>>>() {
        }.getType();
        List<ConfigEntry> entries = handleResponseWrapper(gson.fromJson(response.getContentAsString(), responseType));
        if (entries.isEmpty()) {
            throw new EcovacsApiException("No config entries received from get config response");
        }
        ConfigEntry entry = entries.getFirst();
        final String base64EncodedKey;
        try {
            JsonObject wrapperObject = gson.fromJson(entry.getValue(), JsonObject.class);
            JsonElement publicKeyElement = wrapperObject != null ? wrapperObject.get("publicKey") : null;
            base64EncodedKey = publicKeyElement != null ? publicKeyElement.getAsString() : null;
        } catch (JsonSyntaxException e) {
            throw new EcovacsApiException("Failed to parse get config response", e);
        }
        if (base64EncodedKey == null) {
            throw new EcovacsApiException("Public key missing from get config response");
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64EncodedKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // or "EC", "Ed25519", etc.
            PublicKey key = keyFactory.generatePublic(keySpec);
            if (key == null) {
                throw new EcovacsApiException("Failed to parse public key");
            }
            return key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EcovacsApiException("Failed to parse public key", e);
        }
    }

    private AccessData verifyDeviceAndLogin(String verificationCode) throws EcovacsApiException, InterruptedException {
        String encryptedAccount = encryptAccount();
        Map<String, String> params = getBaseLoginRequestParameters();
        params.put("encryptAccount", encryptedAccount);
        params.put("backUpEmail", "");
        params.put("verifyCode", verificationCode.trim());
        params.put("model", configuration.getAppModel());
        params.put("system", configuration.getAppSystem());

        Request request = createAuthRequest(EcovacsApiUrlFactory.getPrivateApiUrl("user/verifyDevice", configuration),
                configuration.getClientKey(), configuration.getClientSecret(), params);
        ContentResponse response = executeRequest(request);

        Type responseType = new TypeToken<ResponseWrapper<AccessData>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(response.getContentAsString(), responseType));
    }

    private AuthCode getAuthCode(String uid, String token) throws EcovacsApiException, InterruptedException {
        HashMap<String, String> authCodeParameters = new HashMap<>();
        authCodeParameters.put("uid", uid);
        authCodeParameters.put("accessToken", token);
        authCodeParameters.put("bizType", configuration.getBizType());
        authCodeParameters.put("deviceId", configuration.getDeviceId());
        authCodeParameters.put("openId", configuration.getAuthOpenId());
        authCodeParameters.put("authTimespan", String.valueOf(System.currentTimeMillis()));

        Request authCodeRequest = createAuthRequest(EcovacsApiUrlFactory.getAuthUrl(configuration),
                configuration.getAuthClientKey(), configuration.getAuthClientSecret(), authCodeParameters);
        ContentResponse authCodeResponse = executeRequest(authCodeRequest);
        Type responseType = new TypeToken<ResponseWrapper<AuthCode>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(authCodeResponse.getContentAsString(), responseType));
    }

    private Credentials portalLogin(AuthCode authCode, String uid) throws EcovacsApiException, InterruptedException {
        PortalLoginRequest loginRequestData = new PortalLoginRequest(PortalTodo.LOGIN_BY_TOKEN,
                configuration.getCountry().toUpperCase(), "", configuration.getOrg(), configuration.getDeviceId(),
                configuration.getRealm(), authCode.getAuthCode(), uid, configuration.getEdition());
        long now = System.currentTimeMillis();
        String userUrl = EcovacsApiUrlFactory.getPortalUsersUrl(configuration);
        ContentResponse portalLoginResponse = executeRequest(createJsonRequest(userUrl, loginRequestData));
        PortalLoginResponse response = handleResponse(portalLoginResponse, PortalLoginResponse.class);
        if (!response.wasSuccessful()) {
            throw new EcovacsApiException("Login failed");
        }
        return new Credentials(response.getUserId(), response.getResource(), response.getToken(),
                now + response.getValidityDurationMs());
    }

    @Override
    public List<EcovacsDevice> getDevices() throws EcovacsApiException, InterruptedException {
        Map<String, DeviceDescription> descriptions = getSupportedDeviceDescs();
        List<IotProduct> products = null;
        List<EcovacsDevice> devices = new ArrayList<>();
        for (Device dev : getDeviceList()) {
            Optional<DeviceDescription> descOpt = Optional.ofNullable(descriptions.get(dev.getDeviceClass()));
            if (descOpt.isEmpty()) {
                if (products == null) {
                    products = getIotProductMap();
                }
                String modelName = products.stream().filter(prod -> dev.getDeviceClass().equals(prod.getClassId()))
                        .findFirst().map(p -> p.getDefinition().name).orElse("UNKNOWN");
                logger.info("Found unsupported device {} (class {}, company {}), ignoring.", modelName,
                        dev.getDeviceClass(), dev.getCompany());
                continue;
            }
            DeviceDescription desc = descOpt.get();
            if (desc.usesMqtt) {
                devices.add(new EcovacsIotMqDevice(dev, desc, this, gson));
            } else {
                devices.add(new EcovacsXmppDevice(dev, desc, this, gson));
            }
        }
        return devices;
    }

    // maps device class -> device description
    private Map<String, DeviceDescription> getSupportedDeviceDescs() {
        Map<String, DeviceDescription> descs = new HashMap<>();
        ClassLoader cl = Objects.requireNonNull(getClass().getClassLoader());
        try (Reader reader = new InputStreamReader(cl.getResourceAsStream("devices/supported_device_list.json"))) {
            for (DeviceDescription desc : loadSupportedDeviceData(reader)) {
                descs.put(desc.deviceClass, desc);
            }
            logger.trace("Loaded {} built-in device descriptions", descs.size());
        } catch (IOException | JsonSyntaxException e) {
            logger.warn("Failed loading built-in device descriptions", e);
        }

        Path customDescsPath = Paths.get(OpenHAB.getUserDataFolder(), "ecovacs").resolve("custom_device_descs.json");
        if (Files.exists(customDescsPath)) {
            try (Reader reader = Files.newBufferedReader(customDescsPath)) {
                int builtins = descs.size();
                for (DeviceDescription desc : loadSupportedDeviceData(reader)) {
                    DeviceDescription builtinDesc = descs.put(desc.deviceClass, desc);
                    if (builtinDesc != null) {
                        logger.trace("Overriding built-in description for {} with custom description",
                                desc.deviceClass);
                    }
                }
                logger.trace("Loaded {} custom device descriptions", descs.size() - builtins);
            } catch (IOException | JsonSyntaxException e) {
                logger.warn("Failed loading custom device descriptions from {}", customDescsPath, e);
            }
        }

        descs.entrySet().forEach(descEntry -> {
            DeviceDescription desc = descEntry.getValue();
            if (desc.deviceClassLink != null) {
                Optional<DeviceDescription> linkedDescOpt = Optional.ofNullable(descs.get(desc.deviceClassLink));
                if (linkedDescOpt.isEmpty()) {
                    logger.warn("Device description {} links unknown description {}", desc.deviceClass,
                            desc.deviceClassLink);
                }
                desc = desc.resolveLinkWith(linkedDescOpt.get());
                descEntry.setValue(desc);
            }
            desc.addImplicitCapabilities();
        });

        return descs;
    }

    private List<DeviceDescription> loadSupportedDeviceData(Reader input) throws IOException {
        JsonReader reader = new JsonReader(input);
        Type type = new TypeToken<List<DeviceDescription>>() {
        }.getType();
        return gson.fromJson(reader, type);
    }

    private List<Device> getDeviceList() throws EcovacsApiException, InterruptedException {
        PortalAuthRequest data = new PortalAuthRequest(PortalTodo.GET_DEVICE_LIST, createAuthData(this.credentials));
        String userUrl = EcovacsApiUrlFactory.getPortalUsersUrl(configuration);
        ContentResponse deviceResponse = executeRequest(createJsonRequest(userUrl, data));
        logger.trace("Got device list response {}", deviceResponse.getContentAsString());
        List<Device> devices = handleResponse(deviceResponse, PortalDeviceResponse.class).getDevices();
        return devices != null ? devices : Collections.emptyList();
    }

    private List<IotProduct> getIotProductMap() throws EcovacsApiException, InterruptedException {
        PortalIotProductRequest data = new PortalIotProductRequest(createAuthData(this.credentials));
        String url = EcovacsApiUrlFactory.getPortalProductIotMapUrl(configuration);
        ContentResponse productResponse = executeRequest(createJsonRequest(url, data));
        logger.trace("Got product list response {}", productResponse.getContentAsString());
        List<IotProduct> products = handleResponse(productResponse, PortalIotProductResponse.class).getProducts();
        return products != null ? products : Collections.emptyList();
    }

    public <T> T sendIotCommand(Device device, DeviceDescription desc, IotDeviceCommand<T> command)
            throws EcovacsApiException, InterruptedException {
        String commandName = command.getName(desc.protoVersion);
        final Object payload;
        try {
            if (desc.protoVersion == ProtocolVersion.XML) {
                payload = command.getXmlPayload(null);
                logger.trace("{}: Sending IOT command {} with payload {}", device.getName(), commandName, payload);
            } else {
                payload = command.getJsonPayload(desc.protoVersion, gson);
                logger.trace("{}: Sending IOT command {} with payload {}", device.getName(), commandName,
                        gson.toJson(payload));
            }
        } catch (ParserConfigurationException | TransformerException e) {
            logger.debug("Could not convert payload for {}", command, e);
            throw new EcovacsApiException(e);
        }

        PortalIotCommandRequest data = new PortalIotCommandRequest(createAuthData(this.credentials), commandName,
                payload, device.getDid(), device.getResource(), device.getDeviceClass(),
                desc.protoVersion != ProtocolVersion.XML);
        String url = EcovacsApiUrlFactory.getPortalIotDeviceManagerUrl(configuration);
        ContentResponse response = executeRequest(createJsonRequest(url, data));

        final AbstractPortalIotCommandResponse commandResponse;
        if (desc.protoVersion == ProtocolVersion.XML) {
            commandResponse = handleResponse(response, PortalIotCommandXmlResponse.class);
            logger.trace("{}: Got response payload {}", device.getName(),
                    ((PortalIotCommandXmlResponse) commandResponse).getResponsePayloadXml());
        } else {
            commandResponse = handleResponse(response, PortalIotCommandJsonResponse.class);
            logger.trace("{}: Got response payload {}", device.getName(),
                    ((PortalIotCommandJsonResponse) commandResponse).response);
        }
        if (!commandResponse.wasSuccessful()) {
            final String msg = "Sending IOT command " + commandName + " failed: " + commandResponse.getErrorMessage();
            throw new EcovacsApiException(msg, commandResponse.failedDueToAuthProblem());
        }
        try {
            return command.convertResponse(commandResponse, desc.protoVersion, gson);
        } catch (Exception e) {
            logger.debug("Converting response for command {} failed", command, e);
            throw new EcovacsApiException(e);
        }
    }

    public List<PortalCleanLogRecord> fetchCleanLogs(Device device) throws EcovacsApiException, InterruptedException {
        PortalCleanLogsRequest data = new PortalCleanLogsRequest(createAuthData(this.credentials), device.getDid(),
                device.getResource());
        String url = EcovacsApiUrlFactory.getPortalLogUrl(configuration);
        ContentResponse response = executeRequest(createJsonRequest(url, data));
        PortalCleanLogsResponse responseObj = handleResponse(response, PortalCleanLogsResponse.class);
        if (!responseObj.wasSuccessful()) {
            throw new EcovacsApiException("Fetching clean logs failed");
        }
        logger.trace("{}: Fetching cleaning logs yields {} records", device.getName(), responseObj.records.size());
        return responseObj.records;
    }

    public List<PortalCleanLogRecord> fetchCleanResultsLog(Device device)
            throws EcovacsApiException, InterruptedException {
        String url = EcovacsApiUrlFactory.getPortalCleanResultsLogUrl(configuration);
        Request request = createSignedAppRequest(url).param("auth", gson.toJson(createAuthData(this.credentials))) //
                .param("channel", configuration.getChannel()) //
                .param("did", device.getDid()) //
                .param("defaultLang", "EN") //
                .param("logType", "clean") //
                .param("res", device.getResource()) //
                .param("size", "20") //
                .param("version", "v2");

        ContentResponse response = executeRequest(request);
        PortalCleanResultsResponse responseObj = handleResponse(response, PortalCleanResultsResponse.class);
        if (!responseObj.wasSuccessful()) {
            throw new EcovacsApiException("Fetching clean results failed");
        }
        logger.trace("{}: Fetching cleaning results yields {} records", device.getName(), responseObj.records.size());
        return responseObj.records;
    }

    public Optional<byte[]> downloadCleanMapImage(Device device, String url, boolean useSigning)
            throws EcovacsApiException, InterruptedException {
        Request request = useSigning ? createSignedAppRequest(url) : httpClient.newRequest(url).method(HttpMethod.GET);
        ContentResponse response = executeRequest(request);
        if ("application/json".equals(response.getMediaType())) {
            logger.warn("{}: Could not download map image {}: {}", device.getName(), url,
                    response.getContentAsString());
            return Optional.empty();
        }
        return Optional.of(response.getContent());
    }

    private PortalAuthRequestParameter createAuthData(@Nullable Credentials creds) {
        if (creds == null) {
            throw new IllegalStateException("Not logged in");
        }
        return new PortalAuthRequestParameter(configuration.getPortalAuthRequestWith(), creds.userId(),
                configuration.getRealm(), creds.token(), configuration.getDeviceId());
    }

    private <T> T handleResponseWrapper(@Nullable ResponseWrapper<T> response) throws EcovacsApiException {
        if (response == null) {
            // should not happen in practice
            throw new EcovacsApiException("No response received");
        }
        if (!response.isSuccess()) {
            throw new EcovacsApiErrorResponseException(response);
        }
        return response.getData();
    }

    private <T> T handleResponse(ContentResponse response, Class<T> clazz) throws EcovacsApiException {
        try {
            @Nullable
            T respObject = gson.fromJson(response.getContentAsString(), clazz);
            if (respObject == null) {
                // should not happen in practice
                throw new EcovacsApiException("No response received");
            }
            return respObject;
        } catch (JsonSyntaxException e) {
            throw new EcovacsApiException("Failed to parse response '" + response.getContentAsString()
                    + "' as data class " + clazz.getSimpleName(), e);
        }
    }

    private Request createAuthRequest(String url, String clientKey, String clientSecret,
            Map<String, String> requestSpecificParameters) {
        HashMap<String, String> signedRequestParameters = new HashMap<>(requestSpecificParameters);
        StringBuilder signOnText = new StringBuilder(clientKey);
        signedRequestParameters.keySet().stream().sorted().forEach(key -> {
            signOnText.append(key).append("=").append(signedRequestParameters.get(key));
        });
        signOnText.append(clientSecret);

        signedRequestParameters.put("authAppkey", clientKey);
        signedRequestParameters.put("authSign", HashUtil.getMD5Hash(signOnText.toString()));

        Request request = httpClient.newRequest(url).method(HttpMethod.GET);
        signedRequestParameters.forEach(request::param);

        return request;
    }

    private Request createSignedAppRequest(String url) {
        String timestamp = Long.toString(System.currentTimeMillis());
        String signContent = configuration.getAppId() + configuration.getAppKey() + timestamp;
        Credentials creds = this.credentials;
        if (creds == null) {
            throw new IllegalStateException("Not logged in");
        }
        return httpClient.newRequest(url).method(HttpMethod.GET) //
                .header("Authorization", "Bearer " + creds.token()) //
                .header("token", creds.token()) //
                .header("appid", configuration.getAppId()) //
                .header("plat", configuration.getAppPlatform()) //
                .header("userid", creds.userId()) //
                .header("user-agent", configuration.getAppUserAgent()) //
                .header("v", configuration.getAppVersion()) //
                .header("country", configuration.getCountry()) //
                .header("sign", HashUtil.getSHA256Hash(signContent)) //
                .header("signType", "sha256") //
                .param("et1", timestamp);
    }

    private Request createJsonRequest(String url, Object data) {
        return httpClient.newRequest(url).method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/json")
                .content(new StringContentProvider(gson.toJson(data)));
    }

    private ContentResponse executeRequest(Request request) throws EcovacsApiException, InterruptedException {
        request.timeout(10, TimeUnit.SECONDS);
        try {
            ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new EcovacsApiException(response);
            }
            return response;
        } catch (TimeoutException | ExecutionException e) {
            throw new EcovacsApiException(e);
        }
    }

    static class MqttSubscriptionHandle {
        private final MqttConnection mqttConnection;
        private final Device device;

        private MqttSubscriptionHandle(MqttConnection mqttConnection, Device device) {
            this.mqttConnection = mqttConnection;
            this.device = device;
        }

        public void unsubscribe() throws EcovacsApiException, InterruptedException {
            mqttConnection.unsubscribe(device);
        }
    }
}
