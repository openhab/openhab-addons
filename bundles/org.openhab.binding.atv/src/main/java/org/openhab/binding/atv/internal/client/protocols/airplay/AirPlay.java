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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.auth.AuthenticationType;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.capability.Features;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.FileHostService;
import org.openhab.binding.atv.internal.client.core.HostedFile;
import org.openhab.binding.atv.internal.client.core.ProtocolModule;
import org.openhab.binding.atv.internal.client.core.SetupData;
import org.openhab.binding.atv.internal.client.dto.DeviceModel;
import org.openhab.binding.atv.internal.client.dto.FeatureInfo;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.FeatureState;
import org.openhab.binding.atv.internal.client.dto.OperatingSystem;
import org.openhab.binding.atv.internal.client.dto.PairingRequirement;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.exceptions.HttpError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidCredentialsError;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.protocols.mrp.Mrp;
import org.openhab.binding.atv.internal.client.protocols.raop.Raop;
import org.openhab.binding.atv.internal.client.settings.AirPlaySettings.MrpTunnel;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module entry point for the AirPlay protocol.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlay {

    /** Zeroconf service type announced by AirPlay devices. */
    public static final String AIRPLAY_SERVICE = "_airplay._tcp.local";

    /** Device-info map key: operating system ({@code OperatingSystem}). */
    public static final String KEY_OPERATING_SYSTEM = Mrp.KEY_OPERATING_SYSTEM;
    /** Device-info map key: operating system version ({@code String}). */
    public static final String KEY_VERSION = Mrp.KEY_VERSION;
    /** Device-info map key: device model ({@code DeviceModel}). */
    public static final String KEY_MODEL = Mrp.KEY_MODEL;
    /** Device-info map key: raw device model string ({@code String}). */
    public static final String KEY_RAW_MODEL = Mrp.KEY_RAW_MODEL;
    /** Device-info map key: MAC address ({@code String}). */
    public static final String KEY_MAC = Mrp.KEY_MAC;
    /** Device-info map key: output device identifier ({@code String}). */
    public static final String KEY_OUTPUT_DEVICE_ID = "output_device_id";

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlay.class);

    /** Model identifier patterns for macOS. */
    private static final Pattern[] OS_IDENTIFIER_FORMATS = { Pattern.compile("MacBookAir\\d+,\\d+"),
            Pattern.compile("iMac\\d+,\\d+"), Pattern.compile("Macmini\\d+,\\d+"),
            Pattern.compile("MacBookPro\\d+,\\d+"), Pattern.compile("Mac\\d+,\\d+"),
            Pattern.compile("MacPro\\d+,\\d+") };

    /**
     * Unified module singleton implementing the {@link ProtocolModule} contract for the
     * Wave-6 relay. The {@code "name"} pairing option selects our display name during
     * HAP pairing.
     */
    public static final ProtocolModule MODULE = new ProtocolModule() {

        @Override
        public Protocol protocol() {
            return Protocol.AirPlay;
        }

        @Override
        public Set<String> scanServiceTypes() {
            return Set.of(AIRPLAY_SERVICE);
        }

        @Override
        public Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties) {
            return AirPlay.deviceInfo(serviceType, properties);
        }

        @Override
        public void serviceInfo(Service service) {
            AirPlayUtils.updateServiceDetails(service);
        }

        @Override
        public Set<SetupData> setup(Core core) {
            return AirPlay.setup(core);
        }

        @Override
        public PairingHandler pair(Core core, Map<String, Object> options) {
            return AirPlay.pair(core, options);
        }
    };

    private AirPlay() {
    }

    /** Implementation of supported feature functionality. */
    public static final class AirPlayFeatures implements Features {

        private final EnumSet<AirPlayFlags> features;

        /**
         * Creates the feature interface from parsed AirPlay feature flags.
         *
         * @param features parsed {@code features}/{@code ft} property
         */
        public AirPlayFeatures(EnumSet<AirPlayFlags> features) {
            this.features = features;
        }

        @Override
        public FeatureInfo getFeature(FeatureName featureName) {
            if (featureName == FeatureName.PlayUrl && (features.contains(AirPlayFlags.SupportsAirPlayVideoV1)
                    || features.contains(AirPlayFlags.SupportsAirPlayVideoV2))) {
                return new FeatureInfo(FeatureState.Available);
            }
            if (featureName == FeatureName.Stop) {
                return new FeatureInfo(FeatureState.Available);
            }
            return new FeatureInfo(FeatureState.Unavailable);
        }
    }

    /** Implementation of the stream API with AirPlay. */
    public static final class AirPlayStream implements Stream, CapabilitySource {

        private final Core core;

        private volatile @Nullable HttpConnection connection;
        private volatile @Nullable CompletableFuture<Void> playTask;

        AirPlayStream(Core core) {
            this.core = core;
        }

        /**
         * Closes and frees resources.
         */
        @Override
        public void close() {
            HttpConnection current = connection;
            if (current != null) {
                current.close();
                connection = null;
            }
            CompletableFuture<Void> task = playTask;
            if (task != null) {
                LOGGER.debug("Stopping AirPlay play task");
                task.cancel(true);
                playTask = null;
            }
        }

        /**
         * Stops current playback by closing the play connection.
         */
        public void stop() {
            HttpConnection current = connection;
            if (current != null) {
                current.close();
            }
        }

        /**
         * Plays media from an URL on the device. The future does not complete until the
         * media has finished playing: the Apple TV requires the request to stay open
         * during the entire play duration.
         */
        @Override
        public CompletableFuture<Void> playUrl(String url, Map<String, Object> options) {
            return CompletableFuture.runAsync(() -> playUrlBlocking(url, options),
                    runnable -> Thread.ofVirtual().name("airplay-play-url").start(runnable));
        }

        private void playUrlBlocking(String url, Map<String, Object> options) {
            HostedFile hostedFile = null;
            String playedUrl = url;

            if (isLocalFile(url)) {
                FileHostService fileHost = core.runtime().fileHost();
                if (fileHost == null) {
                    throw new NotSupportedError("play_url of a local file requires a file host service");
                }
                LOGGER.debug("URL {} is a local file, hosting it over HTTP", url);
                try {
                    hostedFile = fileHost.host(Path.of(url));
                } catch (IOException e) {
                    throw new UncheckedIOException("failed to host local file for play_url", e);
                }
                playedUrl = hostedFile.url();
            }

            // Take over remote control while playing so stop() etc. are relayed to AirPlay
            Runnable takeoverRelease = core.takeover().takeover(RemoteControl.class);
            try {
                // Set up a new connection and wrap it with an AirPlay stream of correct
                // protocol version
                HttpConnection newConnection = HttpConnection.connect(core.address(), core.service().port()).join();
                this.connection = newConnection;
                RtspSession rtsp = new RtspSession(newConnection);
                AirPlayStreamProtocol streamProtocol = createAirPlayProtocol(rtsp);
                AirPlayPlayer player = new AirPlayPlayer(rtsp, streamProtocol);
                double position = options.get("position") instanceof Number number ? number.longValue() : 0;
                CompletableFuture<Void> task = player.playUrl(playedUrl, position);
                this.playTask = task;
                task.join();
            } finally {
                takeoverRelease.run();
                playTask = null;
                HttpConnection current = connection;
                if (current != null) {
                    current.close();
                    connection = null;
                }
                if (hostedFile != null) {
                    hostedFile.close();
                }
            }
        }

        /** Creates the AirPlay protocol implementation based on the supported version. */
        AirPlayStreamProtocol createAirPlayProtocol(RtspSession rtsp) {
            Optional<String> storedCredentials = core.service().credentials();
            HapCredentials credentials = storedCredentials.isPresent() ? HapCredentials.parse(storedCredentials.get())
                    : HapCredentials.NO_CREDENTIALS;
            AirPlayMajorVersion version = AirPlayUtils.getProtocolVersion(core.service(),
                    core.settings().protocols().raop().protocolVersion());
            if (version == AirPlayMajorVersion.AirPlayV1) {
                return new AirPlayV1StreamProtocol(credentials, rtsp);
            }
            return new AirPlayV2StreamProtocol(credentials, rtsp, core.runtime().scheduler());
        }

        private static boolean isLocalFile(String url) {
            try {
                return Files.exists(Path.of(url));
            } catch (InvalidPathException e) {
                return false;
            }
        }

        @Override
        public Set<Capability> capabilities() {
            return Set.of(Capability.STREAM_CLOSE, Capability.STREAM_PLAY_URL);
        }
    }

    /**
     * Implementation of remote control functionality: only {@code stop}, which closes the
     * play connection.
     */
    public static final class AirPlayRemoteControl implements RemoteControl, CapabilitySource {

        private final AirPlayStream stream;

        AirPlayRemoteControl(AirPlayStream stream) {
            this.stream = stream;
        }

        @Override
        public CompletableFuture<Void> stop() {
            stream.stop();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public Set<Capability> capabilities() {
            return Set.of(Capability.RC_STOP);
        }
    }

    /**
     * Sets up a new AirPlay service: always yields the AirPlay stream {@link SetupData};
     * yields synthetic RAOP setup data when the device announces
     * {@code HasUnifiedAdvertiserInfo} but the configuration holds no RAOP service;
     * conditionally yields the MRP-tunnel {@link SetupData} depending on the
     * {@code mrp_tunnel} setting, remote control support and credential type.
     *
     * @param core core context
     * @return setup data entries for the relay
     */
    public static Set<SetupData> setup(Core core) {
        Set<SetupData> result = new LinkedHashSet<>();

        AirPlayStream stream = new AirPlayStream(core);
        EnumSet<AirPlayFlags> features = AirPlayFlags
                .parse(core.service().properties().getOrDefault("features", "0x0"));
        HapCredentials credentials = AirPlayAuth.extractCredentials(core.service());

        Map<Class<?>, Object> interfaces = Map.of(Features.class, new AirPlayFeatures(features), RemoteControl.class,
                new AirPlayRemoteControl(stream), Stream.class, stream);

        Supplier<Map<String, Object>> deviceInfoSupplier = () -> deviceInfo(AIRPLAY_SERVICE,
                core.service().properties());

        result.add(new SetupData(Protocol.AirPlay, () -> CompletableFuture.completedFuture(true), stream::close,
                deviceInfoSupplier, interfaces, Set.of(FeatureName.PlayUrl, FeatureName.Stop)));

        // AirPlay 2 does not mandate that a separate RAOP service exists for streaming
        // audio, instead the same service used by AirPlay can be used if a particular flag
        // is set (HasUnifiedAdvertiserInfo). If that flag is set and no RAOP service has
        // been found, manually add a service pointing to the AirPlay service. This just
        // simplifies the internal handling, but is not very efficient as no connections
        // are reused amongst the protocols.
        if (features.contains(AirPlayFlags.HasUnifiedAdvertiserInfo)
                && core.config().getService(Protocol.RAOP).isEmpty()) {
            LOGGER.debug("RAOP supported but no service present, adding new service");
            // Create a RAOP service to satisfy internal RAOP handling
            Service raopService = new Service(null, Protocol.RAOP, core.service().port(), core.service().properties(),
                    core.service().credentials().orElse(null), core.service().password().orElse(null), false,
                    PairingRequirement.Unsupported, true);
            core.config().addService(raopService);

            // Re-map the core context to the newly created RAOP service
            result.addAll(Raop.setup(core.withService(raopService)));
        }

        MrpTunnel mrpTunnel = core.settings().protocols().airplay().mrpTunnel();
        if (mrpTunnel == MrpTunnel.Disable) {
            LOGGER.debug("Remote control tunnel disabled by setting");
        } else if (mrpTunnel == MrpTunnel.Force) {
            LOGGER.debug("Remote control channel is supported (forced)");
            result.add(createMrpTunnelData(core, credentials));
        } else if (!AirPlayUtils.isRemoteControlSupported(core.service(), credentials)) {
            LOGGER.debug("Remote control not supported by device");
        } else if (credentials.type() != AuthenticationType.HAP && credentials.type() != AuthenticationType.Transient) {
            LOGGER.debug("{} not supported by remote control channel", credentials.type());
        } else {
            LOGGER.debug("Remote control channel is supported");
            result.add(createMrpTunnelData(core, credentials));
        }

        return result;
    }

    /** Creates the setup data for MRP tunneled over AirPlay. */
    private static SetupData createMrpTunnelData(Core core, HapCredentials credentials) {
        Ap2Session session = new Ap2Session(core.address(), core.service().port(), credentials, core.settings().info(),
                core.runtime().scheduler());

        // A protocol requires its corresponding service to function, so add a dummy one
        // to the configuration if no MRP service exists yet
        BaseService mrpService = core.config().getService(Protocol.MRP).orElse(null);
        if (mrpService == null) {
            mrpService = new Service(null, Protocol.MRP, core.service().port(), Map.of());
            core.config().addService(mrpService);
        }

        // Already have heartbeat (feedback) on the control channel
        SetupData mrpSetup = Mrp.createWithConnection(core.withService(mrpService),
                new AirPlayMrpConnection(session, core.deviceListenerProxy()), false);

        Supplier<CompletableFuture<Boolean>> connect = () -> session.connect()
                .thenCompose(v -> session.setupRemoteControl()).handle((v, error) -> {
                    if (error == null) {
                        return null;
                    }
                    Throwable directCause = error.getCause();
                    Throwable cause = error instanceof CompletionException && directCause != null ? directCause : error;
                    if (cause instanceof HttpError httpError && httpError.statusCode() == 470) {
                        throw new CompletionException(
                                new InvalidCredentialsError("invalid or missing credentials", cause));
                    }
                    throw new CompletionException(new ProtocolError("Failed to set up remote control channel", cause));
                }).thenCompose(v -> {
                    session.startKeepAlive(core.deviceListenerProxy());
                    return mrpSetup.connect().get();
                });

        Runnable close = () -> {
            mrpSetup.close().run();
            session.stop();
        };

        return new SetupData(Protocol.MRP, connect, close, mrpSetup.deviceInfo(), mrpSetup.interfaces(),
                mrpSetup.features());
    }

    /**
     * Returns device information from Zeroconf properties.
     *
     * @param serviceType Zeroconf service type
     * @param properties Zeroconf service properties
     * @return device information map keyed by the {@code KEY_*} constants
     */
    public static Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties) {
        Map<String, Object> devinfo = new LinkedHashMap<>();
        String model = properties.get("model");
        if (model != null) {
            devinfo.put(KEY_RAW_MODEL, model);
            DeviceModel deviceModel = Mrp.lookupModel(model);
            if (deviceModel != DeviceModel.Unknown) {
                devinfo.put(KEY_MODEL, deviceModel);
            }
            OperatingSystem os = lookupOs(model);
            if (os != OperatingSystem.Unknown) {
                devinfo.put(KEY_OPERATING_SYSTEM, os);
            }
        }
        String osvers = properties.get("osvers");
        if (osvers != null) {
            devinfo.put(KEY_VERSION, osvers);
        }
        String deviceid = properties.get("deviceid");
        if (deviceid != null) {
            devinfo.put(KEY_MAC, deviceid);
        }
        String psi = properties.get("psi");
        String pi = properties.get("pi");
        if (psi != null) {
            devinfo.put(KEY_OUTPUT_DEVICE_ID, psi);
        } else if (pi != null) {
            devinfo.put(KEY_OUTPUT_DEVICE_ID, pi);
        }
        return devinfo;
    }

    /**
     * Returns a pairing handler for the protocol.
     *
     * @param core core context
     * @param options pairing options; {@code "name"} selects our display name
     * @return pairing handler
     */
    public static PairingHandler pair(Core core, Map<String, Object> options) {
        String name = options.get("name") instanceof String value ? value : null;
        return new AirPlayPairingHandler(core.service(), core.address(),
                AirPlayUtils.getProtocolVersion(core.service(), core.settings().protocols().raop().protocolVersion()),
                name);
    }

    /** Looks up the operating system from a model identifier string (macOS formats only). */
    private static OperatingSystem lookupOs(String model) {
        for (Pattern pattern : OS_IDENTIFIER_FORMATS) {
            if (pattern.matcher(model).lookingAt()) {
                return OperatingSystem.MacOS;
            }
        }
        return OperatingSystem.Unknown;
    }
}
