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
package org.openhab.binding.ojelectronics.internal.services;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.ojelectronics.internal.common.OJGSonBuilder;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsBridgeConfiguration;
import org.openhab.binding.ojelectronics.internal.models.RequestModelBase;
import org.openhab.binding.ojelectronics.internal.models.userprofile.PostSignInQueryModel;
import org.openhab.binding.ojelectronics.internal.models.userprofile.PostSignInResponseModel;

import com.google.gson.Gson;

/**
 * Handles the sign in process.
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class SignInService {

    private final Gson gson = OJGSonBuilder.getGSon();

    private final HttpClient httpClient;
    private final OJElectronicsBridgeConfiguration config;

    /**
     * Creates a new instance of {@link SignInService}
     *
     * @param config configuration {@link OJElectronicsBridgeConfiguration}
     * @param httpClient HTTP client
     */
    public SignInService(OJElectronicsBridgeConfiguration config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    /**
     * Signing in
     *
     * @param signInDone This method is called if sign in process was successful.
     * @param connectionLosed This method is called if no connection could established.
     * @param unauthorized This method is called if the result is unauthorized.
     */
    public void signIn(Consumer<String> signInDone, Runnable connectionLosed, Runnable unauthorized) {
        Request request = httpClient.POST(config.apiUrl + "/UserProfile/SignIn")
                .header(HttpHeader.CONTENT_TYPE, "application/json")
                .content(new StringContentProvider(gson.toJson(getPostSignInQueryModel())));

        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                if (result == null || result.isFailed()) {
                    connectionLosed.run();
                    return;
                }
                if (result.getResponse().getStatus() == 200) {
                    PostSignInResponseModel signInModel = gson.fromJson(getContentAsString(),
                            PostSignInResponseModel.class);
                    if (signInModel == null || signInModel.errorCode != 0 || signInModel.sessionId.equals("")) {
                        unauthorized.run();
                        return;
                    }
                    signInDone.accept(signInModel.sessionId);
                    return;
                }

                connectionLosed.run();
                return;
            }
        });
    }

    private RequestModelBase getPostSignInQueryModel() {
        return new PostSignInQueryModel().withClientSWVersion(config.softwareVersion).withCustomerId(config.customerId)
                .withUserName(config.userName).withPassword(config.password).withApiKey(config.apiKey);
    }
}
