/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal.dmxoverethernet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpNode} represents a sending or receiving node with address and port
 *
 * @author Jan N. Klug - Initial contribution
 *
 */
public class IpNode {
    protected static final Pattern ADDR_PATTERN = Pattern.compile("([\\w.-]+):?(\\d*)");

    private final Logger logger = LoggerFactory.getLogger(IpNode.class);

    protected int port = 0;
    protected InetAddress address = null;

    /**
     * default constructor
     */
    public IpNode() {
    }

    /**
     * constructor with address
     *
     * @param addrString address in format address[:port]
     */
    public IpNode(String addrString) {
        Matcher addrMatcher = ADDR_PATTERN.matcher(addrString);
        if (addrMatcher.matches()) {
            setInetAddress(addrMatcher.group(1));
            if (!addrMatcher.group(2).isEmpty()) {
                setPort(Integer.valueOf(addrMatcher.group(2)));
            }
        } else {
            logger.warn("invalid format {}, returning empty UdpNode", addrString);
        }
    }

    /**
     * constructor with address and port
     *
     * @param addrString domain name or IP address as string representation
     * @param port UDP port of the receiver node
     */
    public IpNode(String addrString, int port) {
        setPort(port);
        setInetAddress(addrString);
    }

    /**
     * sets the node address
     *
     * @param addrString domain name or IP address as string representation
     */
    public void setInetAddress(String addrString) {
        try {
            this.address = InetAddress.getByName(addrString);
        } catch (UnknownHostException e) {
            this.address = null;
            logger.warn("could not set address from {}", addrString);
        }
    }

    /**
     * set the node address
     *
     * @param address inet address
     */
    public void setInetAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * sets the node port
     *
     * @param port UDP port of the receiver node
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * get this nodes port
     *
     * @return UDP port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * get this nodes address
     *
     * @return address as InetAddress
     */
    public InetAddress getAddress() {
        return address;
    }

    public String getAddressString() {
        String addrString = address.getHostAddress();
        return addrString;
    }

    /**
     * convert node to String
     *
     * @return string representation of this node (address:port)
     */
    @Override
    public String toString() {
        if (this.address == null) {
            return "(null):" + String.valueOf(this.port);
        }
        return this.address.toString() + ":" + String.valueOf(this.port);
    }

    /**
     * create list of nodes from string
     *
     * @param addrString input string, format: address1[:port],address2
     * @param defaultPort default port if none is given in the string
     * @return a List of IpNodes
     */
    public static List<IpNode> fromString(String addrString, int defaultPort) throws IllegalArgumentException {
        List<IpNode> ipNodes = new ArrayList<>();
        int port;

        for (String singleAddrString : addrString.split(",")) {
            Matcher addrMatch = ADDR_PATTERN.matcher(singleAddrString);
            if (addrMatch.matches()) {
                port = (addrMatch.group(2).isEmpty()) ? defaultPort : Integer.valueOf(addrMatch.group(2));
                ipNodes.add(new IpNode(addrMatch.group(1), port));
            } else {
                throw new IllegalArgumentException(String.format("Node definition {} is not valid.", singleAddrString));
            }
        }
        return ipNodes;
    }
}
