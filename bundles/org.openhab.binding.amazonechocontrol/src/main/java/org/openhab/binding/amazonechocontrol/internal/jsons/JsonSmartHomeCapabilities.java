/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class JsonSmartHomeCapabilities {

    public static class SmartHomeCapability {
        public @Nullable String capabilityType;
        public @Nullable String type;
        public @Nullable String version;
        public @Nullable String interfaceName;
        public @Nullable Properties properties;
    }

    public static class Properties {
        public @Nullable Property @Nullable [] supported;
    }

    public static class Property {
        public @Nullable String name;
    }

    public @Nullable SmartHomeCapability @Nullable [] capabilites;
}
