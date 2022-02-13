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
package org.openhab.binding.hue.internal.handler;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.FullGroup;
import org.openhab.binding.hue.internal.Scene;
import org.openhab.binding.hue.internal.State;
import org.openhab.binding.hue.internal.StateUpdate;
import org.openhab.binding.hue.internal.dto.ColorTemperature;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueGroupHandler} is the handler for a hue group of lights. It uses the {@link HueClient} to execute the
 * actual command.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class HueGroupHandler extends BaseThingHandler implements HueLightActionsHandler, GroupStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GROUP);
    public static final String PROPERTY_MEMBERS = "members";

    private final Logger logger = LoggerFactory.getLogger(HueGroupHandler.class);
    private final HueStateDescriptionProvider stateDescriptionOptionProvider;

    private @NonNullByDefault({}) String groupId;

    private @Nullable Integer lastSentColorTemp;
    private @Nullable Integer lastSentBrightness;

    private ColorTemperature colorTemperatureCapabilties = new ColorTemperature();
    private long defaultFadeTime = 400;

    private @Nullable HueClient hueClient;

    private @Nullable ScheduledFuture<?> scheduledFuture;
    private @Nullable FullGroup lastFullGroup;

    private List<String> consoleScenesList = List.of();

    public HueGroupHandler(Thing thing, HueStateDescriptionProvider stateDescriptionOptionProvider) {
        super(thing);
        this.stateDescriptionOptionProvider = stateDescriptionOptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue group handler.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configGroupId = (String) getConfig().get(GROUP_ID);
        if (configGroupId != null) {
            BigDecimal time = (BigDecimal) getConfig().get(FADETIME);
            if (time != null) {
                defaultFadeTime = time.longValueExact();
            }

            groupId = configGroupId;
            // note: this call implicitly registers our handler as a listener on the bridge
            if (getHueClient() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-group-id");
        }
    }

    private synchronized void initializeProperties(@Nullable FullGroup fullGroup) {
        if (fullGroup != null) {
            Map<String, String> properties = editProperties();
            properties.put(PROPERTY_MEMBERS, fullGroup.getLightIds().stream().collect(Collectors.joining(",")));
            updateProperties(properties);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Hue group handler disposes. Unregistering listener.");
        cancelScheduledFuture();
        if (groupId != null) {
            HueClient bridgeHandler = getHueClient();
            if (bridgeHandler != null) {
                bridgeHandler.unregisterGroupStatusListener(this);
                hueClient = null;
            }
            groupId = null;
        }
    }

    protected synchronized @Nullable HueClient getHueClient() {
        if (hueClient == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueBridgeHandler) {
                HueClient bridgeHandler = (HueClient) handler;
                hueClient = bridgeHandler;
                bridgeHandler.registerGroupStatusListener(this);
            } else {
                return null;
            }
        }
        return hueClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommand(channelUID.getId(), command, defaultFadeTime);
    }

    @Override
    public void handleCommand(String channel, Command command, long fadeTime) {
        HueClient bridgeHandler = getHueClient();
        if (bridgeHandler == null) {
            logger.debug("hue bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        FullGroup group = bridgeHandler.getGroupById(groupId);
        if (group == null) {
            logger.debug("hue group not known on bridge. Cannot handle command.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-wrong-group-id");
            return;
        }

        Integer lastColorTemp;
        StateUpdate newState = null;
        switch (channel) {
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        newState = LightStateConverter.toOnOffLightState(OnOffType.OFF);
                    } else {
                        newState = LightStateConverter.toColorLightState(hsbCommand, group.getState());
                        newState.setOn(true);
                        newState.setTransitionTime(fadeTime);
                    }
                } else if (command instanceof PercentType) {
                    newState = LightStateConverter.toBrightnessLightState((PercentType) command);
                    newState.setTransitionTime(fadeTime);
                } else if (command instanceof OnOffType) {
                    newState = LightStateConverter.toOnOffLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    newState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, group);
                    if (newState != null) {
                        newState.setTransitionTime(fadeTime);
                    }
                }
                break;
            case CHANNEL_COLORTEMPERATURE:
                if (command instanceof PercentType) {
                    newState = LightStateConverter.toColorTemperatureLightStateFromPercentType((PercentType) command,
                            colorTemperatureCapabilties);
                    newState.setTransitionTime(fadeTime);
                } else if (command instanceof OnOffType) {
                    newState = LightStateConverter.toOnOffLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    newState = convertColorTempChangeToStateUpdate((IncreaseDecreaseType) command, group);
                    if (newState != null) {
                        newState.setTransitionTime(fadeTime);
                    }
                }
                break;
            case CHANNEL_COLORTEMPERATURE_ABS:
                if (command instanceof DecimalType) {
                    newState = LightStateConverter.toColorTemperatureLightState((DecimalType) command,
                            colorTemperatureCapabilties);
                    newState.setTransitionTime(fadeTime);
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    newState = LightStateConverter.toBrightnessLightState((PercentType) command);
                    newState.setTransitionTime(fadeTime);
                } else if (command instanceof OnOffType) {
                    newState = LightStateConverter.toOnOffLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    newState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, group);
                    if (newState != null) {
                        newState.setTransitionTime(fadeTime);
                    }
                }
                lastColorTemp = lastSentColorTemp;
                if (newState != null && lastColorTemp != null) {
                    // make sure that the light also has the latest color temp
                    // this might not have been yet set in the light, if it was off
                    newState.setColorTemperature(lastColorTemp, colorTemperatureCapabilties);
                    newState.setTransitionTime(fadeTime);
                }
                break;
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    newState = LightStateConverter.toOnOffLightState((OnOffType) command);
                }
                lastColorTemp = lastSentColorTemp;
                if (newState != null && lastColorTemp != null) {
                    // make sure that the light also has the latest color temp
                    // this might not have been yet set in the light, if it was off
                    newState.setColorTemperature(lastColorTemp, colorTemperatureCapabilties);
                    newState.setTransitionTime(fadeTime);
                }
                break;
            case CHANNEL_ALERT:
                if (command instanceof StringType) {
                    newState = LightStateConverter.toAlertState((StringType) command);
                    if (newState == null) {
                        // Unsupported StringType is passed. Log a warning
                        // message and return.
                        logger.warn("Unsupported String command: {}. Supported commands are: {}, {}, {} ", command,
                                LightStateConverter.ALERT_MODE_NONE, LightStateConverter.ALERT_MODE_SELECT,
                                LightStateConverter.ALERT_MODE_LONG_SELECT);
                        return;
                    } else {
                        scheduleAlertStateRestore(command);
                    }
                }
                break;
            case CHANNEL_SCENE:
                if (command instanceof StringType) {
                    newState = new StateUpdate().setScene(command.toString());
                }
                break;
            default:
                break;
        }
        if (newState != null) {
            cacheNewState(newState);
            bridgeHandler.updateGroupState(group, newState, fadeTime);
        } else {
            logger.debug("Command sent to an unknown channel id: {}:{}", getThing().getUID(), channel);
        }
    }

    /**
     * Caches the new state that is sent to the bridge. This is necessary in case the lights are off when the values are
     * sent. In this case, the values are not yet set in the lights.
     *
     * @param newState the state to be cached
     */
    private void cacheNewState(StateUpdate newState) {
        Integer tmpBrightness = newState.getBrightness();
        if (tmpBrightness != null) {
            lastSentBrightness = tmpBrightness;
        }
        Integer tmpColorTemp = newState.getColorTemperature();
        if (tmpColorTemp != null) {
            lastSentColorTemp = tmpColorTemp;
        }
    }

    private @Nullable StateUpdate convertColorTempChangeToStateUpdate(IncreaseDecreaseType command, FullGroup group) {
        StateUpdate stateUpdate = null;
        Integer currentColorTemp = getCurrentColorTemp(group.getState());
        if (currentColorTemp != null) {
            int newColorTemp = LightStateConverter.toAdjustedColorTemp(command, currentColorTemp,
                    colorTemperatureCapabilties);
            stateUpdate = new StateUpdate().setColorTemperature(newColorTemp, colorTemperatureCapabilties);
        }
        return stateUpdate;
    }

    private @Nullable Integer getCurrentColorTemp(@Nullable State groupState) {
        Integer colorTemp = lastSentColorTemp;
        if (colorTemp == null && groupState != null) {
            return groupState.getColorTemperature();
        }
        return colorTemp;
    }

    private @Nullable StateUpdate convertBrightnessChangeToStateUpdate(IncreaseDecreaseType command, FullGroup group) {
        Integer currentBrightness = getCurrentBrightness(group.getState());
        if (currentBrightness == null) {
            return null;
        }
        int newBrightness = LightStateConverter.toAdjustedBrightness(command, currentBrightness);
        return createBrightnessStateUpdate(currentBrightness, newBrightness);
    }

    private @Nullable Integer getCurrentBrightness(@Nullable State groupState) {
        if (lastSentBrightness == null && groupState != null) {
            return groupState.isOn() ? groupState.getBrightness() : 0;
        }
        return lastSentBrightness;
    }

    private StateUpdate createBrightnessStateUpdate(int currentBrightness, int newBrightness) {
        StateUpdate lightUpdate = new StateUpdate();
        if (newBrightness == 0) {
            lightUpdate.turnOff();
        } else {
            lightUpdate.setBrightness(newBrightness);
            if (currentBrightness == 0) {
                lightUpdate.turnOn();
            }
        }
        return lightUpdate;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        HueClient handler = getHueClient();
        if (handler != null) {
            FullGroup group = handler.getGroupById(groupId);
            if (group != null) {
                onGroupStateChanged(group);
            }
        }
    }

    @Override
    public boolean onGroupStateChanged(FullGroup group) {
        logger.trace("onGroupStateChanged() was called for group {}", group.getId());

        State state = group.getState();

        final FullGroup lastState = lastFullGroup;
        if (lastState == null || !Objects.equals(lastState.getState(), state)) {
            lastFullGroup = group;
        } else {
            return true;
        }

        logger.trace("New state for group {}", groupId);

        initializeProperties(group);

        lastSentColorTemp = null;
        lastSentBrightness = null;

        updateStatus(ThingStatus.ONLINE);

        logger.debug("onGroupStateChanged Group {}: on {} bri {} hue {} sat {} temp {} mode {} XY {}", group.getName(),
                state.isOn(), state.getBrightness(), state.getHue(), state.getSaturation(), state.getColorTemperature(),
                state.getColorMode(), state.getXY());

        HSBType hsbType = LightStateConverter.toHSBType(state);
        if (!state.isOn()) {
            hsbType = new HSBType(hsbType.getHue(), hsbType.getSaturation(), PercentType.ZERO);
        }
        updateState(CHANNEL_COLOR, hsbType);

        PercentType brightnessPercentType = state.isOn() ? LightStateConverter.toBrightnessPercentType(state)
                : PercentType.ZERO;
        updateState(CHANNEL_BRIGHTNESS, brightnessPercentType);

        updateState(CHANNEL_SWITCH, OnOffType.from(state.isOn()));

        updateState(CHANNEL_COLORTEMPERATURE,
                LightStateConverter.toColorTemperaturePercentType(state, colorTemperatureCapabilties));
        updateState(CHANNEL_COLORTEMPERATURE_ABS, LightStateConverter.toColorTemperature(state));

        return true;
    }

    @Override
    public void onGroupAdded(FullGroup group) {
        onGroupStateChanged(group);
    }

    @Override
    public void onGroupRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.group-removed");
    }

    @Override
    public void onGroupGone() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "@text/offline.group-removed");
    }

    /**
     * Sets the state options for applicable scenes.
     */
    @Override
    public void onScenesUpdated(List<Scene> updatedScenes) {
        List<StateOption> stateOptions = List.of();
        consoleScenesList = List.of();
        HueClient handler = getHueClient();
        if (handler != null) {
            FullGroup group = handler.getGroupById(groupId);
            if (group != null) {
                stateOptions = updatedScenes.stream().filter(scene -> scene.isApplicableTo(group))
                        .map(Scene::toStateOption).collect(Collectors.toList());
                consoleScenesList = updatedScenes
                        .stream().filter(scene -> scene.isApplicableTo(group)).map(scene -> "Id is \"" + scene.getId()
                                + "\" for scene \"" + scene.toStateOption().getLabel() + "\"")
                        .collect(Collectors.toList());
            }
        }
        stateDescriptionOptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_SCENE),
                stateOptions);
    }

    /**
     * Schedules restoration of the alert item state to {@link LightStateConverter#ALERT_MODE_NONE} after a given time.
     * <br>
     * Based on the initial command:
     * <ul>
     * <li>For {@link LightStateConverter#ALERT_MODE_SELECT} restoration will be triggered after <strong>2
     * seconds</strong>.
     * <li>For {@link LightStateConverter#ALERT_MODE_LONG_SELECT} restoration will be triggered after <strong>15
     * seconds</strong>.
     * </ul>
     * This method also cancels any previously scheduled restoration.
     *
     * @param command The {@link Command} sent to the item
     */
    private void scheduleAlertStateRestore(Command command) {
        cancelScheduledFuture();
        int delay = getAlertDuration(command);

        if (delay > 0) {
            scheduledFuture = scheduler.schedule(() -> {
                updateState(CHANNEL_ALERT, new StringType(LightStateConverter.ALERT_MODE_NONE));
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * This method will cancel previously scheduled alert item state
     * restoration.
     */
    private void cancelScheduledFuture() {
        ScheduledFuture<?> scheduledJob = scheduledFuture;
        if (scheduledJob != null) {
            scheduledJob.cancel(true);
            scheduledFuture = null;
        }
    }

    /**
     * This method returns the time in <strong>milliseconds</strong> after
     * which, the state of the alert item has to be restored to {@link LightStateConverter#ALERT_MODE_NONE}.
     *
     * @param command The initial command sent to the alert item.
     * @return Based on the initial command will return:
     *         <ul>
     *         <li><strong>2000</strong> for {@link LightStateConverter#ALERT_MODE_SELECT}.
     *         <li><strong>15000</strong> for {@link LightStateConverter#ALERT_MODE_LONG_SELECT}.
     *         <li><strong>-1</strong> for any command different from the previous two.
     *         </ul>
     */
    private int getAlertDuration(Command command) {
        int delay;
        switch (command.toString()) {
            case LightStateConverter.ALERT_MODE_LONG_SELECT:
                delay = 15000;
                break;
            case LightStateConverter.ALERT_MODE_SELECT:
                delay = 2000;
                break;
            default:
                delay = -1;
                break;
        }

        return delay;
    }

    public List<String> listScenesForConsole() {
        return consoleScenesList;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }
}
