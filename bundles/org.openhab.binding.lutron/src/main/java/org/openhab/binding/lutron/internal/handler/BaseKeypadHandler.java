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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfig;
import org.openhab.binding.lutron.internal.protocol.DeviceCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class providing common definitions and methods for derived keypad classes
 *
 * @author Bob Adair - Initial contribution, based partly on Allan Tong's KeypadHandler class
 */
public abstract class BaseKeypadHandler extends LutronHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseKeypadHandler.class);

    protected List<KeypadComponent> buttonList = new ArrayList<>();
    protected List<KeypadComponent> ledList = new ArrayList<>();
    protected List<KeypadComponent> cciList = new ArrayList<>();

    Map<Integer, Integer> leapButtonMap;

    protected int integrationId;
    protected String model;
    protected Boolean autoRelease;
    protected Boolean advancedChannels = false;

    protected Map<Integer, String> componentChannelMap = new HashMap<>(50);

    protected abstract void configureComponents(String model);

    private final Object asyncInitLock = new Object();

    protected KeypadConfig kp;
    protected TargetType commandTargetType = TargetType.KEYPAD; // For LEAP bridge

    public BaseKeypadHandler(Thing thing) {
        super(thing);
    }

    /**
     * Determine if keypad component with the specified id is a LED. Keypad handlers which do not use a KeypadConfig
     * object must override this to provide their own test.
     *
     * @param id The component id.
     * @return True if the component is a LED.
     */
    protected boolean isLed(int id) {
        return kp.isLed(id);
    }

    /**
     * Determine if keypad component with the specified id is a button. Keypad handlers which do not use a KeypadConfig
     * object must override this to provide their own test.
     *
     * @param id The component id.
     * @return True if the component is a button.
     */
    protected boolean isButton(int id) {
        return kp.isButton(id);
    }

    /**
     * Determine if keypad component with the specified id is a CCI. Keypad handlers which do not use a KeypadConfig
     * object must override this to provide their own test.
     *
     * @param id The component id.
     * @return True if the component is a CCI.
     */
    protected boolean isCCI(int id) {
        return kp.isCCI(id);
    }

    protected void configureChannels() {
        Channel channel;
        ChannelTypeUID channelTypeUID;
        ChannelUID channelUID;

        logger.debug("Configuring channels for keypad {}", integrationId);

        List<Channel> channelList = new ArrayList<>();
        List<Channel> existingChannels = getThing().getChannels();

        if (!existingChannels.isEmpty()) {
            // Clear existing channels
            logger.debug("Clearing existing channels for keypad {}", integrationId);
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }

        ThingBuilder thingBuilder = editThing();

        // add channels for buttons
        for (KeypadComponent component : buttonList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, advancedChannels ? "buttonAdvanced" : "button");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            channel = ChannelBuilder.create(channelUID, "Switch").withType(channelTypeUID)
                    .withLabel(component.description()).build();
            channelList.add(channel);
        }

        // add channels for LEDs
        for (KeypadComponent component : ledList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, advancedChannels ? "ledIndicatorAdvanced" : "ledIndicator");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            channel = ChannelBuilder.create(channelUID, "Switch").withType(channelTypeUID)
                    .withLabel(component.description()).build();
            channelList.add(channel);
        }

        // add channels for CCIs (for VCRX or eventually HomeWorks CCI)
        for (KeypadComponent component : cciList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "cciState");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            channel = ChannelBuilder.create(channelUID, "Contact").withType(channelTypeUID)
                    .withLabel(component.description()).build();
            channelList.add(channel);
        }

        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
        logger.debug("Done configuring channels for keypad {}", integrationId);
    }

    protected ChannelUID channelFromComponent(int component) {
        String channel = null;

        // Get channel string from Lutron component ID using HashBiMap
        channel = componentChannelMap.get(component);
        if (channel == null) {
            logger.debug("Unknown component {}", component);
        }
        return channel == null ? null : new ChannelUID(getThing().getUID(), channel);
    }

    protected Integer componentFromChannel(ChannelUID channelUID) {
        return componentChannelMap.entrySet().stream().filter(e -> e.getValue().equals(channelUID.getId()))
                .map(Entry::getKey).findAny().orElse(null);
    }

    @Override
    public int getIntegrationId() {
        return integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();

        logger.debug("Initializing Keypad Handler for integration ID {}", id);

        model = (String) getThing().getConfiguration().get("model");
        if (model != null) {
            model = model.toUpperCase();
            if (model.contains("-")) {
                // strip off system prefix if model is of the form "system-model"
                String[] modelSplit = model.split("-", 2);
                model = modelSplit[1];
            }
        }

        Boolean arParam = (Boolean) getThing().getConfiguration().get("autorelease");
        autoRelease = arParam == null ? true : arParam;

        // schedule a thread to finish initialization asynchronously since it can take several seconds
        scheduler.schedule(this::asyncInitialize, 0, TimeUnit.SECONDS);
    }

    private void asyncInitialize() {
        synchronized (asyncInitLock) {
            logger.debug("Async init thread staring for keypad handler {}", integrationId);

            buttonList.clear(); // in case we are re-initializing
            ledList.clear();
            cciList.clear();
            componentChannelMap.clear();

            configureComponents(model);

            // load the channel-id map
            for (KeypadComponent component : buttonList) {
                componentChannelMap.put(component.id(), component.channel());
            }
            for (KeypadComponent component : ledList) {
                componentChannelMap.put(component.id(), component.channel());
            }
            for (KeypadComponent component : cciList) {
                componentChannelMap.put(component.id(), component.channel());
            }

            configureChannels();

            initDeviceState();

            logger.debug("Async init thread finishing for keypad handler {}", integrationId);
        }
    }

    @Override
    public void initDeviceState() {
        synchronized (asyncInitLock) {
            logger.debug("Initializing device state for Keypad {}", integrationId);
            Bridge bridge = getBridge();
            if (bridge == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            } else if (bridge.getStatus() == ThingStatus.ONLINE) {
                if (ledList.isEmpty()) {
                    // Device with no LEDs has nothing to query. Assume it is online if bridge is online.
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    // Query LED states. Method handleUpdate() will set thing status to online when response arrives.
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
                    // To reduce query volume, query only 1st LED and LEDs with linked channels.
                    for (KeypadComponent component : ledList) {
                        if (component.id() == ledList.get(0).id() || isLinked(channelFromComponent(component.id()))) {
                            queryDevice(commandTargetType, component.id(), DeviceCommand.ACTION_LED_STATE);
                        }
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, Command command) {
        logger.debug("Handling command {} for channel {}", command, channelUID);

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Command received on invalid channel {} for device {}", channelUID, getThing().getUID());
            return;
        }

        Integer componentID = componentFromChannel(channelUID);
        if (componentID == null) {
            logger.warn("Command received on invalid channel {} for device {}", channelUID, getThing().getUID());
            return;
        }

        // For LEDs, handle RefreshType and OnOffType commands
        if (isLed(componentID)) {
            if (command instanceof RefreshType) {
                queryDevice(commandTargetType, componentID, DeviceCommand.ACTION_LED_STATE);
            } else if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    device(commandTargetType, componentID, null, DeviceCommand.ACTION_LED_STATE, DeviceCommand.LED_ON);
                } else if (command == OnOffType.OFF) {
                    device(commandTargetType, componentID, null, DeviceCommand.ACTION_LED_STATE, DeviceCommand.LED_OFF);
                }
            } else {
                logger.warn("Invalid command {} received for channel {} device {}", command, channelUID,
                        getThing().getUID());
            }
            return;
        }

        // For buttons, handle OnOffType commands
        if (isButton(componentID)) {
            if (command instanceof OnOffType) {
                // Annotate commands with LEAP button number for LEAP bridge
                Integer leapComponent = (this.leapButtonMap == null) ? null : leapButtonMap.get(componentID);
                if (command == OnOffType.ON) {
                    device(commandTargetType, componentID, leapComponent, DeviceCommand.ACTION_PRESS, null);
                    if (autoRelease) {
                        device(commandTargetType, componentID, leapComponent, DeviceCommand.ACTION_RELEASE, null);
                    }
                } else if (command == OnOffType.OFF) {
                    device(commandTargetType, componentID, leapComponent, DeviceCommand.ACTION_RELEASE, null);
                }
            } else {
                logger.warn("Invalid command type {} received for channel {} device {}", command, channelUID,
                        getThing().getUID());
            }
            return;
        }

        // Contact channels for CCIs are read-only, so ignore commands
        if (isCCI(componentID)) {
            logger.debug("Invalid command type {} received for channel {} device {}", command, channelUID,
                    getThing().getUID());
            return;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Linking keypad {} channel {}", integrationId, channelUID.getId());

        Integer id = componentFromChannel(channelUID);
        if (id == null) {
            logger.warn("Unrecognized channel ID {} linked", channelUID.getId());
            return;
        }

        // if this channel is for an LED, query the Lutron controller for the current state
        if (isLed(id)) {
            queryDevice(commandTargetType, id, DeviceCommand.ACTION_LED_STATE);
        }
        // Button and CCI state can't be queried, only monitored for updates.
        // Init button state to OFF on channel init.
        if (isButton(id)) {
            updateState(channelUID, OnOffType.OFF);
        }
        // Leave CCI channel state undefined on channel init.
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        logger.trace("Handling command {} {} from keypad {}", type, parameters, integrationId);
        if (type == LutronCommandType.DEVICE && parameters.length >= 2) {
            int component;

            try {
                component = Integer.parseInt(parameters[0]);
            } catch (NumberFormatException e) {
                logger.error("Invalid component {} in keypad update event message", parameters[0]);
                return;
            }

            ChannelUID channelUID = channelFromComponent(component);

            if (channelUID != null) {
                if (DeviceCommand.ACTION_LED_STATE.toString().equals(parameters[1]) && parameters.length >= 3) {
                    if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                        updateStatus(ThingStatus.ONLINE); // set thing status online if this is an initial response
                    }
                    if (DeviceCommand.LED_ON.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.ON);
                    } else if (DeviceCommand.LED_OFF.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.OFF);
                    }
                } else if (DeviceCommand.ACTION_PRESS.toString().equals(parameters[1])) {
                    if (isButton(component)) {
                        updateState(channelUID, OnOffType.ON);
                        if (autoRelease) {
                            updateState(channelUID, OnOffType.OFF);
                        }
                    } else { // component is CCI
                        updateState(channelUID, OpenClosedType.CLOSED);
                    }
                } else if (DeviceCommand.ACTION_RELEASE.toString().equals(parameters[1])) {
                    if (isButton(component)) {
                        updateState(channelUID, OnOffType.OFF);
                    } else { // component is CCI
                        updateState(channelUID, OpenClosedType.OPEN);
                    }
                } else if (DeviceCommand.ACTION_HOLD.toString().equals(parameters[1])) {
                    updateState(channelUID, OnOffType.OFF); // Signal a release if we receive a hold code as we will not
                                                            // get a subsequent release.
                }
            } else {
                logger.warn("Unable to determine channel for component {} in keypad update event message",
                        parameters[0]);
            }
        }
    }
}
