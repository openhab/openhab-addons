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
package org.openhab.binding.argoclima.internal.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.ArgoClimaConfigProvider;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.device.api.types.Weekday;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;
import org.openhab.binding.argoclima.internal.utils.StringUtils;
import org.openhab.core.config.core.Configuration;

/**
 * The {@link ArgoClimaConfigurationBase} class contains fields mapping thing configuration parameters.
 * Contains common configuration parameters (same for all supported device types).
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public abstract class ArgoClimaConfigurationBase extends Configuration implements IScheduleConfigurationProvider {
    /////////////////////
    // TYPES
    /////////////////////
    @FunctionalInterface
    public interface ConfigValueSupplier<T> {
        public T get() throws ArgoConfigurationException;
    }

    /////////////////////
    // Configuration parameters
    // These names are defined in thing-types.xml and/or ArgoClimaConfigProvider and get injected on instantiation
    // through {@link org.openhab.core.thing.binding.BaseThingHandler#getConfigAs getConfigAs}
    /////////////////////
    private int refreshInterval = 30; // in seconds
    private String deviceCpuId = "";
    private int oemServerPort = 80;
    private String oemServerAddress = "31.14.128.210";

    // Note this boilerplate is actually necessary as these values are injected by framework!
    private Set<Weekday> schedule1DayOfWeek = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_1)
            .weekdays();
    private String schedule1OnTime = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_1)
            .startTime();
    private String schedule1OffTime = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_1)
            .endTime();
    private Set<Weekday> schedule2DayOfWeek = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_2)
            .weekdays();
    private String schedule2OnTime = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_2)
            .startTime();
    private String schedule2OffTime = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_2)
            .endTime();
    private Set<Weekday> schedule3DayOfWeek = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_3)
            .weekdays();
    private String schedule3OnTime = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_3)
            .startTime();
    private String schedule3OffTime = ArgoClimaConfigProvider.getScheduleDefaults(ScheduleTimerType.SCHEDULE_3)
            .endTime();

    public boolean resetToFactoryDefaults = false;

    /////////////////////
    // Other fields
    /////////////////////
    private static final DateTimeFormatter SCHEDULE_ON_OFF_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm[:ss]");
    protected @Nullable ArgoClimaTranslationProvider i18nProvider;

    /**
     * Initializes the configuration class post construction, injecting i18n provider for localized configuration
     * exceptions
     *
     * @implNote This class requires default/parameterless c-tor for framework-side initialization (from file)
     * @param i18nProvider Framework's translation provider
     */
    public void initialize(ArgoClimaTranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    /**
     * Get the user-configured CPUID of the Argo device (used in matching to a concrete device in a stub mode)
     *
     * @return The configured CPUID (if provided by the user = not blank)
     */
    public Optional<String> getDeviceCpuId() {
        return this.deviceCpuId.isBlank() ? Optional.<String> empty() : Optional.of(this.deviceCpuId);
    }

    /**
     * Get the refresh interval the device is polled with (in seconds)
     *
     * @return The interval value {@code 0} - to disable polling
     */
    public int getRefreshInterval() {
        return this.refreshInterval;
    }

    /**
     * If true, allows the binding to directly communicate with the device (or vendor's server - for remote thing type).
     * When false, binding will not communicate directly with the device and wait for it to call it (through
     * intercepting/stub server)
     * <p>
     * <b>Mode-specific considerations</b>:
     * <ul>
     * <li>in {@code REMOTE_API_STUB} mode - will not issue any outbound connections on its own</li>
     * <li>in {@code REMOTE_API_PROXY} mode - will still communicate with vendor's servers but ONLY when queried by the
     * device (a pass-through)</li>
     * </ul>
     *
     * @implNote While this is configured by its dedicated settings (for better UX) and valid only for Local Thing
     *           types, internal implementation uses {@code refreshInterval == 0} to signify no comms. This is because
     *           without a refresh, the binding would have to function in a fire and forget mode sending commands back
     *           to HVAC and never receiving any ACK... which makes little sense, hence is not supported
     *
     * @return True if the Thing is allowed to communicate outwards on its own, False otherwise
     */
    public boolean useDirectConnection() {
        return getRefreshInterval() > 0; // Uses virtual method overridden for local device!
    }

    /**
     * The OEM server's address, used to pass through the communications to (in REMOTE_API_PROXY) mode
     *
     * @return The vendor's server IP address
     * @throws ArgoConfigurationException In case the IP cannot be found
     */
    public InetAddress getOemServerAddress() throws ArgoConfigurationException {
        try {
            return Objects.requireNonNull(InetAddress.getByName(oemServerAddress));
        } catch (UnknownHostException e) {
            throw ArgoConfigurationException.forInvalidParamValue(
                    ArgoClimaBindingConstants.PARAMETER_OEM_SERVER_ADDRESS, oemServerAddress, i18nProvider, e);
        }
    }

    /**
     * The OEM server's port, used to pass through the communications to (in REMOTE_API_PROXY) mode
     *
     * @return Vendor's server port. {@code -1} for no value
     */
    public int getOemServerPort() {
        return this.oemServerPort;
    }

    /**
     * Converts "raw" {@code Set<Weekday>} into an {@code EnumSet<Weekday>}
     *
     * @implNote Because this configuration parameter is *dynamic* (and deliberately not defined in
     *           {@code thing-types.xml}) when OH is loading a textual thing file, it does not have a full definition
     *           yet, hence CANNOT infer its data type.
     *           The Thing.xtext definition for {@code ModelProperty} allows for arrays, but these are always implicit/
     *           For example {@code schedule1DayOfWeek="MON","TUE"} deserializes as a Collection (and is properly cast
     *           to enum later), however a {@code schedule1DayOfWeek="MON"} deserializes to a String, and causes a
     *           {@link ClassCastException} on access. This impl. accounts for that forced "as-String" interpretation on
     *           load, and coerces such values back to a collection.
     * @param rawInput The value to process
     * @param paramName Name of the textual parameter (for error messaging)
     * @return Converted value
     * @throws ArgoConfigurationException In case the conversion fails
     */
    private EnumSet<Weekday> canonizeWeekdaysAfterDeserialization(Set<Weekday> rawInput, String paramName)
            throws ArgoConfigurationException {
        try {
            var items = rawInput.toArray();
            if (items.length == 1 && !(items[0] instanceof Weekday)) {
                // Text based configuration -> falling back to string parse
                var strValue = StringUtils.strip(items[0].toString(), "[]- \t\"'").trim();
                var daysStr = StringUtils.splitByWholeSeparator(strValue, ",").stream();

                var result = EnumSet.noneOf(Weekday.class);
                daysStr.map(ds -> Weekday.valueOf(ds.strip())).forEach(wd -> result.add(wd));
                return result;
            } else {
                // UI/API configuration (nicely strong-typed already)
                return EnumSet.copyOf(rawInput);
            }
        } catch (ClassCastException | IllegalArgumentException e) {
            throw ArgoConfigurationException.forInvalidParamValue(paramName, rawInput.toString(), i18nProvider, e);
        }
    }

    record ConfigParam<K> (K paramValue, String paramName) {
    }

    @Override
    public EnumSet<Weekday> getScheduleDayOfWeek(ScheduleTimerType scheduleType) throws ArgoConfigurationException {
        ConfigParam<Set<Weekday>> configValue;
        switch (scheduleType) {
            case SCHEDULE_1:
                configValue = new ConfigParam<>(schedule1DayOfWeek,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_DAYS.formatted(1));
                break;
            case SCHEDULE_2:
                configValue = new ConfigParam<>(schedule2DayOfWeek,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_DAYS.formatted(2));
                break;
            case SCHEDULE_3:
                configValue = new ConfigParam<>(schedule3DayOfWeek,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_DAYS.formatted(3));
                break;
            default:
                throw new IllegalArgumentException("Invalid schedule timer: " + scheduleType.toString());
        }

        if (configValue.paramValue().isEmpty()) {
            return ArgoClimaConfigProvider.getScheduleDefaults(scheduleType).weekdays();
        }
        return canonizeWeekdaysAfterDeserialization(configValue.paramValue(), configValue.paramName());
    }

    @Override
    public LocalTime getScheduleOnTime(ScheduleTimerType scheduleType) throws ArgoConfigurationException {
        ConfigParam<String> configValue;
        switch (scheduleType) {
            case SCHEDULE_1:
                configValue = new ConfigParam<>(schedule1OnTime,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_ON_TIME.formatted(1));
                break;
            case SCHEDULE_2:
                configValue = new ConfigParam<>(schedule2OnTime,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_ON_TIME.formatted(2));
                break;
            case SCHEDULE_3:
                configValue = new ConfigParam<>(schedule3OnTime,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_ON_TIME.formatted(3));
                break;
            default:
                throw new IllegalArgumentException("Invalid schedule timer: " + scheduleType.toString());
        }

        try {
            return LocalTime.parse(configValue.paramValue(), SCHEDULE_ON_OFF_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw ArgoConfigurationException.forInvalidParamValue(configValue.paramName(), configValue.paramValue(),
                    i18nProvider, e);
        }
    }

    @Override
    public LocalTime getScheduleOffTime(ScheduleTimerType scheduleType) throws ArgoConfigurationException {
        ConfigParam<String> configValue;
        switch (scheduleType) {
            case SCHEDULE_1:
                configValue = new ConfigParam<>(schedule1OffTime,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_OFF_TIME.formatted(1));
                break;
            case SCHEDULE_2:
                configValue = new ConfigParam<>(schedule2OffTime,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_OFF_TIME.formatted(2));
                break;
            case SCHEDULE_3:
                configValue = new ConfigParam<>(schedule3OffTime,
                        ArgoClimaBindingConstants.PARAMETER_SCHEDULE_X_OFF_TIME.formatted(3));
                break;
            default:
                throw new IllegalArgumentException("Invalid schedule timer: " + scheduleType.toString());
        }

        try {
            return LocalTime.parse(configValue.paramValue(), SCHEDULE_ON_OFF_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw ArgoConfigurationException.forInvalidParamValue(configValue.paramName(), configValue.paramValue(),
                    i18nProvider, e);
        }
    }

    /////////////////////
    // Helper functions
    /////////////////////

    /**
     * Utility function for logging only. Gets a parsed value from the supplier function or, exceptionally the raw
     * value. Swallows exceptions.
     *
     * @param <T> Actual type of variable returned by the supplier (parsed)
     * @param fn Parser function
     * @return String param value (if parsed correctly), or the default value post-fixed with {@code [raw]} - on parse
     *         failure.
     */
    protected static <@NonNull T> String getOrDefault(ConfigValueSupplier<T> fn) {
        try {
            return fn.get().toString();
        } catch (ArgoConfigurationException e) {
            return e.rawValue + "[raw]";
        }
    }

    @Override
    public final String toString() {
        return String.format("Config: { %s, deviceCpuId=%s, refreshInterval=%d, oemServerPort=%d, oemServerAddress=%s,"
                + "schedule1DayOfWeek=%s, schedule1OnTime=%s, schedule1OffTime=%s, schedule2DayOfWeek=%s, schedule2OnTime=%s, schedule2OffTime=%s, schedule3DayOfWeek=%s, schedule3OnTime=%s, schedule3OffTime=%s, resetToFactoryDefaults=%s}",
                getExtraFieldDescription(), deviceCpuId, refreshInterval, oemServerPort,
                getOrDefault(this::getOemServerAddress),
                getOrDefault(() -> getScheduleDayOfWeek(ScheduleTimerType.SCHEDULE_1)),
                getOrDefault(() -> getScheduleOnTime(ScheduleTimerType.SCHEDULE_1)),
                getOrDefault(() -> getScheduleOffTime(ScheduleTimerType.SCHEDULE_1)),
                getOrDefault(() -> getScheduleDayOfWeek(ScheduleTimerType.SCHEDULE_2)),
                getOrDefault(() -> getScheduleOnTime(ScheduleTimerType.SCHEDULE_2)),
                getOrDefault(() -> getScheduleOffTime(ScheduleTimerType.SCHEDULE_2)),
                getOrDefault(() -> getScheduleDayOfWeek(ScheduleTimerType.SCHEDULE_3)),
                getOrDefault(() -> getScheduleOnTime(ScheduleTimerType.SCHEDULE_3)),
                getOrDefault(() -> getScheduleOffTime(ScheduleTimerType.SCHEDULE_3)), resetToFactoryDefaults);
    }

    /**
     * Return derived class'es extra configuration parameters (for a common {@link toString} implementation)
     *
     * @return Comma-separated list of configuration parameter=value pairs or empty String if derived class does not
     *         introduce any.
     */
    protected abstract String getExtraFieldDescription();

    /**
     * Validate derived configuration
     *
     * @throws ArgoConfigurationException - on validation failure
     */
    protected abstract void validateInternal() throws ArgoConfigurationException;

    /**
     * Validate current config
     *
     * @return Error message if config is invalid. Empty string - otherwise
     */
    public final String validate() {
        try {
            if (refreshInterval < 0) {
                throw ArgoConfigurationException.forParamBelowMin(ArgoClimaBindingConstants.PARAMETER_REFRESH_INTERNAL,
                        oemServerPort, i18nProvider, 0);
            }

            if (oemServerPort < 0 || oemServerPort > 65535) {
                throw ArgoConfigurationException.forParamOutOfRange(ArgoClimaBindingConstants.PARAMETER_OEM_SERVER_PORT,
                        oemServerPort, i18nProvider, 0, 65535);
            }

            // want the side-effect of these calls
            getOemServerAddress();

            getScheduleDayOfWeek(ScheduleTimerType.SCHEDULE_1);
            getScheduleOnTime(ScheduleTimerType.SCHEDULE_1);
            getScheduleOffTime(ScheduleTimerType.SCHEDULE_1);

            getScheduleDayOfWeek(ScheduleTimerType.SCHEDULE_2);
            getScheduleOnTime(ScheduleTimerType.SCHEDULE_2);
            getScheduleOffTime(ScheduleTimerType.SCHEDULE_2);

            getScheduleDayOfWeek(ScheduleTimerType.SCHEDULE_3);
            getScheduleOnTime(ScheduleTimerType.SCHEDULE_3);
            getScheduleOffTime(ScheduleTimerType.SCHEDULE_3);

            validateInternal();
            return "";
        } catch (Exception e) {
            var msg = e.getLocalizedMessage();
            var cause = Optional.ofNullable(e.getCause());
            return Objects.requireNonNullElse(msg, "Unknown exception, message is null").concat(
                    Objects.requireNonNull(cause.map(c -> "\n\t[" + c.getClass().getSimpleName() + "]").orElse("")));
        }
    }
}
