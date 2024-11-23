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
package org.openhab.io.hueemulation.internal.upnp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advertises a Hue compatible bridge via UPNP and provides the announced /description.xml http endpoint.
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - Rewritten
 */
@SuppressWarnings("serial")
@NonNullByDefault
@Component(immediate = false, // Don't start the upnp server on its own. Must be pulled in by HueEmulationService.
        configurationPolicy = ConfigurationPolicy.IGNORE, property = {
                EventConstants.EVENT_TOPIC + "=" + ConfigStore.EVENT_ADDRESS_CHANGED }, //
        service = { UpnpServer.class, EventHandler.class })
public class UpnpServer extends HttpServlet implements Consumer<HueEmulationConfigWithRuntime>, EventHandler {
    /**
     * Used by async IO. This is our context object class.
     */
    static class ClientRecord {
        public @Nullable SocketAddress clientAddress;
        public ByteBuffer buffer = ByteBuffer.allocate(1000);
    }

    public static final String DISCOVERY_FILE = "/description.xml";

    // jUPNP shares port 1900, but since this is multicast, we can also bind to it
    public static final int UPNP_PORT = 1900;
    /**
     * Send a keep alive every 2 minutes
     */
    private static final int CACHE_MSECS = 120 * 1000;

    private final Logger logger = LoggerFactory.getLogger(UpnpServer.class);

    public final InetAddress MULTI_ADDR_IPV4;
    public final InetAddress MULTI_ADDR_IPV6;
    private String[] stVersions = { "", "", "" };
    private String notifyMsg = "";

    //// objects, set within activate()
    protected @NonNullByDefault({}) String xmlDoc;
    protected @NonNullByDefault({}) String xmlDocWithAddress;
    private @NonNullByDefault({}) String baseurl;

    //// services
    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) HttpService httpService;

    @Reference
    protected @NonNullByDefault({}) ClientBuilder clientBuilder;

    public boolean overwriteReadyToFalse = false;

    private HueEmulationConfigWithRuntime config;
    protected CompletableFuture<@Nullable HueEmulationConfigWithRuntime> configChangeFuture = CompletableFuture
            .completedFuture(config);

    private List<SelfTestReport> selfTests = new ArrayList<>();
    private final Executor executor;

    /**
     * Creates a server instance.
     * UPnP IPv4/v6 multicast addresses are determined.
     */
    public UpnpServer() {
        this(ForkJoinPool.commonPool());
    }

    public UpnpServer(Executor executor) {
        try {
            MULTI_ADDR_IPV4 = InetAddress.getByName("239.255.255.250");
            MULTI_ADDR_IPV6 = InetAddress.getByName("ff02::c");
            config = new HueEmulationConfigWithRuntime(this, null, MULTI_ADDR_IPV4, MULTI_ADDR_IPV6);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }

        this.executor = executor;
    }

    /**
     * this object is also a servlet for providing /description.xml, the UPnP discovery result
     */
    @NonNullByDefault({})
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (xmlDocWithAddress == null || xmlDocWithAddress.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (PrintWriter out = resp.getWriter()) {
            resp.setContentType("application/xml");
            out.write(xmlDocWithAddress);
        }
    }

    /**
     * Server to send UDP packets onto the network when requested by a Hue API compatible device.
     *
     * @param relativePath The URI path where the discovery xml document can be retrieved
     * @param config The hue datastore. Contains the bridgeid and uuid.
     * @param address IP to advertise for UPNP
     * @throws IOException
     * @throws NamespaceException
     * @throws ServletException
     */
    @Activate
    protected void activate() {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("discovery.xml");
        if (resourceAsStream == null) {
            logger.warn("Could not start Hue Emulation service: discovery.xml not found");
            return;
        }
        try (InputStreamReader r = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(r)) {
            xmlDoc = br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            logger.warn("Could not start Hue Emulation UPNP server: {}", e.getMessage(), e);
            return;
        }

        try {
            httpService.unregister(DISCOVERY_FILE);
        } catch (RuntimeException ignore) {
        }

        try {
            httpService.registerServlet(DISCOVERY_FILE, this, null, null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start Hue Emulation UPNP server: {}", e.getMessage(), e);
        }

        if (cs.isReady() && !overwriteReadyToFalse) {
            handleEvent(null);
        }
    }

    private void useAddressPort(HueEmulationConfigWithRuntime r) {
        final String urlBase = "http://" + r.addressString + ":" + r.port;
        this.baseurl = urlBase + DISCOVERY_FILE;

        final String[] stVersions = { "upnp:rootdevice", "urn:schemas-upnp-org:device:basic:1",
                "uuid:" + config.config.uuid };
        for (int i = 0; i < stVersions.length; ++i) {
            this.stVersions[i] = String.format(
                    "HTTP/1.1 200 OK\r\n" + "HOST: %s:%d\r\n" + "EXT:\r\n" + "CACHE-CONTROL: max-age=%d\r\n"
                            + "LOCATION: %s\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/%s\r\n"
                            + "hue-bridgeid: %s\r\n" + "ST: %s\r\n" + "USN: uuid:%s\r\n\r\n",
                    r.getMulticastAddress(), UPNP_PORT, CACHE_MSECS / 1000, baseurl, // host:port,
                                                                                     // cache,location
                    cs.ds.config.apiversion, cs.ds.config.bridgeid, // version, bridgeid
                    stVersions[i], config.config.uuid);
        }

        this.notifyMsg = String.format(
                "NOTIFY * HTTP/1.1\r\n" + "HOST: %s:%d\r\n" + "CACHE-CONTROL: max-age=%d\r\n" + "LOCATION: %s\r\n"
                        + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/%s\r\nNTS: ssdp:alive\r\nNT: upnp:rootdevice\r\n"
                        + "USN: uuid:%s::upnp:rootdevice\r\n" + "hue-bridgeid: %s\r\n\r\n",
                r.getMulticastAddress(), UPNP_PORT, CACHE_MSECS / 1000, baseurl, // host:port, cache,location
                cs.ds.config.apiversion, config.config.uuid, cs.ds.config.bridgeid);// version, uuid, bridgeid

        xmlDocWithAddress = String.format(xmlDoc, urlBase, r.addressString, cs.ds.config.bridgeid, cs.ds.config.uuid,
                cs.ds.config.devicename);
    }

    protected @Nullable HueEmulationConfigWithRuntime performAddressTest(
            @Nullable HueEmulationConfigWithRuntime config) {
        if (config == null) {
            return null; // Config hasn't changed
        }

        selfTests.clear();
        Client client = clientBuilder.connectTimeout(1, TimeUnit.SECONDS).readTimeout(1, TimeUnit.SECONDS).build();
        Response response;
        String url = "";
        try {
            boolean selfTestOnPort80;
            try {
                response = client.target("http://" + cs.ds.config.ipaddress + ":80" + DISCOVERY_FILE).request().get();
                selfTestOnPort80 = response.getStatus() == 200
                        && response.readEntity(String.class).contains(cs.ds.config.bridgeid);
            } catch (ProcessingException ignored) {
                selfTestOnPort80 = false;
            }

            // Prefer port 80 if possible and if not overwritten by discoveryHttpPort
            config.port = config.config.discoveryHttpPort == 0 && selfTestOnPort80 ? 80 : config.port;

            // Test on all assigned interface addresses on org.osgi.service.http.port as well as port 80
            // Other services might run on port 80, so we search for our bridge ID in the returned document.
            for (InetAddress address : cs.getDiscoveryIps()) {
                String ip = address.getHostAddress();
                if (address instanceof Inet6Address) {
                    ip = "[" + ip.split("%")[0] + "]";
                }
                try {
                    url = "http://" + ip + ":" + config.port + DISCOVERY_FILE;
                    response = client.target(url).request().get();
                    boolean isOurs = response.readEntity(String.class).contains(cs.ds.config.bridgeid);
                    selfTests.add(new SelfTestReport(url, response.getStatus() == 200, isOurs));
                } catch (ProcessingException e) {
                    logger.debug("Self test fail on {}: {}", url, e.getMessage());
                    selfTests.add(SelfTestReport.failed(url));
                }
                try {
                    url = "http://" + ip + DISCOVERY_FILE; // Port 80
                    response = client.target(url).request().get();
                    boolean isOurs = response.readEntity(String.class).contains(cs.ds.config.bridgeid);
                    selfTests.add(new SelfTestReport(url, response.getStatus() == 200, isOurs));
                } catch (ProcessingException e) {
                    logger.debug("Self test fail on {}: {}", url, e.getMessage());
                    selfTests.add(SelfTestReport.failed(url));
                }
            }
        } finally {
            client.close();
        }
        return config;
    }

    /**
     * Create and return new runtime configuration based on {@link ConfigStore}s current configuration.
     * Return null if the configuration has not changed compared to {@link #config}.
     *
     * @throws IllegalStateException If the {@link ConfigStore}s IP is invalid this exception is thrown.
     */
    protected @Nullable HueEmulationConfigWithRuntime createConfiguration(
            @Nullable HueEmulationConfigWithRuntime ignoredParameter) throws IllegalStateException {
        HueEmulationConfigWithRuntime r;
        try {
            r = new HueEmulationConfigWithRuntime(this, cs.getConfig(), cs.ds.config.ipaddress, MULTI_ADDR_IPV4,
                    MULTI_ADDR_IPV6);
        } catch (UnknownHostException e) {
            logger.warn("The picked default IP address is not valid: {}", e.getMessage());
            throw new IllegalStateException(e);
        }
        return r;
    }

    /**
     * Apply the given runtime configuration by stopping the current udp thread, shutting down the socket and restarting
     * the thread.
     */
    protected @Nullable HueEmulationConfigWithRuntime applyConfiguration(
            @Nullable HueEmulationConfigWithRuntime newRuntimeConfig) {
        if (newRuntimeConfig == null) {
            return null;// Config hasn't changed
        }
        config.dispose();
        this.config = newRuntimeConfig;
        useAddressPort(config);
        return config;
    }

    /**
     * We have a hard dependency on the {@link ConfigStore} and that it has initialized the Hue DataStore config
     * completely. That initialization happens asynchronously and therefore we cannot rely on OSGi activate/modified
     * state changes. Instead the {@link EventAdmin} is used and we listen for the
     * {@link ConfigStore#EVENT_ADDRESS_CHANGED} event that is fired as soon as the config is ready.
     * <p>
     * To be really sure that we are called here, this is also issued by the main service after it has received the
     * configuration ready event and depending on service start order we are also called by our own activate() method
     * when the configuration is already ready at that time.
     * <p>
     * Therefore this method is "synchronized" and chains a completable future for each call to re-evaluate the config
     * after the former future has finished.
     */
    @Override
    public synchronized void handleEvent(@Nullable Event event) {
        CompletableFuture<@Nullable HueEmulationConfigWithRuntime> root;
        // There is either already a running future, then chain a new one
        if (!configChangeFuture.isDone()) {
            root = configChangeFuture;
        } else { // Or there is none -> create a new one
            root = CompletableFuture.completedFuture(null);
        }
        configChangeFuture = root.thenApply(this::createConfiguration)
                .thenApplyAsync(this::performAddressTest, executor).thenApply(this::applyConfiguration)
                .thenCompose(c -> c.startNow())
                .whenComplete((HueEmulationConfigWithRuntime config, @Nullable Throwable e) -> {
                    if (e != null) {
                        logger.warn("Upnp server: Address test failed", e);
                    }
                });
    }

    /**
     * Stops the upnp server from running
     */
    @Deactivate
    public void deactivate() {
        config.dispose();
        try {
            httpService.unregister(DISCOVERY_FILE);
        } catch (IllegalArgumentException ignore) {
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        logger.trace("upnp thread handle received message");
        DatagramChannel channel = (DatagramChannel) key.channel();
        ClientRecord clntRec = (ClientRecord) key.attachment();
        clntRec.buffer.clear(); // Prepare buffer for receiving
        clntRec.clientAddress = channel.receive(clntRec.buffer);
        InetSocketAddress recAddress = (InetSocketAddress) clntRec.clientAddress;
        if (recAddress == null) { // Did we receive something?
            return;
        }
        String data = new String(clntRec.buffer.array(), StandardCharsets.UTF_8);
        if (!data.startsWith("M-SEARCH")) {
            return;
        }

        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendUPNPDatagrams(sendSocket, recAddress.getAddress(), recAddress.getPort());
        }
    }

    private void sendUPNPDatagrams(DatagramSocket sendSocket, InetAddress address, int port) {
        logger.trace("upnp thread send announcement");
        for (String msg : stVersions) {
            DatagramPacket response = new DatagramPacket(msg.getBytes(), msg.length(), address, port);
            try {
                logger.trace("Sending to {}:{}", address.getHostAddress(), port);
                sendSocket.send(response);
            } catch (IOException e) {
                logger.warn("Could not send UPNP response: {}", e.getMessage());
            }
        }
    }

    private void sendUPNPNotify(DatagramSocket sendSocket, InetAddress address, int port) {
        DatagramPacket response = new DatagramPacket(notifyMsg.getBytes(), notifyMsg.length(), address, port);
        try {
            logger.trace("Sending to {}:{}", address.getHostAddress(), port);
            sendSocket.send(response);
        } catch (IOException e) {
            logger.warn("Could not send UPNP response: {}", e.getMessage());
        }
    }

    @Override
    public void accept(HueEmulationConfigWithRuntime threadContext) {
        logger.info("Hue Emulation UPNP server started on {}:{}", threadContext.addressString, threadContext.port);
        boolean hasIPv4 = false;
        boolean hasIPv6 = false;

        try (Selector selector = Selector.open();
                DatagramChannel channelV4 = createBoundDataGramChannelOrNull(StandardProtocolFamily.INET);
                DatagramChannel channelV6 = createBoundDataGramChannelOrNull(StandardProtocolFamily.INET6)) {
            // Set global config to thread local config. Otherwise upnpAnnouncementThreadRunning() will report wrong
            // results.
            config = threadContext;
            threadContext.asyncIOselector = selector;

            for (InetAddress address : cs.getDiscoveryIps()) {
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
                if (networkInterface == null) {
                    continue;
                }
                if (address instanceof Inet4Address && channelV4 != null) {
                    channelV4.join(MULTI_ADDR_IPV4, networkInterface);
                    hasIPv4 = true;
                } else if (channelV6 != null) {
                    channelV6.join(MULTI_ADDR_IPV6, networkInterface);
                    hasIPv6 = true;
                }
            }
            if (!hasIPv4 && !hasIPv6) {
                logger.warn("Could not join upnp multicast network!");
                threadContext.future
                        .completeExceptionally(new IllegalStateException("Could not join upnp multicast network!"));
                return;
            }

            if (hasIPv4) {
                channelV4.configureBlocking(false);
                channelV4.register(selector, SelectionKey.OP_READ, new ClientRecord());
                try (DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(config.address, 0))) {
                    sendUPNPDatagrams(sendSocket, MULTI_ADDR_IPV4, UPNP_PORT);
                }
            }
            if (hasIPv6) {
                channelV6.configureBlocking(false);
                channelV6.register(selector, SelectionKey.OP_READ, new ClientRecord());
                try (DatagramSocket sendSocket = new DatagramSocket()) {
                    sendUPNPDatagrams(sendSocket, MULTI_ADDR_IPV6, UPNP_PORT);
                }
            }

            threadContext.future.complete(threadContext);
            Instant time = Instant.now();

            while (selector.isOpen()) { // Run forever, receiving and echoing datagrams
                // Wait for task or until timeout expires
                selector.select(CACHE_MSECS);
                Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    keyIter.remove();
                }

                if (time.plusMillis(CACHE_MSECS - 200).isBefore(Instant.now())) {
                    logger.trace("upnp thread send periodic announcement");
                    time = Instant.now();
                    if (hasIPv4) {
                        try (DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(config.address, 0))) {
                            sendUPNPNotify(sendSocket, MULTI_ADDR_IPV4, UPNP_PORT);
                        }
                    }
                    if (hasIPv6) {
                        try (DatagramSocket sendSocket = new DatagramSocket()) {
                            sendUPNPNotify(sendSocket, MULTI_ADDR_IPV6, UPNP_PORT);
                        }
                    }
                }
            }
        } catch (ClosedSelectorException ignored) {
        } catch (IOException e) {
            logger.warn("Socket error with UPNP server", e);
            threadContext.future.completeExceptionally(e);
        } finally {
            threadContext.asyncIOselector = null;
        }
    }

    @Nullable
    private DatagramChannel createBoundDataGramChannelOrNull(StandardProtocolFamily family) throws IOException {
        try {
            return DatagramChannel.open(family).setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true).bind(new InetSocketAddress(UPNP_PORT));
        } catch (UnsupportedOperationException uoe) {
            return null;
        }
    }

    /**
     * The upnp server performs some self-tests
     *
     * @return
     */
    public List<SelfTestReport> selfTests() {
        return selfTests;
    }

    public String getBaseURL() {
        return baseurl;
    }

    public int getDefaultport() {
        return config.port;
    }

    public boolean upnpAnnouncementThreadRunning() {
        return config.asyncIOselector != null;
    }
}
