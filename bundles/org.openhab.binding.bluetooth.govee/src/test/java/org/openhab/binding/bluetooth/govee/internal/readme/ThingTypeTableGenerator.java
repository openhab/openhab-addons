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
package org.openhab.binding.bluetooth.govee.internal.readme;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.govee.internal.GoveeModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class ThingTypeTableGenerator {

    public static void main(String[] args) throws Exception {
        FileInputStream fileIS = new FileInputStream("src/main/resources/OH-INF/thing/thing-types.xml");
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(fileIS);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/*[local-name()='thing-descriptions']/thing-type";
        XPathExpression labelExpression = xPath.compile("label/text()");
        XPathExpression descriptionExpression = xPath.compile("description/text()");

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

        List<ThingTypeData> thingTypeDataList = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            ThingTypeData data = new ThingTypeData();

            data.id = node.getAttributes().getNamedItem("id").getTextContent();
            data.label = (String) labelExpression.evaluate(node, XPathConstants.STRING);
            data.description = (String) descriptionExpression.evaluate(node, XPathConstants.STRING);

            thingTypeDataList.add(data);
        }

        String[] headerRow = new String[] { "Thing Type ID", "Description", "Supported Models" };

        List<String[]> rows = new ArrayList<>();
        rows.add(headerRow);
        rows.addAll(thingTypeDataList.stream().map(ThingTypeTableGenerator::toRow).collect(Collectors.toList()));

        int[] maxColumns = { maxColumnSize(rows, 0), maxColumnSize(rows, 1), maxColumnSize(rows, 2) };

        StringWriter writer = new StringWriter();

        // write actual rows
        rows.forEach(row -> {
            writer.append(writeRow(maxColumns, row, ' ')).append('\n');
            if (row == headerRow) {
                writer.append(writeRow(maxColumns, new String[] { "", "", "" }, '-')).append('\n');
            }
        });

        System.out.println(writer.toString());
    }

    private static String writeRow(int[] maxColumns, String[] row, char paddingChar) {
        String prefix = "|" + paddingChar;
        String infix = paddingChar + "|" + paddingChar;
        String suffix = paddingChar + "|";

        return Stream.of(0, 1, 2).map(i -> rightPad(row[i], maxColumns[i], paddingChar))
                .collect(Collectors.joining(infix, prefix, suffix));
    }

    private static String rightPad(String str, int minLength, char paddingChar) {
        if (str.length() >= minLength) {
            return str;
        }
        StringBuilder builder = new StringBuilder(minLength);
        builder.append(str);
        while (builder.length() < minLength) {
            builder.append(paddingChar);
        }
        return builder.toString();
    }

    private static int maxColumnSize(List<String[]> rows, int column) {
        return rows.stream().map(row -> row[column].length()).max(Integer::compare).get();
    }

    private static class ThingTypeData {
        private @Nullable String id;
        private @Nullable String label;
        private @Nullable String description;
    }

    private static String[] toRow(ThingTypeData data) {
        return new String[] { data.id, //
                data.description, //
                modelsForType(data.id).stream().map(model -> model.name()).collect(Collectors.joining(",")) };
    }

    private static List<GoveeModel> modelsForType(@Nullable String typeUID) {
        return Arrays.stream(GoveeModel.values()).filter(model -> model.getThingTypeUID().getId().equals(typeUID))
                .collect(Collectors.toList());
    }
}
