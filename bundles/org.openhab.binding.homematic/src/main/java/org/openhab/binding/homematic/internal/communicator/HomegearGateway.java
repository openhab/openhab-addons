/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;

/**
 * HomematicGateway implementation for Homegear.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomegearGateway extends AbstractHomematicGateway {

    protected HomegearGateway(String id, HomematicConfig config, HomematicGatewayAdapter gatewayAdapter,
            HttpClient httpClient) {
        super(id, config, gatewayAdapter, httpClient);
    }

    @Override
    protected void loadVariables(HmChannel channel) throws IOException {
        getRpcClient(getDefaultInterface()).getAllSystemVariables(channel);
    }

    @Override
    protected void loadScripts(HmChannel channel) throws IOException {
        getRpcClient(getDefaultInterface()).getAllScripts(channel);
    }

    @Override
    protected void setVariable(HmDatapoint dp, Object value) throws IOException {
        getRpcClient(getDefaultInterface()).setSystemVariable(dp, value);
    }

    @Override
    protected void executeScript(HmDatapoint dp) throws IOException {
        getRpcClient(getDefaultInterface()).executeScript(dp);
    }

    @Override
    protected void loadDeviceNames(Collection<HmDevice> devices) throws IOException {
        getRpcClient(getDefaultInterface()).loadDeviceNames(getDefaultInterface(), devices);
    }
}
