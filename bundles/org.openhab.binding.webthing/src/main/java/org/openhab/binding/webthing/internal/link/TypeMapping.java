/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
        Set<String> tags = Set.of();

        switch (propertyMetadata.typeKeyword) {
            case "AlarmProperty":
            case "BooleanProperty":
            case "LeakProperty":
            case "LockedProperty":
            case "MotionProperty":
            case "OnOffProperty":
            case "PushedProperty":
                type = CoreItemFactory.SWITCH;
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
                type = CoreItemFactory.DIMMER;
                tags = Set.of("Control", "Light");
                break;
            case "HumidityProperty":
                type = CoreItemFactory.DIMMER;
                tags = Set.of("Measurement", "Humidity");
                break;
            case "ColorModeProperty":
                type = CoreItemFactory.STRING;
                break;
            case "ColorProperty":
                type = CoreItemFactory.COLOR;
                tags = Set.of("Control", "Light");
                break;
            case "ColorTemperatureProperty":
                type = CoreItemFactory.DIMMER;
                tags = Set.of("Control", "ColorTemperature");
                break;
            case "OpenProperty":
                type = CoreItemFactory.CONTACT;
                tags = Set.of("OpenState");
                break;
            case "TargetTemperatureProperty":
                type = CoreItemFactory.NUMBER;
                tags = Set.of("Setpoint", "Temperature");
                break;
            case "TemperatureProperty":
                type = CoreItemFactory.NUMBER;
                tags = Set.of("Measurement", "Temperature");
                break;
            case "ThermostatModeProperty":
                break;
            case "LevelProperty":
                if ((propertyMetadata.unit != null)
                        && "percent".equals(propertyMetadata.unit.toLowerCase(Locale.ENGLISH))) {
                    type = CoreItemFactory.DIMMER;
                } else {
                    type = CoreItemFactory.NUMBER;
                }
                break;
            default:
                switch (propertyMetadata.type.toLowerCase(Locale.ENGLISH)) {
                    case "boolean":
                        type = CoreItemFactory.SWITCH;
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

        return new ItemType(type, tags);
    }

    /**
     * The item type description
     */
    public static class ItemType {
        private final String type;
        private final Set<String> tags;

        ItemType(String type, Set<String> tags) {
            this.type = type;
            this.tags = tags;
        }

        public String getType() {
            return type;
        }

        public Set<String> getTags() {
            return tags;
        }
    }
}
