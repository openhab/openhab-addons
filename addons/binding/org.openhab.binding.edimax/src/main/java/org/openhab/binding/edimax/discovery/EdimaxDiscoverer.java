package org.openhab.binding.edimax.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdimaxDiscoverer {

    private Logger logger = LoggerFactory.getLogger(EdimaxDiscoverer.class);

    private static final int DISCO_PORT = 20560;

    byte[] DISCOVERY_BYTES = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x45, 0x44,
            0x49, 0x4d, 0x41, 0x58, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xa1, (byte) 0xff, 0x5e };

    static protected final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(EdimaxDiscoverer.class.getName());

    private DatagramSocket serverSocket;
    private DiscoveryServer server;

    private ScheduledFuture<?> broadcastFuture;
    private ScheduledFuture<?> timeoutFuture;

    private int timeout;
    private boolean running;

    private List<EdimaxDiscoveryListener> listeners = new CopyOnWriteArrayList<EdimaxDiscoveryListener>();

    public EdimaxDiscoverer(int timeout) {
        this.timeout = timeout;
        running = false;
        listeners = new LinkedList<EdimaxDiscoveryListener>();
    }

    /**
     * Adds a HarmonyHubDiscoveryListener
     *
     * @param listener
     */
    public void addListener(EdimaxDiscoveryListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a HarmonyHubDiscoveryListener
     *
     * @param listener
     */
    public void removeListener(EdimaxDiscoveryListener listener) {
        listeners.remove(listener);
    }

    public synchronized void startDiscovery() {
        if (running) {
            return;
        }

        try {
            serverSocket = new DatagramSocket(0);
            logger.debug("Creating Harmony server on port {}", serverSocket.getLocalPort());
            server = new DiscoveryServer(serverSocket);
            server.start();

            broadcastFuture = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    sendDiscoveryMessage();
                }
            }, 0, 2, TimeUnit.SECONDS);

            timeoutFuture = scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    stopDiscovery();
                }
            }, timeout, TimeUnit.SECONDS);

            running = true;
        } catch (IOException e) {
            logger.error("Could not start Harmony discovery server ", e);
        }
    }

    /**
     * Stops discovery of Harmony Hubs
     */
    public synchronized void stopDiscovery() {
        if (broadcastFuture != null) {
            broadcastFuture.cancel(true);
        }
        if (timeoutFuture != null) {
            broadcastFuture.cancel(true);
        }
        if (server != null) {
            server.setRunning(false);
        }
        try {
            serverSocket.close();
        } catch (Exception e) {
            logger.error("Could not stop harmony discovery socket", e);
        }
        for (EdimaxDiscoveryListener listener : listeners) {
            listener.discoveryFinished();
        }
        running = false;
    }

    /**
     * Send broadcast message over all active interfaces
     *
     * @param discoverString
     *            String to be used for the discovery
     */
    private void sendDiscoveryMessage() {
        DatagramSocket bcSend = null;
        try {
            bcSend = new DatagramSocket();
            bcSend.setBroadcast(true);

            byte[] sendData = DISCOVERY_BYTES;

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress[] broadcast = new InetAddress[3];
                    broadcast[0] = InetAddress.getByName("224.0.0.1");
                    broadcast[1] = InetAddress.getByName("255.255.255.255");
                    broadcast[2] = interfaceAddress.getBroadcast();
                    for (InetAddress bc : broadcast) {
                        // Send the broadcast package!
                        if (bc != null) {
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, bc,
                                        DISCO_PORT);
                                bcSend.send(sendPacket);
                            } catch (IOException e) {
                                logger.error("IO error during HarmonyHub discovery: {}", e.getMessage());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            logger.trace("Request packet sent to: {} Interface: {}", bc.getHostAddress(),
                                    networkInterface.getDisplayName());
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.debug("IO error during HarmonyHub discovery: {}", e.getMessage());
        } finally {
            try {
                if (bcSend != null) {
                    bcSend.close();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private class DiscoveryServer extends Thread {
        private DatagramSocket serverSocket;
        private boolean running;
        private List<String> response = new ArrayList<>();

        public DiscoveryServer(DatagramSocket serverSocket) {
            this.serverSocket = serverSocket;
            running = true;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            byte[] receiveData = new byte[1024];

            while (running) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    String sentence = new String(receivePacket.getData());
                    System.out.println(sentence);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
