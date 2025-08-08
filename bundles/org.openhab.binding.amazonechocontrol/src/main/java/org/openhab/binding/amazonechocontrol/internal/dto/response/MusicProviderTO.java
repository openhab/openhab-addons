/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

/**
 * The {@link MusicProviderTO} encapsulate a single music provider
 *
 * @author Jan N. Klug - Initial contribution
 */
public class MusicProviderTO {
    @SuppressWarnings("unchecked")
    public static final TypeToken<List<MusicProviderTO>> LIST_TYPE_TOKEN = (TypeToken<List<MusicProviderTO>>) TypeToken
            .getParameterized(List.class, MusicProviderTO.class);

    public String id;
    public String displayName;
    public String description;
    public List<String> supportedProperties = List.of();
    public List<Object> supportedTriggers = List.of();
    public List<String> supportedOperations = List.of();
    public String availability;
    public String icon;
    public MusicProviderDataTO providerData = new MusicProviderDataTO();

    @Override
    public @NonNull String toString() {
        return "MusicProviderTO{id='" + id + "', displayName='" + displayName + "', description='" + description
                + "', supportedProperties=" + supportedProperties + ", supportedTriggers=" + supportedTriggers
                + ", supportedOperations=" + supportedOperations + ", availability='" + availability + "', icon='"
                + icon + "', providerData=" + providerData + "}";
    }
}
