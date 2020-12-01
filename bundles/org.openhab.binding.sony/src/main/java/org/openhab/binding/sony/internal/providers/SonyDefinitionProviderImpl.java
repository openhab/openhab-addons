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
package org.openhab.binding.sony.internal.providers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.providers.models.SonyDeviceCapability;
import org.openhab.binding.sony.internal.providers.models.SonyThingChannelDefinition;
import org.openhab.binding.sony.internal.providers.models.SonyThingDefinition;
import org.openhab.binding.sony.internal.providers.models.SonyThingStateDefinition;
import org.openhab.binding.sony.internal.providers.sources.SonyFolderSource;
import org.openhab.binding.sony.internal.providers.sources.SonyGithubSource;
import org.openhab.binding.sony.internal.providers.sources.SonySource;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of {@link SonyDefinitionProvider} will manage the various
 * {@link SonySource} and provide data to and from them
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(immediate = true, service = { DynamicStateDescriptionProvider.class, SonyDynamicStateProvider.class,
        SonyDefinitionProvider.class, ThingTypeProvider.class, ChannelGroupTypeProvider.class,
        SonyModelProvider.class }, properties = "OSGI-INF/SonyDefinitionProviderImpl.properties", configurationPid = "sony.sources")
@NonNullByDefault
public class SonyDefinitionProviderImpl implements SonyDefinitionProvider, SonyDynamicStateProvider {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** The list of sources (created in activate, cleared in deactivate) */
    private final List<SonySource> sources;

    /** The list of dynamic state overrides by channel uid */
    private final Map<ChannelUID, StateDescription> stateOverride = new HashMap<>();

    /** The thing registry used to lookup things */
    private final ThingRegistry thingRegistry;

    /** The thing registry used to lookup things */
    private final ThingTypeRegistry thingTypeRegistry;

    /** Scheduler used to schedule events */
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("SonyDefinitionProviderImpl");

    /**
     * Constructs the sony definition provider implmentation
     * 
     * @param thingRegistry a non-null thing registry
     * @param thingTypeRegistry a non-null thing type registry
     * @param properties the OSGI properties
     */
    @Activate
    public SonyDefinitionProviderImpl(final @Reference ThingRegistry thingRegistry,
            final @Reference ThingTypeRegistry thingTypeRegistry, final Map<String, String> properties) {
        Objects.requireNonNull(thingRegistry, "thingRegistry cannot be null");
        Objects.requireNonNull(thingTypeRegistry, "thingTypeRegistry cannot be null");
        Objects.requireNonNull(properties, "properties cannot be null");

        this.thingRegistry = thingRegistry;
        this.thingTypeRegistry = thingTypeRegistry;

        // local takes preference over github
        final List<SonySource> srcs = new ArrayList<>();
        if (BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(properties.get("local")))) {
            srcs.add(new SonyFolderSource(scheduler, properties));
        }
        if (BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(properties.get("github")))) {
            srcs.add(new SonyGithubSource(scheduler, properties));
        }
        this.sources = Collections.unmodifiableList(srcs);
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(final ChannelGroupTypeUID channelGroupTypeUID,
            final @Nullable Locale locale) {
        Objects.requireNonNull(channelGroupTypeUID, "thingTypeUID cannot be null");
        if (StringUtils.equalsIgnoreCase(channelGroupTypeUID.getBindingId(), SonyBindingConstants.BINDING_ID)) {
            for (final SonySource src : sources) {
                final ChannelGroupType groupType = src.getChannelGroupType(channelGroupTypeUID);
                if (groupType != null) {
                    return groupType;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(final @Nullable Locale locale) {
        final Map<ChannelGroupTypeUID, ChannelGroupType> groupTypes = new HashMap<>();
        for (final SonySource src : sources) {
            final Collection<ChannelGroupType> localGroupTypes = src.getChannelGroupTypes();
            if (localGroupTypes != null) {
                for (final ChannelGroupType gt : localGroupTypes) {
                    if (!groupTypes.containsKey(gt.getUID())) {
                        groupTypes.put(gt.getUID(), gt);
                    }
                }
            }
        }
        return groupTypes.values();
    }

    @Override
    public Collection<ThingType> getThingTypes(final @Nullable Locale locale) {
        final Map<ThingTypeUID, ThingType> thingTypes = new HashMap<>();
        for (final SonySource src : sources) {
            for (final ThingType tt : src.getThingTypes()) {
                if (!thingTypes.containsKey(tt.getUID())) {
                    thingTypes.put(tt.getUID(), tt);
                }
            }
        }
        return thingTypes.values();
    }

    @Override
    public @Nullable ThingType getThingType(final ThingTypeUID thingTypeUID, final @Nullable Locale locale) {
        Objects.requireNonNull(thingTypeUID, "thingTypeUID cannot be null");
        if (StringUtils.equalsIgnoreCase(thingTypeUID.getBindingId(), SonyBindingConstants.BINDING_ID)) {
            for (final SonySource src : sources) {
                final ThingType thingType = src.getThingType(thingTypeUID);
                if (thingType != null) {
                    return thingType;
                }
            }
        }
        return null;
    }

    @Override
    public void addStateOverride(final ThingUID thingUID, final String channelId,
            final StateDescription stateDescription) {
        Objects.requireNonNull(thingUID, "thingUID cannot be null");
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Objects.requireNonNull(stateDescription, "stateDescription cannot be null");

        final ChannelUID id = new ChannelUID(thingUID, channelId);
        stateOverride.put(id, stateDescription);
    }

    @Override
    public @Nullable StateDescription getStateDescription(final Channel channel,
            final @Nullable StateDescription originalStateDescription, final @Nullable Locale locale) {
        Objects.requireNonNull(channel, "channel cannot be null");

        if (StringUtils.equalsIgnoreCase(channel.getUID().getBindingId(), SonyBindingConstants.BINDING_ID)) {
            return getStateDescription(channel.getUID().getThingUID(), channel.getUID().getId(),
                    originalStateDescription);
        }
        return null;
    }

    @Override
    public @Nullable StateDescription getStateDescription(final ThingUID thingUID, final String channelId) {
        return getStateDescription(thingUID, channelId, null);
    }

    /**
     * This is a helper method to get a state description for a specific thingUID
     * and channel ID. This will intelligenly merge the original state description
     * (from a thing definition) with any overrides that have been added
     *
     * @param thingUID a non-null thing uid
     * @param channelId a non-null, non-empty channel id
     * @param originalStateDescription a potentially null (if none) original state
     *            description
     * @return the state definition for the thing/channel or the original if none found
     */
    private @Nullable StateDescription getStateDescription(final ThingUID thingUID, final String channelId,
            final @Nullable StateDescription originalStateDescription) {
        Objects.requireNonNull(thingUID, "thingUID cannot be null");
        Validate.notEmpty(channelId, "channelID cannot be empty");

        final ThingRegistry localThingRegistry = thingRegistry;
        if (localThingRegistry != null) {
            final Thing thing = localThingRegistry.get(thingUID);
            final ChannelUID id = new ChannelUID(thingUID, channelId);

            if (thing != null) {
                BigDecimal min = null, max = null, step = null;
                String pattern = null;
                Boolean readonly = null;
                List<StateOption> options = null;

                // First use any specified override (if found)
                // Note since compiler thinks overrideDesc cannot be null
                // it flags the 'readonly' below as can't be null (which is incorrect)
                final StateDescription overrideDesc = stateOverride.get(id);
                if (overrideDesc != null) {
                    min = overrideDesc.getMinimum();
                    max = overrideDesc.getMaximum();
                    step = overrideDesc.getStep();
                    pattern = overrideDesc.getPattern();
                    readonly = overrideDesc.isReadOnly();
                    options = overrideDesc.getOptions();
                }

                // Finally use the original values
                if (originalStateDescription != null) {
                    if (min == null) {
                        min = originalStateDescription.getMinimum();
                    }

                    if (max == null) {
                        max = originalStateDescription.getMaximum();
                    }

                    if (step == null) {
                        step = originalStateDescription.getStep();
                    }

                    if (pattern == null) {
                        pattern = originalStateDescription.getPattern();
                    }

                    if (readonly == null) {
                        readonly = originalStateDescription.isReadOnly();
                    }

                    if (options == null) {
                        options = originalStateDescription.getOptions();
                    }
                }

                // If anything is specified, create a new state description and go with it
                if (min != null || max != null || step != null || pattern != null || readonly != null
                        || (options != null && !options.isEmpty())) {
                    StateDescriptionFragmentBuilder bld = StateDescriptionFragmentBuilder.create();
                    if (min != null) {
                        bld = bld.withMinimum(min);
                    }
                    if (max != null) {
                        bld = bld.withMaximum(max);
                    }
                    if (step != null) {
                        bld = bld.withStep(step);
                    }
                    if (pattern != null) {
                        bld = bld.withPattern(pattern);
                    }
                    if (readonly != null) {
                        bld = bld.withReadOnly(readonly);
                    }
                    if (!options.isEmpty()) {
                        bld = bld.withOptions(options);
                    }
                    return bld.build().toStateDescription();
                }
            }
        }
        return null;
    }

    @Override
    public void writeDeviceCapabilities(final SonyDeviceCapability deviceCapability) {
        Objects.requireNonNull(deviceCapability, "deviceCapability cannot be null");
        for (final SonySource src : sources) {
            src.writeDeviceCapabilities(deviceCapability);
        }
    }

    @Override
    public void writeThing(final String service, final String configUri, final String modelName, final Thing thing,
            final Predicate<Channel> channelFilter) {
        Validate.notEmpty(service, "service cannot be empty");
        Validate.notEmpty(configUri, "configUri cannot be empty");
        Validate.notEmpty(modelName, "modelName cannot be empty");
        Objects.requireNonNull(thing, "thing cannot be null");
        Objects.requireNonNull(channelFilter, "channelFilter cannot be null");

        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (!StringUtils.equalsIgnoreCase(service, thingTypeUID.getId())) {
            logger.debug("Could not write thing type - already a specific thing type (not generic)");
            return;
        }

        final ThingTypeRegistry localThingTypeRegistry = thingTypeRegistry;
        if (localThingTypeRegistry == null) {
            logger.debug("Could not write thing type - thing type registry was null");
            return;
        }

        final ThingType thingType = localThingTypeRegistry.getThingType(thingTypeUID);
        if (thingType == null) {
            logger.debug("Could not write thing type - thing type was not found in the sony sources");
            return;
        }

        // Get the state channel that have a type (with no mapping)
        // ignore null warning as the filter makes sure it's not null
        final List<SonyThingChannelDefinition> chls = thing.getChannels().stream().filter(channelFilter).map(chl -> {
            final ChannelTypeUID ctuid = chl.getChannelTypeUID();
            return ctuid == null ? null
                    : new SonyThingChannelDefinition(chl.getUID().getId(), ctuid.getId(),
                            new SonyThingStateDefinition(getStateDescription(chl, null, null)), chl.getProperties());
        }).filter(chl -> chl != null).sorted((f, l) -> f.getChannelId().compareToIgnoreCase(l.getChannelId()))
                .collect(Collectors.toList());

        final String label = StringUtils.defaultIfEmpty(thing.getLabel(), thingType.getLabel());
        if (label == null || StringUtils.isEmpty(label)) {
            logger.debug("Could not write thing type - no label was found");
            return;
        }

        final String desc = thingType.getDescription();

        // hardcoded service groups for now
        final SonyThingDefinition ttd = new SonyThingDefinition(service, configUri, modelName, "Sony " + label,
                StringUtils.defaultIfEmpty(desc, label), ScalarWebService.getServiceLabels(), chls);

        for (final SonySource src : sources) {
            src.writeThingDefinition(ttd);
        }
    }

    @Deactivate
    public void deactivate() {
        for (final SonySource src : sources) {
            src.close();
        }
    }

    @Override
    public void addListener(final String modelName, final ThingTypeUID currentThingTypeUID,
            final SonyModelListener listener) {
        sources.forEach(s -> s.addListener(modelName, currentThingTypeUID, listener));
    }

    @Override
    public boolean removeListener(final SonyModelListener listener) {
        return sources.stream().map(s -> s.removeListener(listener)).anyMatch(e -> e);
    }
}
