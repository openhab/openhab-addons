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
package org.openhab.binding.homekit.internal.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for resolving MAC addresses from IP addresses via the operating system's ARP cache. The main method
 * {@link #resolveMac(String)} provides an asynchronous API to get the MAC address for a given IP address. If the MAC
 * address is cached and valid, it returns immediately. Otherwise, it starts a front end process that involves probing
 * the IP address to trigger the OS to populate its ARP table, plus a back end process that bulk loads the OS ARP cache
 * into the in-memory cache and completes the pending future when the MAC address becomes available. The implementation
 * includes optimizations to avoid unnecessary ARP cache loads for non-local or unreachable IP addresses, and to share
 * pending resolution tasks for the same IP address to avoid redundant work. Resolved MAC addresses are cached in-memory
 * with an expiration time to avoid frequent lookups, and the back end process is scheduled to run only when there are
 * pending resolutions to avoid unnecessary resource usage. This class is designed to be thread-safe and efficient for
 * typical home network environments where devices may come and go, and ARP cache entries may expire or change over
 * time.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = MacResolver.class)
public class MacResolver {

    private static final Duration ARP_LOAD_PROCESS_TIMEOUT = Duration.ofMillis(1500);
    private static final Duration CACHE_VALIDITY_DURATION = Duration.ofMinutes(7);
    private static final Duration BACKEND_TASK_RUN_INITIAL_DELAY = Duration.ofMillis(20);
    private static final Duration BACKEND_TASK_RUN_INTERVAL = Duration.ofMillis(1200);
    private static final Duration RESOLVE_MAC_TIMEOUT = Duration.ofSeconds(4);

    private static final Pattern MAC_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}");
    private static final Pattern IP_PATTERN = Pattern
            .compile("\\b((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\b");

    private static final String BROADCAST_IP = "255.255.255.255";

    private final Logger logger = LoggerFactory.getLogger(MacResolver.class);

    // cache of IP / MAC mappings with expiration time stamps; prevents hitting the OS ARP cache too often
    protected final Map<String, ExpiringMac> arpCache = new ConcurrentHashMap<>();

    // map of pending MAC resolution futures for each IP; allows sharing of pending resolutions for the same IP
    private final Map<String, Set<CompletableFuture<@Nullable String>>> pendingFutureMacs = new ConcurrentHashMap<>();

    private @NonNullByDefault({}) ExecutorService frontEndExecutor;
    private @NonNullByDefault({}) ScheduledExecutorService backEndScheduler;
    private @Nullable ScheduledFuture<?> backEndTaskSchedule;

    // operating system type
    private enum OSType {
        LINUX,
        MAC_OS,
        WINDOWS,
        UNKNOWN;

        static OSType from(String osName) {
            String name = osName.toLowerCase(Locale.ROOT);
            if (name.contains("linux")) {
                return LINUX;
            }
            if (name.contains("mac") || name.contains("darwin")) {
                return MAC_OS;
            }
            if (name.contains("win")) {
                return WINDOWS;
            }
            return UNKNOWN;
        }
    }

    private static final String OS_NAME = Objects.requireNonNull(System.getProperty("os.name", ""));
    private static final OSType OS_TYPE = OSType.from(OS_NAME);

    private static final byte[] ARP_TRIGGER_BUF = new byte[1];
    private static final int ARP_TRIGGER_BUF_SIZE = ARP_TRIGGER_BUF.length;
    private static final int DISCARD_PORT = 9;

    private String windowsArp = "arp";

    /**
     * Simple wrapper class to hold a MAC address with its expiration time-stamp.
     */
    protected static class ExpiringMac {

        private final String mac;
        private final Instant expires;

        /**
         * Creates a new expiring MAC entry.
         */
        public ExpiringMac(String mac) {
            this(mac, Instant.now().plus(CACHE_VALIDITY_DURATION));
        }

        /**
         * For Unit tests only: Creates a new expiring MAC entry with a given explicit expires time.
         */
        ExpiringMac(String mac, Instant expires) {
            this.mac = mac;
            this.expires = expires;
        }

        /**
         * Returns the MAC address if not expired, otherwise {@code null}.
         */
        public @Nullable String getMac() {
            return isExpired() ? null : mac;
        }

        /**
         * Checks whether this entry has expired.
         */
        public boolean isExpired() {
            return Instant.now().isAfter(expires);
        }
    }

    @Activate
    public MacResolver() {
        frontEndExecutor = ThreadPoolManager.getPool("OH-MacResolver-FrontEnd");
        backEndScheduler = ThreadPoolManager.getScheduledPool("OH-MacResolver-BackEnd");
        if (OS_TYPE == OSType.UNKNOWN) {
            logger.warn("Unknown OS '{}' MacResolver may not work.", OS_NAME);
        }
        if (OS_TYPE == OSType.WINDOWS) {
            String path = System.getenv("SystemRoot");
            if (path != null) {
                path += "\\System32\\arp.exe";
                if (new File(path).exists()) {
                    windowsArp = path;
                }
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        stopBackEndTaskSchedule();
        pendingFutureMacs.values().forEach(futureMacs -> {
            futureMacs.forEach(futureMac -> futureMac.complete(null));
            futureMacs.clear();
        });
        pendingFutureMacs.clear();
    }

    /**
     * Schedules a periodic task to load the ARP cache and complete pending futures. The scheduler is started
     * when the first resolution request is made, and stopped when there are no more pending resolutions to
     * avoid unnecessary resource usage.
     */
    private synchronized void startBackEndTaskSchedule() {
        if (backEndTaskSchedule != null) {
            return;
        }
        logger.trace("Starting back end");
        backEndTaskSchedule = backEndScheduler.scheduleWithFixedDelay(this::backEndTask,
                BACKEND_TASK_RUN_INITIAL_DELAY.toMillis(), BACKEND_TASK_RUN_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the ARP cache loading schedule if it is running. This is called when there are no more pending
     * resolution tasks to avoid unnecessary resource usage.
     */
    private synchronized void stopBackEndTaskSchedule() {
        ScheduledFuture<?> task;
        task = backEndTaskSchedule;
        backEndTaskSchedule = null;
        if (task != null) {
            logger.trace("Stopping back end");
            task.cancel(false);
        }
    }

    /**
     * Resolves the MAC address for a given IP address. If the MAC address is cached and valid, it is returned
     * immediately. Otherwise, an asynchronous resolution process is started that involves a front end process that
     * probes the IP to trigger the OS to populate its ARP table, and a back end process that bulk loads the ARP table
     * and completes the future when the MAC address becomes available. The future completes with {@code null} if
     * resolution fails or takes too long. The method also includes optimizations to avoid unnecessary ARP cache loads
     * for invalid, non-local, or unreachable IP addresses by checking these conditions before scheduling the
     * asynchronous resolution.
     * 
     * @param ipAddress the IP address to resolve e.g. "192.168.1.1" or "192.168.1.1:port"
     * @return a future that completes with the resolved MAC address or {@code null} if resolution fails
     *         or times out
     */
    public CompletableFuture<@Nullable String> resolveMac(String ipAddress) {
        if (!beginsWithValidIp(ipAddress)) {
            logger.debug("{} invalid", ipAddress);
            return CompletableFuture.completedFuture(null);
        }
        String ip = normalizeIp(ipAddress);

        // FAST PATH: check cache before doing any async work, and complete immediately if present and valid
        String cachedMac = cacheGet(ip);
        if (cachedMac != null) {
            logger.trace("{} -> {} (fast-path)", ip, cachedMac);

            // complete and remove any existing pending futures for this IP
            Set<CompletableFuture<@Nullable String>> pending = pendingFutureMacs.remove(ip);
            if (pending != null && !pending.isEmpty()) {
                pending.forEach(f -> f.complete(cachedMac));
            }

            // if no pending futures remain globally, stop the backend
            if (pendingFutureMacs.isEmpty()) {
                stopBackEndTaskSchedule();
            }

            // return a completed future for this call
            return CompletableFuture.completedFuture(cachedMac);
        }

        // SLOW PATH: validate IP and schedule async resolution process
        InetAddress addr;
        try {
            addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            addr = null;
        }
        if (addr == null || addr.isLoopbackAddress() || addr.isAnyLocalAddress() || addr.isMulticastAddress()
                || BROADCAST_IP.equals(ip)) {
            logger.debug("{} is invalid, loopback, 'any', multicast, or broadcast", ip);
            return CompletableFuture.completedFuture(null);
        }
        if (!isOnLocalSubnet(ip)) {
            logger.debug("{} not on local sub-net", ip);
            return CompletableFuture.completedFuture(null);
        }

        // create this call's independent future
        CompletableFuture<@Nullable String> futureMac = new CompletableFuture<@Nullable String>()
                .completeOnTimeout(null, RESOLVE_MAC_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        futureMac.whenComplete((mac, ex) -> handleFutureCompletion(ip, futureMac));

        pendingFutureMacs.compute(ip, (key, existingSet) -> {
            // if MAC is now already cached, complete immediately and do NOT register
            String nowCached = cacheGet(ip);
            if (nowCached != null) {
                futureMac.complete(nowCached);
                return existingSet; // no change to map
            }
            // otherwise, register this future
            if (existingSet == null) {
                existingSet = ConcurrentHashMap.newKeySet();
            }
            existingSet.add(futureMac);
            return existingSet;
        });

        // continue the slow path if the future was not completed due to a concurrent cachePut, otherwise return it
        if (!futureMac.isDone()) {
            startBackEndTaskSchedule();
            frontEndExecutor.submit(() -> triggerArpTableUpdate(ip));
        }

        return futureMac;
    }

    /**
     * Handles the completion of a MAC resolution future by removing it from the pending set for the given IP, and
     * stopping the back end schedule if there are no more pending futures. This is called when a future completes
     * either with a resolved MAC address or with {@code null} due to failure or timeout.
     * 
     * @param ip the IP address associated with the completed future
     * @param futureMac the future that has completed
     */
    private void handleFutureCompletion(String ip, CompletableFuture<@Nullable String> futureMac) {
        pendingFutureMacs.computeIfPresent(ip, (key, futureMacs) -> {
            futureMacs.remove(futureMac);
            // if still empty after removal, drop the entry
            return futureMacs.isEmpty() ? null : futureMacs;
        });

        // if map is now empty, stop backend
        if (pendingFutureMacs.isEmpty()) {
            stopBackEndTaskSchedule();
        }
    }

    /**
     * Triggers the operating system to update its ARP table for the given IP address by performing probes on all valid
     * network interfaces. The method sends a single UDP data-gram to the discard port 9 to do the probe. However before
     * the OS can actually try to send the data-gram, it first has to check if it has the target MAC address. And if not
     * it must send an ARP request packet to resolve the MAC. In other words, by doing the probe we immediately trigger
     * the ARP resolution process, and for our purposes it does not matter how or if the target device responds. Each
     * probe is scoped to the candidate interfaces that are up, non-loopback, and on the same sub-net as the target IP.
     * 
     * @param ip the target IP address to trigger ARP resolution for
     */
    protected void triggerArpTableUpdate(String ip) {
        try {
            InetAddress target = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(ARP_TRIGGER_BUF, ARP_TRIGGER_BUF_SIZE, target, DISCARD_PORT);
            for (NetworkInterface netIntf : getCandidateInterfaces(ip)) {
                for (InterfaceAddress intfAddr : netIntf.getInterfaceAddresses()) {
                    if (intfAddr.getAddress() instanceof Inet4Address source) {
                        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(source, 0))) {
                            socket.send(packet);
                        } catch (Exception ignore) {
                            // don't care if send fails; all that matters is the prior ARP resolution
                        }
                        break; // only need to send one packet per interface even if there are multiple addresses
                    }
                }
            }
        } catch (UnknownHostException ignore) {
            // ip is already validated so this can't occur
        }
    }

    /**
     * Checks if the given IP address is on the same local sub-net as any of the host's network interfaces. This avoids
     * ARP cache loads for IP addresses that are not local which therefore cannot be resolved to a MAC address via the
     * OS ARP table.
     */
    protected boolean isOnLocalSubnet(String ip) {
        try {
            InetAddress ipv4 = InetAddress.getByName(ip);
            return Optional.ofNullable(NetworkInterface.getNetworkInterfaces()) //
                    .map(en -> Collections.list(en).stream()) //
                    .orElseGet(Stream::empty) //
                    .filter(nif -> {
                        try {
                            return nif.isUp() && !nif.isLoopback();
                        } catch (Exception e) {
                            return false;
                        }
                    }) //
                    .anyMatch(nif -> interfaceMatchesSubnet(nif, ipv4));
        } catch (Exception e) {
            logger.debug("Sub-net check failed for {}", ip, e);
            return false;
        }
    }

    /**
     * Gets a list of network interfaces that are up, non-loopback, and on the same sub-net as the given IP address.
     * This is used for interface-scoped probes to trigger ARP resolution on the correct interface when there are
     * multiple interfaces or sub-nets.
     */
    private List<NetworkInterface> getCandidateInterfaces(String ip) {
        try {
            InetAddress ipv4 = InetAddress.getByName(ip);
            return Optional.ofNullable(NetworkInterface.getNetworkInterfaces()).map(en -> Collections.list(en).stream())
                    .orElseGet(Stream::empty).filter(nif -> {
                        try {
                            return nif.isUp() && !nif.isLoopback() && interfaceMatchesSubnet(nif, ipv4);
                        } catch (Exception ignored) {
                            return false;
                        }
                    }).toList();
        } catch (Exception e) {
            logger.debug("Candidate interface check failed for {}", ip, e);
            return List.of();
        }
    }

    /**
     * Returns true if the given interface has any IPv4 address on the same sub-net as the target IPv4 address.
     * 
     * @param nif the network interface to check
     * @param ipv4 the target IP address to compare against the interface's addresses
     * 
     * @return true if the interface has any IPv4 address on the same sub-net as the target, false otherwise
     */
    private boolean interfaceMatchesSubnet(NetworkInterface nif, InetAddress ipv4) {
        try {
            byte[] t = ipv4.getAddress();
            if (t.length != 4) {
                return false; // IPv4 only
            }
            int ti = ByteBuffer.wrap(t).getInt();

            for (InterfaceAddress ia : nif.getInterfaceAddresses()) {
                InetAddress addr = ia.getAddress();
                byte[] a = addr.getAddress();
                if (a.length != 4) {
                    continue; // skip IPv6
                }

                int prefix = ia.getNetworkPrefixLength();
                if (prefix <= 0 || prefix > 32) {
                    continue; // allow /32
                }

                int mask = ~((1 << (32 - prefix)) - 1);
                int ai = ByteBuffer.wrap(a).getInt();

                if ((ai & mask) == (ti & mask)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Periodic task that is run by a back end scheduler that loads the ARP cache from the operating system. If
     * there are no more pending futures, the back end scheduler is stopped to avoid unnecessary resource usage.
     */
    protected void backEndTask() {
        // if there are no pending futures, skip loading and stop the scheduler
        if (pendingFutureMacs.isEmpty()) {
            stopBackEndTaskSchedule();
            return;
        }
        // load new OS ARP table entries into the in-memory cache
        arpCacheLoad();
        // remove any remaining expired in-memory cache entries
        cacheFlush();
    }

    /**
     * Executes a bulk load of the operating system's ARP cache into the in-memory cache.
     */
    private void arpCacheLoad() {
        switch (OS_TYPE) {
            case LINUX -> linuxArpCacheLoad();
            case MAC_OS -> runCommandAndParse(ARP_LOAD_PROCESS_TIMEOUT, "/usr/sbin/arp", "-n");
            case WINDOWS -> runCommandAndParse(ARP_LOAD_PROCESS_TIMEOUT, windowsArp, "-a");
            default -> {
                return;
            }
        }
    }

    /**
     * Loads ARP entries from Linux's {@code /proc/net/arp} file.
     */
    private void linuxArpCacheLoad() {
        File arpFile = new File("/proc/net/arp");
        if (!arpFile.exists()) {
            logger.debug("ARP file {} does not exist", arpFile.getAbsolutePath());
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(arpFile.toPath(), StandardCharsets.UTF_8)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception e) {
            logger.debug("Error reading /proc/net/arp", e);
        }
    }

    /**
     * Removes all expired entries from the in-memory cache.
     */
    private void cacheFlush() {
        arpCache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * Retrieves a MAC address from the in-memory cache, if present and not expired.
     */
    protected @Nullable String cacheGet(String ip) {
        ExpiringMac entry = arpCache.get(ip);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            arpCache.remove(ip, entry); // remove only if same instance
            return null;
        }
        return entry.getMac();
    }

    /**
     * Stores an IP => MAC mapping in the cache with expiration and if possible eagerly resolves any pending MAC
     * future(s).
     */
    protected void cachePut(String ip, String mac) {
        arpCache.put(ip, new ExpiringMac(mac));

        // eager execution: check if a running future can be completed early
        Set<CompletableFuture<@Nullable String>> futureMacs = pendingFutureMacs.remove(ip);
        if (futureMacs != null && !futureMacs.isEmpty()) {
            logger.trace("{} -> {} (eager for {} futures)", ip, mac, futureMacs.size());
            futureMacs.forEach(futureMac -> futureMac.complete(mac));
        }

        // if no pending futures remain globally, stop scheduler immediately
        if (pendingFutureMacs.isEmpty()) {
            stopBackEndTaskSchedule();
        }
    }

    /**
     * Checks whether the given MAC address matches typical OS-supported formats, such as {@code AA:BB:CC:DD:EE:FF},
     * {@code AA-BB-CC-DD-EE-FF}, or {@code aa-bb-cc-dd-ee-ff}. Mixed separators are also accepted. The method
     * additionally rejects the all-zero MAC {@code 00:00:00:00:00:00}.
     */
    protected static boolean isValidMac(String mac) {
        return MAC_PATTERN.matcher(mac).matches() && !"00:00:00:00:00:00".equalsIgnoreCase(normalizeMac(mac));
    }

    /**
     * Converts a MAC address to the standard format {@code XX:XX:XX:XX:XX:XX}.
     */
    protected static String normalizeMac(String mac) {
        return mac.toUpperCase(Locale.ROOT).replaceAll("[^A-F0-9]", "").replaceAll("(.{2})(?=.)", "$1:");
    }

    /**
     * Checks if the text begins with a standard format and valid IP address. e.g. {@code 192.168.1.1} and
     * {@code 192.168.1.1:1234} are valid whereas {@code 999.999.999.999} or {@code foo 192.168.1.1} are not.
     * 
     * @param ip the IP address to check
     * @return true if the text begins with a valid IP address, false otherwise
     */
    protected static boolean beginsWithValidIp(String ip) {
        return IP_PATTERN.matcher(ip).lookingAt();
    }

    /**
     * Extracts IP part of a string. e.g. both {@code 192.168.1.1:8080} and {@code 192.168.1.1} produce the
     * output {@code 192.168.1.1}
     * 
     * @param ip the IP address to normalize
     * @return the normalized IP address, or the original string if it cannot be normalized
     */
    protected static String normalizeIp(String ip) {
        Matcher m = IP_PATTERN.matcher(ip);
        return m.lookingAt() ? m.group() : ip; // fallback: return original
    }

    /**
     * Parses a single line from ARP output, extracts the IP MAC mapping if present, and caches it.
     * 
     * @param line the line to parse
     */
    protected void parseLine(String line) {
        if (line.isBlank()) {
            return;
        }
        Matcher ipMatcher = IP_PATTERN.matcher(line);
        Matcher macMatcher = MAC_PATTERN.matcher(line);
        if (ipMatcher.find() && macMatcher.find()) {
            String ip = ipMatcher.group();
            String mac = normalizeMac(macMatcher.group());
            if (isValidMac(mac)) {
                cachePut(ip, mac);
            }
        }
    }

    /**
     * Runs an OS process with the given timeout and command (String... args). Returns the finished Process, or null
     * if the process timed out or failed. Timeout is enforced.
     * 
     * @param timeout the duration to wait for the process to finish
     * @param command the command and its arguments to execute
     * 
     * @return the finished Process, or null if timed out or failed
     */
    private @Nullable Process runProcess(Duration timeout, String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean finished;
            try {
                finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted while waiting for command: {}", String.join(" ", command));
                process.destroyForcibly();
                // best-effort wait after interrupt
                try {
                    process.waitFor(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
            if (!finished) {
                process.destroyForcibly();
                logger.debug("Time out executing command: {}", String.join(" ", command));
                // optional short grace wait
                try {
                    process.waitFor(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
            return process;
        } catch (Exception e) {
            logger.debug("Failed to execute command: {}", String.join(" ", command), e);
            return null;
        }
    }

    /**
     * Executes an OS process with the given timeout and command (String... args) and parses its output line by line
     * using {@link #parseLine(String)}. Timeout is enforced.
     * 
     * @param timeout the duration to wait for the process to finish
     * @param command the command and its arguments to execute
     */
    private void runCommandAndParse(Duration timeout, String... command) {
        Process process = runProcess(timeout, command);
        if (process == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception e) {
            logger.debug("Error reading result of command: {}", String.join(" ", command), e);
        }
    }
}
