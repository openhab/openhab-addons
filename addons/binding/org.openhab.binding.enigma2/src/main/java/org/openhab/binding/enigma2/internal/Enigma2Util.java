/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link Enigma2Util} is a static helper class, to get infos from a XML
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Enigma2Util {

    private static final String SUFFIX_ALL_SERVICES = "/web/getallservices";

    /**
     * Finds the content in an element
     *
     * This is a quick and dirty methode, it only works if element only existis once in content
     */
    public static String getContentOfElement(String content, String element) {
        final String beginTag = "<" + element + ">";
        final String endTag = "</" + element + ">";

        final int startIndex = content.indexOf(beginTag) + beginTag.length();
        final int endIndex = content.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            return content.substring(startIndex, endIndex);
        } else {
            return null;
        }
    }

    /**
     * Executes an URL and returns to answer
     */
    public static String executeUrl(String url) throws IOException {
        return HttpUtil.executeUrl("GET", url, 5000);
    }

    /**
     * Scans "http://enigma2/web/getallservices" and generates map
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Enigma2ServiceContainer generateServiceMaps(String deviceURL)
            throws IOException, ParserConfigurationException, SAXException {
        Enigma2ServiceContainer serviceContainer = new Enigma2ServiceContainer();

        File inputFile = new File("services.xml");
        BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile));

        String content = executeUrl(deviceURL + SUFFIX_ALL_SERVICES);
        writer.write(content);
        writer.close();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        NodeList listOfBouquets = doc.getElementsByTagName("e2bouquet");
        for (int bouquetIndex = 0; bouquetIndex < listOfBouquets.getLength(); bouquetIndex++) {
            NodeList listOfServices = doc.getElementsByTagName("e2servicelist");
            for (int serviceIndex = 0; serviceIndex < listOfServices.getLength(); serviceIndex++) {
                NodeList serviceList = doc.getElementsByTagName("e2service");
                for (int i = 0; i < serviceList.getLength(); i++) {
                    Node service = serviceList.item(i);
                    if (service.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) service;
                        String e2servicereference = eElement.getElementsByTagName("e2servicereference").item(0)
                                .getTextContent();
                        String e2servicename = eElement.getElementsByTagName("e2servicename").item(0).getTextContent();

                        serviceContainer.add(e2servicename, e2servicereference);
                    }
                }
            }
        }
        inputFile.delete();
        return serviceContainer;
    }

    public static String createUserPasswordHostnamePrefix(String hostName, String userName, String password) {
        String returnString;
        if ((userName == null) || (userName.length() == 0)) {
            returnString = new StringBuffer("http://" + hostName).toString();
        } else {
            returnString = new StringBuffer("http://" + userName).append(":").append(password).append("@")
                    .append(hostName).toString();
        }
        return returnString;
    }
}
