/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.dto;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * General information about a device.
 *
 * <p>
 * When the operating system is unknown, it is derived from the device model.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceInfo {

    private static final List<DeviceModel> AIRPORT_MODELS = List.of(DeviceModel.AirPortExpress,
            DeviceModel.AirPortExpressGen2);
    private static final List<DeviceModel> TVOS_MODELS = List.of(DeviceModel.HomePod, DeviceModel.HomePodMini,
            DeviceModel.Gen2, DeviceModel.Gen3, DeviceModel.Gen4, DeviceModel.Gen4K, DeviceModel.AppleTV4KGen2,
            DeviceModel.AppleTV4KGen3);

    private final OperatingSystem operatingSystem;
    private final String version;
    private final String buildNumber;
    private final DeviceModel model;
    private final String rawModel;
    private final String mac;
    private final String outputDeviceId;

    private DeviceInfo(Builder builder) {
        this.operatingSystem = builder.operatingSystem;
        this.version = builder.version;
        this.buildNumber = builder.buildNumber;
        this.model = builder.model;
        this.rawModel = builder.rawModel;
        this.mac = builder.mac;
        this.outputDeviceId = builder.outputDeviceId;
    }

    /**
     * Returns a new builder for creating {@link DeviceInfo} instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns empty device info (everything unknown).
     *
     * @return empty device info
     */
    public static DeviceInfo empty() {
        return builder().build();
    }

    /**
     * Returns the operating system running on the device. If unknown, it is derived from the device model when
     * possible.
     *
     * @return operating system (never {@code null})
     */
    public OperatingSystem operatingSystem() {
        if (operatingSystem != OperatingSystem.Unknown) {
            return operatingSystem;
        }
        if (AIRPORT_MODELS.contains(model)) {
            return OperatingSystem.AirPortOS;
        }
        if (TVOS_MODELS.contains(model)) {
            return OperatingSystem.TvOS;
        }
        return OperatingSystem.Unknown;
    }

    /**
     * Returns the operating system version. Falls back to a version derived from the build number when possible (see
     * {@link #lookupVersion(String)}).
     *
     * @return version if available
     */
    public Optional<String> version() {
        String localVersion = version;
        if (localVersion != null && !localVersion.isEmpty()) {
            return Optional.of(localVersion);
        }
        String localBuildNumber = buildNumber;
        if (localBuildNumber == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(lookupVersion(localBuildNumber));
    }

    /**
     * Looks up an operating system version from a build number, delegating to the scan package's lookup table.
     * Subclasses may override this.
     *
     * @param buildNumber build number to look up
     * @return version or {@code null} if unknown
     */
    protected String lookupVersion(String buildNumber) {
        return org.openhab.binding.atv.internal.client.scan.DeviceInfoLookup.lookupVersion(buildNumber);
    }

    /**
     * Returns the operating system build number, e.g. 17K795.
     *
     * @return build number if available
     */
    public Optional<String> buildNumber() {
        return Optional.ofNullable(buildNumber);
    }

    /**
     * Returns the hardware model, e.g. Gen4 or Gen4K.
     *
     * @return device model (never {@code null})
     */
    public DeviceModel model() {
        return model;
    }

    /**
     * Returns the raw model description. If {@link #model()} returns {@link DeviceModel#Unknown}, this contains the
     * raw model string (if any is available).
     *
     * @return raw model if available
     */
    public Optional<String> rawModel() {
        return Optional.ofNullable(rawModel);
    }

    /**
     * Returns the model name as string, falling back to the raw model if the model is unknown.
     *
     * @return model name
     */
    public String modelStr() {
        String localRawModel = rawModel;
        if (model == DeviceModel.Unknown && localRawModel != null) {
            return localRawModel;
        }
        return model.toString();
    }

    /**
     * Returns the device MAC address.
     *
     * @return MAC address if available
     */
    public Optional<String> mac() {
        return Optional.ofNullable(mac);
    }

    /**
     * Returns the output device (AirPlay) identifier.
     *
     * @return output device identifier if available
     */
    public Optional<String> outputDeviceId() {
        return Optional.ofNullable(outputDeviceId);
    }

    @Override
    public String toString() {
        String osName = switch (operatingSystem()) {
            case Legacy -> "ATV SW";
            case TvOS -> "tvOS";
            case AirPortOS -> "AirPortOS";
            case MacOS -> "MacOS";
            default -> "Unknown OS";
        };
        StringBuilder output = new StringBuilder(modelStr()).append(", ").append(osName);
        version().ifPresent(v -> output.append(' ').append(v));
        buildNumber().ifPresent(b -> output.append(" build ").append(b));
        return output.toString();
    }

    /**
     * Builder for {@link DeviceInfo} instances.
     */
    public static final class Builder {

        private OperatingSystem operatingSystem = OperatingSystem.Unknown;
        private String version;
        private String buildNumber;
        private DeviceModel model = DeviceModel.Unknown;
        private String rawModel;
        private String mac;
        private String outputDeviceId;

        private Builder() {
        }

        /**
         * Sets the operating system (defaults to {@link OperatingSystem#Unknown}).
         *
         * @param operatingSystem operating system
         * @return this builder
         */
        public Builder operatingSystem(OperatingSystem operatingSystem) {
            this.operatingSystem = Objects.requireNonNull(operatingSystem);
            return this;
        }

        /**
         * Sets the operating system version.
         *
         * @param version version or {@code null}
         * @return this builder
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the operating system build number.
         *
         * @param buildNumber build number or {@code null}
         * @return this builder
         */
        public Builder buildNumber(String buildNumber) {
            this.buildNumber = buildNumber;
            return this;
        }

        /**
         * Sets the device model (defaults to {@link DeviceModel#Unknown}).
         *
         * @param model device model
         * @return this builder
         */
        public Builder model(DeviceModel model) {
            this.model = Objects.requireNonNull(model);
            return this;
        }

        /**
         * Sets the raw model string.
         *
         * @param rawModel raw model or {@code null}
         * @return this builder
         */
        public Builder rawModel(String rawModel) {
            this.rawModel = rawModel;
            return this;
        }

        /**
         * Sets the device MAC address.
         *
         * @param mac MAC address or {@code null}
         * @return this builder
         */
        public Builder mac(String mac) {
            this.mac = mac;
            return this;
        }

        /**
         * Sets the output device (AirPlay) identifier.
         *
         * @param outputDeviceId output device identifier or {@code null}
         * @return this builder
         */
        public Builder outputDeviceId(String outputDeviceId) {
            this.outputDeviceId = outputDeviceId;
            return this;
        }

        /**
         * Builds the {@link DeviceInfo} instance.
         *
         * @return new instance
         */
        public DeviceInfo build() {
            return new DeviceInfo(this);
        }
    }
}
