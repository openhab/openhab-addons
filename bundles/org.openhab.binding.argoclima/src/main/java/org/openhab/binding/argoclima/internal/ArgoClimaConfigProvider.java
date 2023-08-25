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
    private final ArgoClimaTranslationProvider i18nProvider;
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
    public ArgoClimaConfigProvider(final @Reference ThingRegistry thingRegistry,
            final @Reference ArgoClimaTranslationProvider i18nProvider) {
        this.thingRegistry = thingRegistry;
        this.i18nProvider = i18nProvider;
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
     * @param locale locale (not using this value, as our i18n provider comes with it pre-populated!)
     * @return config description or null if no config description could be found
     *
     * @implNote {@code ConfigDescriptionParameterBuilder} doesn't have non-null-defaults, while
     *           {@code ConfigDescriptionBuilder} does... so while it's quite redundant, using Objects.requireNonNull()
     *           to keep number of warnings low
     */
    @Override
    @Nullable
    public ConfigDescription getConfigDescription(URI uri, @Nullable Locale locale) {
        if (!"thing".equalsIgnoreCase(uri.getScheme())) {
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
                    .withLabel(
                            i18nProvider.getText("dynamic-config.argoclima.group.schedule.label", "Schedule {0} ", i))
                    .withDescription(i18nProvider.getText("dynamic-config.argoclima.group.schedule.description",
                            "Schedule timer - profile {0}.", i))
                    .build());
        }
        if (thing.isEnabled()) {
            paramGroups.add(ConfigDescriptionParameterGroupBuilder.create("actions").withContext("actions")
                    .withLabel(i18nProvider.getText("dynamic-config.argoclima.group.actions.label", "Actions"))
                    .build());
        }

        var parameters = new ArrayList<ConfigDescriptionParameter>();

        var daysOfWeek = List.<@Nullable ParameterOption> of(
                new ParameterOption(Weekday.MON.toString(),
                        i18nProvider.getText("dynamic-config.argoclima.schedule.days.monday", "Monday")),
                new ParameterOption(Weekday.TUE.toString(),
                        i18nProvider.getText("dynamic-config.argoclima.schedule.days.tuesday", "Tuesday")),
                new ParameterOption(Weekday.WED.toString(),
                        i18nProvider.getText("dynamic-config.argoclima.schedule.days.wednesday", "Wednesday")),
                new ParameterOption(Weekday.THU.toString(),
                        i18nProvider.getText("dynamic-config.argoclima.schedule.days.thursday", "Thursday")),
                new ParameterOption(Weekday.FRI.toString(),
                        i18nProvider.getText("dynamic-config.argoclima.schedule.days.friday", "Friday")),
                new ParameterOption(Weekday.SAT.toString(),
                        i18nProvider.getText("dynamic-config.argoclima.schedule.days.saturday", "Saturday")),
                new ParameterOption(Weekday.SUN.toString(),
                        i18nProvider.getText("dynamic-config.argoclima.schedule.days.sunday", "Sunday")));

        for (int i = 1; i <= SCHEDULE_TIMERS_COUNT; ++i) {
            // NOTE: Deliberately *not* using .withContext("dayOfWeek") - doesn't seem to work correctly :(
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_DAYS, i), Type.TEXT)
                    .withRequired(true)
                    .withGroupName(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_GROUP_NAME, i))//
                    .withLabel(i18nProvider.getText("dynamic-config.argoclima.schedule.days.label", "Days"))
                    .withDescription(i18nProvider.getText("dynamic-config.argoclima.schedule.days.description",
                            "Days when the schedule is run"))
                    .withOptions(daysOfWeek)
                    .withDefault(getScheduleDefaults(ScheduleTimerType.fromInt(i)).weekdays().toString())
                    .withMultiple(true).withMultipleLimit(7).build()));

            // NOTE: Deliberately *not* using .withContext("time") - does work, but causes UI to detect each entry to
            // the page as a change
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_ON_TIME, i), Type.TEXT)
                    .withRequired(true)
                    .withGroupName(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_GROUP_NAME, i))
                    .withPattern("\\d{1-2}:\\d{1-2}")
                    .withLabel(i18nProvider.getText("dynamic-config.argoclima.schedule.on-time.label", "On Time"))
                    .withDescription(i18nProvider.getText("dynamic-config.argoclima.schedule.on-time.description",
                            "Time when the A/C turns on"))
                    .withDefault(getScheduleDefaults(ScheduleTimerType.fromInt(i)).startTime()).build()));
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_OFF_TIME, i), Type.TEXT)
                    .withRequired(true)
                    .withGroupName(String.format(ArgoClimaBindingConstants.PARAMETER_SCHEDULE_GROUP_NAME, i))
                    .withLabel(i18nProvider.getText("dynamic-config.argoclima.schedule.off-time.label", "Off Time"))
                    .withDescription(i18nProvider.getText("dynamic-config.argoclima.schedule.off-time.description",
                            "Time when the A/C turns off"))
                    .withDefault(getScheduleDefaults(ScheduleTimerType.fromInt(i)).endTime()).build()));
        }
        if (thing.isEnabled()) {
            parameters.add(Objects.requireNonNull(ConfigDescriptionParameterBuilder
                    .create(ArgoClimaBindingConstants.PARAMETER_RESET_TO_FACTORY_DEFAULTS, Type.BOOLEAN)
                    .withRequired(false).withGroupName(ArgoClimaBindingConstants.PARAMETER_ACTIONS_GROUP_NAME)
                    .withLabel(i18nProvider.getText("dynamic-config.argoclima.schedule.reset.label", "Reset Settings"))
                    .withDescription(i18nProvider.getText("dynamic-config.argoclima.schedule.reset.description",
                            "Reset device settings to factory defaults"))
                    .withDefault("false").withVerify(true).build()));
        }

        return ConfigDescriptionBuilder.create(uri).withParameterGroups(paramGroups).withParameters(parameters).build();
    }
}
