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
package org.openhab.binding.homematic.test.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.client.RpcClient;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;

/**
 * @author Florian Stolte - Initial contribution
 */
@NonNullByDefault
public class RpcClientMockImpl extends RpcClient<String> {

    public static final String GET_PARAMSET_DESCRIPTION_NAME = "getParamsetDescription";
    public static final String GET_PARAMSET_NAME = "getParamset";

    public Map<String, Integer> numberOfCalls = new HashMap<>();

    public RpcClientMockImpl() throws IOException {
        this(new HomematicConfig());
    }

    public RpcClientMockImpl(HomematicConfig config) throws IOException {
        super(config);

        Arrays.asList(GET_PARAMSET_DESCRIPTION_NAME, GET_PARAMSET_NAME).forEach(method -> numberOfCalls.put(method, 0));
    }

    @Override
    protected Object[] sendMessage(int port, RpcRequest<String> request) throws IOException {
        String methodName = Objects.requireNonNull(request.getMethodName());

        increaseNumberOfCalls(methodName);

        return mockResponse();
    }

    private void increaseNumberOfCalls(String methodName) {
        Integer currentNumber = numberOfCalls.get(methodName);
        if (currentNumber == null) {
            numberOfCalls.put(methodName, 1);
        } else {
            numberOfCalls.put(methodName, currentNumber + 1);
        }
    }

    private Object[] mockResponse() {
        Object[] response = new Object[1];
        response[0] = new HashMap<>();
        return response;
    }

    @Override
    protected RpcRequest<String> createRpcRequest(String methodName) {
        return new RpcRequest<>() {

            @Override
            public void addArg(Object arg) {
            }

            @Override
            public String createMessage() {
                return "MockMessage";
            }

            @Override
            public @Nullable String getMethodName() {
                return methodName;
            }
        };
    }

    @Override
    public void dispose() {
    }

    @Override
    protected String getRpcCallbackUrl() {
        return "mock://callback";
    }
}
