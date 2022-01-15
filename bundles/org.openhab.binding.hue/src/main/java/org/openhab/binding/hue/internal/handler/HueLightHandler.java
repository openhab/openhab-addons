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
import static org.openhab.core.thing.Thing.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.FullLight;
import org.openhab.binding.hue.internal.State;
import org.openhab.binding.hue.internal.StateUpdate;
import org.openhab.binding.hue.internal.dto.Capabilities;
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
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueLightHandler} is the handler for a hue light. It uses the {@link HueClient} to execute the actual
 * command.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Adjustments
 * @author Kai Kreuzer - stabilized code
 * @author Andre Fuechsel - implemented switch off when brightness == 0, changed to support generic thing types, changed
 *         the initialization of properties
 * @author Thomas Höfer - added thing properties
 * @author Jochen Hiller - fixed status updates for reachable=true/false
 * @author Markus Mazurczak - added code for command handling of OSRAM PAR16 50
 *         bulbs
 * @author Yordan Zhelev - added alert and effect functions
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 * @author Jochen Leopold - Added support for custom fade times
 * @author Jacob Laursen - Add workaround for LK Wiser products
 */
@NonNullByDefault
public class HueLightHandler extends BaseThingHandler implements HueLightActionsHandler, LightStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_COLOR_LIGHT,
            THING_TYPE_COLOR_TEMPERATURE_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_EXTENDED_COLOR_LIGHT,
            THING_TYPE_ON_OFF_LIGHT, THING_TYPE_ON_OFF_PLUG, THING_TYPE_DIMMABLE_PLUG);

    public static final String OSRAM_PAR16_50_TW_MODEL_ID = "PAR16_50_TW";
    public static final String LK_WISER_MODEL_ID = "LK_Dimmer";

    private final Logger logger = LoggerFactory.getLogger(HueLightHandler.class);

    private final HueStateDescriptionProvider stateDescriptionProvider;

    private @NonNullByDefault({}) String lightId;

    private @Nullable FullLight lastFullLight;
    private long endBypassTime = 0L;

    private @Nullable Integer lastSentColorTemp;
    private @Nullable Integer lastSentBrightness;

    /**
     * Flag to indicate whether the bulb is of type Osram par16 50 TW
     */
    private boolean isOsramPar16 = false;
    /**
     * Flag to indicate whether the dimmer/relay is of type LK Wiser by Schneider Electric
     */
    private boolean isLkWiser = false;

    private boolean propertiesInitializedSuccessfully = false;
    private boolean capabilitiesInitializedSuccessfully = false;
    private ColorTemperature colorTemperatureCapabilties = new ColorTemperature();
    private long defaultFadeTime = 400;

    private @Nullable HueClient hueClient;

    private @Nullable ScheduledFuture<?> scheduledFuture;

    public HueLightHandler(Thing hueLight, HueStateDescriptionProvider stateDescriptionProvider) {
        super(hueLight);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
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
        final String configLightId = (String) getConfig().get(LIGHT_ID);
        if (configLightId != null) {
            BigDecimal time = (BigDecimal) getConfig().get(FADETIME);
            if (time != null) {
                defaultFadeTime = time.longValueExact();
            }

            lightId = configLightId;
            // note: this call implicitly registers our handler as a listener on the bridge
            HueClient bridgeHandler = getHueClient();
            if (bridgeHandler != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    FullLight fullLight = bridgeHandler.getLightById(lightId);
                    initializeProperties(fullLight);
                    initializeCapabilities(fullLight);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-light-id");
        }
    }

    private synchronized void initializeProperties(@Nullable FullLight fullLight) {
        if (!propertiesInitializedSuccessfully && fullLight != null) {
            Map<String, String> properties = editProperties();
            String softwareVersion = fullLight.getSoftwareVersion();
            if (softwareVersion != null) {
                properties.put(PROPERTY_FIRMWARE_VERSION, softwareVersion);
            }
            String modelId = fullLight.getNormalizedModelID();
            if (modelId != null) {
                properties.put(PROPERTY_MODEL_ID, modelId);

                switch (modelId) {
                    case OSRAM_PAR16_50_TW_MODEL_ID:
                        isOsramPar16 = true;
                        break;
                    case LK_WISER_MODEL_ID:
                        isLkWiser = true;
                        break;
                }
            }
            properties.put(PROPERTY_VENDOR, fullLight.getManufacturerName());
            properties.put(PRODUCT_NAME, fullLight.getProductName());
            String uniqueID = fullLight.getUniqueID();
            if (uniqueID != null) {
                properties.put(UNIQUE_ID, uniqueID);
            }
            updateProperties(properties);
            propertiesInitializedSuccessfully = true;
        }
    }

    private void initializeCapabilities(@Nullable FullLight fullLight) {
        if (!capabilitiesInitializedSuccessfully && fullLight != null) {
            Capabilities capabilities = fullLight.capabilities;
            if (capabilities != null) {
                ColorTemperature ct = capabilities.control.ct;
                if (ct != null) {
                    colorTemperatureCapabilties = ct;

                    // minimum and maximum are inverted due to mired/Kelvin conversion!
                    StateDescriptionFragment stateDescriptionFragment = StateDescriptionFragmentBuilder.create()
                            .withMinimum(new BigDecimal(LightStateConverter.miredToKelvin(ct.max))) //
                            .withMaximum(new BigDecimal(LightStateConverter.miredToKelvin(ct.min))) //
                            .withStep(new BigDecimal(100)) //
                            .withPattern("%.0f K") //
                            .build();
                    stateDescriptionProvider.setStateDescriptionFragment(
                            new ChannelUID(thing.getUID(), CHANNEL_COLORTEMPERATURE_ABS), stateDescriptionFragment);
                }
            }
            capabilitiesInitializedSuccessfully = true;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Hue light handler disposes. Unregistering listener.");
        cancelScheduledFuture();
        if (lightId != null) {
            HueClient bridgeHandler = getHueClient();
            if (bridgeHandler != null) {
                bridgeHandler.unregisterLightStatusListener(this);
                hueClient = null;
            }
            lightId = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommand(channelUID.getId(), command, defaultFadeTime);
    }

    @Override
    public void handleCommand(String channel, Command command, long fadeTime) {
        HueClient bridgeHandler = getHueClient();
        if (bridgeHandler == null) {
            logger.warn("hue bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        final FullLight light = lastFullLight == null ? bridgeHandler.getLightById(lightId) : lastFullLight;
        if (light == null) {
            logger.debug("hue light not known on bridge. Cannot handle command.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-wrong-light-id");
            return;
        }

        Integer lastColorTemp;
        StateUpdate newState = null;
        switch (channel) {
            case CHANNEL_COLORTEMPERATURE:
                if (command instanceof PercentType) {
                    newState = LightStateConverter.toColorTemperatureLightStateFromPercentType((PercentType) command,
                            colorTemperatureCapabilties);
                    newState.setTransitionTime(fadeTime);
                } else if (command instanceof OnOffType) {
                    newState = LightStateConverter.toOnOffLightState((OnOffType) command);
                    if (isOsramPar16) {
                        newState = addOsramSpecificCommands(newState, (OnOffType) command);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    newState = convertColorTempChangeToStateUpdate((IncreaseDecreaseType) command, light);
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
                    if (isOsramPar16) {
                        newState = addOsramSpecificCommands(newState, (OnOffType) command);
                    } else if (isLkWiser) {
                        newState = addLkWiserSpecificCommands(newState, (OnOffType) command);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    newState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, light);
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
                    if (isOsramPar16) {
                        newState = addOsramSpecificCommands(newState, (OnOffType) command);
                    } else if (isLkWiser) {
                        newState = addLkWiserSpecificCommands(newState, (OnOffType) command);
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
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        newState = LightStateConverter.toOnOffLightState(OnOffType.OFF);
                    } else {
                        newState = LightStateConverter.toColorLightState(hsbCommand, light.getState());
                        newState.setTransitionTime(fadeTime);
                    }
                } else if (command instanceof PercentType) {
                    newState = LightStateConverter.toBrightnessLightState((PercentType) command);
                    newState.setTransitionTime(fadeTime);
                } else if (command instanceof OnOffType) {
                    newState = LightStateConverter.toOnOffLightState((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    newState = convertBrightnessChangeToStateUpdate((IncreaseDecreaseType) command, light);
                    if (newState != null) {
                        newState.setTransitionTime(fadeTime);
                    }
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
            case CHANNEL_EFFECT:
                if (command instanceof OnOffType) {
                    newState = LightStateConverter.toOnOffEffectState((OnOffType) command);
                }
                break;
        }
        if (newState != null) {
            // Cache values which we have sent
            Integer tmpBrightness = newState.getBrightness();
            if (tmpBrightness != null) {
                lastSentBrightness = tmpBrightness;
            }
            Integer tmpColorTemp = newState.getColorTemperature();
            if (tmpColorTemp != null) {
                lastSentColorTemp = tmpColorTemp;
            }
            bridgeHandler.updateLightState(this, light, newState, fadeTime);
        } else {
            logger.warn("Command sent to an unknown channel id: {}:{}", getThing().getUID(), channel);
        }
    }

    /**
     * Applies additional {@link StateUpdate} commands as a workaround for Osram
     * Lightify PAR16 TW firmware bug. Also see
     * http://www.everyhue.com/vanilla/discussion/1756/solved-lightify-turning-off
     */
    private StateUpdate addOsramSpecificCommands(StateUpdate lightState, OnOffType actionType) {
        if (actionType.equals(OnOffType.ON)) {
            lightState.setBrightness(254);
        } else {
            lightState.setTransitionTime(0);
        }
        return lightState;
    }

    /**
     * Applies additional {@link StateUpdate} commands as a workaround for LK Wiser
     * Dimmer/Relay firmware bug. Additional details here:
     * https://techblog.vindvejr.dk/?p=455
     */
    private StateUpdate addLkWiserSpecificCommands(StateUpdate lightState, OnOffType actionType) {
        if (actionType.equals(OnOffType.OFF)) {
            lightState.setTransitionTime(0);
        }
        return lightState;
    }

    private @Nullable StateUpdate convertColorTempChangeToStateUpdate(IncreaseDecreaseType command, FullLight light) {
        StateUpdate stateUpdate = null;
        Integer currentColorTemp = getCurrentColorTemp(light.getState());
        if (currentColorTemp != null) {
            int newColorTemp = LightStateConverter.toAdjustedColorTemp(command, currentColorTemp,
                    colorTemperatureCapabilties);
            stateUpdate = new StateUpdate().setColorTemperature(newColorTemp, colorTemperatureCapabilties);
        }
        return stateUpdate;
    }

    private @Nullable Integer getCurrentColorTemp(@Nullable State lightState) {
        Integer colorTemp = lastSentColorTemp;
        if (colorTemp == null && lightState != null) {
            return lightState.getColorTemperature();
        }
        return colorTemp;
    }

    private @Nullable StateUpdate convertBrightnessChangeToStateUpdate(IncreaseDecreaseType command, FullLight light) {
        Integer currentBrightness = getCurrentBrightness(light.getState());
        if (currentBrightness == null) {
            return null;
        }
        int newBrightness = LightStateConverter.toAdjustedBrightness(command, currentBrightness);
        return createBrightnessStateUpdate(currentBrightness, newBrightness);
    }

    private @Nullable Integer getCurrentBrightness(@Nullable State lightState) {
        if (lastSentBrightness == null && lightState != null) {
            return lightState.isOn() ? lightState.getBrightness() : 0;
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

    protected synchronized @Nullable HueClient getHueClient() {
        if (hueClient == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueClient) {
                HueClient bridgeHandler = (HueClient) handler;
                hueClient = bridgeHandler;
                bridgeHandler.registerLightStatusListener(this);
            } else {
                return null;
            }
        }
        return hueClient;
    }

    @Override
    public void setPollBypass(long bypassTime) {
        endBypassTime = System.currentTimeMillis() + bypassTime;
    }

    @Override
    public void unsetPollBypass() {
        endBypassTime = 0L;
    }

    @Override
    public boolean onLightStateChanged(FullLight fullLight) {
        logger.trace("onLightStateChanged() was called");

        if (System.currentTimeMillis() <= endBypassTime) {
            logger.debug("Bypass light update after command ({}).", lightId);
            return false;
        }

        State state = fullLight.getState();

        final FullLight lastState = lastFullLight;
        if (lastState == null || !Objects.equals(lastState.getState(), state)) {
            lastFullLight = fullLight;
        } else {
            return true;
        }

        logger.trace("New state for light {}", lightId);

        initializeProperties(fullLight);

        lastSentColorTemp = null;
        lastSentBrightness = null;

        // update status (ONLINE, OFFLINE)
        if (state.isReachable()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            // we assume OFFLINE without any error (NONE), as this is an
            // expected state (when bulb powered off)
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.light-not-reachable");
        }

        logger.debug("onLightStateChanged Light {}: on {} bri {} hue {} sat {} temp {} mode {} XY {}",
                fullLight.getName(), state.isOn(), state.getBrightness(), state.getHue(), state.getSaturation(),
                state.getColorTemperature(), state.getColorMode(), state.getXY());

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

        StringType stringType = LightStateConverter.toAlertStringType(state);
        if (!"NULL".equals(stringType.toString())) {
            updateState(CHANNEL_ALERT, stringType);
            scheduleAlertStateRestore(stringType);
        }

        return true;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        HueClient handler = getHueClient();
        if (handler != null) {
            FullLight light = handler.getLightById(lightId);
            if (light != null) {
                onLightStateChanged(light);
            }
        }
    }

    @Override
    public void onLightRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.light-removed");
    }

    @Override
    public void onLightGone() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "@text/offline.light-not-reachable");
    }

    @Override
    public void onLightAdded(FullLight light) {
        onLightStateChanged(light);
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

    @Override
    public String getLightId() {
        return lightId;
    }
}
