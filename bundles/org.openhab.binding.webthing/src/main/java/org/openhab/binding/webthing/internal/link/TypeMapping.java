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
        String type = "String";
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
                type = "Switch";
                tag = "Switchable";
                break;
            case "CurrentProperty":
            case "FrequencyProperty":
            case "InstantaneousPowerProperty":
            case "VoltageProperty":
                type = "Number";
                break;
            case "HeatingCoolingProperty":
            case "ImageProperty":
            case "VideoProperty":
                type = "String";
                break;
            case "BrightnessProperty":
            case "HumidityProperty":
                type = "Dimmer";
                break;
            case "ColorModeProperty":
                type = "String";
                tag = "lighting";
                break;
            case "ColorProperty":
                type = "Color";
                tag = "Lighting";
                break;
            case "ColorTemperatureProperty":
                type = "Dimmer";
                tag = "Lighting";
                break;
            case "OpenProperty":
                type = "Contact";
                tag = "ContactSensor";
                break;
            case "TargetTemperatureProperty":
                type = "Number";
                tag = "TargetTemperature";
                break;
            case "TemperatureProperty":
                type = "Number";
                tag = "CurrentTemperature";
                break;
            case "ThermostatModeProperty":
                break;
            case "LevelProperty":
                if ((propertyMetadata.unit != null)
                        && propertyMetadata.unit.toLowerCase(Locale.ENGLISH).equals("percent")) {
                    type = "Dimmer";
                } else {
                    type = "Number";
                }
                break;
            default:
                switch (propertyMetadata.type.toLowerCase(Locale.ENGLISH)) {
                    case "boolean":
                        type = "Switch";
                        tag = "Switchable";
                        break;
                    case "integer":
                    case "number":
                        type = "Number";
                        break;
                    default:
                        type = "String";
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
