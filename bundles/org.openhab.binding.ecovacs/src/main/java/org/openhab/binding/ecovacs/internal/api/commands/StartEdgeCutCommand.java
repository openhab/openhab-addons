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
package org.openhab.binding.ecovacs.internal.api.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Start edge cutting command for GOAT mowers. Sends a 'clean' command with border type
 * to instruct the mower to cut along the boundary wire.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class StartEdgeCutCommand extends AbstractMowerCommand {
    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Mower commands are not supported for XML");
        }
        return "clean";
    }

    @Override
    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        JsonObject args = new JsonObject();
        args.addProperty("act", "start");
        JsonObject content = new JsonObject();
        content.addProperty("type", "border");
        args.add("content", content);
        return args;
    }
}
