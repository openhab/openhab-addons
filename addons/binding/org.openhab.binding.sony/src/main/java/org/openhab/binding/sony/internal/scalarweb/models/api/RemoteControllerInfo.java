/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// TODO: Auto-generated Javadoc
/**
 * The Class RemoteControllerInfo.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class RemoteControllerInfo {

    /** The bundled. */
    private final boolean bundled;

    /** The type. */
    private final String type;

    /** The commands. */
    private final List<RemoteCommand> commands;

    /**
     * Instantiates a new remote controller info.
     *
     * @param results the results
     */
    public RemoteControllerInfo(ScalarWebResult results) {
        final Gson gson = new Gson();

        Boolean myBundled = null;
        String myType = null;
        List<RemoteCommand> myCommands = new ArrayList<RemoteCommand>();

        for (JsonElement elm : results.getResults()) {
            if (elm.isJsonArray()) {
                for (JsonElement elm2 : elm.getAsJsonArray()) {
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
     * Checks if is bundled.
     *
     * @return true, if is bundled
     */
    public boolean isBundled() {
        return bundled;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the commands.
     *
     * @return the commands
     */
    public List<RemoteCommand> getCommands() {
        return commands;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RemoteControllerInfo [bundled=" + bundled + ", type=" + type + ", commands=" + commands + "]";
    }

    /**
     * The Class RemoteCommand.
     */
    public class RemoteCommand {

        /** The name. */
        private final String name;

        /** The value. */
        private final String value;

        /**
         * Instantiates a new remote command.
         *
         * @param name the name
         * @param value the value
         */
        public RemoteCommand(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "RemoteCommand [name=" + name + ", value=" + value + "]";
        }
    }
}
