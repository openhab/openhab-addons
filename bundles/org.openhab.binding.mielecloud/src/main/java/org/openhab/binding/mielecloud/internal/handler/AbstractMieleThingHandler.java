/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.handler;

import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;
import static org.openhab.binding.mielecloud.internal.webservice.api.PowerStatus.*;
import static org.openhab.binding.mielecloud.internal.webservice.api.ProgramStatus.*;
import static org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction.*;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.I18NKeys;
import org.openhab.binding.mielecloud.internal.discovery.ThingInformationExtractor;
import org.openhab.binding.mielecloud.internal.handler.channel.ActionsChannelState;
import org.openhab.binding.mielecloud.internal.handler.channel.DeviceChannelState;
import org.openhab.binding.mielecloud.internal.handler.channel.TransitionChannelState;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.UnavailableMieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.PowerStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.ProgramStatus;
import org.openhab.binding.mielecloud.internal.webservice.api.TransitionState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all Miele thing handlers.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Add channel state wrappers
 */
@NonNullByDefault
public abstract class AbstractMieleThingHandler extends BaseThingHandler {
    protected DeviceState latestDeviceState = new DeviceState(getDeviceId(), null);
    protected TransitionState latestTransitionState = new TransitionState(null, latestDeviceState);
    protected ActionsState latestActionsState = new ActionsState(getDeviceId(), null);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates a new {@link AbstractMieleThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public AbstractMieleThingHandler(Thing thing) {
        super(thing);
    }

    private Optional<MieleBridgeHandler> getMieleBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return Optional.empty();
        }

        BridgeHandler handler = bridge.getHandler();
        if (!(handler instanceof MieleBridgeHandler)) {
            return Optional.empty();
        }

        return Optional.of((MieleBridgeHandler) handler);
    }

    protected MieleWebservice getWebservice() {
        return getMieleBridgeHandler().map(MieleBridgeHandler::getWebservice)
                .orElse(UnavailableMieleWebservice.INSTANCE);
    }

    @Override
    public void initialize() {
        getWebservice().dispatchDeviceState(getDeviceId());

        // If no device state update was received so far, set the device to OFFLINE.
        if (getThing().getStatus() == ThingStatus.INITIALIZING) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            updateDeviceState(new DeviceChannelState(latestDeviceState));
            updateTransitionState(new TransitionChannelState(latestTransitionState));
            updateActionState(new ActionsChannelState(latestActionsState));
        }

        switch (channelUID.getId()) {
            case PROGRAM_START_STOP:
                if (PROGRAM_STARTED.matches(command.toString())) {
                    triggerProcessAction(START);
                } else if (PROGRAM_STOPPED.matches(command.toString())) {
                    triggerProcessAction(STOP);
                }
                break;

            case PROGRAM_START_STOP_PAUSE:
                if (PROGRAM_STARTED.matches(command.toString())) {
                    triggerProcessAction(START);
                } else if (PROGRAM_STOPPED.matches(command.toString())) {
                    triggerProcessAction(STOP);
                } else if (PROGRAM_PAUSED.matches(command.toString())) {
                    triggerProcessAction(PAUSE);
                }
                break;

            case LIGHT_SWITCH:
                if (command instanceof OnOffType) {
                    triggerLight(OnOffType.ON.equals(command));
                }
                break;

            case POWER_ON_OFF:
                if (POWER_ON.matches(command.toString()) || POWER_OFF.matches(command.toString())) {
                    triggerPowerState(OnOffType.ON.equals(OnOffType.from(command.toString())));
                }
                break;
        }
    }

    @Override
    public void dispose() {
    }

    /**
     * Invoked when an update of the available actions for the device managed by this handler is received from the Miele
     * cloud.
     */
    public final void onProcessActionUpdated(ActionsState actionState) {
        latestActionsState = actionState;
        updateActionState(new ActionsChannelState(latestActionsState));
    }

    /**
     * Invoked when the device managed by this handler was removed from the Miele cloud.
     */
    public final void onDeviceRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, I18NKeys.THING_STATUS_DESCRIPTION_REMOVED);
    }

    /**
     * Invoked when a device state update for the device managed by this handler is received from the Miele cloud.
     */
    public final void onDeviceStateUpdated(DeviceState deviceState) {
        latestTransitionState = new TransitionState(latestTransitionState, deviceState);
        latestDeviceState = deviceState;

        updateThingProperties(deviceState);
        updateDeviceState(new DeviceChannelState(latestDeviceState));
        updateTransitionState(new TransitionChannelState(latestTransitionState));
        updateThingStatus(latestDeviceState);
    }

    protected void triggerProcessAction(final ProcessAction processAction) {
        performPutAction(() -> getWebservice().putProcessAction(getDeviceId(), processAction),
                t -> logger.warn("Failed to perform '{}' operation for device '{}'.", processAction, getDeviceId(), t));
    }

    protected void triggerLight(final boolean on) {
        performPutAction(() -> getWebservice().putLight(getDeviceId(), on),
                t -> logger.warn("Failed to set light state to '{}' for device '{}'.", on, getDeviceId(), t));
    }

    protected void triggerPowerState(final boolean on) {
        performPutAction(() -> getWebservice().putPowerState(getDeviceId(), on),
                t -> logger.warn("Failed to set the power state to '{}' for device '{}'.", on, getDeviceId(), t));
    }

    protected void triggerProgram(final long programId) {
        performPutAction(() -> getWebservice().putProgram(getDeviceId(), programId), t -> logger
                .warn("Failed to activate program with ID '{}' for device '{}'.", programId, getDeviceId(), t));
    }

    private void performPutAction(Runnable action, Consumer<Exception> onError) {
        scheduler.execute(() -> {
            try {
                action.run();
            } catch (TooManyRequestsException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        I18NKeys.THING_STATUS_DESCRIPTION_RATELIMIT);
                onError.accept(e);
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    protected final String getDeviceId() {
        return getConfig().get(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER).toString();
    }

    /**
     * Creates a {@link ChannelUID} from the given name.
     *
     * @param name channel name
     * @return {@link ChannelUID}
     */
    protected ChannelUID channel(String name) {
        return new ChannelUID(getThing().getUID(), name);
    }

    /**
     * Updates the thing status depending on whether the managed device is connected and reachable.
     */
    private void updateThingStatus(DeviceState deviceState) {
        if (deviceState.isInState(StateType.NOT_CONNECTED)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    I18NKeys.THING_STATUS_DESCRIPTION_DISCONNECTED);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Determines the status of the currently selected program.
     */
    protected ProgramStatus getProgramStatus(StateType rawStatus) {
        if (rawStatus.equals(StateType.RUNNING)) {
            return PROGRAM_STARTED;
        }
        return PROGRAM_STOPPED;
    }

    /**
     * Determines the power status of the managed device.
     */
    protected PowerStatus getPowerStatus(StateType rawStatus) {
        if (rawStatus.equals(StateType.OFF) || rawStatus.equals(StateType.NOT_CONNECTED)) {
            return POWER_OFF;
        }
        return POWER_ON;
    }

    /**
     * Updates the thing properties. This is necessary if properties have not been set during discovery.
     */
    private void updateThingProperties(DeviceState deviceState) {
        var properties = editProperties();
        properties.putAll(ThingInformationExtractor.extractProperties(getThing().getThingTypeUID(), deviceState));
        updateProperties(properties);
    }

    /**
     * Updates the device state channels.
     *
     * @param device The {@link DeviceChannelState} information to update the device channel states with.
     */
    protected abstract void updateDeviceState(DeviceChannelState device);

    /**
     * Updates the transition state channels.
     *
     * @param transition The {@link TransitionChannelState} information to update the transition channel states with.
     */
    protected abstract void updateTransitionState(TransitionChannelState transition);

    /**
     * Updates the device action state channels.
     *
     * @param actions The {@link ActionsChannelState} information to update the action channel states with.
     */
    protected abstract void updateActionState(ActionsChannelState actions);
}
