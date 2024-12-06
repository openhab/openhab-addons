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
 * implements the set smart home mode api call of the site.
 *
 * @author Anders Alfredsson - initial contribution
 */
@NonNullByDefault
public class SetSmartHomeMode extends AbstractWriteCommand {
    private String url;

    public SetSmartHomeMode(MyUplinkThingHandler handler, Channel channel, Command command, String systemId,
            JsonResultProcessor resultProcessor) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, channel, command, RetryOnFailure.NO, ProcessFailureResponse.YES, resultProcessor);
        this.url = SET_SMART_HOME_MODE_URL.replaceAll("\\{systemId\\}", systemId);
    }

    @Override
    protected String getURL() {
        return this.url;
    }

    @Override
    protected Request prepareWriteRequest(Request requestToPrepare) throws ValidationException {
        requestToPrepare.method(HttpMethod.PUT);

        String body = buildJsonObject(JSON_KEY_SMART_HOME_MODE, command.toString());

        StringContentProvider cp = new StringContentProvider(WEB_REQUEST_PATCH_CONTENT_TYPE, body,
                StandardCharsets.UTF_8);

        requestToPrepare.content(cp);

        return requestToPrepare;
    }
}
