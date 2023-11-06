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

import static java.util.stream.Collectors.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.getChildElements;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Feature;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.config.YamahaUtils;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * Represents device descriptor for XML protocol
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class DeviceDescriptorXML {

    private final Logger logger = LoggerFactory.getLogger(DeviceDescriptorXML.class);

    private String unitName;
    public SystemDescriptor system = new SystemDescriptor(null);
    public Map<Zone, ZoneDescriptor> zones = new HashMap<>();
    public Map<Feature, FeatureDescriptor> features = new HashMap<>();

    public void attach(DeviceInformationState state) {
        state.properties.put("desc", this);
    }

    public static DeviceDescriptorXML getAttached(DeviceInformationState state) {
        return (DeviceDescriptorXML) state.properties.getOrDefault("desc", null);
    }

    public String getUnitName() {
        return unitName;
    }

    /**
     * Checks if the condition is met, on false result calls the runnable.
     *
     * @param predicate
     * @param falseAction
     * @return
     */
    public boolean hasFeature(Predicate<DeviceDescriptorXML> predicate, Runnable falseAction) {
        boolean result = predicate.test(this);
        if (!result) {
            falseAction.run();
        }
        return result;
    }

    public abstract static class HasCommands {

        public final Set<String> commands;

        public HasCommands(Element element) {
            Element cmdList = (Element) XMLUtils.getNode(element, "Cmd_List");
            if (cmdList != null) {
                commands = XMLUtils.toStream(cmdList.getElementsByTagName("Define")).map(x -> x.getTextContent())
                        .collect(toSet());
            } else {
                commands = new HashSet<>();
            }
        }

        public boolean hasCommandEnding(String command) {
            return commands.stream().anyMatch(x -> x.endsWith(command));
        }

        public boolean hasAnyCommandEnding(String... anyCommand) {
            return Arrays.stream(anyCommand).anyMatch(x -> hasCommandEnding(x));
        }

        /**
         * Checks if the command is available, on false result calls the runnable.
         *
         * @param command
         * @param falseAction
         * @return
         */
        public boolean hasCommandEnding(String command, Runnable falseAction) {
            boolean result = hasCommandEnding(command);
            if (!result) {
                falseAction.run();
            }
            return result;
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
     *
     * @param con
     */
    public void load(XMLConnection con) {
        // Get and store the Yamaha Description XML. This will be used to detect proper element naming in other areas.
        Node descNode = tryGetDescriptor(con);

        unitName = descNode.getAttributes().getNamedItem("Unit_Name").getTextContent();

        system = buildFeatureLookup(descNode, "Unit", tag -> tag, (tag, e) -> new SystemDescriptor(e))
                .getOrDefault("System", this.system); // there will be only one System entry

        zones = buildFeatureLookup(descNode, "Subunit", tag -> YamahaUtils.tryParseEnum(Zone.class, tag),
                (zone, e) -> new ZoneDescriptor(zone, e));

        features = buildFeatureLookup(descNode, "Source_Device",
                tag -> XMLConstants.FEATURE_BY_YNC_TAG.getOrDefault(tag, null),
                (feature, e) -> new FeatureDescriptor(feature, e));

        logger.debug("Found system {}, zones {}, features {}", system != null ? 1 : 0, zones.size(), features.size());
    }

    /**
     * Tires to get the XML descriptor for the AVR
     *
     * @param con
     * @return
     */
    private Node tryGetDescriptor(XMLConnection con) {
        for (String path : Arrays.asList("/YamahaRemoteControl/desc.xml", "/YamahaRemoteControl/UnitDesc.xml")) {
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

    private <T, V> Map<T, V> buildFeatureLookup(Node descNode, String funcValue, Function<String, T> converter,
            BiFunction<T, Element, V> factory) {
        Map<T, V> groupedElements = new HashMap<>();

        if (descNode != null) {
            Stream<Element> elements = getChildElements(descNode)
                    .filter(x -> "Menu".equals(x.getTagName()) && funcValue.equals(x.getAttribute("Func")));

            elements.forEach(e -> {
                String tag = e.getAttribute("YNC_Tag");

                if (!tag.isEmpty()) {
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
