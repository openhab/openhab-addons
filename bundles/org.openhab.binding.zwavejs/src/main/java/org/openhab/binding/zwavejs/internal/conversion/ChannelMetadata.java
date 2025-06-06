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
package org.openhab.binding.zwavejs.internal.conversion;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.MetadataType;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSChannelConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelMetadata} class represents channel metadata information for a Z-Wave node.
 * It contains various properties and methods to handle metadata and state information.
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ChannelMetadata extends BaseMetadata {

    private Logger logger = LoggerFactory.getLogger(ChannelMetadata.class);
    private static final List<String> IGNORED_COMMANDCLASSES = List.of("Manufacturer Specific", "Version");
    private static final List<String> ADVANCED_CHANNELS = List.of("32-restorePrevious", "32-duration", //
            "38-On", //
            "38-Off", //
            "38-duration", //
            "38-restorePrevious", //
            "113-alarmType", //
            "113-alarmLevel", //
            "113-System"); //
    private static final List<String> INVERTIBLE_ITEM_TYPES = List.of(CoreItemFactory.DIMMER, CoreItemFactory.CONTACT,
            CoreItemFactory.SWITCH);

    public @Nullable State state;
    public @Nullable StateDescriptionFragment statePattern;

    public ChannelMetadata(int nodeId, Value data) {
        super(nodeId, data);

        this.statePattern = createStatePattern(data.metadata.writeable, min, max, data.metadata.steps, data.value);
        this.state = toState(data.value, itemType, unit, false, factor);
    }

    public ChannelMetadata(int nodeId, Event data) {
        super(nodeId, data);
    }

    protected String itemTypeFromMetadata(MetadataType type, @Nullable Object value, String commandClassName,
            @Nullable Map<String, String> optionList) {
        String baseItemType = super.itemTypeFromMetadata(type, value, commandClassName, optionList);
        if (CoreItemFactory.NUMBER.equals(baseItemType) && writable && min != null && max != null) {
            if (min == 0 && max == 99) {
                this.max = 100; // ZUI uses 0-99, but openHAB uses 0-100
                return CoreItemFactory.DIMMER;
            }
        }

        return baseItemType;
    }

    public boolean isInvertible() {
        return INVERTIBLE_ITEM_TYPES.contains(itemType);
    }

    @Override
    protected boolean isAdvanced(int commandClassId, String propertyName) {
        return super.isAdvanced(commandClassId, propertyName)
                || ADVANCED_CHANNELS.contains(commandClassId + "-" + propertyName);
    }

    private static boolean compare(@Nullable Object str1, @Nullable Object str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    public static boolean isSameReadWriteChannel(Configuration configA, Configuration configB) {
        ZwaveJSChannelConfiguration cA = configA.as(ZwaveJSChannelConfiguration.class);
        ZwaveJSChannelConfiguration cB = configB.as(ZwaveJSChannelConfiguration.class);

        return cA.endpoint == cB.endpoint //
                && cA.commandClassId == cB.commandClassId //
                && compare(cA.propertyKeyInt, cB.propertyKeyInt) //
                && compare(cA.propertyKeyStr, cB.propertyKeyStr) //
                && !compare(cA.writeProperty, cB.writeProperty); //
    }

    public boolean isIgnoredCommandClass(@Nullable String commandClassName) {
        return commandClassName != null && IGNORED_COMMANDCLASSES.contains(commandClassName);
    }

    /*
     * Sets the state based on the provided event, item type, and unit symbol.
     *
     * @param event The event containing the new value to set the state to.
     * 
     * @param itemType The type of the item for which the state is being set.
     * 
     * @param unitSymbol The unit symbol to be used for the state, can be null.
     * 
     * @return The new state after setting it based on the event's new value.
     */
    public @Nullable State setState(Object value, String itemType, @Nullable String unitSymbol, boolean inverted) {
        this.unitSymbol = normalizeUnit(unitSymbol, value);
        Double factor = determineFactor(unitSymbol);
        this.unit = UnitUtils.parseUnit(this.unitSymbol);
        if (unitSymbol != null && this.unit == null) {
            logger.warn("Node {}. Unable to parse unitSymbol '{}' from channel config, this is a bug", nodeId,
                    unitSymbol);
        }
        if (CoreItemFactory.DIMMER.equals(itemType) && value instanceof Number numberValue) {
            value = numberValue.intValue() >= 99 ? 100 : value;
        }
        return this.state = toState(value, itemType, this.unit, inverted, factor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ChannelMetadata [");
        sb.append("nodeId=" + nodeId);
        sb.append(", Id=" + id);
        sb.append(", label=" + label);
        sb.append(", description=" + description);
        sb.append(", unitSymbol=" + unitSymbol);
        sb.append(", value=" + value);
        sb.append(", itemType=" + itemType);
        sb.append(", writable=" + writable);
        sb.append(", writeProperty=" + writeProperty);
        sb.append(", state=" + state);
        sb.append(", statePattern=" + statePattern);
        sb.append(", commandClassName=" + commandClassName);
        sb.append(", commandClassId=" + commandClassId);
        sb.append(", endpoint=" + endpoint);
        sb.append("]");
        return sb.toString();
    }
}
