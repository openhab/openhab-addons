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
package org.openhab.binding.dirigera.internal.handler.light;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.ColorLightConfiguration;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.interfaces.PowerListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BaseLight} for handling light commands in a controlled way
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BaseLight extends BaseHandler implements PowerListener {
    private final Logger logger = LoggerFactory.getLogger(BaseLight.class);

    protected ColorLightConfiguration lightConfig = new ColorLightConfiguration();
    protected Map<LightCommand.Action, LightCommand> lastUserMode = new HashMap<>();

    private List<LightCommand> lightRequestQueue = new ArrayList<>();
    private Instant readyForNextCommand = Instant.now();
    private JSONObject placeHolder = new JSONObject();
    private boolean executingCommand = false;

    public BaseLight(Thing thing, Map<String, String> mapping) {
        super(thing, mapping);
        super.setChildHandler(this);
        // links of types which can be established towards this device
        linkCandidateTypes = List.of(DEVICE_TYPE_LIGHT_CONTROLLER, DEVICE_TYPE_MOTION_SENSOR);
    }

    @Override
    public void initialize() {
        super.initialize();
        lightConfig = getConfigAs(ColorLightConfiguration.class);
        super.addPowerListener(this);
    }

    @Override
    public void dispose() {
        super.removePowerListener(this);
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();
        if (CHANNEL_POWER_STATE.equals(channel) && (command instanceof OnOffType onOff)) {
            // route power state into queue instead of direct switch on / off
            addOnOffCommand(OnOffType.ON.equals(onOff));
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    protected void addOnOffCommand(boolean on) {
        LightCommand command;
        if (on) {
            command = new LightCommand(placeHolder, LightCommand.Action.ON);
        } else {
            command = new LightCommand(placeHolder, LightCommand.Action.OFF);
        }
        synchronized (lightRequestQueue) {
            lightRequestQueue.add(command);
            if (customDebug) {
                logger.info("DIRIGERA BASE_LIGHT {} add command {}", thing.getLabel(), command.toString());
            }
        }
        scheduler.execute(this::executeCommand);
    }

    protected void addCommand(@Nullable LightCommand command) {
        if (command == null) {
            return;
        }
        synchronized (lightRequestQueue) {
            lightRequestQueue.add(command);
            if (customDebug) {
                logger.info("DIRIGERA BASE_LIGHT {} add command {}", thing.getLabel(), command.toString());
            }
        }
        scheduler.execute(this::executeCommand);
    }

    /**
     * execute commands in the order and delays of the lightRequestQueue
     */
    protected void executeCommand() {
        LightCommand request = null;
        synchronized (lightRequestQueue) {
            if (lightRequestQueue.isEmpty()) {
                return;
            }

            // wait for next time window and previous command is fully executed
            while (readyForNextCommand.isAfter(Instant.now()) || executingCommand) {
                try {
                    lightRequestQueue.wait(50);
                } catch (InterruptedException e) {
                    lightRequestQueue.clear();
                    Thread.interrupted();
                    return;
                }
            }

            /*
             * get command from queue and check if it needs to be executed
             * if several requests of the same kind e.g. 5 brightness requests are in only the last one shall be
             * executed
             */
            if (!lightRequestQueue.isEmpty()) {
                request = lightRequestQueue.remove(0);
            } else {
                lightRequestQueue.notifyAll();
                return;
            }
            if (lightRequestQueue.contains(request)) {
                lightRequestQueue.notifyAll();
                return;
            }
            // now execute command
            executingCommand = true;
        }
        if (customDebug) {
            logger.info("DIRIGERA BASE_LIGHT {} execute {}", thing.getLabel(), request);
        }
        int addonMillis = 0;
        switch (request.action) {
            case ON:
                super.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), OnOffType.ON);
                addonMillis = lightConfig.fadeTime;
                break;
            case OFF:
                super.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), OnOffType.OFF);
                break;
            case BRIGHTNESS:
            case TEMPERATURE:
            case COLOR:
                super.sendAttributes(request.request);
                if (isPowered()) {
                    addonMillis = lightConfig.fadeTime;
                }
                break;
        }
        // after command is sent to API add the time
        readyForNextCommand = Instant.now().plus(addonMillis, ChronoUnit.MILLIS);
        synchronized (lightRequestQueue) {
            executingCommand = false;
            lightRequestQueue.notifyAll();
        }
    }

    protected void changeProperty(LightCommand.Action action, JSONObject request) {
        LightCommand requestedCommand = new LightCommand(request, action);
        if (isPowered()) {
            addCommand(requestedCommand);
        } else {
            lastUserMode.put(action, requestedCommand);
            switch (action) {
                case COLOR:
                    addCommand(requestedCommand);
                    lastUserMode.remove(LightCommand.Action.TEMPERATURE);
                    break;
                case TEMPERATURE:
                    addCommand(requestedCommand);
                    lastUserMode.remove(LightCommand.Action.COLOR);
                    break;
                case BRIGHTNESS:
                case ON:
                case OFF:
                default:
                    break;
            }
            logger.trace("DIRIGERA BASE_LIGHT {} last user mode settings {}", thing.getLabel(), lastUserMode);
        }
    }

    @Override
    public void powerChanged(OnOffType power, boolean requested) {
        // apply lum settings according to configuration in the right sequence if power changed to ON
        if (OnOffType.ON.equals(power)) {
            if (!requested) {
                addOnOffCommand(true);
            }
            if (customDebug) {
                logger.info("DIRIGERA BASE_LIGHT {} last user mode restore {}", thing.getLabel(), lastUserMode);
            }
            LightCommand brightnessCommand = lastUserMode.remove(LightCommand.Action.BRIGHTNESS);
            LightCommand colorCommand = lastUserMode.remove(LightCommand.Action.COLOR);
            LightCommand temperatureCommand = lastUserMode.remove(LightCommand.Action.TEMPERATURE);
            switch (lightConfig.fadeSequence) {
                case 0:
                    addCommand(brightnessCommand);
                    addCommand(colorCommand);
                    addCommand(temperatureCommand);
                    break;
                case 1:
                    addCommand(colorCommand);
                    addCommand(temperatureCommand);
                    addCommand(brightnessCommand);
                    break;
            }
        } else {
            // assure settings are clean for next startup
            lastUserMode.clear();
        }
    }
}
