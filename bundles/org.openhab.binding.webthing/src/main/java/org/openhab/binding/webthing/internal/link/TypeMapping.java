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
package org.openhab.binding.webthing.internal.link;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.webthing.internal.client.dto.Property;

/**
 * The {@link TypeMapping} class defines the mapping of Item types <-> WebThing Property types.
 *
 * Please consider that changes of 'Item types <-> WebThing Property types' mapping will break the
 * compatibility to former releases
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class TypeMapping {

    /**
     * maps a property type to an item type
     * 
     * @param propertyMetadata the property meta data
     * @return the associated item type
     */
    public static ItemType toItemType(Property propertyMetadata) {
        String type = "string";
        @Nullable
        String tag = null;

        switch (propertyMetadata.typeKeyword) {
            case "AlarmProperty":
            case "BooleanProperty":
            case "LeakProperty":
            case "LockedProperty":
            case "MotionProperty":
            case "OnOffProperty":
            case "PushedProperty":
                type = "switch";
                tag = "Switchable";
                break;
            case "CurrentProperty":
            case "FrequencyProperty":
            case "InstantaneousPowerProperty":
            case "VoltageProperty":
                type = "number";
                break;
            case "HeatingCoolingProperty":
            case "ImageProperty":
            case "VideoProperty":
                type = "string";
                break;
            case "BrightnessProperty":
            case "HumidityProperty":
                type = "dimmer";
                break;
            case "ColorModeProperty":
                type = "string";
                tag = "lighting";
                break;
            case "ColorProperty":
                type = "color";
                tag = "Lighting";
                break;
            case "ColorTemperatureProperty":
                type = "dimmer";
                tag = "Lighting";
                break;
            case "OpenProperty":
                type = "contact";
                tag = "ContactSensor";
                break;
            case "TargetTemperatureProperty":
                type = "number";
                tag = "TargetTemperature";
                break;
            case "TemperatureProperty":
                type = "number";
                tag = "CurrentTemperature";
                break;
            case "ThermostatModeProperty":
                break;
            case "LevelProperty":
                if ((propertyMetadata.unit != null)
                        && propertyMetadata.unit.toLowerCase(Locale.ENGLISH).equals("percent")) {
                    type = "dimmer";
                } else {
                    type = "number";
                }
                break;
            default:
                switch (propertyMetadata.type.toLowerCase(Locale.ENGLISH)) {
                    case "boolean":
                        type = "switch";
                        tag = "Switchable";
                        break;
                    case "integer":
                    case "number":
                        type = "number";
                        break;
                    default:
                        type = "string";
                        break;
                }
                break;
        }

        return new ItemType(type, tag);
    }

    /**
     * The item type description
     */
    public static class ItemType {
        private final String type;
        private final @Nullable String tag;

        ItemType(String type, @Nullable String tag) {
            this.type = type;
            this.tag = tag;
        }

        public String getType() {
            return type;
        }

        public @Nullable String getTag() {
            return tag;
        }
    }
}
