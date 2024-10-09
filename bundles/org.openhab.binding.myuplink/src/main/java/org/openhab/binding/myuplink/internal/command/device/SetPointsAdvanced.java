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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myuplink.internal.command.AbstractWriteCommand;
import org.openhab.binding.myuplink.internal.command.JsonResultProcessor;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;
import org.openhab.binding.myuplink.internal.model.ValidationException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * implements the set points api call of the API. Extracts channel ID and value from the command string. Needed by the
 * "generic command" channel.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SetPointsAdvanced extends AbstractWriteCommand {
    private final String url;

    public SetPointsAdvanced(MyUplinkThingHandler handler, Channel channel, Command command, String deviceId,
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

        StringContentProvider cp = new StringContentProvider(WEB_REQUEST_PATCH_CONTENT_TYPE,
                buildJsonObject(getChannelId(), getChannelValue()), StandardCharsets.UTF_8);
        requestToPrepare.content(cp);

        return requestToPrepare;
    }

    private String getChannelId() {
        if (command instanceof StringType stringCommand) {
            String[] tokens = stringCommand.toString().split(":");
            return tokens.length == 2 ? tokens[0] : command.toString();
        } else {
            return command.toString();
        }
    }

    private String getChannelValue() {
        if (command instanceof StringType stringCommand) {
            String[] tokens = stringCommand.toString().split(":");
            return tokens.length == 2 ? tokens[1] : command.toString();
        } else {
            return command.toString();
        }
    }

    /**
     * handling of result in case of HTTP response OK.
     *
     * @param json
     */
    protected void onCompleteCodeOk(@Nullable String json) {
        Map<String, String> content = new HashMap<>(2);
        content.put(JSON_KEY_CHANNEL_ID, CHANNEL_ID_COMMAND);
        content.put(JSON_KEY_CHANNEL_VALUE, getCommunicationStatus().getHttpCode().name());
        content.put(JSON_KEY_ROOT_DATA, json == null ? EMPTY : json);

        var jsonObjectString = gson.toJson(content);
        var jsonObject = gson.fromJson(jsonObjectString, JsonObject.class);

        var jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        super.onCompleteCodeOk(gson.toJson(jsonArray));
    }
}
