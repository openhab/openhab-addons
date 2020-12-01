/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This class represents the remote controller information and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class RemoteControllerInfo {

    /** Whether bundled or not */
    private final boolean bundled;

    /** The controller type */
    private final @Nullable String type;

    /** The commands support (unmodifiable and can be empty) */
    private final List<RemoteCommand> commands;

    /**
     * Instantiates a new remote controller info.
     *
     * @param results the results
     */
    public RemoteControllerInfo(final ScalarWebResult results) {
        Objects.requireNonNull(results, "result cannot be null");

        final Gson gson = GsonUtilities.getDefaultGson();

        Boolean myBundled = null;
        String myType = null;
        final List<RemoteCommand> myCommands = new ArrayList<RemoteCommand>();

        final JsonArray rsts = results.getResults();
        if (rsts == null) {
            throw new JsonParseException("No results to deserialize");
        }

        for (final JsonElement elm : rsts) {
            if (elm.isJsonArray()) {
                for (final JsonElement elm2 : elm.getAsJsonArray()) {
                    myCommands.add(gson.fromJson(elm2, RemoteCommand.class));
                }
            } else if (elm.isJsonObject()) {
                final JsonObject obj = elm.getAsJsonObject();

                final JsonElement bundElm = obj.get("bundled");
                if (bundElm != null && bundElm.isJsonPrimitive() && bundElm.getAsJsonPrimitive().isBoolean()) {
                    myBundled = bundElm.getAsBoolean();
                }

                final JsonElement typeElm = obj.get("type");
                if (typeElm != null) {
                    myType = typeElm.getAsString();
                }
            } else {
                throw new JsonParseException("Unknown element in array: " + elm);
            }
        }

        if (myBundled == null) {
            throw new JsonParseException("'bundled' was not found or not an boolean");
        }
        if (StringUtils.isEmpty(myType)) {
            throw new JsonParseException("'type' was not found or not an boolean");
        }

        bundled = myBundled;
        type = myType;
        commands = Collections.unmodifiableList(myCommands);
    }

    /**
     * Checks if is bundled
     *
     * @return true, if bundled - false otherwise
     */
    public boolean isBundled() {
        return bundled;
    }

    /**
     * Gets the controller type
     *
     * @return the controller type
     */
    public @Nullable String getType() {
        return type;
    }

    /**
     * Gets the commands
     *
     * @return the commands
     */
    public List<RemoteCommand> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        return "RemoteControllerInfo [bundled=" + bundled + ", type=" + type + ", commands=" + commands + "]";
    }

    /**
     * This class represents the remote command information
     *
     * @author Tim Roberts - Initial contribution
     */
    @NonNullByDefault
    public class RemoteCommand {

        /** The name of the command */
        private @Nullable String name;

        /** The value of the command */
        private @Nullable String value;

        /**
         * Gets the name of the command
         *
         * @return the name of the command
         */
        public @Nullable String getName() {
            return name;
        }

        /**
         * Gets the value of the command
         *
         * @return the value of the command
         */
        public @Nullable String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "RemoteCommand [name=" + name + ", value=" + value + "]";
        }
    }
}
