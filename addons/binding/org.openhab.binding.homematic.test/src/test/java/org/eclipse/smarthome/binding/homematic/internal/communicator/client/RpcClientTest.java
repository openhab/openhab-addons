/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.homematic.internal.communicator.client;

import static org.eclipse.smarthome.binding.homematic.HomematicBindingConstants.RX_BURST_MODE;
import static org.eclipse.smarthome.binding.homematic.HomematicBindingConstants.RX_WAKEUP_MODE;
import static org.eclipse.smarthome.binding.homematic.test.util.DimmerHelper.createDimmerDummyChannel;
import static org.eclipse.smarthome.binding.homematic.test.util.DimmerHelper.createDimmerHmChannel;
import static org.eclipse.smarthome.binding.homematic.test.util.RpcClientMockImpl.GET_PARAMSET_DESCRIPTION_NAME;
import static org.eclipse.smarthome.binding.homematic.test.util.RpcClientMockImpl.GET_PARAMSET_NAME;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.eclipse.smarthome.binding.homematic.internal.communicator.message.RpcRequest;
import org.eclipse.smarthome.binding.homematic.internal.communicator.message.XmlRpcRequest;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.test.util.RpcClientMockImpl;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.Test;

public class RpcClientTest extends JavaTest {

    private RpcClientMockImpl rpcClient;

    @Before
    public void setup() throws IOException {
        this.rpcClient = new RpcClientMockImpl();
    }

    @Test
    public void valuesParamsetDescriptionIsLoadedForChannel() throws IOException {
        HmChannel channel = createDimmerHmChannel();

        rpcClient.addChannelDatapoints(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_DESCRIPTION_NAME), is(1));
    }

    @Test
    public void masterParamsetDescriptionIsLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.addChannelDatapoints(channel, HmParamsetType.MASTER);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_DESCRIPTION_NAME), is(1));
    }

    @Test
    public void valuesParamsetDescriptionIsNotLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.addChannelDatapoints(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_DESCRIPTION_NAME), is(0));
    }

    @Test
    public void valuesParamsetIsLoadedForChannel() throws IOException {
        HmChannel channel = createDimmerHmChannel();

        rpcClient.setChannelDatapointValues(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_NAME), is(1));
    }

    @Test
    public void masterParamsetIsLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.setChannelDatapointValues(channel, HmParamsetType.MASTER);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_NAME), is(1));
    }

    @Test
    public void valuesParamsetIsNotLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.setChannelDatapointValues(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_NAME), is(0));
    }

    @Test
    public void burstRxModeIsConfiguredAsParameterOnRequest() throws IOException {
        RpcRequest<String> request = new XmlRpcRequest("setValue");

        rpcClient.configureRxMode(request, RX_BURST_MODE);

        assertThat(request.createMessage(), containsString(String.format("<value>%s</value>", RX_BURST_MODE)));
    }

    @Test
    public void wakeupRxModeIsConfiguredAsParameterOnRequest() throws IOException {
        RpcRequest<String> request = new XmlRpcRequest("setValue");

        rpcClient.configureRxMode(request, RX_WAKEUP_MODE);

        assertThat(request.createMessage(), containsString(String.format("<value>%s</value>", RX_WAKEUP_MODE)));
    }

    @Test
    public void rxModeIsNotConfiguredAsParameterOnRequestForNull() throws IOException {
        RpcRequest<String> request = new XmlRpcRequest("setValue");

        rpcClient.configureRxMode(request, null);

        assertThat(request.createMessage(), not(containsString("<value>")));
    }

    @Test
    public void rxModeIsNotConfiguredAsParameterOnRequestForInvalidString() throws IOException {
        RpcRequest<String> request = new XmlRpcRequest("setValue");

        rpcClient.configureRxMode(request, "SUPER_RX_MODE");

        assertThat(request.createMessage(), not(containsString("<value>")));
    }
}
