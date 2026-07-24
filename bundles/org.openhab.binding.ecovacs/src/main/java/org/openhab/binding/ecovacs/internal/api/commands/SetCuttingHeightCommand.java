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
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class SetCuttingHeightCommand extends AbstractNoResponseCommand {
    private final int heightMm;

    /**
     * @param heightMm cutting height in millimeters (30-75)
     */
    public SetCuttingHeightCommand(int heightMm) {
        this.heightMm = heightMm;
    }

    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Mower commands are not supported for XML");
        }
        return "setCutHeight";
    }

    @Override
    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        // Convert mm to level: level = (height - 25) / 5
        // level 1 = 30mm, level 2 = 35mm, ..., level 10 = 75mm
        int level = (heightMm - 25) / 5;
        level = Math.max(1, Math.min(10, level));
        JsonObject args = new JsonObject();
        args.addProperty("level", level);
        return args;
    }
}
