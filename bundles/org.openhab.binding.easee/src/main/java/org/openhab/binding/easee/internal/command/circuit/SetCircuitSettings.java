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

import static org.openhab.binding.easee.internal.EaseeBindingConstants.CIRCUIT_SETTINGS_URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.AbstractWriteCommand;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;
import org.openhab.binding.easee.internal.model.ValidationException;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;

/**
 * implements the settings api call of the circuit.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SetCircuitSettings extends AbstractWriteCommand {
    private final String url;

    public SetCircuitSettings(EaseeThingHandler handler, Channel channel, Command command, String circuitId,
            JsonResultProcessor resultProcessor) {
        super(handler, channel, command, RetryOnFailure.YES, ProcessFailureResponse.YES, resultProcessor);
        String siteId = handler.getBridgeConfiguration().getSiteId();
        this.url = CIRCUIT_SETTINGS_URL.replaceAll("\\{siteId\\}", siteId).replaceAll("\\{circuitId\\}", circuitId);
    }

    @Override
    protected Request prepareWriteRequest(Request requestToPrepare) throws ValidationException {
        requestToPrepare.method(HttpMethod.POST);
        StringContentProvider cp = new StringContentProvider(getJsonContent());
        requestToPrepare.content(cp);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return url;
    }
}
