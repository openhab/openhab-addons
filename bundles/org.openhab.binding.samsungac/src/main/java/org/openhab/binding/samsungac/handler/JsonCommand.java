/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.samsungac.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * The {@link JsonCommand} class holds the json commands for communication with Samsung Digital Inverter
 *
 * @author Jan Grønlien - Initial contribution
 * @author Kai Kreuzer - Refactoring as preparation for openHAB contribution
 */

@NonNullByDefault
public class JsonCommand {
    private String path;
    private String json;

    public JsonCommand() {
        this.path = "";
        this.json = "";
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the json
     */
    public String getJson() {
        return json;
    }

    /**
     * @param json the json to set
     */
    public void setJson(String json) {
        this.json = json;
    }
}
