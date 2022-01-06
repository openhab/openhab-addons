/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.dto.thermostat.summary;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link RevisionDTODeserializer} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class RevisionDTODeserializer implements JsonDeserializer<@Nullable RevisionDTO> {

    @Override
    public @Nullable RevisionDTO deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        if (json == null || typeOfT == null || context == null) {
            return null;
        }
        String[] fields = json.getAsString().split(":");
        if (fields.length < 7) {
            throw new JsonParseException("unable to parse RevisionList");
        }
        RevisionDTO revisionList = new RevisionDTO();
        revisionList.identifier = fields[0];
        revisionList.name = fields[1];
        revisionList.connected = "true".equals(fields[2]);
        revisionList.thermostatRevision = fields[3];
        revisionList.alertsRevision = fields[4];
        revisionList.runtimeRevision = fields[5];
        revisionList.intervalRevision = fields[6];
        return revisionList;
    }
}
