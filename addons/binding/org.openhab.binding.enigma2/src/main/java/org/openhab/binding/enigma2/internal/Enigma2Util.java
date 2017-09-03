/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
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
     * This is a quick and dirty method, it always delivers the first appearance of content in an element
     */
    public static String getContentOfFirstElement(String content, String element) {
        String beginTag = "<" + element + ">";
        String endTag = "</" + element + ">";

        int startIndex = content.indexOf(beginTag) + beginTag.length();
        int endIndex = content.indexOf(endTag);

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

        String content = executeUrl(deviceURL + SUFFIX_ALL_SERVICES);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(content)));
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
        return serviceContainer;
    }

    public static String createUserPasswordHostnamePrefix(String hostName, String userName, String password) {
        String returnString = "http://" + hostName;
        if (!((userName == null) || (userName.length() == 0))) {
            returnString = "http://" + userName + ":" + password + "@" + hostName;
        }
        return returnString;
    }

    public static String cleanString(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = sb.length() - 1; i >= 0; i--) {
            if (!isValidChar(sb.charAt(i))) {
                sb.deleteCharAt(i);
            }
        }
        return sb.toString();
    }

    private static boolean isValidChar(char c) {
        int castChar = c;
        if ((castChar >= 32) && (castChar <= 125)) {
            return true;
        }
        return false;
    }
}
