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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.Commands.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLProtocolService.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Set;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Feature;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * The system control protocol class is used to control basic non-zone functionality
 * of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link SystemControlState} instead.
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - DAB support, Spotify support, better feature detection
 */
public class DeviceInformationXML implements DeviceInformation {
    private final Logger logger = LoggerFactory.getLogger(DeviceInformationXML.class);

    private final WeakReference<AbstractConnection> comReference;
    protected DeviceInformationState state;

    public DeviceInformationXML(AbstractConnection com, DeviceInformationState state) {
        this.comReference = new WeakReference<>(com);
        this.state = state;
    }

    /**
     * We need that called only once. Will give us name, id, version and zone information.
     *
     * Example:
     * <Feature_Existence>
     * <Main_Zone>1</Main_Zone>
     * <Zone_2>1</Zone_2>
     * <Zone_3>0</Zone_3>
     * <Zone_4>0</Zone_4>
     * <Tuner>0</Tuner>
     * <DAB>1</DAB>
     * <HD_Radio>0</HD_Radio>
     * <Rhapsody>0</Rhapsody>
     * <Napster>0</Napster>
     * <SiriusXM>0</SiriusXM>
     * <Spotify>1</Spotify>
     * <Pandora>0</Pandora>
     * <JUKE>1</JUKE>
     * <MusicCast_Link>1</MusicCast_Link>
     * <SERVER>1</SERVER>
     * <NET_RADIO>1</NET_RADIO>
     * <Bluetooth>1</Bluetooth>
     * <USB>1</USB>
     * <iPod_USB>1</iPod_USB>
     * <AirPlay>1</AirPlay>
     * </Feature_Existence>
     *
     * @throws IOException
     */
    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        XMLConnection con = (XMLConnection) comReference.get();

        Node systemConfigNode = getResponse(con, SYSTEM_STATUS_CONFIG_CMD, SYSTEM_STATUS_CONFIG_PATH);

        state.host = con.getHost();
        state.name = getNodeContentOrEmpty(systemConfigNode, "Model_Name");
        state.id = getNodeContentOrEmpty(systemConfigNode, "System_ID");
        state.version = getNodeContentOrEmpty(systemConfigNode, "Version");

        state.zones.clear();
        state.features.clear();
        state.properties.clear();

        // Get and store the Yamaha Description XML. This will be used to detect proper command naming in other areas.
        DeviceDescriptorXML descriptor = new DeviceDescriptorXML();
        descriptor.load(con);
        descriptor.attach(state);

        Node featureNode = getNode(systemConfigNode, "Feature_Existence");
        if (featureNode != null) {
            for (Zone zone : Zone.values()) {
                checkFeature(featureNode, zone.toString(), zone, state.zones);
            }

            XMLConstants.FEATURE_BY_YNC_TAG
                    .forEach((name, feature) -> checkFeature(featureNode, name, feature, state.features));

        } else {
            // on older models (RX-V3900) the Feature_Existence element does not exist

            descriptor.zones.forEach((zone, x) -> state.zones.add(zone));
            descriptor.features.forEach((feature, x) -> state.features.add(feature));
        }

        detectZoneBSupport(con);

        logger.debug("Found zones: {}, features: {}", state.zones, state.features);
    }

    /**
     * Detect if Zone_B is supported (HTR-4069). This will allow Zone_2 to be emulated by the Zone_B feature.
     *
     * @param con
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    private void detectZoneBSupport(XMLConnection con) throws IOException, ReceivedMessageParseException {
        if (state.zones.contains(Main_Zone) && !state.zones.contains(Zone_2)) {
            // Detect if Zone_B is supported (HTR-4069). This will allow Zone_2 to be emulated.

            // Retrieve Main_Zone basic status, from which we will know this AVR supports Zone_B feature.
            Node basicStatusNode = getZoneResponse(con, Main_Zone, ZONE_BASIC_STATUS_CMD, ZONE_BASIC_STATUS_PATH);
            String power = getNodeContentOrEmpty(basicStatusNode, "Power_Control/Zone_B_Power_Info");
            if (!power.isEmpty()) {
                logger.debug("Zone_2 emulation enabled via Zone_B");
                state.zones.add(Zone_2);
                state.features.add(Feature.ZONE_B);
            }
        }
    }

    private boolean isFeatureSupported(Node node, String name) {
        String value = getNodeContentOrEmpty(node, name);
        return "1".equals(value) || "Available".equals(value);
    }

    private <T> void checkFeature(Node node, String name, T value, Set<T> set) {
        if (isFeatureSupported(node, name)) {
            set.add(value);
        }
    }
}
