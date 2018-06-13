/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws;

import static org.openhab.binding.ihc2.Ihc2BindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.ihc2.internal.discovery.Ihc2DiscoveredThing;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client.DiscoveryLevel;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to store controller's project file information.
 *
 * @author Pauli Anttila
 * @since 1.5.0
 */
public class Ihc2ProjectFile {

    private final Logger logger = LoggerFactory.getLogger(Ihc2ProjectFile.class);

    private Document fileDoc;

    public void parseProject(String filePath) throws Ihc2Execption {
        logger.debug("Parsing IHC project file...");
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            fileDoc = db.parse(new File(filePath));
        } catch (ParserConfigurationException e) {
            throw new Ihc2Execption(e);
        } catch (SAXException e) {
            throw new Ihc2Execption(e);
        } catch (IOException e) {
            throw new Ihc2Execption(e);
        }
    }

    //
    // static HashMap<Integer, ArrayList<Ihc2EnumValue>> parseProject(String filePath, String dumpResourcesToFile)
    // throws Ihc2Execption {
    //
    // try {
    // DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    //
    // DocumentBuilder db = dbf.newDocumentBuilder();
    // Document doc = db.parse(new File(filePath));
    // return parseProject(doc, dumpResourcesToFile);
    //
    // } catch (ParserConfigurationException e) {
    // throw new Ihc2Execption(e);
    // } catch (SAXException e) {
    // throw new Ihc2Execption(e);
    // } catch (IOException e) {
    // throw new Ihc2Execption(e);
    // }
    // }

    /**
     * Parse IHC / ELKO LS project file.
     *
     */
    public HashMap<Integer, ArrayList<Ihc2EnumValue>> getEnumDictonary() {
        logger.debug("Extracting enum dictonary.");

        HashMap<Integer, ArrayList<Ihc2EnumValue>> enumDictionary = new HashMap<Integer, ArrayList<Ihc2EnumValue>>();

        NodeList nodes = fileDoc.getElementsByTagName("enum_definition");

        // iterate enum definitions from project

        for (int i = 0; i < nodes.getLength(); i++) {

            Element element = (Element) nodes.item(i);

            // String enumName = element.getAttribute("name");
            int typedefId = Integer.parseInt(element.getAttribute("id").replace("_0x", ""), 16);

            ArrayList<Ihc2EnumValue> enumValues = new ArrayList<Ihc2EnumValue>();

            NodeList name = element.getElementsByTagName("enum_value");

            for (int j = 0; j < name.getLength(); j++) {
                Element val = (Element) name.item(j);
                Ihc2EnumValue enumVal = new Ihc2EnumValue();
                enumVal.id = Integer.parseInt(val.getAttribute("id").replace("_0x", ""), 16);
                enumVal.name = val.getAttribute("name");

                enumValues.add(enumVal);
            }
            enumDictionary.put(typedefId, enumValues);
        }

        return enumDictionary;
    }

    public WSDate getProjectFileModifiedDate() {
        NodeList nodesModified = fileDoc.getElementsByTagName("modified");
        WSDate modifiedDate = new WSDate();
        if (nodesModified.getLength() > 0) {
            Element elementModified = (Element) nodesModified.item(0);

            modifiedDate.setYear(Integer.valueOf(elementModified.getAttribute("year")));
            modifiedDate.setMonthWithJanuaryAsOne(Integer.valueOf(elementModified.getAttribute("month")));
            modifiedDate.setDay(Integer.valueOf(elementModified.getAttribute("day")));
            modifiedDate.setHours(Integer.valueOf(elementModified.getAttribute("hour")));
            modifiedDate.setMinutes(Integer.valueOf(elementModified.getAttribute("minute")));
        }
        return modifiedDate;
    }

    /**
     * Parse resources from IHC / ELKO LS project file.
     *
     */
    public void saveResourcesInFile(String fileName) {
        logger.info("Saving resources from project file in file {}...", fileName);

        String val = "";

        val += nodeListToString(fileDoc.getElementsByTagName("dataline_input"), "dataline_input");
        val += nodeListToString(fileDoc.getElementsByTagName("dataline_output"), "dataline_output");

        val += nodeListToString(fileDoc.getElementsByTagName("airlink_input"), "airlink_input");
        val += nodeListToString(fileDoc.getElementsByTagName("airlink_output"), "airlink_output");
        val += nodeListToString(fileDoc.getElementsByTagName("airlink_dimming"), "airlink_dimming");
        val += nodeListToString(fileDoc.getElementsByTagName("airlink_relay"), "airlink_relay");

        val += nodeListToString(fileDoc.getElementsByTagName("light_indication"), "light_indication");

        val += nodeListToString(fileDoc.getElementsByTagName("resource_temperature"), "resource_temperature");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_flag"), "resource_flag");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_timer"), "resource_timer");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_counter"), "resource_counter");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_weekday"), "resource_weekday");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_light_level"), "resource_light_level");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_integer"), "resource_integer");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_time"), "resource_time");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_date"), "resource_date");
        // val += nodeListToString(fileDoc.getElementsByTagName("resource_scene"), "resource_scene");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_enum"), "resource_enum");

        val += nodeListToString(fileDoc.getElementsByTagName("resource_temperature"), "resource_temperature");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_light"), "resource_light");
        val += nodeListToString(fileDoc.getElementsByTagName("resource_humidity_level"), "resource_humidity_level");

        try {
            File file = new File(fileName);
            logger.info("Saving IHC resource info to file '{}'", file.getAbsolutePath());

            PrintWriter out = new PrintWriter(file);
            out.println(val);
            out.close();
        } catch (FileNotFoundException e) {
            logger.warn("Unable to write IHC resources to file", e);
        }
    }

    private String nodeListToString(NodeList nodes, String header) {
        String val = "";

        for (int i = 0; i < nodes.getLength(); i++) {

            Element element = (Element) nodes.item(i);
            Element parent = (Element) nodes.item(i).getParentNode();
            Element parentParent = (Element) nodes.item(i).getParentNode().getParentNode();

            Boolean hasChild = nodes.item(i).hasChildNodes();

            String parentName = parent.getAttribute("name");
            String parentPosition = parent.getAttribute("position");
            String parentParentName = parentParent.getAttribute("name");

            String resourceName = element.getAttribute("name");
            String resourceId = element.getAttribute("id").replace("_", "");

            switch (parent.getNodeName()) {
                case "internalsettings":
                case "case_action":
                case "condition":
                case "action":
                case "event":
                    continue;
                default:
                    break;
            }

            val += resourceId;
            val += hasChild ? "+" : " ";
            val += "= " + header;
            val += " -> " + parentParentName;
            val += " -> " + parentPosition;
            val += " -> " + parentName;
            val += " -> " + resourceName;
            val += "\n";
        }

        return val;
    }

    /**
     * Parse resources from IHC / ELKO LS project file.
     *
     */
    public List<Ihc2DiscoveredThing> getThingsFromProjectFile(DiscoveryLevel discoveryLevel) {
        logger.debug("Creating list of thing from IHC projectfile");
        List<Ihc2DiscoveredThing> discoveredList = new ArrayList<Ihc2DiscoveredThing>();

        if (discoveryLevel == DiscoveryLevel.NOTHING || discoveryLevel == DiscoveryLevel.CLEAN) {
            return discoveredList;
        }

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("dataline_input"),
                THING_TYPE_SWITCH, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("dataline_output"),
                THING_TYPE_SWITCH, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("airlink_input"),
                THING_TYPE_SWITCH, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("airlink_output"),
                THING_TYPE_SWITCH, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("airlink_dimming"),
                THING_TYPE_DIMMER, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("airlink_relay"),
                THING_TYPE_SWITCH, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("light_indication"),
                THING_TYPE_SWITCH, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_temperature"),
                THING_TYPE_NUMBER, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_flag"),
                THING_TYPE_NUMBER, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_timer"),
                THING_TYPE_NUMBER, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_counter"),
                THING_TYPE_NUMBER, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_weekday"),
                THING_TYPE_NUMBER, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_light_level"),
                THING_TYPE_DIMMER, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_integer"),
                THING_TYPE_NUMBER, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_time"),
                THING_TYPE_DATETIME, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_date"),
                THING_TYPE_DATETIME, discoveryLevel));

        // discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_scene"),
        // THING_TYPE_STRING, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_enum"),
                THING_TYPE_STRING, discoveryLevel));

        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_temperature"),
                THING_TYPE_NUMBER, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_light"),
                THING_TYPE_NUMBER, discoveryLevel));
        discoveredList.addAll(nodeListToDiscoveredThingList(fileDoc.getElementsByTagName("resource_humidity_level"),
                THING_TYPE_NUMBER, discoveryLevel));

        return discoveredList;

    }

    private List<Ihc2DiscoveredThing> nodeListToDiscoveredThingList(NodeList nodes, ThingTypeUID thingTypeUID,
            DiscoveryLevel discoveryLevel) {
        List<Ihc2DiscoveredThing> discoveredList = new ArrayList<Ihc2DiscoveredThing>();

        if (discoveryLevel != DiscoveryLevel.NOTHING) {
            for (int i = 0; i < nodes.getLength(); i++) {

                Element element = (Element) nodes.item(i);
                Element parent = (Element) nodes.item(i).getParentNode();
                Element parentParent = (Element) nodes.item(i).getParentNode().getParentNode();

                Boolean hasChild = nodes.item(i).hasChildNodes();

                String parentName = parent.getAttribute("name");
                String parentPosition = parent.getAttribute("position");
                String parentParentName = parentParent.getAttribute("name");

                String resourceName = element.getAttribute("name");
                String resourceId = element.getAttribute("id").replace("_", "");

                // don't add resources from within function blocks
                switch (parent.getNodeName()) {
                    case "internalsettings":
                    case "case_action":
                    case "condition":
                    case "action":
                    case "event":
                        continue;
                    default:
                        break;
                }

                if (thingTypeUID == THING_TYPE_DIMMER) { // THING_TYPE_DIMMER never has children - always add
                    hasChild = true;
                }

                if (discoveryLevel == DiscoveryLevel.LINKED_RESOURCES && !hasChild) {
                    continue;
                }

                Ihc2DiscoveredThing discoveredThing = new Ihc2DiscoveredThing();

                discoveredThing.setThingTypeUID(thingTypeUID);
                discoveredThing.setGroup(parentParentName);
                discoveredThing.setLocation(parentPosition);
                discoveredThing.setName(resourceName);
                discoveredThing.setProduct(parentName);
                discoveredThing.setLinked(hasChild);
                discoveredThing.setResourceId(resourceId);

                discoveredList.add(discoveredThing);
            }
        }
        return discoveredList;
    }

}
