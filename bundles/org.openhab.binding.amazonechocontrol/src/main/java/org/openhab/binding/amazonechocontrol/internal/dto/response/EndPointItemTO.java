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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;

/**
 * The {@link EndPointItemTO} encapsulate the GSON data of a smarthome graphql query
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class EndPointItemTO {
    public @Nullable String endpointId;
    public @Nullable String id;
    public @Nullable String friendlyName;
    public @Nullable JsonSmartHomeDevice legacyAppliance;
    public @Nullable StringWrapper serialNumber;
    public @Nullable String enablement;
    public @Nullable StringWrapper model;
    public @Nullable StringWrapper manufacturer;

    public static class TextValue {
        public @Nullable String text;
    }

    public static class StringWrapper {
        public @Nullable TextValue value;
    }
}
