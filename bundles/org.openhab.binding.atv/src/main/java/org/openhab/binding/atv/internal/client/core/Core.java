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
package org.openhab.binding.atv.internal.client.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.DeviceListener;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.settings.Settings;

/**
 * Instance for protocols to access core features; the unified per-protocol context handed to
 * every {@link ProtocolModule}.
 *
 * <p>
 * No connection session is shared across protocols; each protocol creates its own. RAOP's
 * test-only streaming knobs ({@code StreamTiming}, {@code Random}) stay out of this record;
 * {@code Raop.setup} has an overload accepting them directly.
 *
 * @param runtime shared runtime (scheduler and clock)
 * @param loop device loop all protocol state updates are confined to
 * @param config configuration of the device this core belongs to
 * @param service service the protocol instance operates on
 * @param settings device settings
 * @param deviceListener registry notified on connection loss/close
 * @param stateDispatcher protocol-scoped dispatcher for internal state updates
 * @param takeover takeover method bound to this protocol by the relay
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record Core(AtvRuntime runtime, DeviceLoop loop, AtvConfig config, BaseService service, Settings settings,
        ListenerRegistry<DeviceListener> deviceListener, ProtocolStateDispatcher stateDispatcher,
        TakeoverMethod takeover) {

    /**
     * Canonical constructor validating that all components are present.
     */
    public Core {
        Objects.requireNonNull(runtime, "runtime");
        Objects.requireNonNull(loop, "loop");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(deviceListener, "deviceListener");
        Objects.requireNonNull(stateDispatcher, "stateDispatcher");
        Objects.requireNonNull(takeover, "takeover");
    }

    /**
     * Returns the device address as a string.
     */
    public String address() {
        return config.address().getHostAddress();
    }

    /**
     * Returns the main identifier of the device configuration, or {@code null} if none is set.
     */
    public @Nullable String deviceIdentifier() {
        return config.identifier().orElse(null);
    }

    /**
     * Returns a single-callback view of {@link #deviceListener()} for components that take a
     * plain {@link DeviceListener}.
     */
    public DeviceListener deviceListenerProxy() {
        ListenerRegistry<DeviceListener> registry = deviceListener;
        return new DeviceListener() {

            @Override
            public void connectionLost(Exception exception) {
                registry.fire(listener -> listener.connectionLost(exception));
            }

            @Override
            public void connectionClosed() {
                registry.fire(DeviceListener::connectionClosed);
            }
        };
    }

    /**
     * Returns a copy of this core mapped to another service, stamping dispatched state with
     * that service's protocol. Used when one protocol sets up another (e.g. AirPlay's MRP
     * tunnel and synthetic RAOP service).
     *
     * @param service service for the copy
     * @return new core sharing every other component
     */
    public Core withService(BaseService service) {
        return new Core(runtime, loop, config, service, settings, deviceListener,
                stateDispatcher.createCopy(service.protocol()), takeover);
    }

    /**
     * Creates a builder; unset components get functional defaults.
     *
     * @param config device configuration
     * @param service service the protocol operates on
     * @return builder
     */
    public static Builder builder(AtvConfig config, BaseService service) {
        return new Builder(config, service);
    }

    /**
     * Creates a builder around a single-service configuration for the given address
     * (convenience for tests and tools that only have a service at hand).
     *
     * @param address device IP address literal
     * @param service service the protocol operates on
     * @return builder
     */
    public static Builder builder(String address, BaseService service) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("invalid address: " + address, e);
        }
        AtvConfig config = new AtvConfig(inetAddress, "Apple TV");
        config.addService(service);
        return new Builder(config, service);
    }

    /**
     * Builder applying the {@code create_core} defaults: default runtime, a fresh device
     * loop, default settings, an empty device-listener registry, a protocol-stamped state
     * dispatcher over a fresh {@link CoreStateDispatcher} and a no-op takeover.
     */
    public static final class Builder {

        private final AtvConfig config;
        private final BaseService service;
        private @Nullable AtvRuntime runtime;
        private @Nullable DeviceLoop loop;
        private @Nullable Settings settings;
        private @Nullable ListenerRegistry<DeviceListener> deviceListener;
        private @Nullable ProtocolStateDispatcher stateDispatcher;
        private @Nullable TakeoverMethod takeover;

        private Builder(AtvConfig config, BaseService service) {
            this.config = Objects.requireNonNull(config, "config");
            this.service = Objects.requireNonNull(service, "service");
        }

        /**
         * Sets the runtime; {@code null} keeps the default.
         *
         * @param runtime runtime to use
         * @return this builder
         */
        public Builder runtime(AtvRuntime runtime) {
            this.runtime = runtime;
            return this;
        }

        /**
         * Sets the device loop; {@code null} keeps the default.
         *
         * @param loop loop to use
         * @return this builder
         */
        public Builder loop(DeviceLoop loop) {
            this.loop = loop;
            return this;
        }

        /**
         * Sets the settings; {@code null} keeps the default.
         *
         * @param settings settings to use
         * @return this builder
         */
        public Builder settings(Settings settings) {
            this.settings = settings;
            return this;
        }

        /**
         * Sets the device-listener registry; {@code null} keeps the default.
         *
         * @param deviceListener registry to use
         * @return this builder
         */
        public Builder deviceListener(ListenerRegistry<DeviceListener> deviceListener) {
            this.deviceListener = deviceListener;
            return this;
        }

        /**
         * Sets the protocol-scoped state dispatcher; {@code null} keeps the default.
         *
         * @param stateDispatcher dispatcher to use
         * @return this builder
         */
        public Builder stateDispatcher(ProtocolStateDispatcher stateDispatcher) {
            this.stateDispatcher = stateDispatcher;
            return this;
        }

        /**
         * Sets the takeover method; {@code null} keeps the default.
         *
         * @param takeover takeover method to use
         * @return this builder
         */
        public Builder takeover(TakeoverMethod takeover) {
            this.takeover = takeover;
            return this;
        }

        /**
         * Builds the core, filling unset components with defaults.
         *
         * @return new core
         */
        public Core build() {
            @Nullable
            AtvRuntime localRuntime = runtime;
            AtvRuntime actualRuntime = localRuntime != null ? localRuntime : AtvRuntime.defaultRuntime();
            @Nullable
            DeviceLoop localLoop = loop;
            DeviceLoop actualLoop = localLoop != null ? localLoop : actualRuntime.newDeviceLoop();
            @Nullable
            Settings localSettings = settings;
            @Nullable
            ListenerRegistry<DeviceListener> localDeviceListener = deviceListener;
            @Nullable
            ProtocolStateDispatcher localStateDispatcher = stateDispatcher;
            @Nullable
            TakeoverMethod localTakeover = takeover;
            return new Core(actualRuntime, actualLoop, config, service,
                    localSettings != null ? localSettings : Settings.ofDefaults(),
                    localDeviceListener != null ? localDeviceListener : new ListenerRegistry<>(actualLoop),
                    localStateDispatcher != null ? localStateDispatcher
                            : new ProtocolStateDispatcher(service.protocol(), new CoreStateDispatcher(actualLoop)),
                    localTakeover != null ? localTakeover : TakeoverMethod.NO_OP);
        }
    }
}
