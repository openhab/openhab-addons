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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.main.ResponseWrapper;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.Device;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.IotProduct;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalCleanLogsResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalDeviceResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotProductResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalLoginResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.MD5Util;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
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

    private final EcovacsApiConfiguration configuration;
    private @Nullable PortalLoginResponse loginData;

    public EcovacsApiImpl(HttpClient httpClient, EcovacsApiConfiguration configuration) {
        this.httpClient = httpClient;
        this.configuration = configuration;
    }

    @Override
    public void loginAndGetAccessToken() throws EcovacsApiException, InterruptedException {
        loginData = null;

        AccessData accessData = login();
        AuthCode authCode = getAuthCode(accessData);
        loginData = portalLogin(authCode, accessData);
    }

    EcovacsApiConfiguration getConfig() {
        return configuration;
    }

    @Nullable
    PortalLoginResponse getLoginData() {
        return loginData;
    }

    private AccessData login() throws EcovacsApiException, InterruptedException {
        HashMap<String, String> loginParameters = new HashMap<>();
        loginParameters.put("account", configuration.getUsername());
        loginParameters.put("password", MD5Util.getMD5Hash(configuration.getPassword()));
        loginParameters.put("requestId", MD5Util.getMD5Hash(String.valueOf(System.currentTimeMillis())));
        loginParameters.put("authTimeZone", configuration.getTimeZone());
        loginParameters.put("country", configuration.getCountry());
        loginParameters.put("lang", configuration.getLanguage());
        loginParameters.put("deviceId", configuration.getDeviceId());
        loginParameters.put("appCode", configuration.getAppCode());
        loginParameters.put("appVersion", configuration.getAppVersion());
        loginParameters.put("channel", configuration.getChannel());
        loginParameters.put("deviceType", configuration.getDeviceType());

        Request loginRequest = createAuthRequest(EcovacsApiUrlFactory.getLoginUrl(configuration),
                configuration.getClientKey(), configuration.getClientSecret(), loginParameters);
        ContentResponse loginResponse = executeRequest(loginRequest);
        Type responseType = new TypeToken<ResponseWrapper<AccessData>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(loginResponse.getContentAsString(), responseType));
    }

    private AuthCode getAuthCode(AccessData accessData) throws EcovacsApiException, InterruptedException {
        HashMap<String, String> authCodeParameters = new HashMap<>();
        authCodeParameters.put("uid", accessData.getUid());
        authCodeParameters.put("accessToken", accessData.getAccessToken());
        authCodeParameters.put("bizType", configuration.getBizType());
        authCodeParameters.put("deviceId", configuration.getDeviceId());
        authCodeParameters.put("openId", configuration.getAuthOpenId());

        Request authCodeRequest = createAuthRequest(EcovacsApiUrlFactory.getAuthUrl(configuration),
                configuration.getAuthClientKey(), configuration.getAuthClientSecret(), authCodeParameters);
        ContentResponse authCodeResponse = executeRequest(authCodeRequest);
        Type responseType = new TypeToken<ResponseWrapper<AuthCode>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(authCodeResponse.getContentAsString(), responseType));
    }

    private PortalLoginResponse portalLogin(AuthCode authCode, AccessData accessData)
            throws EcovacsApiException, InterruptedException {
        PortalLoginRequest loginRequestData = new PortalLoginRequest(PortalTodo.LOGIN_BY_TOKEN,
                configuration.getCountry().toUpperCase(), "", configuration.getOrg(), configuration.getResource(),
                configuration.getRealm(), authCode.getAuthCode(), accessData.getUid(), configuration.getEdition());
        String userUrl = EcovacsApiUrlFactory.getPortalUsersUrl(configuration);
        ContentResponse portalLoginResponse = executeRequest(createJsonRequest(userUrl, loginRequestData));
        PortalLoginResponse response = handleResponse(portalLoginResponse, PortalLoginResponse.class);
        if (!response.wasSuccessful()) {
            throw new EcovacsApiException("Login failed");
        }
        return response;
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
        PortalAuthRequest data = new PortalAuthRequest(PortalTodo.GET_DEVICE_LIST, createAuthData());
        String userUrl = EcovacsApiUrlFactory.getPortalUsersUrl(configuration);
        ContentResponse deviceResponse = executeRequest(createJsonRequest(userUrl, data));
        logger.trace("Got device list response {}", deviceResponse.getContentAsString());
        List<Device> devices = handleResponse(deviceResponse, PortalDeviceResponse.class).getDevices();
        return devices != null ? devices : Collections.emptyList();
    }

    private List<IotProduct> getIotProductMap() throws EcovacsApiException, InterruptedException {
        PortalIotProductRequest data = new PortalIotProductRequest(createAuthData());
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

        PortalIotCommandRequest data = new PortalIotCommandRequest(createAuthData(), commandName, payload,
                device.getDid(), device.getResource(), device.getDeviceClass(),
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
        } catch (DataParsingException e) {
            logger.debug("Converting response for command {} failed", command, e);
            throw new EcovacsApiException(e);
        }
    }

    public List<PortalCleanLogsResponse.LogRecord> fetchCleanLogs(Device device)
            throws EcovacsApiException, InterruptedException {
        PortalCleanLogsRequest data = new PortalCleanLogsRequest(createAuthData(), device.getDid(),
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

    private PortalAuthRequestParameter createAuthData() {
        PortalLoginResponse loginData = this.loginData;
        if (loginData == null) {
            throw new IllegalStateException("Not logged in");
        }
        return new PortalAuthRequestParameter(configuration.getPortalAUthRequestWith(), loginData.getUserId(),
                configuration.getRealm(), loginData.getToken(), configuration.getResource());
    }

    private <T> T handleResponseWrapper(@Nullable ResponseWrapper<T> response) throws EcovacsApiException {
        if (response == null) {
            // should not happen in practice
            throw new EcovacsApiException("No response received");
        }
        if (!response.isSuccess()) {
            throw new EcovacsApiException("API call failed: " + response.getMessage() + ", code " + response.getCode());
        }
        return response.getData();
    }

    private <T> T handleResponse(ContentResponse response, Class<T> clazz) throws EcovacsApiException {
        @Nullable
        T respObject = gson.fromJson(response.getContentAsString(), clazz);
        if (respObject == null) {
            // should not happen in practice
            throw new EcovacsApiException("No response received");
        }
        return respObject;
    }

    private Request createAuthRequest(String url, String clientKey, String clientSecret,
            Map<String, String> requestSpecificParameters) {
        HashMap<String, String> signedRequestParameters = new HashMap<>(requestSpecificParameters);
        signedRequestParameters.put("authTimespan", String.valueOf(System.currentTimeMillis()));

        StringBuilder signOnText = new StringBuilder(clientKey);
        signedRequestParameters.keySet().stream().sorted().forEach(key -> {
            signOnText.append(key).append("=").append(signedRequestParameters.get(key));
        });
        signOnText.append(clientSecret);

        signedRequestParameters.put("authAppkey", clientKey);
        signedRequestParameters.put("authSign", MD5Util.getMD5Hash(signOnText.toString()));

        Request request = httpClient.newRequest(url).method(HttpMethod.GET);
        signedRequestParameters.forEach(request::param);

        return request;
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
}
