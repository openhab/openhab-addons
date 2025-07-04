package org.openhab.binding.jellyfin.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerDiscoveryService {
    private static final int DISCOVERY_PORT = 7359;
    private static final int TIMEOUT_MS = 2000;
    private static final String DISCOVERY_MESSAGE = "who is JellyfinServer?";

    private final List<JellyfinServerInfo> discoveredServers = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public List<JellyfinServerInfo> discoverServers() {
        discoveredServers.clear();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        executorService.submit(() -> sendDiscoveryPacket(broadcast));
                    }
                }
            }
            // Give some time for responses to come back
            Thread.sleep(TIMEOUT_MS + 500); // Wait a bit longer than the timeout
        } catch (SocketException | InterruptedException e) {
            System.err.println("Error during network interface enumeration or sleep: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
        return new ArrayList<>(discoveredServers);
    }

    private void sendDiscoveryPacket(InetAddress broadcastAddress) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS); // Set a timeout for receiving responses

            byte[] sendData = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, DISCOVERY_PORT);
            socket.send(sendPacket);

            System.out.println("Sent discovery packet to " + broadcastAddress.getHostAddress() + ":" + DISCOVERY_PORT);

            // Listen for responses
            byte[] receiveData = new byte[1024];
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    JellyfinServerInfo serverInfo = parseJellyfinResponse(response,
                            receivePacket.getAddress().getHostAddress());
                    if (serverInfo != null) {
                        if (!discoveredServers.contains(serverInfo)) { // Avoid duplicates
                            discoveredServers.add(serverInfo);
                            System.out.println("Discovered Jellyfin server: " + serverInfo);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // No more responses within the timeout
                    break;
                } catch (IOException e) {
                    System.err.println("Error receiving discovery response: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating or sending discovery socket: " + e.getMessage());
        }
    }

    // Parses the response from a Jellyfin server.
    // The response is usually a JSON string, but for basic discovery,
    // we can extract key information.
    private JellyfinServerInfo parseJellyfinResponse(String response, String ipAddress) {
        // Jellyfin discovery response is typically a JSON string like:
        // {"Name":"MyJellyfinServer","Id":"some_id","Address":"http://192.168.1.100:8096","Port":8096}
        Pattern namePattern = Pattern.compile("\"Name\":\"([^\"]+)\"");
        Pattern idPattern = Pattern.compile("\"Id\":\"([^\"]+)\"");
        Pattern addressPattern = Pattern.compile("\"Address\":\"([^\"]+)\"");
        Pattern portPattern = Pattern.compile("\"Port\":(\\d+)");

        String name = null;
        String id = null;
        String serverAddress = null;
        int port = 0;

        Matcher nameMatcher = namePattern.matcher(response);
        if (nameMatcher.find()) {
            name = nameMatcher.group(1);
        }

        Matcher idMatcher = idPattern.matcher(response);
        if (idMatcher.find()) {
            id = idMatcher.group(1);
        }

        Matcher addressMatcher = addressPattern.matcher(response);
        if (addressMatcher.find()) {
            serverAddress = addressMatcher.group(1);
        }

        Matcher portMatcher = portPattern.matcher(response);
        if (portMatcher.find()) {
            try {
                port = Integer.parseInt(portMatcher.group(1));
            } catch (NumberFormatException e) {
                // Ignore, port will remain 0
            }
        }

        if (name != null && id != null && serverAddress != null && port != 0) {
            return new JellyfinServerInfo(name, id, serverAddress, port, ipAddress);
        } else {
            System.err.println("Could not parse Jellyfin response: " + response);
            return null;
        }
    }

    public static void main(String[] args) {
        ServerDiscoveryService discoverer = new ServerDiscoveryService();
        System.out.println("Searching for Jellyfin servers...");
        List<JellyfinServerInfo> servers = discoverer.discoverServers();

        if (servers.isEmpty()) {
            System.out.println("No Jellyfin servers found on the local network.");
        } else {
            System.out.println("\nDiscovered Jellyfin Servers:");
            for (JellyfinServerInfo server : servers) {
                System.out.println(server);
            }
        }
    }
}
