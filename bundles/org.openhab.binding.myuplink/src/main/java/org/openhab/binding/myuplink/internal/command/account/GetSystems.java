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
package org.openhab.binding.myuplink.internal.command.account;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Result;
import org.openhab.binding.myuplink.internal.command.AbstractPagingCommand;
import org.openhab.binding.myuplink.internal.command.JsonResultProcessor;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;

import com.google.gson.JsonObject;

/**
 * implements the get sites api call of the site.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GetSystems extends AbstractPagingCommand {

    public GetSystems(MyUplinkThingHandler handler, JsonResultProcessor resultProcessor) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.YES, resultProcessor);
    }

    @Override
    protected String getURL() {
        String url = GET_SYSTEMS_URL;
        return url;
    }

    @Override
    public void onComplete(@Nullable Result result) {
        String json = getContentAsString(StandardCharsets.UTF_8);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        if (jsonObject != null) {
            processResult(jsonObject);
        }
    }

    @Override
    protected String getChannelGroup() {
        return EMPTY;
    }
}
