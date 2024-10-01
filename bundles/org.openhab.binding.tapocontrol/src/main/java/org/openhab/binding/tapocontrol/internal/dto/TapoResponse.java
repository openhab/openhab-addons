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
package org.openhab.binding.tapocontrol.internal.dto;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;
import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * Tapo-Response Structure Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoResponse(@Expose @SerializedName(value = "errorcode", alternate = "error_code") int errorCode,
        @Expose @SerializedName("result") JsonObject result, @Expose String method,
        @Expose @SerializedName("msg") String message) {

    private static final String MULTI_RESPONSE_KEY = "responses";

    public TapoResponse() {
        this(0, new JsonObject(), "", "");
    }

    public TapoResponse(int errorCode) {
        this(errorCode, new JsonObject(), "", "");
    }

    /***********************************************
     * RETURN VALUES
     * Return default data if recordobject is null
     **********************************************/

    public boolean hasError() {
        return errorCode != 0;
    }

    public boolean isMultiRequestResponse() {
        return result.has(MULTI_RESPONSE_KEY);
    }

    @Override
    public int errorCode() {
        return Objects.requireNonNullElse(errorCode, ERR_API_CLOUD_FAILED.getCode());
    }

    @Override
    public JsonObject result() {
        return Objects.requireNonNullElse(result, new JsonObject());
    }

    @Override
    public String message() {
        return Objects.requireNonNullElse(message, "");
    }

    @Override
    public String method() {
        return Objects.requireNonNullElse(method, "");
    }

    public List<TapoResponse> responses() {
        JsonArray responses = result.getAsJsonArray(MULTI_RESPONSE_KEY);
        Type repsonseListType = new TypeToken<List<TapoResponse>>() {
        }.getType();
        return Objects.requireNonNullElse(GSON.fromJson(responses.toString(), repsonseListType),
                new ArrayList<TapoResponse>());
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
