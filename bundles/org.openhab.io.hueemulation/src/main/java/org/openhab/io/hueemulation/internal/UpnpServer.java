/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal;

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
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.joda.time.Instant;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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
        property = { EventConstants.EVENT_TOPIC + "=" + ConfigStore.EVENT_ADDRESS_CHANGED,
                "com.eclipsesource.jaxrs.publish=false" }, //
        service = { UpnpServer.class, EventHandler.class })
public class UpnpServer extends HttpServlet implements Runnable, EventHandler {

    public static final String DISCOVERY_FILE = "/description.xml";

    // jUPNP shares port 1900, but since this is multicast, we can also bind to it
    private static final int UPNP_PORT_RECV = 1900;
    /**
     * Send a keep alive every 2 minutes
     */
    private static final int CACHE_MSECS = 120 * 1000;

    private final Logger logger = LoggerFactory.getLogger(UpnpServer.class);

    private final InetAddress MULTI_ADDR_IPV4;
    private final InetAddress MULTI_ADDR_IPV6;
    private String[] stVersions = { "", "", "" };
    private String notifyMsg = "";
    private @Nullable Thread upnpResponseThread;

    //// objects, set within activate()
    private @NonNullByDefault({}) String xmlDoc;
    private @NonNullByDefault({}) String xmlDocWithAddress;
    private @NonNullByDefault({}) String baseurl;

    //// services
    @Reference
    protected @NonNullByDefault({}) ConfigStore cs;
    @Reference
    protected @NonNullByDefault({}) HttpService httpService;

    // IO
    private @Nullable Selector asyncIOselector;
    private @NonNullByDefault({}) HueEmulationConfig emulationConfig;

    /**
     * A self test report includes the tested address, a success flag and
     * if the service at the given address is actually ours.
     */
    public static class SelfTestReport {
        final public String address;
        final public boolean reachable;
        final public boolean isOurs;

        private SelfTestReport(String address, boolean testReport, boolean isOurs) {
            this.address = address;
            this.reachable = testReport;
            this.isOurs = isOurs;
        }

        /**
         * A failed address is not reachable and not ours
         */
        public static SelfTestReport failed(String address) {
            return new SelfTestReport(address, false, false);
        }
    }

    private List<SelfTestReport> selfTests = new ArrayList<>();
    private InetAddress defaultAddress;
    private int defaultport;

    /**
     * Creates a server instance.
     * UPnP IPv4/v6 multicast addresses are determined.
     */
    public UpnpServer() {
        try {
            MULTI_ADDR_IPV4 = InetAddress.getByName("239.255.255.250");
            MULTI_ADDR_IPV6 = InetAddress.getByName("ff02::c");
            defaultAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * this object is also a servlet for providing /description.xml, the UPnP discovery result
     */
    @NonNullByDefault({})
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            xmlDocWithAddress = xmlDoc;
        } catch (IOException e) {
            logger.warn("Could not start Hue Emulation UPNP server: {}", e.getMessage(), e);
            return;
        }

        try {
            httpService.unregister(DISCOVERY_FILE);
        } catch (IllegalArgumentException ignore) {
        }

        try {
            httpService.registerServlet(DISCOVERY_FILE, this, null, null);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start Hue Emulation UPNP server: {}", e.getMessage(), e);
        }

        if (cs.isReady()) {
            handleEvent(null);
        }
    }

    private void startThread(String address, int httpPort) {
        if (this.upnpResponseThread != null) {
            throw new IllegalStateException("Thread is not null!");
        }

        Thread thread = new Thread(this);
        this.upnpResponseThread = thread;
        thread.start();
    }

    private void stopThread() {
        Thread thread = this.upnpResponseThread;
        Selector selector = this.asyncIOselector;
        if (thread != null && selector != null) {
            try {
                selector.close();
            } catch (IOException ignored) {
            }

            try {
                thread.join();
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            this.upnpResponseThread = null;
            this.asyncIOselector = null;
        }

    }

    // Don't restart the service on config change
    @Modified
    public void modified() {
    }

    private void useAddressPort(String address, int port, String multicastAddr) {
        final String urlBase = "http://" + address + ":" + String.valueOf(port);
        this.baseurl = urlBase + DISCOVERY_FILE;

        final String[] stVersions = { "upnp:rootdevice", "urn:schemas-upnp-org:device:basic:1",
                "uuid:" + emulationConfig.uuid };
        for (int i = 0; i < stVersions.length; ++i) {
            this.stVersions[i] = String.format(
                    "HTTP/1.1 200 OK\r\n" + "HOST: %s:%d\r\n" + "EXT:\r\n" + "CACHE-CONTROL: max-age=%d\r\n"
                            + "LOCATION: %s\r\n" + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/%s\r\n"
                            + "hue-bridgeid: %s\r\n" + "ST: %s\r\n" + "USN: uuid:%s\r\n\r\n",
                    multicastAddr, UPNP_PORT_RECV, CACHE_MSECS / 1000, baseurl, // host:port, cache,location
                    cs.ds.config.apiversion, cs.ds.config.bridgeid, // version, bridgeid
                    stVersions[i], emulationConfig.uuid);
        }

        this.notifyMsg = String.format(
                "NOTIFY * HTTP/1.1\r\n" + "HOST: %s:%d\r\n" + "CACHE-CONTROL: max-age=%d\r\n" + "LOCATION: %s\r\n"
                        + "SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/%s\r\nNTS: ssdp:alive\r\nNT: upnp:rootdevice\r\n"
                        + "USN: uuid:%s::upnp:rootdevice\r\n" + "hue-bridgeid: %s\r\n\r\n",
                multicastAddr, UPNP_PORT_RECV, CACHE_MSECS / 1000, baseurl, // host:port, cache,location
                cs.ds.config.apiversion, emulationConfig.uuid, cs.ds.config.bridgeid);// version, uuid, bridgeid

        xmlDocWithAddress = String.format(xmlDoc, urlBase, address, cs.ds.config.bridgeid, cs.ds.config.uuid,
                cs.ds.config.devicename);

    }

    /**
     * We have a hard dependency on the {@link ConfigStore} and that it has initialized the Hue DataStore config
     * completely. That initialization happens asynchronously and therefore we cannot rely on OSGi activate/modified
     * state changes. Instead the {@link EventAdmin} is used and we listen for the
     * {@link ConfigStore#EVENT_ADDRESS_CHANGED} event that is fired as soon as the config is ready.
     */
    @Override
    public synchronized void handleEvent(@Nullable Event event) {
        this.emulationConfig = cs.getConfig();
        this.defaultport = emulationConfig.discoveryHttpPort == 0
                ? Integer.getInteger("org.osgi.service.http.port", 8080)
                : emulationConfig.discoveryHttpPort;

        try {
            defaultAddress = InetAddress.getByName(cs.ds.config.ipaddress);
        } catch (UnknownHostException e) {
            logger.warn("The picked default IP address is not valid: ", e.getMessage());
        }

        // Use default port preliminary before the async self-test is completed.
        useAddressPort(cs.ds.config.ipaddress, defaultport, MULTI_ADDR_IPV4.getHostAddress());
        stopThread();

        CompletableFuture.supplyAsync(() -> {
            stopThread();
            selfTests.clear();

            ClientConfig configuration = new ClientConfig();
            configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 1000);
            configuration = configuration.property(ClientProperties.READ_TIMEOUT, 1000);
            Client client = ClientBuilder.newClient(configuration);
            Response response;
            String url = "";
            int announcePort;
            try {
                logger.debug("Async address test: Testing for port 80");
                boolean selfTestOnPort80;
                try {
                    response = client.target("http://" + cs.ds.config.ipaddress + ":80" + DISCOVERY_FILE).request()
                            .get();
                    selfTestOnPort80 = response.getStatus() == 200
                            && response.readEntity(String.class).contains(cs.ds.config.bridgeid);
                } catch (ProcessingException ignored) {
                    selfTestOnPort80 = false;
                }

                logger.debug("Async address test: useAddressPort+startThread");
                // Prefer port 80 if possible and if not overwritten by discoveryHttpPort
                announcePort = emulationConfig.discoveryHttpPort == 0 && selfTestOnPort80 ? 80 : defaultport;
                useAddressPort(cs.ds.config.ipaddress, announcePort, MULTI_ADDR_IPV4.getHostAddress());
                startThread(cs.ds.config.ipaddress, announcePort);

                // Test on all assigned interface addresses on org.osgi.service.http.port as well as port 80
                // Other services might run on port 80, so we search for our bridge ID in the returned document.
                for (InetAddress address : cs.getDiscoveryIps()) {
                    String ip = address.getHostAddress();
                    if (address instanceof Inet6Address) {
                        ip = "[" + ip.split("%")[0] + "]";
                    }
                    logger.debug("Async address test: Test address {}", ip);
                    try {
                        url = "http://" + ip + ":" + String.valueOf(defaultport) + DISCOVERY_FILE;
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
            return announcePort;
        }) //
                .thenAccept(announcePort -> logger.info("Hue Emulation UPNP server started on {}:{}",
                        cs.ds.config.ipaddress, announcePort)) //
                .exceptionally(e -> {
                    logger.warn("Upnp server: Address test failed", e);
                    return null;
                });
    }

    /**
     * Stops the upnp server from running
     *
     * @throws InterruptedException
     */
    @Deactivate
    public void deactivate() {
        stopThread();
        try {
            httpService.unregister(DISCOVERY_FILE);
        } catch (IllegalArgumentException ignore) {
        }
    }

    static class ClientRecord {
        public @Nullable SocketAddress clientAddress;
        public ByteBuffer buffer = ByteBuffer.allocate(1000);
    }

    private void handleRead(SelectionKey key, Set<InetAddress> addresses) throws IOException {
        logger.debug("upnp thread handle received message");
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
        logger.debug("upnp thread send announcement");
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
    public void run() {
        logger.debug("upnp thread is running");
        Thread.currentThread().setName("HueEmulation UPNP Server");
        boolean hasIPv4 = false;
        boolean hasIPv6 = false;

        try (DatagramChannel channel = DatagramChannel.open(); Selector selector = Selector.open()) {

            this.asyncIOselector = selector;

            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true)
                    .bind(new InetSocketAddress(UPNP_PORT_RECV));
            for (InetAddress address : cs.getDiscoveryIps()) {
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
                if (networkInterface == null) {
                    continue;
                }
                if (address instanceof Inet4Address) {
                    channel.join(MULTI_ADDR_IPV4, networkInterface);
                    hasIPv4 = true;
                } else {
                    channel.join(MULTI_ADDR_IPV6, networkInterface);
                    hasIPv6 = true;
                }
            }
            if (!hasIPv4 && !hasIPv6) {
                logger.warn("Could not join upnp multicast network!");
                return;
            }

            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_READ, new ClientRecord());

            if (hasIPv4) {
                try (DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(defaultAddress, 0))) {
                    sendUPNPDatagrams(sendSocket, MULTI_ADDR_IPV4, UPNP_PORT_RECV);
                }
            }
            if (hasIPv6) {
                try (DatagramSocket sendSocket = new DatagramSocket()) {
                    sendUPNPDatagrams(sendSocket, MULTI_ADDR_IPV6, UPNP_PORT_RECV);
                }
            }

            Instant time = Instant.now();

            while (selector.isOpen()) { // Run forever, receiving and echoing datagrams
                // Wait for task or until timeout expires
                selector.select(CACHE_MSECS);
                Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    if (key.isReadable()) {
                        handleRead(key, cs.getDiscoveryIps());
                    }
                    keyIter.remove();
                }

                if (time.plus(CACHE_MSECS - 200).isBefore(Instant.now())) {
                    logger.debug("upnp thread send periodic announcement");
                    time = Instant.now();
                    if (hasIPv4) {
                        try (DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(defaultAddress, 0))) {
                            sendUPNPNotify(sendSocket, MULTI_ADDR_IPV4, UPNP_PORT_RECV);
                        }
                    }
                    if (hasIPv6) {
                        try (DatagramSocket sendSocket = new DatagramSocket()) {
                            sendUPNPNotify(sendSocket, MULTI_ADDR_IPV6, UPNP_PORT_RECV);
                        }
                    }
                }
            }
        } catch (ClosedSelectorException ignored) {
        } catch (IOException e) {
            logger.warn("Socket error with UPNP server", e);
        } finally {
            this.asyncIOselector = null;
            logger.debug("upnp thread ends");
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
        return defaultport;
    }

    public boolean upnpAnnouncementThreadRunning() {
        return asyncIOselector != null;
    }
}
