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
package org.openhab.binding.wolfsmartset.internal.handler;

import static org.openhab.binding.wolfsmartset.internal.WolfSmartsetBindingConstants.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wolfsmartset.internal.config.WolfSmartsetUnitConfiguration;
import org.openhab.binding.wolfsmartset.internal.dto.GetParameterValuesDTO;
import org.openhab.binding.wolfsmartset.internal.dto.MenuItemTabViewDTO;
import org.openhab.binding.wolfsmartset.internal.dto.ParameterDescriptorDTO;
import org.openhab.binding.wolfsmartset.internal.dto.SubMenuEntryDTO;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WolfSmartsetUnitThingHandler} is responsible for updating the channels associated
 * with an WolfSmartset unit.
 *
 * @author Bo Biene - Initial contribution
 */
@NonNullByDefault
public class WolfSmartsetUnitThingHandler extends BaseThingHandler {

    public static final String CAPABILITY_ADC = "adc";
    public static final String CAPABILITY_CO2 = "co2";
    public static final String CAPABILITY_DRY_CONTACT = "dryContact";
    public static final String CAPABILITY_HUMIDITY = "humidity";
    public static final String CAPABILITY_OCCUPANCY = "occupancy";
    public static final String CAPABILITY_TEMPERATURE = "temperature";
    public static final String CAPABILITY_UNKNOWN = "unknown";

    private final Logger logger = LoggerFactory.getLogger(WolfSmartsetUnitThingHandler.class);

    private @NonNullByDefault({}) String unitId;
    private @Nullable Instant lastRefreshTime;

    private Map<String, State> stateCache = new ConcurrentHashMap<>();
    private Map<Long, ParameterDescriptorDTO> paramDescriptionMap = new ConcurrentHashMap<>();
    private @Nullable SubMenuEntryDTO submenu;
    private @Nullable MenuItemTabViewDTO tabmenu;

    public WolfSmartsetUnitThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        unitId = getConfigAs(WolfSmartsetUnitConfiguration.class).unitId;
        logger.debug("UnitThing: Initializing unit '{}'", unitId);
        clearSavedState();
        var bridgeHandler = getBridge();
        if (bridgeHandler != null) {
            bridgeStatusChanged(bridgeHandler.getStatusInfo());
        }
    }

    @Override
    public void dispose() {
        logger.debug("UnitThing: Disposing unit '{}'", unitId);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (this.submenu != null && this.tabmenu != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State state = stateCache.get(channelUID.getId());
            if (state != null) {
                updateState(channelUID.getId(), state);
            }
        }
    }

    /**
     * Get the {@link SubMenuEntryDTO} for this unit
     * 
     * @return the {@link SubMenuEntryDTO} for this unit
     */
    public @Nullable SubMenuEntryDTO getSubMenu() {
        return this.submenu;
    }

    /**
     * Get the {@link MenuItemTabViewDTO} for this unit
     * 
     * @return the {@link MenuItemTabViewDTO} for this unit
     */
    public @Nullable MenuItemTabViewDTO getTabMenu() {
        return this.tabmenu;
    }

    /**
     * Get the {@link Instant} of the last valid call of updateValues
     * 
     * @return the getInstallationDate of the last valid call of updateValues
     */
    public @Nullable Instant getLastRefreshTime() {
        return this.lastRefreshTime;
    }

    /**
     * Update the configuration of this unit and create / update the related channels
     * 
     * @param submenu the {@link SubMenuEntryDTO} for this unit
     * @param tabmenu the {@link MenuItemTabViewDTO} for this unit
     */
    public void updateConfiguration(SubMenuEntryDTO submenu, MenuItemTabViewDTO tabmenu) {
        this.submenu = submenu;
        this.tabmenu = tabmenu;
        var bridgeHandler = getBridge();
        if (bridgeHandler != null) {
            bridgeStatusChanged(bridgeHandler.getStatusInfo());
        }
        lastRefreshTime = null;

        ThingBuilder thingBuilder = editThing();
        var thingId = thing.getUID();

        paramDescriptionMap.clear();
        for (var param : tabmenu.parameterDescriptors) {
            paramDescriptionMap.put(param.valueId, param);
            var channelId = new ChannelUID(thingId, param.parameterId.toString()); // "bindingId:type:thingId:1")
            if (thing.getChannel(channelId) == null) {
                logger.debug("UnitThing: Create channel '{}'", channelId);
                Channel channel = ChannelBuilder.create(channelId, getItemType(param.controlType)).withLabel(param.name)
                        .withType(getChannelType(param)).build();
                thingBuilder.withChannel(channel);
            }
        }

        updateThing(thingBuilder.build());

        for (var param : tabmenu.parameterDescriptors) {
            var channelId = new ChannelUID(thingId, param.parameterId.toString());
            setState(channelId, WolfSmartsetUtils.undefOrString(param.value));
        }
    }

    /**
     * Update the values of the channels
     * 
     * @param values {@link GetParameterValuesDTO} representing the new values
     */
    public void updateValues(@Nullable GetParameterValuesDTO values) {
        var thingId = thing.getUID();
        if (values != null && values.getValues() != null && !values.getValues().isEmpty()) {
            if (!values.getIsNewJobCreated()) {
                lastRefreshTime = Instant.now();
            }

            for (var value : values.getValues()) {
                var param = paramDescriptionMap.get(value.getValueId());
                if (param != null) {
                    var channelId = new ChannelUID(thingId, param.parameterId.toString());
                    setState(channelId, WolfSmartsetUtils.undefOrString(value.getValue()));
                }
            }
        }
    }

    /**
     * Stores the state for the channel in stateCache and calls updateState of this Thing
     * 
     * @param channelId {@link ChannelUID} the id of the channel to update
     * @param state {@link State} the new state for the channel
     */
    private void setState(ChannelUID channelId, State state) {
        stateCache.put(channelId.getId(), state);
        updateState(channelId, state);
    }

    private ChannelTypeUID getChannelType(ParameterDescriptorDTO parmeter) {
        if (parmeter.unit == null || parmeter.unit.isBlank()) {
            if (parmeter.controlType == null) {
                return new ChannelTypeUID(BINDING_ID, CH_STRING);
            } else {
                switch (parmeter.controlType) {
                    case 1:
                    case 3:
                    case 6:
                    case 8:
                        return new ChannelTypeUID(BINDING_ID, CH_NUMBER);
                    case 5:
                        return new ChannelTypeUID(BINDING_ID, CH_CONTACT);
                    case 9:
                    case 10:
                        return new ChannelTypeUID(BINDING_ID, CH_DATETIME);
                    default:
                        return new ChannelTypeUID(BINDING_ID, CH_STRING);
                }
            }
        } else {
            switch (parmeter.unit) {
                case "bar":
                    return new ChannelTypeUID(BINDING_ID, CH_PRESSURE);
                case "%":
                case "Std":
                    return new ChannelTypeUID(BINDING_ID, CH_NUMBER);
                case "°C":
                    return new ChannelTypeUID(BINDING_ID, CH_TEMPERATURE);
                default:
                    return new ChannelTypeUID(BINDING_ID, CH_STRING);
            }
        }
    }

    private String getItemType(Integer controlType) {
        switch (controlType) {
            case 1:
            case 3:
            case 6:
            case 8:
                return "Number";
            case 5:
                return "Contact";
            case 9:
            case 10:
                return "DateTime";
            default:
                return "String";
        }
    }

    private void clearSavedState() {
        stateCache.clear();
    }
}
