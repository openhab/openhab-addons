/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Default HomematicGateway implementation for RF and HS485 daemons.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DefaultGateway extends AbstractHomematicGateway {

    protected DefaultGateway(String id, HomematicConfig config, HomematicGatewayAdapter gatewayAdapter,
            HttpClient httpClient) {
        super(id, config, gatewayAdapter, httpClient);
    }

    @Override
    protected void loadVariables(HmChannel channel) throws IOException {
        // not supported
    }

    @Override
    protected void loadScripts(HmChannel channel) throws IOException {
        // not supported
    }

    @Override
    protected void setVariable(HmDatapoint dp, Object value) throws IOException {
        // not supported
    }

    @Override
    protected void executeScript(HmDatapoint dp) throws IOException {
        // not supported
    }

    @Override
    protected void loadDeviceNames(Collection<HmDevice> devices) throws IOException {
        // not supported
    }

}
