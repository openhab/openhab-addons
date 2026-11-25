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
package org.openhab.binding.tibber.internal.pulse.handler;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.BINDING_ID;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibber.internal.pulse.config.BridgeConfiguration;
import org.openhab.binding.tibber.internal.pulse.discovery.MeterDiscoveryService;
import org.openhab.binding.tibber.internal.pulse.sml.SmlDecoder;
import org.openhab.binding.tibber.internal.pulse.sml.SmlReading;
import org.openhab.binding.tibber.internal.pulse.stream.Envelope;
import org.openhab.binding.tibber.internal.pulse.stream.StreamClient;
import org.openhab.binding.tibber.internal.pulse.stream.StreamFailure;
import org.openhab.binding.tibber.internal.pulse.stream.StreamListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the one stream to a Tibber bridge, maps what happens on it to the status of the {@code bridge} thing, and hands
 * each reading to the {@link MeterHandler} of the Pulse device that sent it.
 *
 * The stream client runs its own reader thread (see {@link StreamClient}); the listener callbacks run on that thread
 * and decode and apply each message without a hand-off, which keeps the readings in order and the latency low. The
 * handler is a single-use consumer of clients: every {@link #initialize()} creates a new client, and
 * {@link #dispose()} stops it and waits for its thread, so a restarted handler never has two readers of its own.
 * The binding does not prevent two {@code bridge} things for the same bridge; the bridge serves one stream client at a
 * time, so they would take the stream from each other.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class BridgeHandler extends BaseBridgeHandler {

    /** The two operations of a stream client that this handler uses; public so that tests can replace it. */
    public interface StreamConnection {
        void start();

        void stop();
    }

    /** Creates a stream connection; the production implementation creates a {@link StreamClient}. */
    @FunctionalInterface
    public interface StreamConnectionFactory {
        StreamConnection create(String host, String password, String threadName, StreamListener listener);
    }

    /** Told about each device id that sends valid readings and for which no meter thing exists. */
    @FunctionalInterface
    public interface DeviceSeenListener {
        void deviceSeen(String deviceId);
    }

    /** Bound on the device ids remembered for discovery, so that the set cannot grow without limit. */
    static final int MAX_SEEN_DEVICES = 32;

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);

    private final StreamConnectionFactory connectionFactory;
    private final Set<String> seenDevices = ConcurrentHashMap.newKeySet();
    private final Set<DeviceSeenListener> deviceSeenListeners = new CopyOnWriteArraySet<>();

    // Written by initialize and dispose, which the framework never runs concurrently.
    private @Nullable StreamConnection connection;
    /** The listener of the current stream; callbacks of an older one are ignored. */
    private volatile @Nullable Session session;

    public BridgeHandler(Bridge bridge) {
        this(bridge, BridgeHandler::openClient);
    }

    /** For tests: the factory decides which stream client, or double, serves the bridge. */
    public BridgeHandler(Bridge bridge, StreamConnectionFactory connectionFactory) {
        super(bridge);
        this.connectionFactory = connectionFactory;
    }

    private static StreamConnection openClient(String host, String password, String threadName,
            StreamListener listener) {
        StreamClient client = new StreamClient(host, password, threadName, listener);
        return new StreamConnection() {
            @Override
            public void start() {
                client.start();
            }

            @Override
            public void stop() {
                client.stop();
            }
        };
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MeterDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The bridge has no channels.
    }

    @Override
    public void initialize() {
        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        String error = config.validate();
        if (error != null) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, error);
            return;
        }
        String host = config.normalizedHostname();
        boolean running = false;
        try {
            updateStatus(ThingStatus.UNKNOWN);
            Session newSession = new Session();
            session = newSession;
            StreamConnection newConnection = connectionFactory.create(host, config.password,
                    "OH-binding-" + BINDING_ID + "-" + getThing().getUID(), newSession);
            connection = newConnection;
            // Returns at once; the first attempt runs on the reader thread.
            newConnection.start();
            running = true;
        } finally {
            if (!running) {
                // Do not rely on dispose() after a failed initialize(): stop what was created at once. Calling it
                // twice is safe.
                releaseStream();
            }
        }
    }

    @Override
    public void dispose() {
        releaseStream();
        super.dispose();
    }

    /**
     * Stops the stream. Closes the socket and waits a bounded time for the reader thread. The session is cleared first,
     * so a callback that is still running after that wait publishes nothing.
     */
    private void releaseStream() {
        session = null;
        StreamConnection old = connection;
        connection = null;
        if (old != null) {
            old.stop();
        }
        seenDevices.clear();
    }

    /**
     * Registers a listener for device ids seen on the stream and tells it at once about those seen before.
     * Registrations belong to the discovery service, not to a stream, so they survive a restart of the stream.
     */
    public void addDeviceSeenListener(DeviceSeenListener listener) {
        deviceSeenListeners.add(listener);
        for (String deviceId : getSeenDeviceIds()) {
            scheduler.execute(() -> notifyIfRegistered(listener, deviceId));
        }
    }

    public void removeDeviceSeenListener(DeviceSeenListener listener) {
        deviceSeenListeners.remove(listener);
    }

    /**
     * @return the device ids that sent valid readings and have no running meter thing, in a stable order
     */
    public Collection<String> getSeenDeviceIds() {
        Set<String> unclaimed = new TreeSet<>();
        for (String deviceId : seenDevices) {
            if (findMeters(deviceId).isEmpty()) {
                unclaimed.add(deviceId);
            }
        }
        return unclaimed;
    }

    /**
     * Pre-filter only: a meter may be reconfigured after it was found, so {@link MeterHandler#onReading} decides.
     *
     * @return the handlers of all meter things of this bridge that are configured for the device; two things may share
     *         a device id, and each must receive the readings
     */
    private List<MeterHandler> findMeters(String deviceId) {
        List<MeterHandler> found = new ArrayList<>();
        for (Thing child : getThing().getThings()) {
            ThingHandler handler = child.getHandler();
            if (handler instanceof MeterHandler meter && meter.matches(deviceId)) {
                found.add(meter);
            }
        }
        return found;
    }

    private void noteUnknownDevice(String deviceId, Session current) {
        if (seenDevices.size() >= MAX_SEEN_DEVICES || !seenDevices.add(deviceId)) {
            return;
        }
        // The inbox is notified from the scheduler: it can take a while and must not slow down the stream.
        for (DeviceSeenListener listener : deviceSeenListeners) {
            scheduler.execute(() -> {
                if (current.isCurrent()) {
                    notifyIfRegistered(listener, deviceId);
                }
            });
        }
    }

    /**
     * A call queued before the listener was removed is dropped: removal ends background discovery, and the framework
     * would still put the result into the inbox.
     */
    private void notifyIfRegistered(DeviceSeenListener listener, String deviceId) {
        if (deviceSeenListeners.contains(listener)) {
            listener.deviceSeen(deviceId);
        }
    }

    /** Maps what the stream client reports to the bridge status; runs on the reader thread. */
    private final class Session implements StreamListener {

        /** False once the handler was disposed or started a newer stream. */
        boolean isCurrent() {
            return equals(session);
        }

        @Override
        public void onStreamActive() {
            if (!isCurrent()) {
                return;
            }
            updateStatus(ThingStatus.ONLINE);
        }

        @Override
        public boolean onMessage(byte[] message) {
            if (!isCurrent()) {
                return false;
            }
            try {
                return process(message);
            } catch (RuntimeException e) {
                // A defect, not bad input. The exception text is left out: it could carry meter data.
                StackTraceElement[] trace = e.getStackTrace();
                logger.warn("Unexpected error while processing a message: {}{}", e.getClass().getName(),
                        trace.length > 0 ? " at " + trace[0] : "");
                return false;
            }
        }

        private boolean process(byte[] message) {
            Envelope envelope = Envelope.parse(message);
            if (envelope == null) {
                logger.debug("Ignored a message of {} bytes: no valid header", message.length);
                return false;
            }
            if (!envelope.isSml()) {
                logger.debug("Ignored a message of {} bytes: not an SML message", message.length);
                return false;
            }
            List<SmlReading> readings = SmlDecoder.decode(message, envelope.payloadOffset());
            if (readings.isEmpty()) {
                logger.debug("Dropped a message of {} bytes: no valid reading", message.length);
                return false;
            }
            String deviceId = envelope.deviceId();
            List<MeterHandler> targets = findMeters(deviceId);
            if (targets.isEmpty()) {
                noteUnknownDevice(deviceId, this);
            } else {
                for (SmlReading reading : readings) {
                    for (MeterHandler meter : targets) {
                        meter.onReading(deviceId, reading);
                    }
                }
            }
            return true;
        }

        @Override
        public void onStreamLost(StreamFailure failure) {
            if (!isCurrent()) {
                return;
            }
            ThingStatusDetail detail = failure.isFinal() ? ThingStatusDetail.CONFIGURATION_ERROR
                    : ThingStatusDetail.COMMUNICATION_ERROR;
            String description = switch (failure.getKind()) {
                case REJECTED -> TEXT_PASSWORD_REJECTED;
                case ENDPOINT_MISSING -> TEXT_NO_STREAM;
                case HTTP_STATUS -> TEXT_HTTP_STATUS + " [\"" + failure.getHttpStatus() + "\"]";
                case UNREACHABLE -> TEXT_UNREACHABLE;
                case CLOSED_BY_BRIDGE -> TEXT_CLOSED_BY_BRIDGE;
                case SILENT -> TEXT_SILENT;
                case PROTOCOL -> TEXT_INVALID_DATA;
            };
            updateStatus(detail, description);
        }
    }
}
