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
package org.openhab.binding.easee.internal.command.circuit;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.AbstractCommand;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;

/**
 * implements the dynamicCurrent api call of the circuit.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class DynamicCircuitCurrent extends AbstractCommand {
    private final String url;

    public DynamicCircuitCurrent(EaseeThingHandler handler, String circuitId, JsonResultProcessor resultProcessor) {
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.YES, resultProcessor);
        String siteId = handler.getBridgeConfiguration().getSiteId();
        this.url = DYNAMIC_CIRCUIT_CURRENT_URL.replaceAll("\\{siteId\\}", siteId).replaceAll("\\{circuitId\\}",
                circuitId);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.method(HttpMethod.GET);
        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return url;
    }

    @Override
    protected String getChannelGroup() {
        return CHANNEL_GROUP_CIRCUIT_DYNAMIC_CURRENT;
    }
}
