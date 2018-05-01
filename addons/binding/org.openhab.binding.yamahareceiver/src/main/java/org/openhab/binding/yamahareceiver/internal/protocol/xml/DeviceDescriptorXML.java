/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Feature;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.config.YamahaUtils;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.getChildElementsWhere;

/**
 *
 * Represents device descriptor for XML protocol
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class DeviceDescriptorXML {

    private final Logger logger = LoggerFactory.getLogger(DeviceDescriptorXML.class);

    public SystemDescriptor system = new SystemDescriptor(null);
    public Map<Zone, ZoneDescriptor> zones = new HashMap<>();
    public Map<Feature, FeatureDescriptor> features = new HashMap<>();

    public void attach(DeviceInformationState state) {
        state.properties.put("desc", this);
    }

    public static DeviceDescriptorXML getAttached(DeviceInformationState state) {
        return (DeviceDescriptorXML) state.properties.getOrDefault("desc", null);
    }
    
    public static abstract class HasCommands {

        public final Set<String> commands = new HashSet<>();

        public HasCommands(Element element) {
            Element cmdList = (Element) XMLUtils.getNode(element, "Cmd_List");
            if (cmdList != null) {
                for (int i = 0; i < cmdList.getChildNodes().getLength(); i++) {
                    String cmd = cmdList.getChildNodes().item(i).getTextContent();
                    commands.add(cmd);
                }
            }
        }

        @Override
        public String toString() {
            return commands.stream().collect(joining(";"));
        }
    }

    public class SystemDescriptor extends HasCommands {

        public SystemDescriptor(Element element) {
            super(element);
        }
    }

    public class ZoneDescriptor extends HasCommands {

        public final Zone zone;

        public ZoneDescriptor(Zone zone, Element element) {
            super(element);
            this.zone = zone;
            logger.trace("Zone {} has commands: {}", zone, super.toString());
        }
    }

    public class FeatureDescriptor extends HasCommands {

        public final Feature feature;

        public FeatureDescriptor(Feature feature, Element element) {
            super(element);
            this.feature = feature;
            logger.trace("Feature {} has commands: {}", feature, super.toString());
        }
    }

    /**
     * Get the descriptor XML from the AVR and parse
     * @param con
     */
    public void load(XMLConnection con) {

        // Get and store the Yamaha Description XML. This will be used to detect proper element naming in other areas.
        Node descNode = tryGetDescriptor(con);

        system = buildFeatureLookup(descNode, "Unit",
                tag -> tag,
                (tag, e) -> new SystemDescriptor(e))
                .getOrDefault("System", null); // there will be only one System entry

        zones = buildFeatureLookup(descNode, "Subunit",
                tag -> YamahaUtils.tryParseEnum(Zone.class, tag),
                (zone, e) -> new ZoneDescriptor(zone, e));

        features = buildFeatureLookup(descNode, "Source_Device",
                tag -> XMLConstants.FEATURE_BY_YNC_TAG.getOrDefault(tag, null),
                (feature, e) -> new FeatureDescriptor(feature, e));

        logger.debug("Found system {}, zones {}, features {}", system != null ? 1 : 0, zones.size(), features.size());
    }

    /**
     * Tires to get the XML descriptor for the AVR
     * @param con
     * @return
     */
    private Node tryGetDescriptor(XMLConnection con) {
        for (String path: Arrays.asList("/YamahaRemoteControl/desc.xml", "/YamahaRemoteControl/UnitDesc.xml")) {
            try {
                String descXml = con.getResponse(path);
                Document doc = XMLUtils.xml(descXml);
                Node root = doc.getFirstChild();
                if (root != null && "Unit_Description".equals(root.getNodeName())) {
                    logger.debug("Retrieved descriptor from {}", path);
                    return root;
                }
                logger.debug("The {} response was invalid: {}", path, descXml);
            } catch (IOException e) {
                // The XML document under specified path does not exist for this model
                logger.debug("No descriptor at path {}", path);
            } catch (Exception e) {
                // Note: We were able to get the XML, but likely cannot parse it properly
                logger.warn("Could not parse descriptor at path {}", path, e);
                break;
            }
        }
        logger.warn("Could not retrieve descriptor");
        return null;
    }

    private <T, V> Map<T, V> buildFeatureLookup(Node descNode, String funcValue, Function<String, T> converter, BiFunction<T, Element, V> factory) {
        Map<T, V> groupedElements = new HashMap<>();

        if (descNode != null) {
            Stream<Element> elements = getChildElementsWhere(descNode,
                    x -> "Menu".equals(x.getTagName()) && funcValue.equals(x.getAttribute("Func")));

            elements.forEach(e -> {
                String tag = e.getAttribute("YNC_Tag");

                if (StringUtils.isNotEmpty(tag)) {
                    T key = converter.apply(tag);
                    if (key != null) {
                        V value = factory.apply(key, e);

                        // a VNC_Tag value might appear more than once (e.g. Zone B has Main_Zone tag)
                        groupedElements.putIfAbsent(key, value);
                    }
                }
            });
        }

        return groupedElements;
    }
}
