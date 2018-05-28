/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.projectfile;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class to parse controller's project file information.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcProjectFileParser {

    private final Logger logger = LoggerFactory.getLogger(IhcProjectFileParser.class);
    private Document doc;

    public IhcProjectFileParser(Document doc) {
        this.doc = doc;
    }

    /**
     * Parse IHC / ELKO LS project file.
     *
     */
    public HashMap<Integer, ArrayList<IhcEnumValue>> parseProject() {
        logger.debug("Parsing project file...");

        HashMap<Integer, ArrayList<IhcEnumValue>> enumDictionary = new HashMap<Integer, ArrayList<IhcEnumValue>>();
        NodeList nodes = doc.getElementsByTagName("enum_definition");

        // iterate enum definitions from project
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            // String enumName = element.getAttribute("name");
            int typedefId = Integer.parseInt(element.getAttribute("id").replace("_0x", ""), 16);
            String enum_name = element.getAttribute("name");

            ArrayList<IhcEnumValue> enumValues = new ArrayList<IhcEnumValue>();

            NodeList name = element.getElementsByTagName("enum_value");

            for (int j = 0; j < name.getLength(); j++) {
                Element val = (Element) name.item(j);
                IhcEnumValue enumVal = new IhcEnumValue();
                enumVal.id = Integer.parseInt(val.getAttribute("id").replace("_0x", ""), 16);
                enumVal.name = val.getAttribute("name");
                enumValues.add(enumVal);
            }

            logger.debug("Enum values found for enum id={} ({}): {}", typedefId, enum_name, enumValues);
            enumDictionary.put(typedefId, enumValues);
        }

        return enumDictionary;
    }
}
