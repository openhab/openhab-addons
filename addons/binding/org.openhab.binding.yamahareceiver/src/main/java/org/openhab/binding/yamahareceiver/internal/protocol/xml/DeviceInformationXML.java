/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The system control protocol class is used to control basic non-zone functionality
 * of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link SystemControlState} instead.
 *
 * @author David Gräff <david.graeff@tu-dortmund.de>
 * @author Tomasz Maruszak - DAB support, Spotify support
 */
public class DeviceInformationXML implements DeviceInformation {
    private Logger logger = LoggerFactory.getLogger(DeviceInformationXML.class);
    private WeakReference<AbstractConnection> comReference;
    protected DeviceInformationState state;

    public DeviceInformationXML(AbstractConnection com, DeviceInformationState state) {
        this.comReference = new WeakReference<>(com);
        this.state = state;
    }

    /**
     * We need that called only once. Will give us name, id, version and
     * zone information.
     *
     * Example:
     * <Feature_Existence>
     *   <Main_Zone>1</Main_Zone>
     *   <Zone_2>1</Zone_2>
     *   <Zone_3>0</Zone_3>
     *   <Zone_4>0</Zone_4>
     *   <Tuner>0</Tuner>
     *   <DAB>1</DAB>
     *   <HD_Radio>0</HD_Radio>
     *   <Rhapsody>0</Rhapsody>
     *   <Napster>0</Napster>
     *   <SiriusXM>0</SiriusXM>
     *   <Spotify>1</Spotify>
     *   <Pandora>0</Pandora>
     *   <JUKE>1</JUKE>
     *   <MusicCast_Link>1</MusicCast_Link>
     *   <SERVER>1</SERVER>
     *   <NET_RADIO>1</NET_RADIO>
     *   <Bluetooth>1</Bluetooth>
     *   <USB>1</USB>
     *   <iPod_USB>1</iPod_USB>
     *   <AirPlay>1</AirPlay>
     * </Feature_Existence>
     *
     * @throws IOException
     */
    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        AbstractConnection com = comReference.get();
        String response = com.sendReceive("<System><Config>GetParam</Config></System>");
        Document doc = XMLUtils.xml(response);
        if (doc == null || doc.getFirstChild() == null) {
            throw new ReceivedMessageParseException("<System><Config>GetParam failed: " + response);
        }

        state.host = com.getHost();

        Node basicStatus = XMLUtils.getNode(doc.getFirstChild(), "System/Config");

        state.name = XMLUtils.getNodeContentOrDefault(basicStatus, "Model_Name", "");
        state.id = XMLUtils.getNodeContentOrDefault(basicStatus, "System_ID", "");
        state.version = XMLUtils.getNodeContentOrDefault(basicStatus, "Version", "");

        state.zones.clear();

        Node node = XMLUtils.getNode(basicStatus, "Feature_Existence");
        if (node == null) {
            throw new ReceivedMessageParseException("Zone information not provided: " + response);
        }

        for (YamahaReceiverBindingConstants.Zone zone : YamahaReceiverBindingConstants.Zone.values()) {
            if (isFeatureSupported(node, zone.toString())) {
                logger.trace("Adding zone: {}", zone);
                state.zones.add(zone);
            }
        }

        state.supportTuner = isFeatureSupported(node, "Tuner");
        state.supportDAB = isFeatureSupported(node, "DAB");
        state.supportSpotify = isFeatureSupported(node, "Spotify");
    }

    private boolean isFeatureSupported(Node node, String name) {
        String value = XMLUtils.getNodeContentOrDefault(node, name, "");
        boolean supported = value.equals("1") || value.equals("Available");
        if (supported) {
            logger.trace("Found feature {}", name);
        }
        return supported;
    }
}
