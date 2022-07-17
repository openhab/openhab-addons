/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.argoclima.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.argoclima.internal.configuration.IScheduleConfigurationProvider.ScheduleTimerType;
import org.openhab.binding.argoclima.internal.device.api.types.Weekday;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameterGroup;
import org.openhab.core.config.core.ConfigDescriptionParameterGroupBuilder;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArgoClimaConfigProvider} class provides dynamic configuration entries
 * for the things supported by the binding (on top of static properties defined in
 * {@code thing-types.xml})
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
@Component(service = { ConfigDescriptionProvider.class })
public class ArgoClimaConfigProvider implements ConfigDescriptionProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingRegistry thingRegistry;
    private static final int SCHEDULE_TIMERS_COUNT = 3;

    public record ScheduleDefaults(String startTime, String endTime, EnumSet<Weekday> weekdays) {
        /**
         * @implNote Overriding the default-generated method, as it doesn't preserve {@code NonNull} annotation on the
         *           element set.
         */
        public EnumSet<Weekday> weekdays() {
            return weekdays;
        }
    }

    private static final Map<ScheduleTimerType, ScheduleDefaults> SCHEDULE_DEFAULTS = Map.of(
            ScheduleTimerType.SCHEDULE_1,
            new ScheduleDefaults("08:00", "18:00",
                    EnumSet.of(
                            Weekday.MON, Weekday.TUE, Weekday.WED, Weekday.THU, Weekday.FRI, Weekday.SAT, Weekday.SUN)),
            ScheduleTimerType.SCHEDULE_2,
            new ScheduleDefaults("15:00", "20:00",
                    EnumSet.of(Weekday.MON, Weekday.TUE, Weekday.WED, Weekday.THU, Weekday.FRI)),
            ScheduleTimerType.SCHEDULE_3, new ScheduleDefaults("11:00", "22:00", EnumSet.of(Weekday.SAT, Weekday.SUN)));

    public static final ScheduleDefaults getScheduleDefaults(ScheduleTimerType scheduleTimerType) {
        if (!EnumSet.allOf(ScheduleTimerType.class).contains(scheduleTimerType)) {
            throw new IllegalArgumentException("Invalid schedule timer: " + scheduleTimerType.toString());
        }
        var result = SCHEDULE_DEFAULTS.get(scheduleTimerType);
        Objects.requireNonNull(result);
        return result;
    }

    @Activate
    public ArgoClimaConfigProvider(final @Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    /**
     * Provides a collection of {@link ConfigDescription}s.
     *
     * @param locale locale
     * @return the configuration descriptions provided by this provider (not
     *         null, could be empty)
     */
    @Override
    public Collection<ConfigDescription> getConfigDescriptions(@Nullable Locale locale) {
        return Collections.emptySet(); // no dynamic values
    }

    /**
     * Provides a {@link ConfigDescription} for the given URI.
     *
     * @param uri URI of the config description (may be either thing or thing-type URI)
     * @param locale locale
     * @return config description or null if no config description could be found
     *
     * @implNote {@code ConfigDescriptionParameterBuilder} doesn't have non-null-defaults, while
     *           {@code ConfigDescriptionBuilder} does... so while it's quite redundant, using Objects.requireNonNull()
     *           to keep number of warnings low
     */
    @Override
    @Nullable
    public ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale) {
        if (!uri.getScheme().equalsIgnoreCase("thing")) {
            return null; // Deliberately not supporting "thing-type" (no dynamic parameters there)
        }
        ThingUID thingUID = new ThingUID(Objects.requireNonNull(uri.getSchemeSpecificPart()));
        if (!thingUID.getBindingId().equals(ArgoClimaBindingConstants.BINDING_ID)) {
            return null;
        }

        var thing = this.thingRegistry.get(thingUID);
        if (thing == null) {
            logger.trace("getConfigDescription: No thing found for uri: {}", uri);
            return null;
        }

        var paramGroups = new ArrayList<ConfigDescriptionParameterGroup>();
        for (int i = 1; i <= SCHEDULE_TIMERS_COUNT; ++i) {
            paramGroups.add(ConfigDescriptionParameterGroupBuilder
                    .create(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_GROUP_NAME, i))
                    .withLabel(String.format("Schedule %d", i))
                    .withDescription(String.format("Schedule timer - profile %d.", i)).build());
        }
        if (thing.isEnabled()) {
            // Note: Do not localize the label & description (ref: https://github.com/openhab/openhab-webui/issues/1491)
            paramGroups.add(ConfigDescriptionParameterGroupBuilder.create("actions").withLabel("Actions")
                    .withDescription("Actions").build());
        }

        var parameters = new ArrayList<ConfigDescriptionParameter>();

        var daysOfWeek = List.<@Nullable ParameterOption> of(new ParameterOption(Weekday.MON.toString(), "Monday"),
                new ParameterOption(Weekday.TUE.toString(), "Tuesday"),
                new ParameterOption(Weekday.WED.toString(), "Wednesday"),
                new ParameterOption(Weekday.THU.toString(), "Thursday"),
                new ParameterOption(Weekday.FRI.toString(), "Friday"),
                new ParameterOption(Weekday.SAT.toString(), "Saturday"),
                new ParameterOption(Weekday.SUN.toString(), "Sunday"));

        for (int i = 1; i <= SCHEDULE_TIMERS_COUNT; ++i) {
            // NOTE: Deliberately *not* using .withContext("dayOfWeek") - doesn't seem to work correctly :(
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_DAYS, i), Type.TEXT)
                    .withRequired(true)
                    .withGroupName(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_GROUP_NAME, i))//
                    .withLabel("Days").withDescription("Days when the schedule is run").withOptions(daysOfWeek)
                    .withDefault(getScheduleDefaults(ScheduleTimerType.fromInt(i)).weekdays().toString())
                    .withMultiple(true).withMultipleLimit(7).build()));

            // NOTE: Deliberately *not* using .withContext("time") - does work, but causes UI to detect each entry to
            // the page as a change
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_ON_TIME, i), Type.TEXT)
                    .withRequired(true)
                    .withGroupName(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_GROUP_NAME, i))
                    .withPattern("\\d{1-2}:\\d{1-2}").withLabel("On time").withDescription("Time when the A/C turns on")
                    .withDefault(getScheduleDefaults(ScheduleTimerType.fromInt(i)).startTime()).build()));
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_OFF_TIME, i), Type.TEXT)
                    .withRequired(true)
                    .withGroupName(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_GROUP_NAME, i))
                    .withLabel("Off time").withDescription("Time when the A/C turns off")
                    .withDefault(getScheduleDefaults(ScheduleTimerType.fromInt(i)).endTime()).build()));
        }
        if (thing.isEnabled()) {
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(ArgoClimaBindingConstants.PARAMETER_RESET_TO_FACTORY_DEFAULTS, Type.BOOLEAN)
                    .withRequired(false).withGroupName(ArgoClimaBindingConstants.PARAMETER_ACTIONS_GROUP_NAME)
                    .withLabel("Reset settings").withDescription("Reset device settings to factory defaults")
                    .withDefault("false").withVerify(true).build()));
        }

        var config = ConfigDescriptionBuilder.create(uri).withParameterGroups(paramGroups).withParameters(parameters)
                .build();
        return config;
    }
}
