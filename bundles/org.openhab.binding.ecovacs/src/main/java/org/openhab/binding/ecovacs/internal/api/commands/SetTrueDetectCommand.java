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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class SetTrueDetectCommand extends AbstractNoResponseCommand {
    private final boolean on;

    public SetTrueDetectCommand(boolean on) {
        this.on = on;
    }

    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Set true detect is not supported for XML");
        }
        return "setTrueDetect";
    }

    @Override
    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        JsonObject args = new JsonObject();
        args.addProperty("enable", on ? 1 : 0);
        return args;
    }
}
