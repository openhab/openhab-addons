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
import org.openhab.core.library.CoreItemFactory;

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
        String type = CoreItemFactory.STRING;
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
                type = CoreItemFactory.SWITCH;
                tag = "Switchable";
                break;
            case "CurrentProperty":
            case "FrequencyProperty":
            case "InstantaneousPowerProperty":
            case "VoltageProperty":
                type = CoreItemFactory.NUMBER;
                break;
            case "HeatingCoolingProperty":
            case "ImageProperty":
            case "VideoProperty":
                type = CoreItemFactory.STRING;
                break;
            case "BrightnessProperty":
            case "HumidityProperty":
                type = CoreItemFactory.DIMMER;
                break;
            case "ColorModeProperty":
                type = CoreItemFactory.STRING;
                tag = "lighting";
                break;
            case "ColorProperty":
                type = CoreItemFactory.COLOR;
                tag = "Lighting";
                break;
            case "ColorTemperatureProperty":
                type = CoreItemFactory.DIMMER;
                tag = "Lighting";
                break;
            case "OpenProperty":
                type = CoreItemFactory.CONTACT;
                tag = "ContactSensor";
                break;
            case "TargetTemperatureProperty":
                type = CoreItemFactory.NUMBER;
                tag = "TargetTemperature";
                break;
            case "TemperatureProperty":
                type = CoreItemFactory.NUMBER;
                tag = "CurrentTemperature";
                break;
            case "ThermostatModeProperty":
                break;
            case "LevelProperty":
                if ((propertyMetadata.unit != null)
                        && propertyMetadata.unit.toLowerCase(Locale.ENGLISH).equals("percent")) {
                    type = CoreItemFactory.DIMMER;
                } else {
                    type = CoreItemFactory.NUMBER;
                }
                break;
            default:
                switch (propertyMetadata.type.toLowerCase(Locale.ENGLISH)) {
                    case "boolean":
                        type = CoreItemFactory.SWITCH;
                        tag = "Switchable";
                        break;
                    case "integer":
                    case "number":
                        type = CoreItemFactory.NUMBER;
                        break;
                    default:
                        type = CoreItemFactory.STRING;
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
