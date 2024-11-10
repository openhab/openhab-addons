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
/*
 * This file is based on:
 *
 * ServiceCommand
 * Connect SDK
 *
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.lgwebos.internal.handler.command;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Internal implementation of ServiceCommand for URL-based commands
 *
 * @author Hyun Kook Khang - Connect SDK initial contribution
 * @author Sebastian Prehn - Adoption for openHAB
 */
@NonNullByDefault
public class ServiceCommand<T> {

    protected enum Type {
        request,
        subscribe
    }

    protected Type type;
    protected @Nullable JsonObject payload;
    protected String target;
    protected Function<JsonObject, @Nullable T> converter;

    ResponseListener<T> responseListener;

    public ServiceCommand(String targetURL, @Nullable JsonObject payload, Function<JsonObject, @Nullable T> converter,
            ResponseListener<T> listener) {
        this.target = targetURL;
        this.payload = payload;
        this.converter = converter;
        this.responseListener = listener;
        this.type = Type.request;
    }

    public @Nullable JsonElement getPayload() {
        return payload;
    }

    public String getType() {
        return type.name();
    }

    public String getTarget() {
        return target;
    }

    public void processResponse(@Nullable JsonObject response) {
        if (response != null) {
            this.getResponseListener().onSuccess(this.converter.apply(response));
        }
    }

    public void processError(String error) {
        this.getResponseListener().onError(error);
    }

    public ResponseListener<T> getResponseListener() {
        return responseListener;
    }

    @Override
    public String toString() {
        return "ServiceCommand [type=" + type + ", target=" + target + ", payload=" + payload + "]";
    }
}
