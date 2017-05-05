/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuBindingHandler} class provides abstraction to the discovery
 * class so that a list of devices can be return with each scan
 *
 * @author Jarod Peters - Initial contribution
 */
public class RokuDiscoveryProvider {

    public static final String DEFAULT_MCAST_GRP = "239.255.255.250";
    public static final int DEFAULT_MCAST_PORT = 1900;
    private String multicastGroup;
    private int port;
    private ArrayList<String> networkAddr;

    private final Logger logger = LoggerFactory.getLogger(RokuDiscoveryProvider.class);

    public RokuDiscoveryProvider(String multicastGroup, int port) {
        this.multicastGroup = multicastGroup;
        this.port = port;
    }

    public void discover() throws IOException {
        ArrayList<String> arrayList = new ArrayList<String>();
        String address;
        for (int i = 0; i < 20; i++) {
            if (i < 9) {
                System.out.print("0" + (i + 1));
            } else {
                System.out.print(i + 1);
            }
            System.out.print("/20)");
            try {
                address = scanForRoku();
                if (!arrayList.contains(address)) {
                    arrayList.add(address);
                }
            } catch (Exception e) {
            }
        }
        this.networkAddr = arrayList;
    }

    private String scanForRoku() throws Exception {
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        String MSEARCH = "M-SEARCH * HTTP/1.1\nHost: " + multicastGroup + ":" + port
                + "\nMan: \"ssdp:discover\"\nST: roku:ecp\n";
        sendData = MSEARCH.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                InetAddress.getByName(DEFAULT_MCAST_GRP), DEFAULT_MCAST_PORT);

        logger.debug("Sending multicast SSDP MSEARCH request...");
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.send(sendPacket);

        logger.debug("Waiting for network response...");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        String response = new String(receivePacket.getData());
        clientSocket.close();

        response = response.toLowerCase();
        String address = response.split("location:")[1].split("\n")[0].split("http://")[1].trim().replace("/", "");
        logger.debug("Found Roku at " + address);
        return address;
    }

    public ArrayList<String> getResults() {
        return networkAddr;
    }
}
