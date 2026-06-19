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
package org.openhab.binding.homeconnectdirect.internal.service.profile.json.adapter;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ZONE_ID;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.AesCredentials;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.ApplianceProfile;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.Credentials;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.TlsCredentials;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson adapter for appliance profile serialization and deserialization.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ApplianceProfileAdapter extends TypeAdapter<ApplianceProfile> {

    @Override
    public void write(@Nullable JsonWriter out, @Nullable ApplianceProfile value) throws IOException {
        if (out == null || value == null) {
            return;
        }
        out.beginObject();
        out.name("haId").value(value.haId());
        out.name("type").value(value.type());
        out.name("serialNumber").value(value.serialNumber());
        out.name("brand").value(value.brand());
        out.name("vib").value(value.vib());
        out.name("mac").value(value.mac());

        Credentials credentials = value.credentials();
        if (credentials instanceof AesCredentials(String key, String iv)) {
            out.name("connectionType").value("AES");
            out.name("key").value(key);
            out.name("iv").value(iv);
        } else if (credentials instanceof TlsCredentials(String key)) {
            out.name("connectionType").value("TLS");
            out.name("key").value(key);
        }

        out.name("featureMappingFileName").value(value.featureMappingFileName());
        out.name("deviceDescriptionFileName").value(value.deviceDescriptionFileName());
        out.name("created").value(value.created().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        out.endObject();
    }

    @Override
    public @Nullable ApplianceProfile read(@Nullable JsonReader in) throws IOException {
        if (in == null || in.peek() == JsonToken.NULL) {
            return null;
        }

        String haId = "";
        String type = "";
        String serialNumber = "";
        String brand = "";
        String vib = "";
        String mac = "";
        String connectionType = "AES";
        String key = "";
        String iv = "";
        String featureMappingFileName = "";
        String deviceDescriptionFileName = "";
        OffsetDateTime created = OffsetDateTime.now(ZONE_ID);

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "haId" -> haId = in.nextString();
                case "type" -> type = in.nextString();
                case "serialNumber" -> serialNumber = in.nextString();
                case "brand" -> brand = in.nextString();
                case "vib" -> vib = in.nextString();
                case "mac" -> mac = in.nextString();
                case "connectionType" -> connectionType = in.nextString();
                case "key" -> key = in.nextString();
                case "iv" -> iv = in.nextString();
                case "featureMappingFileName" -> featureMappingFileName = in.nextString();
                case "deviceDescriptionFileName" -> deviceDescriptionFileName = in.nextString();
                case "created" -> {
                    String createdStr = in.nextString();
                    created = OffsetDateTime.parse(createdStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }
                default -> in.skipValue();
            }
        }
        in.endObject();

        Credentials credentials = "TLS".equals(connectionType) ? new TlsCredentials(key) : new AesCredentials(key, iv);

        return new ApplianceProfile(haId, type, serialNumber, brand, vib, mac, credentials, featureMappingFileName,
                deviceDescriptionFileName, created);
    }
}
