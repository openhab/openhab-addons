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
package org.openhab.binding.sensibo.internal.handler;

import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Temperature;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensibo.internal.CallbackChannelsTypeProvider;
import org.openhab.binding.sensibo.internal.SensiboBindingConstants;
import org.openhab.binding.sensibo.internal.config.SensiboSkyConfiguration;
import org.openhab.binding.sensibo.internal.dto.poddetails.TemperatureDTO;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SensiboSkyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboSkyHandler extends SensiboBaseThingHandler implements ChannelTypeProvider {
    public static final String SWING_PROPERTY = "swing";
    public static final String MASTER_SWITCH_PROPERTY = "on";
    public static final String FAN_LEVEL_PROPERTY = "fanLevel";
    public static final String MODE_PROPERTY = "mode";
    public static final String TARGET_TEMPERATURE_PROPERTY = "targetTemperature";
    public static final String SWING_MODE_LABEL = "Swing Mode";
    public static final String FAN_LEVEL_LABEL = "Fan Level";
    public static final String MODE_LABEL = "Mode";
    public static final String TARGET_TEMPERATURE_LABEL = "Target Temperature";
    private static final String ITEM_TYPE_STRING = "String";
    private static final String ITEM_TYPE_NUMBER_TEMPERATURE = "Number:Temperature";
    private final Logger logger = LoggerFactory.getLogger(SensiboSkyHandler.class);
    private final Map<ChannelTypeUID, ChannelType> generatedChannelTypes = new HashMap<>();
    private Optional<SensiboSkyConfiguration> config = Optional.empty();

    public SensiboSkyHandler(final Thing thing) {
        super(thing);
    }

    private static String beautify(final String camelCaseWording) {
        final StringBuilder b = new StringBuilder();
        for (final String s : StringUtils.splitByCharacterTypeCamelCase(camelCaseWording)) {
            b.append(" ");
            b.append(s);
        }
        final StringBuilder bs = new StringBuilder();
        for (final String t : StringUtils.splitByWholeSeparator(b.toString(), " _")) {
            bs.append(" ");
            bs.append(t);
        }

        return WordUtils.capitalizeFully(bs.toString()).trim();
    }

    private String getMacAddress() {
        if (config.isPresent()) {
            return config.get().macAddress;
        }
        throw new IllegalArgumentException("No configuration present");
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        handleCommand(channelUID, command, getSensiboModel());
    }

    /*
     * Package private in order to be reachable from unit test
     */
    void updateAcState(SensiboSky sensiboSky, String property, Object value) {
        StateChange stateChange = checkStateChangeValid(sensiboSky, property, value);
        if (stateChange.valid) {
            getAccountHandler().ifPresent(
                    handler -> handler.updateSensiboSkyAcState(getMacAddress(), property, stateChange.value, this));
        } else {
            logger.info("Update command not sent; invalid state change for SensiboSky AC state: {}",
                    stateChange.validationMessage);
        }
    }

    private void updateTimer(@Nullable Integer secondsFromNowUntilSwitchOff) {
        getAccountHandler()
                .ifPresent(handler -> handler.updateSensiboSkyTimer(getMacAddress(), secondsFromNowUntilSwitchOff));
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command, final SensiboModel model) {
        model.findSensiboSkyByMacAddress(getMacAddress()).ifPresent(sensiboSky -> {
            if (sensiboSky.isAlive()) {
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    addDynamicChannelsAndProperties(sensiboSky);
                    updateStatus(ThingStatus.ONLINE); // In case it has been offline
                }
                switch (channelUID.getId()) {
                    case CHANNEL_CURRENT_HUMIDITY:
                        handleCurrentHumidityCommand(channelUID, command, sensiboSky);
                        break;
                    case CHANNEL_CURRENT_TEMPERATURE:
                        handleCurrentTemperatureCommand(channelUID, command, sensiboSky);
                        break;
                    case CHANNEL_MASTER_SWITCH:
                        handleMasterSwitchCommand(channelUID, command, sensiboSky);
                        break;
                    case CHANNEL_TARGET_TEMPERATURE:
                        handleTargetTemperatureCommand(channelUID, command, sensiboSky);
                        break;
                    case CHANNEL_MODE:
                        handleModeCommand(channelUID, command, sensiboSky);
                        break;
                    case CHANNEL_SWING_MODE:
                        handleSwingCommand(channelUID, command, sensiboSky);
                        break;
                    case CHANNEL_FAN_LEVEL:
                        handleFanLevelCommand(channelUID, command, sensiboSky);
                        break;
                    case CHANNEL_TIMER:
                        handleTimerCommand(channelUID, command, sensiboSky);
                        break;
                    default:
                        logger.debug("Received command on unknown channel {}, ignoring", channelUID.getId());
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unreachable by Sensibo servers");
            }
        });
    }

    private void handleTimerCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType) {
            if (sensiboSky.getTimer().isPresent() && sensiboSky.getTimer().get().secondsRemaining > 0) {
                updateState(channelUID, new DecimalType(sensiboSky.getTimer().get().secondsRemaining));
            } else {
                updateState(channelUID, UnDefType.UNDEF);
            }
        } else if (command instanceof DecimalType) {
            final DecimalType newValue = (DecimalType) command;
            updateTimer(newValue.intValue());
        } else {
            updateTimer(null);
        }
    }

    private void handleFanLevelCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType) {
            if (sensiboSky.getAcState().isPresent() && sensiboSky.getAcState().get().getFanLevel() != null) {
                updateState(channelUID, new StringType(sensiboSky.getAcState().get().getFanLevel()));
            } else {
                updateState(channelUID, UnDefType.UNDEF);
            }
        } else if (command instanceof StringType) {
            final StringType newValue = (StringType) command;
            updateAcState(sensiboSky, FAN_LEVEL_PROPERTY, newValue.toString());
        }
    }

    private void handleSwingCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType && sensiboSky.getAcState().isPresent()) {
            if (sensiboSky.getAcState().isPresent() && sensiboSky.getAcState().get().getSwing() != null) {
                updateState(channelUID, new StringType(sensiboSky.getAcState().get().getSwing()));
            } else {
                updateState(channelUID, UnDefType.UNDEF);
            }
        } else if (command instanceof StringType) {
            final StringType newValue = (StringType) command;
            updateAcState(sensiboSky, SWING_PROPERTY, newValue.toString());
        }
    }

    private void handleModeCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType) {
            if (sensiboSky.getAcState().isPresent()) {
                updateState(channelUID, new StringType(sensiboSky.getAcState().get().getMode()));
            } else {
                updateState(channelUID, UnDefType.UNDEF);
            }
        } else if (command instanceof StringType) {
            final StringType newValue = (StringType) command;
            updateAcState(sensiboSky, MODE_PROPERTY, newValue.toString());
            addDynamicChannelsAndProperties(sensiboSky);
        }
    }

    private void handleTargetTemperatureCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType) {
            sensiboSky.getAcState().ifPresent(acState -> {
                @Nullable
                Integer targetTemperature = acState.getTargetTemperature();
                if (targetTemperature != null) {
                    updateState(channelUID, new QuantityType<>(targetTemperature, sensiboSky.getTemperatureUnit()));
                } else {
                    updateState(channelUID, UnDefType.UNDEF);
                }
            });
            if (!sensiboSky.getAcState().isPresent()) {
                updateState(channelUID, UnDefType.UNDEF);
            }
        } else if (command instanceof QuantityType<?>) {
            QuantityType<?> newValue = (QuantityType<?>) command;
            if (!Objects.equals(sensiboSky.getTemperatureUnit(), newValue.getUnit())) {
                // If quantity is given in celsius when fahrenheit is used or opposite
                try {
                    UnitConverter temperatureConverter = newValue.getUnit()
                            .getConverterToAny(sensiboSky.getTemperatureUnit());
                    // No decimals supported
                    long convertedValue = (long) temperatureConverter.convert(newValue.longValue());
                    updateAcState(sensiboSky, TARGET_TEMPERATURE_PROPERTY, new DecimalType(convertedValue));
                } catch (UnconvertibleException | IncommensurableException e) {
                    logger.info("Could not convert {} to {}: {}", newValue, sensiboSky.getTemperatureUnit(),
                            e.getMessage());
                }
            } else {
                updateAcState(sensiboSky, TARGET_TEMPERATURE_PROPERTY, new DecimalType(newValue.intValue()));
            }
        } else if (command instanceof DecimalType) {
            updateAcState(sensiboSky, TARGET_TEMPERATURE_PROPERTY, command);
        }
    }

    private void handleMasterSwitchCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType) {
            sensiboSky.getAcState().ifPresent(e -> updateState(channelUID, OnOffType.from(e.isOn())));
        } else if (command instanceof OnOffType) {
            updateAcState(sensiboSky, MASTER_SWITCH_PROPERTY, command == OnOffType.ON);
        }
    }

    private void handleCurrentTemperatureCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType) {
            updateState(channelUID, new QuantityType<>(sensiboSky.getTemperature(), SIUnits.CELSIUS));
        }
    }

    private void handleCurrentHumidityCommand(ChannelUID channelUID, Command command, SensiboSky sensiboSky) {
        if (command instanceof RefreshType) {
            updateState(channelUID, new QuantityType<>(sensiboSky.getHumidity(), Units.PERCENT));
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(CallbackChannelsTypeProvider.class);
    }

    @Override
    public void initialize() {
        config = Optional.ofNullable(getConfigAs(SensiboSkyConfiguration.class));
        logger.debug("Initializing SensiboSky using config {}", config);
        getSensiboModel().findSensiboSkyByMacAddress(getMacAddress()).ifPresent(pod -> {

            if (pod.isAlive()) {
                addDynamicChannelsAndProperties(pod);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unreachable by Sensibo servers");
            }
        });
    }

    private boolean isDynamicChannel(final ChannelTypeUID uid) {
        return SensiboBindingConstants.DYNAMIC_CHANNEL_TYPES.stream().anyMatch(e -> uid.getId().startsWith(e));
    }

    private void addDynamicChannelsAndProperties(final SensiboSky sensiboSky) {
        logger.debug("Updating dynamic channels for {}", sensiboSky.getId());
        final List<Channel> newChannels = new ArrayList<>();
        for (final Channel channel : getThing().getChannels()) {
            final ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null && !isDynamicChannel(channelTypeUID)) {
                newChannels.add(channel);
            }
        }

        newChannels.addAll(createDynamicChannels(sensiboSky));
        Map<String, String> properties = sensiboSky.getThingProperties();
        updateThing(editThing().withChannels(newChannels).withProperties(properties).build());
    }

    public List<Channel> createDynamicChannels(final SensiboSky sensiboSky) {
        final List<Channel> newChannels = new ArrayList<>();
        generatedChannelTypes.clear();

        sensiboSky.getCurrentModeCapabilities().ifPresent(capabilities -> {
            // Not all modes have swing and fan level
            final ChannelTypeUID swingModeChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_SWING_MODE,
                    SWING_MODE_LABEL, ITEM_TYPE_STRING, capabilities.swingModes, null, null);
            newChannels
                    .add(ChannelBuilder
                            .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_SWING_MODE),
                                    ITEM_TYPE_STRING)
                            .withLabel(SWING_MODE_LABEL).withType(swingModeChannelType).build());

            final ChannelTypeUID fanLevelChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_FAN_LEVEL,
                    FAN_LEVEL_LABEL, ITEM_TYPE_STRING, capabilities.fanLevels, null, null);
            newChannels.add(ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_FAN_LEVEL),
                            ITEM_TYPE_STRING)
                    .withLabel(FAN_LEVEL_LABEL).withType(fanLevelChannelType).build());
        });

        final ChannelTypeUID modeChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_MODE, MODE_LABEL,
                ITEM_TYPE_STRING, sensiboSky.getRemoteCapabilities().keySet(), null, null);
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_MODE), ITEM_TYPE_STRING)
                .withLabel(MODE_LABEL).withType(modeChannelType).build());

        final ChannelTypeUID targetTemperatureChannelType = addChannelType(
                SensiboBindingConstants.CHANNEL_TYPE_TARGET_TEMPERATURE, TARGET_TEMPERATURE_LABEL,
                ITEM_TYPE_NUMBER_TEMPERATURE, sensiboSky.getTargetTemperatures(), "%d %unit%", "TargetTemperature");
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                        ITEM_TYPE_NUMBER_TEMPERATURE)
                .withLabel(TARGET_TEMPERATURE_LABEL).withType(targetTemperatureChannelType).build());

        return newChannels;
    }

    private ChannelTypeUID addChannelType(final String channelTypePrefix, final String label, final String itemType,
            final Collection<?> options, @Nullable final String pattern, @Nullable final String tag) {
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(SensiboBindingConstants.BINDING_ID,
                channelTypePrefix + getThing().getUID().getId());
        final List<StateOption> stateOptions = options.stream()
                .map(e -> new StateOption(e.toString(), e instanceof String ? beautify((String) e) : e.toString()))
                .collect(Collectors.toList());

        StateDescriptionFragmentBuilder stateDescription = StateDescriptionFragmentBuilder.create().withReadOnly(false)
                .withOptions(stateOptions);
        if (pattern != null) {
            stateDescription = stateDescription.withPattern(pattern);
        }
        final StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                .withStateDescriptionFragment(stateDescription.build());
        if (tag != null) {
            builder.withTag(tag);
        }
        final ChannelType channelType = builder.build();

        generatedChannelTypes.put(channelTypeUID, channelType);

        return channelTypeUID;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable final Locale locale) {
        return generatedChannelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(final ChannelTypeUID channelTypeUID, @Nullable final Locale locale) {
        return generatedChannelTypes.get(channelTypeUID);
    }

    /*
     * Package private in order to be reachable from unit test
     */
    StateChange checkStateChangeValid(SensiboSky sensiboSky, String property, Object newPropertyValue) {
        StateChange stateChange = new StateChange(newPropertyValue);

        sensiboSky.getCurrentModeCapabilities().ifPresent(currentModeCapabilities -> {
            switch (property) {
                case TARGET_TEMPERATURE_PROPERTY:
                    Unit<Temperature> temperatureUnit = sensiboSky.getTemperatureUnit();
                    TemperatureDTO validTemperatures = currentModeCapabilities.temperatures
                            .get(SIUnits.CELSIUS.equals(temperatureUnit) ? "C" : "F");
                    DecimalType rawValue = (DecimalType) newPropertyValue;
                    stateChange.updateValue(rawValue.intValue());
                    if (!validTemperatures.validValues.contains(rawValue.intValue())) {
                        stateChange.addError(String.format(
                                "Cannot change targetTemperature to '%d', valid targetTemperatures are one of %s",
                                rawValue.intValue(), ToStringBuilder.reflectionToString(
                                        validTemperatures.validValues.toArray(), ToStringStyle.SIMPLE_STYLE)));
                    }
                    break;
                case MODE_PROPERTY:
                    if (!sensiboSky.getRemoteCapabilities().containsKey(newPropertyValue)) {
                        stateChange.addError(
                                String.format("Cannot change mode to %s, valid modes are %s", newPropertyValue,
                                        ToStringBuilder.reflectionToString(
                                                sensiboSky.getRemoteCapabilities().keySet().toArray(),
                                                ToStringStyle.SIMPLE_STYLE)));
                    }
                    break;
                case FAN_LEVEL_PROPERTY:
                    if (!currentModeCapabilities.fanLevels.contains(newPropertyValue)) {
                        stateChange.addError(String.format("Cannot change fanLevel to %s, valid fanLevels are %s",
                                newPropertyValue, ToStringBuilder.reflectionToString(
                                        currentModeCapabilities.fanLevels.toArray(), ToStringStyle.SIMPLE_STYLE)));
                    }
                    break;
                case MASTER_SWITCH_PROPERTY:
                    // Always allowed
                    break;
                case SWING_PROPERTY:
                    if (!currentModeCapabilities.swingModes.contains(newPropertyValue)) {
                        stateChange.addError(String.format("Cannot change swing to %s, valid swings are %s",
                                newPropertyValue, ToStringBuilder.reflectionToString(
                                        currentModeCapabilities.swingModes.toArray(), ToStringStyle.SIMPLE_STYLE)));
                    }
                    break;
                default:
                    stateChange.addError(String.format("No such ac state property %s", property));
            }
            logger.debug("State change request {}", stateChange);
        });
        return stateChange;
    }

    @NonNullByDefault
    public class StateChange {
        Object value;

        boolean valid = true;
        @Nullable
        String validationMessage;

        public StateChange(Object value) {
            this.value = value;
        }

        public void updateValue(Object updatedValue) {
            value = updatedValue;
        }

        public void addError(String validationMessage) {
            valid = false;
            this.validationMessage = validationMessage;
        }

        @Override
        public String toString() {
            return "StateChange [valid=" + valid + ", validationMessage=" + validationMessage + ", value=" + value
                    + ", value Class=" + value.getClass() + "]";
        }
    }
}
