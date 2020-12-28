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
package org.openhab.binding.omatic.internal.handler;

import static org.openhab.core.thing.ThingStatus.ONLINE;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omatic.internal.OMaticBindingConstants;
import org.openhab.binding.omatic.internal.OMaticChannel;
import org.openhab.binding.omatic.internal.OMaticMachineThingConfig;
import org.openhab.binding.omatic.internal.api.model.OMaticMachine;
import org.openhab.binding.omatic.internal.api.model.OMaticMachineUtil;
import org.openhab.binding.omatic.internal.event.OMaticEventSubscriber;
import org.openhab.core.events.Event;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * The {@link OMaticClientThingHandler} is responsible for handling commands and status
 * updates for OMatic State Machines.
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
@NonNullByDefault
public class OMaticMachineThingHandler extends BaseThingHandler implements PropertyChangeListener {

    private static final String PREFIX_DEBUG_LOG = "[{}] [{}] {}";

    private static final String PREFIX_INFO_LOG = "[{}] {}";

    private static final Logger logger = LoggerFactory.getLogger(OMaticMachineThingHandler.class);

    private static Set<String> POWER_ITEM_TYPES = new HashSet<>();
    static {
        POWER_ITEM_TYPES.add(CoreItemFactory.SWITCH);
        POWER_ITEM_TYPES.add(CoreItemFactory.NUMBER);
    }

    private static Set<String> ENERGY_ITEM_TYPES = new HashSet<>();
    static {
        ENERGY_ITEM_TYPES.add(CoreItemFactory.NUMBER);
    }

    @Nullable
    private OMaticMachine oMaticMachine;

    @NonNullByDefault({})
    private OMaticEventSubscriber eventSubscriber;

    @NonNullByDefault({})
    private ItemRegistry itemRegistry;

    private @Nullable Double staticPower = null;

    private OMaticMachineThingConfig config = getConfigAs(OMaticMachineThingConfig.class);

    public OMaticMachineThingHandler(Thing thing, OMaticEventSubscriber eventSubscriber, ItemRegistry itemRegistry) {
        super(thing);
        this.eventSubscriber = eventSubscriber;
        this.itemRegistry = itemRegistry;
    }

    @SuppressWarnings("null")
    @Override
    public final void initialize() {
        staticPower = null;
        config = getConfigAs(OMaticMachineThingConfig.class);
        logDebug("SettingConfig name: {} config: {}", config.getName(), config.toString());

        if (eventSubscriber == null) {
            setErrorStatus("Eventsubscriber not initialized");
            return;
        }
        final String powerInputItemName = config.getPowerInputItem().trim();
        final String energyInputItemName = config.getEnergyInputItem().trim();
        final Item powerInputItem = getItemFromRegistry(powerInputItemName);
        if (powerInputItem == null) {
            setErrorStatus("No item could be found for PowerInputItem name: " + powerInputItemName);
            return;
        }

        if (!isCompatiblePowerType(powerInputItem)) {
            setErrorStatus("PowerInputItem type Error: " + powerInputItem.getType());
            return;
        }

        final Double staticPower = config.getStaticPower();
        if (staticPower != null && staticPower > 0.0D) {
            if (powerInputItem.getType().equals(CoreItemFactory.SWITCH)) {
                this.staticPower = staticPower;
            } else {
                setErrorStatus("PowerInputItem static type Error: " + powerInputItem.getType());
                return;
            }
        }

        Item energyInputItem = getItemFromRegistry(energyInputItemName);
        if (!energyInputItemName.isEmpty() && energyInputItem == null) {
            setErrorStatus("Energy input item not found Error: " + energyInputItemName);
            return;
        } else {
            if (energyInputItem != null && isCompatibleEnergyType(energyInputItem)) {
                eventSubscriber.addItemItemName(energyInputItemName);
            } else {
                logDebug("No energy input item set, or incompatible type: {}", energyInputItemName);
            }
        }
        logInfo("Initializing, setting status ONLINE");
        oMaticMachine = new OMaticMachine(scheduler, config);
        oMaticMachine.addPropertyChangeListener(this);
        restoreProperties();
        eventSubscriber.addPropertyChangeListener(this);
        eventSubscriber.addItemItemName(powerInputItemName);
        updateStatus(ONLINE);
    }

    private void setErrorStatus(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
        logger.error("OMatic Machine Offline due to failure: {}", message);
    }

    private boolean isCompatibleEnergyType(Item energyInputItem) {
        return ENERGY_ITEM_TYPES.contains(energyInputItem.getType());
    }

    private @Nullable Item getItemFromRegistry(final String itemName) {
        if (!itemName.isEmpty()) {
            try {
                return itemRegistry.getItem(itemName);
            } catch (ItemNotFoundException e) {
                logDebug("Failed to find Item: {} in registry", itemName);
            }
        }
        return null;
    }

    private boolean isCompatiblePowerType(Item item) {
        return POWER_ITEM_TYPES.contains(item.getType());
    }

    @SuppressWarnings("null")
    private void logDebug(String message, Object... parameters) {
        final FormattingTuple logMessage = MessageFormatter.arrayFormat(message, parameters);
        logger.debug(PREFIX_DEBUG_LOG, oMaticMachine != null ? oMaticMachine.getConfig().getName() : "", hashCode(),
                logMessage.getMessage());
    }

    @SuppressWarnings("null")
    private void logInfo(String message, Object... parameters) {
        final FormattingTuple logMessage = MessageFormatter.arrayFormat(message, parameters);
        logger.info(PREFIX_INFO_LOG, oMaticMachine != null ? oMaticMachine.getConfig().getName() : "",
                logMessage.getMessage());
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        if (oMaticMachine != null) {
            oMaticMachine.removePropertyChangeListener(this);
            oMaticMachine.terminate();
            oMaticMachine = null;
        }
        if (eventSubscriber != null) {
            eventSubscriber.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @SuppressWarnings("null")
    private void restoreProperties() {
        Double maxPower = OMaticMachineUtil.loadPropertyAsDouble(getThing(), OMaticBindingConstants.PROPERTY_MAX_POWER);
        oMaticMachine.setMaxPower(maxPower);
        logDebug("Restoring max power: {}", maxPower);
        long totalTime = OMaticMachineUtil.loadPropertyAsLong(getThing(), OMaticBindingConstants.PROPERTY_TOTAL_TIME);
        oMaticMachine.setTotalTime(totalTime);
        logDebug("Restoring Total time: {}", totalTime);
        double totalEnergy = OMaticMachineUtil.loadPropertyAsDouble(getThing(),
                OMaticBindingConstants.PROPERTY_TOTAL_ENERGY);
        oMaticMachine.setTotalEnergy(totalEnergy);
        double totalEnergyEstimated = OMaticMachineUtil.loadPropertyAsDouble(getThing(),
                OMaticBindingConstants.PROPERTY_TOTAL_ESTIMATED_ENERGY);
        oMaticMachine.setTotalEnergyEstimated(totalEnergyEstimated);
        double lastKnownEnergyValue = OMaticMachineUtil.loadPropertyAsDouble(getThing(),
                OMaticBindingConstants.PROPERTY_LAST_KNOWN_ENERGY_VALUE);
        oMaticMachine.setLatKnownEnergyValue(lastKnownEnergyValue);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logDebug("Handling command = {} for channel = {}", command.getClass(), channelUID);
        String channelId = channelUID.getIdWithoutGroup();
        OMaticChannel channel = OMaticChannel.fromString(channelId);
        if (command instanceof RefreshType) {
            refreshChannel(channel, channelId);
            return;
        }
        if (command instanceof OnOffType) {
            updateOnOffChannel(channel, channelId, (OnOffType) command);
            return;
        }
    }

    @SuppressWarnings("null")
    private void updateOnOffChannel(OMaticChannel channel, String channelId, OnOffType command) {
        switch (channel) {
            case RESET:
                logInfo("Resetting all stored properies for thing");
                oMaticMachine.reset();
                storeProperty(OMaticBindingConstants.PROPERTY_MAX_POWER, "" + 0.0D);
                storeProperty(OMaticBindingConstants.PROPERTY_TOTAL_TIME, "" + 0);
                storeProperty(OMaticBindingConstants.PROPERTY_TOTAL_ENERGY, "" + 0.0D);
                storeProperty(OMaticBindingConstants.PROPERTY_TOTAL_ESTIMATED_ENERGY, "" + 0.0D);
                storeProperty(OMaticBindingConstants.PROPERTY_LAST_KNOWN_ENERGY_VALUE, "" + 0.0D);
                refreshChannels();
                break;
            case DISABLE:
                logInfo("Setting disable: {}", command == OnOffType.ON);
                oMaticMachine.disable(command == OnOffType.ON);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("null")
    private void refreshChannel(OMaticChannel channel, String channelId) {
        State state = UnDefType.NULL;
        switch (channel) {
            case ENERGY1:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getEnergy());
                break;
            case ENERGY2:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getEstimatedEnergy());
                break;
            case MAX_POWER:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getMaxPower());
                break;
            case RUNNING:
                state = OnOffType.from(oMaticMachine.isRunning());
                break;
            case POWER:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getPower());
                break;
            case TOTAL_COST1:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getTotalCost());
                break;
            case TOTAL_COST2:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getEstimatedTotalCost());
                break;
            case COST1:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getCost());
                break;
            case COST2:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getEstimatedCost());
                break;
            case TOTAL_ENERGY1:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getTotalEnergy());
                break;
            case TOTAL_ENERGY2:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getTotalEnergyEstimated());
                break;
            case TOTAL_TIME:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getTotalTime());
                break;
            case TOTAL_TIME_STR:
                state = new StringType(oMaticMachine.getTotalTimeString());
                break;
            case TIME:
                state = OMaticMachineUtil.getStateFromDecimalType(oMaticMachine.getRunningTime());
                break;
            case TIME_STR:
                state = new StringType(oMaticMachine.getRunningTimeString());
                break;
            case STATE:
                state = new StringType(oMaticMachine.getState().toString());
                break;
            case COMPLETED:
                state = OMaticMachineUtil.getStateFromTimeStr(oMaticMachine.getCompletedTimeStr());
                break;
            case STARTED:
                state = OMaticMachineUtil.getStateFromTimeStr(oMaticMachine.getStartedTimeStr());
                break;
            case RESET:
            case DISABLE:
                logDebug("Not handling refresh for these: {}", channel.toString());
                break;
            default:
                logger.warn("{} Unexpected error handling refresh command for channel = {} : {}",
                        oMaticMachine.getConfig().getName(), channel, "NOT IMPL");
                break;
        }
        if (state != UnDefType.NULL) {
            updateState(channelId, state);
            logDebug("Update state by refresh: {} channelid: {}", state, channelId);
        }
    }

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return OMaticBindingConstants.THING_TYPE_MACHINE.equals(thingTypeUID);
    }

    @SuppressWarnings("null")
    @Override
    public synchronized void propertyChange(@Nullable PropertyChangeEvent evt) {
        logDebug("Change event!");
        if (evt.getPropertyName().equals(OMaticEventSubscriber.PROPERTY_ITEM_EVENT)) {
            logDebug("Property change item event! : {}", ((Event) evt.getNewValue()).getTopic());
            handleEventUpdate((Event) evt.getNewValue());
            return;
        }

        if (evt.getPropertyName().equals(OMaticBindingConstants.PROPERTY_POWER_INPUT)) {
            refreshChannels();
            return;
        }

        if (evt.getPropertyName().equals(OMaticBindingConstants.PROPERTY_MAX_POWER)) {
            storeProperty(OMaticBindingConstants.PROPERTY_MAX_POWER, "" + (double) evt.getNewValue());
            logInfo("Updating maxPower: {}", (double) evt.getNewValue());
            refreshChannels();
            return;
        }

        if (evt.getPropertyName().equals(OMaticBindingConstants.PROPERTY_COMPLETED)) {
            storeProperty(OMaticBindingConstants.PROPERTY_TOTAL_TIME, "" + oMaticMachine.getTotalTime());
            storeProperty(OMaticBindingConstants.PROPERTY_TOTAL_ENERGY, "" + oMaticMachine.getTotalEnergy());
            storeProperty(OMaticBindingConstants.PROPERTY_TOTAL_ESTIMATED_ENERGY,
                    "" + oMaticMachine.getEstimatedEnergy());
            refreshChannels();
            logInfo("State Machine completed time: {}, energy(measured): {}, energy(estimated): {}",
                    oMaticMachine.getRunningTimeString(), oMaticMachine.getEnergy(),
                    oMaticMachine.getEstimatedEnergy());
            return;
        }

        if (evt.getPropertyName().equals(OMaticBindingConstants.PROPERTY_LAST_KNOWN_ENERGY_VALUE)) {
            storeProperty(OMaticBindingConstants.PROPERTY_LAST_KNOWN_ENERGY_VALUE,
                    "" + oMaticMachine.getLastKnownEnergyValue());
            refreshChannels();
            return;
        }

        logDebug("Failed to handle property change for prop: {}", evt.getPropertyName());
    }

    @SuppressWarnings("null")
    private synchronized void handleEventUpdate(Event event) {
        logDebug("Handle event: {}", event.getTopic());
        String itemName = null;
        String stringValue = null;
        if (event instanceof ItemStateEvent) {
            itemName = ((ItemStateEvent) event).getItemName();
            logDebug("Name of state event: {} configName: {}", itemName, config.getPowerInputItem());
            stringValue = ((ItemStateEvent) event).getItemState().toFullString();
            String type = ((ItemStateEvent) event).getType();
            if (itemName.equals(config.getPowerInputItem())) {
                double itemValue = 0;
                if (staticPower != null && staticPower > 0) {
                    itemValue = stringValue.equals(OnOffType.ON.name()) ? staticPower : 0;
                    logDebug("Got static value: {}", itemValue);
                } else {
                    try {
                        itemValue = Double.parseDouble(stringValue);
                        logDebug("Got number value: {}", itemValue);
                    } catch (NumberFormatException ex) {
                        logger.error("Failed to parse value from event {}", ((ItemStateEvent) event).getPayload());
                        return;
                    }
                }
                oMaticMachine.powerInput(itemValue);
            } else if (itemName.equals(config.getEnergyInputItem())) {
                Double itemValue = null;
                try {
                    itemValue = Double.parseDouble(stringValue);
                    oMaticMachine.energyInput(itemValue);
                } catch (NumberFormatException ex) {
                    logger.error("Failed to parse value from event {}", ((ItemStateEvent) event).getPayload());
                    return;
                }
            } else {
                logDebug("Failed to determine what to do with event: {}", event.getPayload());
                logDebug("ItemName: {} configName: {}", itemName, config.getPowerInputItem());
            }
        } else {
            logDebug("Not parsing event, since no item state event: {}", event.getClass());
        }
    }

    private void storeProperty(String key, String value) {
        logDebug("Storing {}: {}", key, value);
        Map<String, String> properties = editProperties();
        properties.put(key, value);
        updateProperties(properties);
    }

    private void refreshChannels() {
        if (getThing().getStatus() == ONLINE) {
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                String channelId = channelUID.getIdWithoutGroup();
                OMaticChannel omaticChannel = OMaticChannel.fromString(channelId);
                refreshChannel(omaticChannel, channelId);
            }
        }
    }
}
