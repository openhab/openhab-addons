/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.client;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.sungrow.internal.SungrowBindingConstants;
import org.openhab.binding.sungrow.internal.client.dto.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * @author Christian Kemper - Initial contribution
 */
public class SungrowClient {

    private final String appKey;
    private final String secretKey;
    private final URI uri;
    private final Gson gson;
    private final HttpClient commonHttpClient;
    private LoginResponse loginResponse;
    private EncryptionUtility encryptionUtility;
    private LocalDateTime lastAPICall;

    SungrowClient(HttpClient commonHttpClient, URI uri, String appKey, String secretKey) {
        Objects.requireNonNull(uri, "URI cannot be null");
        Objects.requireNonNull(appKey, "App key cannot be null");
        Objects.requireNonNull(secretKey, "Secret key cannot be null");
        this.commonHttpClient = commonHttpClient;
        this.uri = uri;
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.gson = new GsonBuilder().create();
    }

    public void activateEncryption(String rsaPublicKey, String password) {
        encryptionUtility = new EncryptionUtility(rsaPublicKey, password);
    }

    public void login(String username, String password) throws IOException {
        try {
            Login login = new Login(username, password, appKey);

            String json;
            if (encryptionUtility != null) {
                login.setApiKey(encryptionUtility.createApiKeyParameter());
                json = encryptionUtility.encrypt(gson.toJson(login));
            } else {
                json = gson.toJson(login);
            }

            Request request = commonHttpClient.POST(uri.resolve("/openapi/login"))
                    .timeout(SungrowBindingConstants.TIMEOUT, TimeUnit.SECONDS);
            addDefaultHeaders(request);

            ContentResponse contentResponse = request.send();
            String body = contentResponse.getContentAsString();

            if (contentResponse.getStatus() >= 200 && contentResponse.getStatus() < 500 && encryptionUtility != null) {
                body = encryptionUtility.decrypt(body);
            }
            if (contentResponse.getStatus() == 200) {
                LoginResponse loginResponse = gson.fromJson(body, LoginResponse.class);
                if (loginResponse.isSuccess()) {
                    LoginResponse.LoginResult loginResult = loginResponse.getData();
                    if (loginResult.getLoginState().equals(LoginState.SUCCESS)) {
                        this.loginResponse = loginResponse;
                        apiCallSuccess();
                    } else {
                        throw new IOException(
                                "Login error " + loginResult.getLoginState() + ", Message:" + loginResult.getMessage());
                    }
                } else {
                    throw new IOException("Login error: '" + body + "'");
                }
            } else {
                throw new IOException("Login failed. ResponseCode " + contentResponse.getStatus() + ": '" + body + "'");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException("Error during login.", e);
        }
    }

    private void addDefaultHeaders(Request request) {
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.header("x-access-key", secretKey);
        request.header("sys_code", "901");
        if (encryptionUtility != null) {
            request.header("x-random-secret-key", encryptionUtility.createRandomPublicKey());
        }
    }

    private void apiCallSuccess() {
        lastAPICall = LocalDateTime.now();
    }

    public void execute(APIOperation operation) throws IOException {
        if (operation.getMethod() != APIOperation.Method.POST) {
            throw new IOException("Method not supported: " + operation.getMethod());
        }
        String jsonResponse = null;
        try {
            BaseRequest baseRequest = operation.getRequest();
            baseRequest.setAppKey(appKey);
            baseRequest.setToken(loginResponse.getData().getToken());

            String jsonRequest;
            if (encryptionUtility != null) {
                baseRequest.setApiKey(encryptionUtility.createApiKeyParameter());
                jsonRequest = encryptionUtility.encrypt(gson.toJson(baseRequest));
            } else {
                jsonRequest = gson.toJson(baseRequest);
            }

            Request request = commonHttpClient.POST(uri.resolve(operation.getPath()))
                    .timeout(SungrowBindingConstants.TIMEOUT, TimeUnit.SECONDS);
            addDefaultHeaders(request);

            ContentResponse contentResponse = request.send();
            jsonResponse = contentResponse.getContentAsString();

            if (contentResponse.getStatus() >= 200 && contentResponse.getStatus() < 500 && encryptionUtility != null) {
                jsonResponse = encryptionUtility.decrypt(jsonResponse);
            }

            if (contentResponse.getStatus() == 200) {
                Type baseResponseType = getResponseType(operation);
                BaseResponse<?> baseResponse = gson.fromJson(jsonResponse, baseResponseType);

                if ("1".equals(baseResponse.getErrorCode())) {
                    apiCallSuccess();
                    operation.setResponse(baseResponse.getData());
                } else {
                    throw new IOException("Operation error: '" + jsonResponse + "'");
                }
            } else {
                throw new IOException(
                        "Operation failed. ResponseCode " + contentResponse.getStatus() + ": '" + jsonResponse + "'");
            }
        } catch (InterruptedException | NumberFormatException e) {
            throw new RuntimeException("Unable to execute Operation. Json from server: '" + jsonResponse + "'.", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Type getResponseType(Object operation) {
        Type[] genericTypes;

        if (operation.getClass().getGenericSuperclass() instanceof ParameterizedType parameterizedType) {
            genericTypes = parameterizedType.getActualTypeArguments();
        } else if (operation.getClass().getGenericInterfaces().length > 0
                && operation.getClass().getGenericInterfaces()[0] instanceof ParameterizedType parameterizedType) {
            genericTypes = parameterizedType.getActualTypeArguments();
        } else {
            throw new IllegalArgumentException(
                    "Class not implementing an generic interface or extends a generic base class.");
        }
        Type resType = genericTypes[1];
        return TypeToken.getParameterized(BaseResponse.class, resType).getType();
    }
}
