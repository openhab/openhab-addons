/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.ws.projectfile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openhab.binding.ihc.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Generic methods related to IHC / ELKO project file handling.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ProjectFileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectFileUtils.class);

    /**
     * Read IHC project file from local file.
     *
     * @param filePath File to read.
     * @return XML document.
     * @throws IhcExecption when file read fails.
     */
    public static Document readFromFile(String filePath) throws IhcExecption {
        File fXmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(fXmlFile);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new IhcExecption(e);
        }
    }

    /**
     * Save IHC project file to local file.
     *
     * @param filePath File path.
     * @param data Data to write
     * @throws IhcExecption when file write fails.
     */
    public static void saveToFile(String filePath, byte[] data) throws IhcExecption {
        try {
            try (FileOutputStream stream = new FileOutputStream(filePath)) {
                stream.write(data);
                stream.flush();
            }
        } catch (IOException e) {
            throw new IhcExecption(e);
        }
    }

    /**
     * Convert bytes to XML document.
     *
     * @return XML document or null if conversion fails.
     */
    public static Document converteBytesToDocument(byte[] data) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(data));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.warn("Error occured when trying to convert data to XML, reason {}", e.getMessage());
        }
        return null;
    }

    /**
     * Compare XML document header information to project info.
     *
     * @return true if information is equal and false if not.
     */
    public static boolean projectEqualsToControllerProject(Document projectfile, WSProjectInfo projectInfo) {
        if (projectInfo != null) {
            try {
                NodeList nodes = projectfile.getElementsByTagName("modified");
                if (nodes.getLength() == 1) {
                    Element node = (Element) nodes.item(0);
                    int year = Integer.parseInt(node.getAttribute("year"));
                    int month = Integer.parseInt(node.getAttribute("month"));
                    int day = Integer.parseInt(node.getAttribute("day"));
                    int hour = Integer.parseInt(node.getAttribute("hour"));
                    int minute = Integer.parseInt(node.getAttribute("minute"));

                    LOGGER.debug("Project file from file, date: {}.{}.{} {}:{}", year, month, day, hour, minute);
                    LOGGER.debug("Project file in controller, date: {}.{}.{} {}:{}",
                            projectInfo.getLastmodified().getYear(),
                            projectInfo.getLastmodified().getMonthWithJanuaryAsOne(),
                            projectInfo.getLastmodified().getDay(), projectInfo.getLastmodified().getHours(),
                            projectInfo.getLastmodified().getMinutes());

                    if (projectInfo.getLastmodified().getYear() == year
                            && projectInfo.getLastmodified().getMonthWithJanuaryAsOne() == month
                            && projectInfo.getLastmodified().getDay() == day
                            && projectInfo.getLastmodified().getHours() == hour
                            && projectInfo.getLastmodified().getMinutes() == minute) {
                        return true;
                    }
                }
            } catch (RuntimeException e) {
                LOGGER.debug("Error occured during project file date comparasion, reason {}.", e.getMessage(), e);
                // There is no documentation available for XML content. This is part of inessential feature, so do
                // nothing, but return false
            }
        }
        return false;
    }

    /**
     * Parse all enum values from IHC project file.
     *
     * @param doc IHC project file in XML format.
     * @return enum dictionary.
     */
    public static Map<Integer, List<IhcEnumValue>> parseEnums(Document doc) {
        Map<Integer, List<IhcEnumValue>> enumDictionary = new HashMap<>();
        if (doc != null) {
            NodeList nodes = doc.getElementsByTagName("enum_definition");

            // iterate enum definitions from project
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);

                int typedefId = Integer.parseInt(element.getAttribute("id").replace("_0x", ""), 16);
                String enumName = element.getAttribute("name");

                List<IhcEnumValue> enumValues = new ArrayList<>();

                NodeList name = element.getElementsByTagName("enum_value");

                for (int j = 0; j < name.getLength(); j++) {
                    Element val = (Element) name.item(j);
                    int id = Integer.parseInt(val.getAttribute("id").replace("_0x", ""), 16);
                    String n = val.getAttribute("name");
                    IhcEnumValue enumVal = new IhcEnumValue(id, n);
                    enumValues.add(enumVal);
                }

                LOGGER.debug("Enum values found: typedefId={}, name={}: {}", typedefId, enumName, enumValues);
                enumDictionary.put(typedefId, enumValues);
            }
        }
        return enumDictionary;
    }
}
