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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.AppleTV;
import org.openhab.binding.atv.internal.client.DeviceListener;
import org.openhab.binding.atv.internal.client.capability.Apps;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.capability.Keyboard;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.capability.TouchGestures;
import org.openhab.binding.atv.internal.client.capability.UserAccounts;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.dto.DeviceInfo;
import org.openhab.binding.atv.internal.client.dto.DeviceModel;
import org.openhab.binding.atv.internal.client.dto.OperatingSystem;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.exceptions.NoServiceError;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.settings.Settings;
import org.openhab.binding.atv.internal.client.support.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Relay implementation of the external {@link AppleTV} interface.
 *
 * <p>
 * The relay forwards calls on the public interface to the appropriate protocol instance based on priority,
 * supporting partial implementations of the interface per protocol (see {@link Relayer}).
 *
 * <p>
 * Protocols are contributed as {@link SetupData} via {@link #addProtocol(SetupData)} <em>before</em>
 * {@link #connect()}; connecting drains the queue (ignoring duplicate protocols), awaits each protocol's connect,
 * registers the contributed interface instances into the per-interface relays, merges device information and
 * feature declarations. {@link #close()} is idempotent: it stops all push updaters, runs each protocol's close,
 * blocks the shared {@link Guard} (all public interface methods then throw
 * {@link org.openhab.binding.atv.internal.client.exceptions.BlockedStateError}) and shuts down the device loop.
 *
 * <p>
 * The relay is also the {@link DeviceListener} target for the protocols: a connection-lost/closed report from
 * any protocol is forwarded to the external listeners exactly once and triggers a full close.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AppleTVRelay implements AppleTV, DeviceListener {

    /**
     * Default protocol priority order used by all relays.
     */
    public static final List<Protocol> DEFAULT_PRIORITIES = List.of(Protocol.MRP, Protocol.Companion, Protocol.AirPlay,
            Protocol.RAOP);

    // Device information keys shared by the protocol modules' deviceInfo() maps
    private static final String KEY_OPERATING_SYSTEM = "os";
    private static final String KEY_VERSION = "version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_MODEL = "model";
    private static final String KEY_RAW_MODEL = "raw_model";
    private static final String KEY_MAC = "mac";
    private static final String KEY_OUTPUT_DEVICE_ID = "output_device_id";

    private static final Logger LOGGER = LoggerFactory.getLogger(AppleTVRelay.class);

    private final AtvConfig config;
    private final Settings settings;
    private final DeviceLoop loop;
    private final Guard guard = new Guard();
    private final ListenerRegistry<DeviceListener> deviceListeners;

    private final Deque<SetupData> protocolsToSetup = new ArrayDeque<>();
    private final Map<Protocol, SetupData> protocolHandlers = new LinkedHashMap<>();

    private final PushUpdaterRelay pushUpdates;
    private final FeaturesRelay features;
    private final PowerRelay power;
    private final Map<Class<?>, BaseRelay<?>> interfaces = new LinkedHashMap<>();

    private volatile DeviceInfo deviceInfo = DeviceInfo.empty();
    private @Nullable CompletableFuture<Void> closeFuture;

    /**
     * Creates a new relay Apple TV.
     *
     * @param config device configuration
     * @param settings device settings used by the library
     * @param coreDispatcher per-device state dispatcher shared by all protocols
     * @param loop device loop shared by all protocols; shut down when the relay is closed
     */
    public AppleTVRelay(AtvConfig config, Settings settings, CoreStateDispatcher coreDispatcher, DeviceLoop loop) {
        this.config = Objects.requireNonNull(config, "config");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.loop = Objects.requireNonNull(loop, "loop");
        this.deviceListeners = new ListenerRegistry<>(loop, 1);
        this.pushUpdates = new PushUpdaterRelay(guard, loop);
        this.features = new FeaturesRelay(guard, pushUpdates);
        this.power = new PowerRelay(guard, coreDispatcher, loop);
        interfaces.put(Features.class, features);
        interfaces.put(RemoteControl.class, new RemoteControlRelay(guard));
        interfaces.put(Metadata.class, new MetadataRelay(guard));
        interfaces.put(Power.class, power);
        interfaces.put(PushUpdater.class, pushUpdates);
        interfaces.put(Stream.class, new StreamRelay(guard, features));
        interfaces.put(Apps.class, new AppsRelay(guard));
        interfaces.put(UserAccounts.class, new UserAccountsRelay(guard));
        interfaces.put(Audio.class, new AudioRelay(guard, coreDispatcher, loop));
        interfaces.put(Keyboard.class, new KeyboardRelay(guard, coreDispatcher, loop));
        interfaces.put(TouchGestures.class, new TouchGesturesRelay(guard));
    }

    /**
     * Adds a new protocol to the relay. Connecting commits the current configuration, thus adding new protocols
     * is not allowed anymore after {@link #connect()} was called.
     *
     * @param setupData protocol setup data
     * @throws InvalidStateError if called after connect
     */
    public synchronized void addProtocol(SetupData setupData) {
        if (!protocolHandlers.isEmpty()) {
            throw new InvalidStateError("cannot add protocol after connect was called");
        }
        LOGGER.debug("Adding handler for protocol {}", setupData.protocol());
        protocolsToSetup.add(setupData);
    }

    @Override
    public CompletableFuture<Void> connect() {
        guard.requireNotBlocked("connect");

        List<SetupData> toSetup = new ArrayList<>();
        synchronized (this) {
            // No protocols to setup + no protocols previously set up => no service
            if (protocolsToSetup.isEmpty() && protocolHandlers.isEmpty()) {
                return CompletableFuture.failedFuture(new NoServiceError("no service to connect to"));
            }
            // Protocols set up already => we have already connected
            if (!protocolHandlers.isEmpty()) {
                return CompletableFuture.failedFuture(new InvalidStateError("already connected"));
            }
            while (!protocolsToSetup.isEmpty()) {
                toSetup.add(protocolsToSetup.poll());
            }
        }

        Map<String, Object> devinfo = new LinkedHashMap<>();
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (SetupData setupData : toSetup) {
            chain = chain.thenCompose(ignore -> setUpProtocol(setupData, devinfo));
        }
        return chain.thenRun(() -> {
            deviceInfo = buildDeviceInfo(devinfo);
            wirePowerListener();
        }).exceptionallyCompose(error -> {
            // Tear down any protocols that already connected so their connections and heartbeat
            // loops do not leak when a later protocol fails to set up.
            return close().handle((ignore, closeError) -> null)
                    .thenCompose(ignore -> CompletableFuture.<Void> failedFuture(error));
        });
    }

    private CompletableFuture<Void> setUpProtocol(SetupData setupData, Map<String, Object> devinfo) {
        synchronized (this) {
            // Set up protocols, ignoring duplicates
            if (protocolHandlers.containsKey(setupData.protocol())) {
                LOGGER.debug("Protocol {} already set up, ignoring", setupData.protocol());
                return CompletableFuture.completedFuture(null);
            }
        }
        LOGGER.debug("Connecting to protocol: {}", setupData.protocol());
        return setupData.connect().get().thenAccept(connected -> {
            if (Boolean.TRUE.equals(connected)) {
                LOGGER.debug("Connected to protocol: {}", setupData.protocol());
                registerProtocol(setupData, devinfo);
            }
        });
    }

    private synchronized void registerProtocol(SetupData setupData, Map<String, Object> devinfo) {
        protocolHandlers.put(setupData.protocol(), setupData);
        for (Map.Entry<Class<?>, Object> entry : setupData.interfaces().entrySet()) {
            BaseRelay<?> relay = interfaces.get(entry.getKey());
            if (relay == null) {
                throw new IllegalArgumentException(
                        "unknown interface " + entry.getKey() + " contributed by " + setupData.protocol());
            }
            relay.registerInstance(entry.getValue(), setupData.protocol());
        }
        features.addMapping(setupData.protocol(), setupData.features());
        Collections.dictMerge(devinfo, setupData.deviceInfo().get());
    }

    private void wirePowerListener() {
        // Forward power events in case an interface exists for it
        try {
            power.wireMainInstance();
        } catch (NotSupportedError e) {
            LOGGER.debug("Power management not supported by any protocols");
        }
    }

    @Override
    public synchronized CompletableFuture<Void> close() {
        // If close was called before, return the previous (pending) result
        @Nullable
        CompletableFuture<Void> existing = closeFuture;
        if (existing != null) {
            return existing;
        }

        // Stop all push updaters, otherwise they might continue in the background
        pushUpdates.stop();

        for (SetupData setupData : protocolHandlers.values()) {
            try {
                setupData.close().run();
            } catch (RuntimeException e) {
                LOGGER.warn("Error closing protocol {}", setupData.protocol(), e);
            }
        }

        // Block access to everything in the public interface
        guard.block();

        loop.shutdown();

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        closeFuture = future;
        return future;
    }

    /**
     * Performs a takeover of one or more capability interfaces by a protocol.
     * When one of the takeovers fails because another protocol already performed a takeover, the takeovers already
     * performed by this call are rolled back before the exception is rethrown.
     *
     * @param protocol protocol taking over
     * @param interfaceClasses capability interface classes to take over (e.g. {@code Audio.class}); unknown classes
     *            are ignored
     * @return runnable releasing the takeover
     * @throws IllegalStateException if one of the interfaces was already taken over by another protocol
     */
    public Runnable takeover(Protocol protocol, Class<?>... interfaceClasses) {
        List<BaseRelay<?>> takenOver = new ArrayList<>();
        Runnable release = () -> {
            LOGGER.debug("Release {} by {}", List.of(interfaceClasses), protocol);
            for (BaseRelay<?> relay : takenOver) {
                relay.release();
            }
        };

        LOGGER.debug("Takeover {} by {}", List.of(interfaceClasses), protocol);

        for (Class<?> interfaceClass : interfaceClasses) {
            BaseRelay<?> relay = interfaces.get(interfaceClass);
            if (relay == null) {
                continue;
            }
            try {
                relay.takeover(protocol);
            } catch (IllegalStateException e) {
                release.run();
                throw e;
            }
            takenOver.add(relay);
        }

        return release;
    }

    @Override
    public Settings settings() {
        guard.requireNotBlocked("settings");
        return settings;
    }

    @Override
    public DeviceInfo deviceInfo() {
        guard.requireNotBlocked("deviceInfo");
        return deviceInfo;
    }

    @Override
    public BaseService service() {
        guard.requireNotBlocked("service");
        for (Protocol protocol : DEFAULT_PRIORITIES) {
            Optional<BaseService> service = config.getService(protocol);
            if (service.isPresent()) {
                return service.get();
            }
        }
        throw new IllegalStateException("no service (bug)");
    }

    @Override
    public RemoteControl remoteControl() {
        guard.requireNotBlocked("remoteControl");
        return Objects.requireNonNull((RemoteControl) interfaces.get(RemoteControl.class));
    }

    @Override
    public Metadata metadata() {
        guard.requireNotBlocked("metadata");
        return Objects.requireNonNull((Metadata) interfaces.get(Metadata.class));
    }

    @Override
    public PushUpdater pushUpdater() {
        guard.requireNotBlocked("pushUpdater");
        return pushUpdates;
    }

    @Override
    public Stream stream() {
        guard.requireNotBlocked("stream");
        return Objects.requireNonNull((Stream) interfaces.get(Stream.class));
    }

    @Override
    public Power power() {
        guard.requireNotBlocked("power");
        return power;
    }

    @Override
    public Features features() {
        guard.requireNotBlocked("features");
        return features;
    }

    @Override
    public Apps apps() {
        guard.requireNotBlocked("apps");
        return Objects.requireNonNull((Apps) interfaces.get(Apps.class));
    }

    @Override
    public UserAccounts userAccounts() {
        guard.requireNotBlocked("userAccounts");
        return Objects.requireNonNull((UserAccounts) interfaces.get(UserAccounts.class));
    }

    @Override
    public Audio audio() {
        guard.requireNotBlocked("audio");
        return Objects.requireNonNull((Audio) interfaces.get(Audio.class));
    }

    @Override
    public Keyboard keyboard() {
        guard.requireNotBlocked("keyboard");
        return Objects.requireNonNull((Keyboard) interfaces.get(Keyboard.class));
    }

    @Override
    public TouchGestures touch() {
        guard.requireNotBlocked("touch");
        return Objects.requireNonNull((TouchGestures) interfaces.get(TouchGestures.class));
    }

    @Override
    public void addListener(DeviceListener listener) {
        deviceListeners.add(listener);
    }

    @Override
    public void removeListener(DeviceListener listener) {
        deviceListeners.remove(listener);
    }

    /**
     * Called by a protocol when its connection was unexpectedly lost. Forwarded to the external listeners exactly
     * once (across both listener methods) and triggers a full close.
     *
     * @param exception error causing the disconnect
     */
    @Override
    public void connectionLost(Exception exception) {
        deviceListeners.fire(listener -> listener.connectionLost(exception));
        close();
    }

    /**
     * Called by a protocol when its connection was intentionally closed. Forwarded to the external listeners
     * exactly once (across both listener methods) and triggers a full close.
     */
    @Override
    public void connectionClosed() {
        deviceListeners.fire(DeviceListener::connectionClosed);
        close();
    }

    private static DeviceInfo buildDeviceInfo(Map<String, Object> devinfo) {
        DeviceInfo.Builder builder = DeviceInfo.builder();
        if (devinfo.get(KEY_OPERATING_SYSTEM) instanceof OperatingSystem os) {
            builder.operatingSystem(os);
        }
        if (devinfo.get(KEY_VERSION) instanceof String version) {
            builder.version(version);
        }
        if (devinfo.get(KEY_BUILD_NUMBER) instanceof String buildNumber) {
            builder.buildNumber(buildNumber);
        }
        if (devinfo.get(KEY_MODEL) instanceof DeviceModel model) {
            builder.model(model);
        }
        if (devinfo.get(KEY_RAW_MODEL) instanceof String rawModel) {
            builder.rawModel(rawModel);
        }
        if (devinfo.get(KEY_MAC) instanceof String mac) {
            builder.mac(mac);
        }
        if (devinfo.get(KEY_OUTPUT_DEVICE_ID) instanceof String outputDeviceId) {
            builder.outputDeviceId(outputDeviceId);
        }
        return builder.build();
    }
}
