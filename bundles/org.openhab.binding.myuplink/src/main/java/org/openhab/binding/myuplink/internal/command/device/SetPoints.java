/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.command.device;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myuplink.internal.command.AbstractWriteCommand;
import org.openhab.binding.myuplink.internal.command.JsonResultProcessor;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;
import org.openhab.binding.myuplink.internal.model.ValidationException;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;

/**
 * implements the set points api call of the myUplink API.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SetPoints extends AbstractWriteCommand {
    private final String url;

    public SetPoints(MyUplinkThingHandler handler, Channel channel, Command command, String deviceId,
            JsonResultProcessor resultProcessor) {
        super(handler, channel, command, RetryOnFailure.YES, ProcessFailureResponse.YES, resultProcessor);
        this.url = SET_DEVICE_POINTS.replaceAll("\\{deviceId\\}", deviceId);
    }

    @Override
    protected String getURL() {
        return url;
    }

    @Override
    protected Request prepareWriteRequest(Request requestToPrepare) throws ValidationException {
        requestToPrepare.method(HttpMethod.PATCH);

        StringContentProvider cp = new StringContentProvider(WEB_REQUEST_PATCH_CONTENT_TYPE, getJsonContent(),
                StandardCharsets.UTF_8);

        requestToPrepare.content(cp);

        return requestToPrepare;
    }
}
