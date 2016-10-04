/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zigbee.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.bubblecloud.zigbee.ZigBeeApi;
import org.bubblecloud.zigbee.network.ZigBeeEndpoint;
import org.bubblecloud.zigbee.network.ZigBeeNode;
import org.bubblecloud.zigbee.network.ZigBeeNodeDescriptor;
import org.bubblecloud.zigbee.network.ZigBeeNodePowerDescriptor;
import org.bubblecloud.zigbee.network.impl.ZigBeeEndpointImpl;
import org.bubblecloud.zigbee.network.impl.ZigBeeNodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZigBeeNodeSerializer {
    private Logger logger = LoggerFactory.getLogger(ZigBeeNodeSerializer.class);
    private String folderName = "userdata/zigbee";

    private XStream createXStream() {
        final String USERDATA_DIR_PROG_ARGUMENT = "smarthome.userdata";
        final String eshUserDataFolder = System.getProperty(USERDATA_DIR_PROG_ARGUMENT);
        if (eshUserDataFolder != null) {
            folderName = eshUserDataFolder + "/zigbee";
        }

        final File folder = new File(folderName);

        // create path for serialization.
        if (!folder.exists()) {
            logger.debug("Creating directory {}", folderName);
            folder.mkdirs();
        }

        XStream stream = new XStream(new StaxDriver());

        stream.alias("Endpoint", ZigBeeEndpointImpl.class);
        stream.alias("Node", ZigBeeNodeImpl.class);
        stream.alias("NodeDescriptor", ZigBeeNodeDescriptor.class);
        stream.alias("PowerDescriptor", ZigBeeNodePowerDescriptor.class);
        stream.setClassLoader(ZigBeeEndpointImpl.class.getClassLoader());
        // stream.addImplicitCollection(ZigBeeNodeDescriptor.class, "macCapabilities");

        return stream;
    }

    public void serializeNode(ZigBeeApi zigbeeApi, ZigBeeNode node) {
        // Create a copy of the node for serialization
        ZigBeeNodeImpl node2 = new ZigBeeNodeImpl();
        node2.setIeeeAddress(node.getIeeeAddress());
        node2.setNetworkAddress(node.getNetworkAddress());
        node2.setNodeDescriptor(node.getNodeDescriptor());
        node2.setPowerDescriptor(node.getPowerDescriptor());

        final List<ZigBeeEndpoint> endpoints = new ArrayList<ZigBeeEndpoint>();
        for (final ZigBeeEndpoint endpoint : zigbeeApi.getNodeEndpoints(node)) {
            ZigBeeEndpointImpl endpoint2 = new ZigBeeEndpointImpl();
            endpoint2.setNode(node2);
            endpoint2.setDeviceTypeId(endpoint.getDeviceTypeId());
            endpoint2.setProfileId(endpoint.getProfileId());
            endpoint2.setDeviceVersion(endpoint.getDeviceVersion());
            endpoint2.setEndPointAddress(endpoint.getEndPointAddress());
            endpoint2.setInputClusters(endpoint.getInputClusters());
            endpoint2.setOutputClusters(endpoint.getOutputClusters());
            endpoint2.setEndpointId(endpoint.getEndpointId());

            endpoints.add(endpoint2);
        }

        final XStream stream = createXStream();

        File file = new File(this.folderName, node.getIeeeAddress() + ".xml");
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            stream.marshal(endpoints, new PrettyPrintWriter(writer));
            writer.flush();
        } catch (IOException e) {
            logger.error("{}: Error serializing network to file: {}", node.getIeeeAddress(), e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public List<ZigBeeEndpoint> deserializeNode(String address) {
        File file = new File(this.folderName, address + ".xml");
        BufferedReader reader = null;

        logger.debug("{}: Serializing from file {}", address, file.getPath());

        if (!file.exists()) {
            logger.debug("{}: Error serializing from file: file does not exist.", address);
            return null;
        }

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            final XStream stream = createXStream();
            Object xxx = stream.fromXML(reader);
            final List<ZigBeeEndpoint> endpoints = (List<ZigBeeEndpoint>) xxx;

            return endpoints;
        } catch (IOException e) {
            logger.error("{}: Error serializing from file: {}", address, e.getMessage());
        } catch (ConversionException e) {
            logger.error("{}: Error serializing from file: {}", address, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
