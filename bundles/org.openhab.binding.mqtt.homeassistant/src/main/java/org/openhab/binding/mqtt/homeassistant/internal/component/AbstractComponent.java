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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantChannelTransformation;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.binding.mqtt.homeassistant.internal.component.ComponentFactory.ComponentContext;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractComponentConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.Availability;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AvailabilityMode;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelDefinition;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeBuilder;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.StateDescription;

/**
 * A HomeAssistant component is comparable to a channel group.
 * It has a name and consists of multiple channels.
 *
 * @author David Graeff - Initial contribution
 * @param <C> Config class derived from {@link AbstractComponentConfiguration}
 */
@NonNullByDefault
public abstract class AbstractComponent<C extends AbstractComponentConfiguration> {
    public enum TemperatureUnit {
        CELSIUS(SIUnits.CELSIUS, new BigDecimal("0.1")),
        FAHRENHEIT(ImperialUnits.FAHRENHEIT, BigDecimal.ONE);

        private final Unit<Temperature> unit;
        private final BigDecimal defaultPrecision;

        public static TemperatureUnit fromString(String unit) {
            if ("°F".equals(unit)) {
                return FAHRENHEIT;
            } else {
                return CELSIUS;
            }
        }

        TemperatureUnit(Unit<Temperature> unit, BigDecimal defaultPrecision) {
            this.unit = unit;
            this.defaultPrecision = defaultPrecision;
        }

        public Unit<Temperature> getUnit() {
            return unit;
        }

        public BigDecimal getDefaultPrecision() {
            return defaultPrecision;
        }
    }

    public static final String JSON_ATTRIBUTES_CHANNEL_ID = "json-attributes";

    protected static final String FORMAT_INTEGER = "%.0f";

    // Component location fields
    protected final ComponentContext componentContext;

    // Channels and configuration
    protected final Map<String, ComponentChannel> channels = new TreeMap<>();
    protected final List<ComponentChannel> hiddenChannels = new ArrayList<>();

    // The hash code ({@link String#hashCode()}) of the configuration string
    // Used to determine if a component has changed.
    protected final int configHash;
    protected final C config;

    protected boolean configSeen;
    protected final String uniqueId;
    protected @Nullable String groupId;
    protected String componentId;

    /**
     * Creates component based on generic configuration and component configuration type.
     *
     * @param componentContext generic componentContext with not parsed JSON config
     * @param clazz target configuration type
     * @param singleChannelComponent if this component only ever has one channel, so should never be in a group
     */
    public AbstractComponent(ComponentFactory.ComponentContext componentContext, Class<C> clazz) {
        this(componentContext, AbstractComponentConfiguration.create(componentContext.getPython(),
                componentContext.getHaID().component, componentContext.getConfigJSON(), clazz));
    }

    /**
     * Creates component based on generic configuration and component configuration type.
     *
     * @param componentContext generic componentContext with not parsed JSON config
     * @param clazz target configuration type
     * @param singleChannelComponent if this component only ever has one channel, so should never be in a group
     */
    public AbstractComponent(ComponentFactory.ComponentContext componentContext, C config) {
        this.componentContext = componentContext;

        this.config = config;
        this.configHash = componentContext.getConfigJSON().hashCode();

        // try for a simple component/group ID first; if there are conflicts
        // (components of different types, but the same object id)
        // we'll resolve them later
        HaID haID = componentContext.getHaID();
        groupId = componentId = haID.objectID.replace('-', '_');
        uniqueId = haID.component + "_" + haID.getGroupId(config.getUniqueId());

        this.configSeen = false;

        final List<Availability> availabilities = config.getAvailability();
        if (availabilities != null) {
            String mode = config.getAvailabilityMode();
            AvailabilityTracker.AvailabilityMode availabilityTrackerMode = switch (mode) {
                case AvailabilityMode.ALL -> AvailabilityTracker.AvailabilityMode.ALL;
                case AvailabilityMode.ANY -> AvailabilityTracker.AvailabilityMode.ANY;
                case AvailabilityMode.LATEST -> AvailabilityTracker.AvailabilityMode.LATEST;
                default -> AvailabilityTracker.AvailabilityMode.LATEST;
            };
            componentContext.getTracker().setAvailabilityMode(availabilityTrackerMode);
            for (Availability availability : availabilities) {
                org.graalvm.polyglot.Value availabilityTemplate = availability.getValueTemplate();
                ChannelTransformation transformation = null;
                if (availabilityTemplate != null) {
                    transformation = new HomeAssistantChannelTransformation(getPython(), this, availabilityTemplate,
                            false);
                }
                componentContext.getTracker().addAvailabilityTopic(availability.getTopic(),
                        availability.getPayloadAvailable(), availability.getPayloadNotAvailable(), transformation);
            }
        } else {
            String availabilityTopic = this.config.getAvailabilityTopic();
            if (availabilityTopic != null) {
                org.graalvm.polyglot.Value availabilityTemplate = this.config.getAvailabilityTemplate();
                ChannelTransformation transformation = null;
                if (availabilityTemplate != null) {
                    transformation = new HomeAssistantChannelTransformation(getPython(), this, availabilityTemplate,
                            false);
                }
                componentContext.getTracker().addAvailabilityTopic(availabilityTopic, this.config.getPayloadAvailable(),
                        this.config.getPayloadNotAvailable(), transformation);
            }
        }
    }

    protected void addJsonAttributesChannel() {
        if (config instanceof EntityConfiguration entityConfig) {
            String jsonAttributesTopic = entityConfig.getJsonAttributesTopic();
            if (jsonAttributesTopic != null) {
                ChannelStateUpdateListener listener = (this instanceof ChannelStateUpdateListener localThis) ? localThis
                        : componentContext.getUpdateListener();
                buildChannel(JSON_ATTRIBUTES_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(),
                        "JSON Attributes", listener)
                        .stateTopic(jsonAttributesTopic, entityConfig.getJsonAttributesTemplate())
                        .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).isAdvanced(true).build();
            }
        }
    }

    protected void finalizeChannels() {
        addJsonAttributesChannel();
        if (channels.size() == 1) {
            groupId = null;
            channels.values().forEach(c -> c.resetUID(buildChannelUID(componentId), getName()));
        } else {
            // only the first channel needs to persist the configuration
            channels.values().stream().skip(1).forEach(c -> {
                c.clearConfiguration();
            });
        }
    }

    public void resolveConflict() {
        HaID haID = componentContext.getHaID();
        if (channels.size() == 1) {
            componentId = componentId + "_" + haID.component;
            channels.values().forEach(c -> c.resetUID(buildChannelUID(componentId)));
        } else {
            groupId = componentId = componentId + "_" + haID.component;
            channels.values().forEach(c -> c.resetUID(buildChannelUID(c.getChannel().getUID().getIdWithoutGroup())));
        }
    }

    protected ComponentChannel.Builder buildChannel(String channelID, ComponentChannelType channelType,
            Value valueState, String label, ChannelStateUpdateListener channelStateUpdateListener) {
        if (groupId == null) {
            channelID = componentId;
        }
        return new ComponentChannel.Builder(this, channelID, channelType.getChannelTypeUID(), valueState, label,
                channelStateUpdateListener);
    }

    public void setConfigSeen() {
        this.configSeen = true;
    }

    /**
     * Subscribes to all state channels of the component and adds all channels to the provided channel type provider.
     *
     * @param connection connection to the MQTT broker
     * @param scheduler thing scheduler
     * @param timeout channel subscription timeout
     * @return A future that completes as soon as all subscriptions have been performed. Completes exceptionally on
     *         errors.
     */
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        // Hidden channels (used by a component to simulate other channels or non-channel behavior),
        // triggers channels (which can be used by rules without ever being linked),
        // and linked channels are started.
        // Therefore, unlinked channels are not started.
        return Stream.concat(channels.values().stream().filter(c -> {
            return c.getChannel().getKind().equals(ChannelKind.TRIGGER)
                    || componentContext.getLinkageChecker().isChannelLinked(c.getChannel().getUID());
        }), hiddenChannels.stream()).map(v -> v.start(connection, scheduler, timeout)) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    /**
     * Unsubscribes from all state channels of the component.
     *
     * @return A future that completes as soon as all subscriptions removals have been performed. Completes
     *         exceptionally on errors.
     */
    public CompletableFuture<@Nullable Void> stop() {
        return Stream.concat(channels.values().stream(), hiddenChannels.stream()) //
                .filter(Objects::nonNull) //
                .map(ComponentChannel::stop) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    /**
     * Add all state and command descriptions to the state description provider.
     *
     * @param stateDescriptionProvider The state description provider
     */
    public void addStateDescriptions(MqttChannelStateDescriptionProvider stateDescriptionProvider) {
        channels.values().forEach(channel -> {
            StateDescription stateDescription = channel.getStateDescription();
            if (stateDescription != null) {
                stateDescriptionProvider.setDescription(channel.getChannel().getUID(), stateDescription);
            }
            CommandDescription commandDescription = channel.getCommandDescription();
            if (commandDescription != null) {
                stateDescriptionProvider.setDescription(channel.getChannel().getUID(), commandDescription);
            }
        });
    }

    public ChannelUID buildChannelUID(String channelID) {
        final String localGroupID = groupId;
        if (localGroupID != null) {
            return new ChannelUID(componentContext.getThingUID(), localGroupID, channelID);
        }
        return new ChannelUID(componentContext.getThingUID(), channelID);
    }

    public String getComponentId() {
        return componentId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Component (Channel Group) name.
     */
    public String getName() {
        return config.getName();
    }

    /**
     * Each component consists of multiple Channels.
     */
    public Map<String, ComponentChannel> getChannelMap() {
        return channels;
    }

    /**
     * Return a components channel. A HomeAssistant MQTT component consists of multiple functions
     * and those are mapped to one or more channels. The channel IDs are constants within the
     * derived Component, like the {@link Switch#SWITCH_CHANNEL_ID}.
     *
     * @param channelID The channel ID
     * @return A components channel
     */
    public @Nullable ComponentChannel getChannel(String channelID) {
        return channels.get(channelID);
    }

    /**
     * @return Returns the configuration hash value for easy comparison.
     */
    public int getConfigHash() {
        return configHash;
    }

    /**
     * Return the channel group type.
     */
    public @Nullable ChannelGroupType getChannelGroupType(String prefix) {
        if (groupId == null) {
            return null;
        }
        return ChannelGroupTypeBuilder.instance(getChannelGroupTypeUID(prefix), getName())
                .withChannelDefinitions(getAllChannelDefinitions()).build();
    }

    public List<ChannelDefinition> getChannelDefinitions() {
        if (groupId != null) {
            return List.of();
        }
        return getAllChannelDefinitions();
    }

    private List<ChannelDefinition> getAllChannelDefinitions() {
        return channels.values().stream().map(ComponentChannel::channelDefinition).toList();
    }

    public List<Channel> getChannels() {
        return channels.values().stream().map(ComponentChannel::getChannel).toList();
    }

    public void getChannelStates(Map<ChannelUID, ChannelState> states) {
        channels.values().forEach(c -> states.put(c.getChannel().getUID(), c.getState()));
    }

    /**
     * Resets all channel states to state UNDEF. Call this method after the connection
     * to the MQTT broker got lost.
     */
    public void resetState() {
        channels.values().forEach(ComponentChannel::resetState);
    }

    /**
     * Return the channel group definition for this component.
     */
    public @Nullable ChannelGroupDefinition getGroupDefinition(String prefix) {
        String localGroupId = groupId;
        if (localGroupId == null) {
            return null;
        }
        return new ChannelGroupDefinition(localGroupId, getChannelGroupTypeUID(prefix), getName(), null);
    }

    public boolean hasGroup() {
        return groupId != null;
    }

    public HaID getHaID() {
        return componentContext.getHaID();
    }

    public String getChannelConfigurationJson() {
        return componentContext.getConfigJSON();
    }

    public boolean isEnabledByDefault() {
        return config.isEnabledByDefault();
    }

    public HomeAssistantPythonBridge getPython() {
        return componentContext.getPython();
    }

    public C getConfig() {
        return config;
    }

    private ChannelGroupTypeUID getChannelGroupTypeUID(String prefix) {
        return new ChannelGroupTypeUID(MqttBindingConstants.BINDING_ID, prefix + "_" + uniqueId);
    }

    public boolean mergeable(AbstractComponent<?> other) {
        return false;
    }

    protected Configuration mergeChannelConfiguration(ComponentChannel channel, AbstractComponent<C> other) {
        Configuration currentConfiguration = channel.getChannel().getConfiguration();
        Configuration newConfiguration = new Configuration();
        newConfiguration.put("component", currentConfiguration.get("component"));
        newConfiguration.put("nodeid", currentConfiguration.get("nodeid"));
        Object objectIdObject = currentConfiguration.get("objectid");
        if (objectIdObject instanceof String objectIdString) {
            if (!objectIdString.equals(other.getHaID().objectID)) {
                newConfiguration.put("objectid", List.of(objectIdString, other.getHaID().objectID));
            }
        } else if (objectIdObject instanceof List<?> objectIdList) {
            newConfiguration.put("objectid", Stream.concat(objectIdList.stream(), Stream.of(other.getHaID().objectID))
                    .sorted().distinct().toList());
        }
        Object configObject = currentConfiguration.get("config");
        if (configObject instanceof String configString) {
            if (!configString.equals(other.getChannelConfigurationJson())) {
                newConfiguration.put("config", List.of(configString, other.getChannelConfigurationJson()));
            }
        } else if (configObject instanceof List<?> configList) {
            newConfiguration.put("config",
                    Stream.concat(configList.stream(), Stream.of(other.getChannelConfigurationJson())).sorted()
                            .distinct().toList());
        }
        return newConfiguration;
    }

    /**
     * Take another component of the same type, and merge it so that only one (set of)
     * channel(s) exist on the Thing.
     *
     * @return if the component was stopped, and thus needs restarted
     */
    public boolean merge(AbstractComponent<?> other) {
        return false;
    }

    protected TemperatureUnit getTemperatureUnit(@Nullable String configTemperatureUnit) {
        TemperatureUnit temperatureUnit;
        if (configTemperatureUnit == null) {
            if (ImperialUnits.FAHRENHEIT.equals(componentContext.getUnitProvider().getUnit(Temperature.class))) {
                temperatureUnit = TemperatureUnit.FAHRENHEIT;
            } else {
                temperatureUnit = TemperatureUnit.CELSIUS;
            }
        } else {
            temperatureUnit = TemperatureUnit.fromString(configTemperatureUnit);
        }
        return temperatureUnit;
    }
}
