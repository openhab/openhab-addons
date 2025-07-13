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

import static org.openhab.binding.zwavejs.internal.CommandClassConstants.COMMAND_CLASS_SWITCH_COLOR;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.MetadataType;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
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
    private static final List<String> ADVANCED_CHANNELS = List.of( //
            "32-restorePrevious", //
            "32-duration", //
            "38-On", //
            "38-Off", //
            "38-duration", //
            "38-restorePrevious", //
            "51-duration", //
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

    @Override
    protected String itemTypeFromMetadata(MetadataType type, @Nullable Object value, int commandClass,
            @Nullable Map<String, String> optionList) {
        String baseItemType = super.itemTypeFromMetadata(type, value, commandClass, optionList);
        if (CoreItemFactory.NUMBER.equals(baseItemType) && writable && min != null && max != null) {
            if (min == 0 && max == 99) {
                this.max = 100L; // ZUI uses 0-99, but openHAB uses 0-100
                return CoreItemFactory.DIMMER;
            }
        }

        return baseItemType;
    }

    public boolean isInvertible() {
        return INVERTIBLE_ITEM_TYPES.contains(itemType);
    }

    @Override
    protected boolean isAdvanced(int commandClassId, String propertyName, @Nullable Object propertyKey) {
        return super.isAdvanced(commandClassId, propertyName, propertyKey)
                || ADVANCED_CHANNELS.contains(commandClassId + "-" + propertyName)
                || (commandClassId == COMMAND_CLASS_SWITCH_COLOR && propertyKey != null);
    }

    public boolean isIgnoredCommandClass(@Nullable String commandClassName) {
        return commandClassName != null && IGNORED_COMMANDCLASSES.contains(commandClassName);
    }

    /**
     * Sets the state for this channel metadata based on the provided value and configuration.
     *
     * @param value the raw value to convert to a {@link State}
     * @param itemType the openHAB item type (e.g., "Switch", "Number", "Color")
     * @param unitSymbol the unit symbol of the incoming value, or {@code null} if not applicable
     * @param inverted {@code true} if the value should be logically inverted; {@code false} otherwise
     * @return the corresponding {@link State} for the given value and configuration, or {@code null} if conversion is
     *         not possible
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
        sb.append(", readProperty=" + readProperty);
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
