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
package org.openhab.io.neeo.internal.discovery;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.transport.mdns.MDNSClient;
import org.openhab.io.neeo.internal.NeeoApi;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.models.NeeoSystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * An implementations of {@link BrainDiscovery} that will discovery brains from the MDNS/Zeroconf/Bonjour service
 * announcements
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class MdnsBrainDiscovery extends AbstractBrainDiscovery {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(MdnsBrainDiscovery.class);

    /** The lock that controls access to the {@link #systems} set */
    private final Lock systemsLock = new ReentrantLock();

    /** The set of {@link NeeoSystemInfo} that has been discovered */
    private final Map<NeeoSystemInfo, InetAddress> systems = new HashMap<>();

    /** The MDNS listener used. */
    private final ServiceListener mdnsListener = new ServiceListener() {

        @Override
        public void serviceAdded(@Nullable ServiceEvent event) {
            if (event != null) {
                considerService(event.getInfo());
            }
        }

        @Override
        public void serviceRemoved(@Nullable ServiceEvent event) {
            if (event != null) {
                removeService(event.getInfo());
            }
        }

        @Override
        public void serviceResolved(@Nullable ServiceEvent event) {
            if (event != null) {
                considerService(event.getInfo());
            }
        }
    };

    /** The service context */
    private final ServiceContext context;

    /** The scheduler used to schedule tasks */
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(NeeoConstants.THREAD_POOL_NAME);

    private final Gson gson = new Gson();

    /** The file we store definitions in */
    private final File file = new File(NeeoConstants.FILENAME_DISCOVEREDBRAINS);

    private final ClientBuilder clientBuilder;

    /**
     * Creates the MDNS brain discovery from the given {@link ServiceContext}
     *
     * @param context the non-null service context
     */
    public MdnsBrainDiscovery(ServiceContext context, ClientBuilder clientBuilder) {
        Objects.requireNonNull(context, "context cannot be null");
        this.context = context;
        this.clientBuilder = clientBuilder;
    }

    /**
     * Starts discovery by
     * <ol>
     * <li>Listening to future service announcements from the {@link MDNSClient}</li>
     * <li>Getting a list of all current announcements</li>
     * </ol>
     *
     */
    @Override
    public void startDiscovery() {
        logger.debug("Starting NEEO Brain MDNS Listener");
        context.getMdnsClient().addServiceListener(NeeoConstants.NEEO_MDNS_TYPE, mdnsListener);

        scheduler.execute(() -> {
            if (file.exists()) {
                try {
                    logger.debug("Reading contents of {}", file.getAbsolutePath());
                    final byte[] contents = Files.readAllBytes(file.toPath());
                    final String json = new String(contents, StandardCharsets.UTF_8);
                    final String[] ipAddresses = gson.fromJson(json, String[].class);
                    if (ipAddresses != null) {
                        logger.debug("Restoring discovery from {}: {}", file.getAbsolutePath(),
                                String.join(",", ipAddresses));
                        for (String ipAddress : ipAddresses) {
                            if (!ipAddress.isBlank()) {
                                addDiscovered(ipAddress, false);
                            }
                        }
                    }
                } catch (JsonParseException | UnsupportedOperationException e) {
                    logger.debug("JsonParseException reading {}: {}", file.toPath(), e.getMessage(), e);
                } catch (IOException e) {
                    logger.debug("IOException reading {}: {}", file.toPath(), e.getMessage(), e);
                }
            }

            for (ServiceInfo info : context.getMdnsClient().list(NeeoConstants.NEEO_MDNS_TYPE)) {
                considerService(info);
            }
        });
    }

    @Override
    public void addListener(DiscoveryListener listener) {
        super.addListener(listener);
        systemsLock.lock();
        try {
            for (Entry<NeeoSystemInfo, InetAddress> entry : systems.entrySet()) {
                listener.discovered(entry.getKey(), entry.getValue());
            }
        } finally {
            systemsLock.unlock();
        }
    }

    /**
     * Return the brain ID and {@link InetAddress} from the {@link ServiceInfo}
     *
     * @param info the non-null {@link ServiceInfo}
     * @return an {@link Entry} that represents the brain ID and the associated IP address
     */
    @Nullable
    private Entry<String, InetAddress> getNeeoBrainInfo(ServiceInfo info) {
        Objects.requireNonNull(info, "info cannot be null");
        if (!"neeo".equals(info.getApplication())) {
            logger.debug("A non-neeo application was found for the NEEO MDNS: {}", info);
            return null;
        }

        final InetAddress ipAddress = getIpAddress(info);
        if (ipAddress == null) {
            logger.debug("Got a NEEO lookup without an IP address (scheduling a list): {}", info);
            return null;
        }

        String model = info.getPropertyString("hon"); // model
        if (model == null) {
            final String server = info.getServer(); // NEEO-xxxxx.local.
            if (server != null) {
                final int idx = server.indexOf(".");
                if (idx >= 0) {
                    model = server.substring(0, idx);
                }
            }
        }
        if (model == null || model.length() <= 5 || !model.toLowerCase().startsWith("neeo")) {
            logger.debug("No HON or server found to retrieve the model # from: {}", info);
            return null;
        }

        return new AbstractMap.SimpleImmutableEntry<>(model, ipAddress);
    }

    /**
     * Consider whether the {@link ServiceInfo} is for a NEEO brain. This method simply calls
     * {@link #considerService(ServiceInfo, int)} with the first attempt (attempts=1).
     *
     * @param info the non-null {@link ServiceInfo}
     */
    private void considerService(ServiceInfo info) {
        considerService(info, 1);
    }

    /**
     * Consider whether the {@link ServiceInfo} is for a NEEO brain. We first get the info via
     * {@link #getNeeoBrainInfo(ServiceInfo)} and then attempt to connect to it to retrieve the {@link NeeoSystemInfo}.
     * If successful and the brain has not been already discovered, a
     * {@link #fireDiscovered(NeeoSystemInfo, InetAddress)} is issued.
     *
     * @param info the non-null {@link ServiceInfo}
     * @param attempts the number of attempts that have been made
     */
    private void considerService(ServiceInfo info, int attempts) {
        Objects.requireNonNull(info, "info cannot be null");
        if (attempts < 1) {
            throw new IllegalArgumentException("attempts cannot be below 1: " + attempts);
        }

        final Entry<String, InetAddress> brainInfo = getNeeoBrainInfo(info);
        if (brainInfo == null) {
            logger.debug("BrainInfo null (ignoring): {}", info);
            return;
        }

        logger.debug("NEEO Brain Found: {} (attempt #{} to get information)", brainInfo.getKey(), attempts);

        if (attempts > 120) {
            logger.debug("NEEO Brain found but couldn't retrieve the system information for {} at {} - giving up!",
                    brainInfo.getKey(), brainInfo.getValue());
            return;
        }

        NeeoSystemInfo sysInfo;
        try {
            sysInfo = NeeoApi.getSystemInfo(brainInfo.getValue().toString(), clientBuilder);
        } catch (IOException e) {
            // We can get an MDNS notification BEFORE the brain is ready to process.
            // if that happens, we'll get an IOException (usually bad gateway message), schedule another attempt to get
            // the info (rather than lose the notification)
            scheduler.schedule(() -> {
                considerService(info, attempts + 1);
            }, 1, TimeUnit.SECONDS);
            return;
        }

        systemsLock.lock();
        try {
            final InetAddress oldAddr = systems.get(sysInfo);
            final InetAddress newAddr = brainInfo.getValue();
            if (oldAddr == null) {
                systems.put(sysInfo, newAddr);
                fireDiscovered(sysInfo, newAddr);
                save();
            } else if (!oldAddr.equals(newAddr)) {
                fireRemoved(sysInfo);
                systems.put(sysInfo, newAddr);
                fireUpdated(sysInfo, oldAddr, newAddr);
                save();
            } else {
                logger.debug("NEEO Brain {} already registered", brainInfo.getValue());
            }
        } finally {
            systemsLock.unlock();
        }
    }

    @Override
    public boolean addDiscovered(String ipAddress) {
        return addDiscovered(ipAddress, true);
    }

    /**
     * Adds a discovered IP address and optionally saving it to the brain's discovered file
     *
     * @param ipAddress a non-null, non-empty IP address
     * @param save true to save changes, false otherwise
     * @return true if discovered, false otherwise
     */
    private boolean addDiscovered(String ipAddress, boolean save) {
        NeeoUtil.requireNotEmpty(ipAddress, "ipAddress cannot be empty");

        try {
            final InetAddress addr = InetAddress.getByName(ipAddress);
            final NeeoSystemInfo sysInfo = NeeoApi.getSystemInfo(ipAddress, clientBuilder);
            logger.debug("Manually adding brain ({}) with system information: {}", ipAddress, sysInfo);

            systemsLock.lock();
            try {
                final InetAddress oldAddr = systems.get(sysInfo);

                systems.put(sysInfo, addr);

                if (oldAddr == null) {
                    fireDiscovered(sysInfo, addr);
                } else {
                    fireUpdated(sysInfo, oldAddr, addr);
                }
                if (save) {
                    save();
                }
            } finally {
                systemsLock.unlock();
            }

            return true;
        } catch (IOException e) {
            logger.debug("Tried to manually add a brain ({}) but an exception occurred: {}", ipAddress, e.getMessage(),
                    e);
            return false;
        }
    }

    @Override
    public boolean removeDiscovered(String servletUrl) {
        NeeoUtil.requireNotEmpty(servletUrl, "servletUrl cannot be null");
        systemsLock.lock();
        try {
            final Optional<NeeoSystemInfo> sysInfo = systems.keySet().stream()
                    .filter(e -> servletUrl.equals(NeeoUtil.getServletUrl(e.getHostname()))).findFirst();
            if (sysInfo.isPresent()) {
                systems.remove(sysInfo.get());
                fireRemoved(sysInfo.get());
                save();
                return true;
            } else {
                logger.debug("Tried to remove a servlet for {} but none were found - ignored.", servletUrl);
                return false;
            }
        } finally {
            systemsLock.unlock();
        }
    }

    /**
     * Removes the service. If the info represents a brain we already discovered, a {@link #fireRemoved(NeeoSystemInfo)}
     * is issued.
     *
     * @param info the non-null {@link ServiceInfo}
     */
    private void removeService(ServiceInfo info) {
        Objects.requireNonNull(info, "info cannot be null");

        final Entry<String, InetAddress> brainInfo = getNeeoBrainInfo(info);
        if (brainInfo == null) {
            return;
        }

        systemsLock.lock();
        try {
            NeeoSystemInfo foundInfo = null;
            for (NeeoSystemInfo existingSysInfo : systems.keySet()) {
                if (existingSysInfo.getHostname().equals(brainInfo.getKey())) {
                    foundInfo = existingSysInfo;
                    break;
                }
            }
            if (foundInfo != null) {
                fireRemoved(foundInfo);
                systems.remove(foundInfo);
                save();
            }
        } finally {
            systemsLock.unlock();
        }
    }

    /**
     * Saves the current brains to the {@link #file}. Any {@link IOException} will be logged and ignored. Please note
     * that this method ASSUMES that it is called under a lock on {@link #systemsLock}
     */
    private void save() {
        try {
            // ensure full path exists
            file.getParentFile().mkdirs();

            final List<String> ipAddresses = systems.values().stream().map(e -> e.getHostAddress())
                    .collect(Collectors.toList());

            logger.debug("Saving brain's discovered to {}: {}", file.toPath(), String.join(",", ipAddresses));

            final String json = gson.toJson(ipAddresses);
            final byte[] contents = json.getBytes(StandardCharsets.UTF_8);
            Files.write(file.toPath(), contents);
        } catch (IOException e) {
            logger.debug("IOException writing {}: {}", file.toPath(), e.getMessage(), e);
        }
    }

    /**
     * Get's the IP address from the given service
     *
     * @param service the non-null {@link ServiceInfo}
     * @return the ip address of the service or null if not found
     */
    @Nullable
    private InetAddress getIpAddress(ServiceInfo service) {
        Objects.requireNonNull(service, "service cannot be null");

        for (String addr : service.getHostAddresses()) {
            try {
                return InetAddress.getByName(addr);
            } catch (UnknownHostException e) {
                // ignore
            }
        }

        InetAddress address = null;
        for (InetAddress addr : service.getInet4Addresses()) {
            return addr;
        }
        // Fallback for Inet6addresses
        for (InetAddress addr : service.getInet6Addresses()) {
            return addr;
        }
        return address;
    }

    @Override
    public void close() {
        context.getMdnsClient().unregisterAllServices();
        systemsLock.lock();
        try {
            save();
            systems.clear();
        } finally {
            systemsLock.unlock();
        }
        logger.debug("Stopped NEEO Brain MDNS Listener");
    }
}
