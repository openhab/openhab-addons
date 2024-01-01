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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeGroupIdentifiers.SmartHomeGroupIdentifier;

/**
 * @author Lukas Knoeller - Initial contribution
 */
@NonNullByDefault
public class JsonSmartHomeGroups {

    public static class SmartHomeGroup implements SmartHomeBaseDevice {

        @Override
        public @Nullable String findId() {
            SmartHomeGroupIdentifier applianceGroupIdentifier = this.applianceGroupIdentifier;
            if (applianceGroupIdentifier == null) {
                return null;
            }
            String value = applianceGroupIdentifier.value;
            if (value == null) {
                return null;
            }
            return value;
        }

        @Override
        public @Nullable String findEntityId() {
            SmartHomeGroupIdentifier applianceGroupIdentifier = this.applianceGroupIdentifier;
            if (applianceGroupIdentifier == null) {
                return null;
            }
            String value = applianceGroupIdentifier.value;
            if (value == null) {
                return null;
            }
            return value;
        }

        @Override
        public boolean isGroup() {
            return true;
        }

        public @Nullable String applianceGroupName;
        public @Nullable Boolean isSpace;
        public @Nullable Boolean space;
        public @Nullable SmartHomeGroupIdentifier applianceGroupIdentifier;

        @Override
        public String toString() {
            return "SmartHomeGroup{" + "applianceGroupName='" + applianceGroupName + '\'' + ", isSpace=" + isSpace
                    + ", space=" + space + ", applianceGroupIdentifier=" + applianceGroupIdentifier + '}';
        }
    }

    public @Nullable List<SmartHomeGroup> groups;
}
