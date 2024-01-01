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
import org.openhab.binding.ecovacs.internal.api.model.MoppingWaterAmount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class SetMoppingWaterAmountCommand extends AbstractNoResponseCommand {
    private final int level;

    public SetMoppingWaterAmountCommand(MoppingWaterAmount amount) {
        super();
        this.level = amount.toApiValue();
    }

    @Override
    public String getName(ProtocolVersion version) {
        return version == ProtocolVersion.XML ? "SetWaterPermeability" : "setWaterInfo";
    }

    @Override
    protected void applyXmlPayload(Document doc, Element ctl) {
        ctl.setAttribute("v", String.valueOf(level));
    }

    @Override
    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        JsonObject args = new JsonObject();
        args.addProperty("amount", level);
        return args;
    }
}
