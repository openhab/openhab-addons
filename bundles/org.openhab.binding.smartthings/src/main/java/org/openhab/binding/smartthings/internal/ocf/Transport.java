/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.ocf;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Authenticated access to one appliance, without ownership provisioning.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
interface Transport extends AutoCloseable {
    JsonElement get(String path) throws IOException;

    void post(String href, JsonObject fields) throws IOException;

    interface Listener {
        void onUpdate(JsonElement representation);

        void onFailure();
    }

    @FunctionalInterface
    interface Subscription extends AutoCloseable {
        @Override
        void close();
    }

    Subscription observe(String href, Listener listener) throws IOException;

    @Override
    void close();
}
