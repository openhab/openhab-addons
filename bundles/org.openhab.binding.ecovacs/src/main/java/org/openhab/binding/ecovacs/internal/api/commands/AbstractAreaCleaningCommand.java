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
package org.openhab.binding.ecovacs.internal.api.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
class AbstractAreaCleaningCommand extends AbstractNoResponseCommand {
    private final String jsonTypeName;
    private final String areaDefinition;
    private final int cleanPasses;

    AbstractAreaCleaningCommand(String jsonTypeName, String areaDefinition, int cleanPasses) {
        this.jsonTypeName = jsonTypeName;
        this.areaDefinition = areaDefinition;
        this.cleanPasses = cleanPasses;
    }

    @Override
    public String getName(ProtocolVersion version) {
        switch (version) {
            case XML:
                return "Clean";
            case JSON:
                return "clean";
            case JSON_V2:
                return "clean_V2";
        }
        throw new AssertionError();
    }

    @Override
    protected void applyXmlPayload(Document doc, Element ctl) {
        Element clean = doc.createElement("clean");
        clean.setAttribute("act", "s");
        clean.setAttribute("type", "SpotArea");
        clean.setAttribute("speed", "standard");
        clean.setAttribute("p", areaDefinition);
        clean.setAttribute("deep", String.valueOf(cleanPasses));
        ctl.appendChild(clean);
    }

    @Override
    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        JsonObject args = new JsonObject();
        args.addProperty("act", "start");

        JsonObject payload = args;
        if (version == ProtocolVersion.JSON_V2) {
            JsonObject content = new JsonObject();
            args.add("content", content);
            payload = content;
            payload.addProperty("value", this.areaDefinition);
            payload.addProperty("donotClean", 0);
            payload.addProperty("total", 0);
        } else {
            payload.addProperty("content", this.areaDefinition);
        }
        payload.addProperty("count", cleanPasses);
        payload.addProperty("type", this.jsonTypeName);
        return args;
    }
}
