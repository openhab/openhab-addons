/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.feature.xml.converter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.DoubleKeyMap;
import org.openhab.binding.homeconnectdirect.internal.common.xml.converter.AbstractConverter;
import org.openhab.binding.homeconnectdirect.internal.service.feature.model.EnumDescription;
import org.openhab.binding.homeconnectdirect.internal.service.feature.model.FeatureMapping;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * XStream converter for feature mapping XML.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class FeatureMappingConverter extends AbstractConverter<FeatureMapping, Void> {

    @Override
    public FeatureMapping process(HierarchicalStreamReader reader, @Nullable Void contextObject) {
        Map<Integer, String> featureMap = new HashMap<>();
        Map<Integer, String> errorMap = new HashMap<>();
        DoubleKeyMap<Integer, String, EnumDescription> enums = new DoubleKeyMap<>();

        read(reader, featureMap, errorMap, enums);
        return new FeatureMapping(featureMap, errorMap, enums);
    }

    private void read(HierarchicalStreamReader reader, Map<Integer, String> featureMap, Map<Integer, String> errorMap,
            DoubleKeyMap<Integer, String, EnumDescription> enums) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();

            if ("featureDescription".equals(nodeName)) {
                read(reader, featureMap, errorMap, enums);
            } else if ("feature".equals(nodeName)) {
                var uid = mapHexId(reader.getAttribute("refUID"));
                var value = reader.getValue();
                featureMap.put(uid, value);
            } else if ("errorDescription".equals(nodeName)) {
                read(reader, featureMap, errorMap, enums);
            } else if ("error".equals(nodeName)) {
                var id = mapHexId(reader.getAttribute("refEID"));
                var value = reader.getValue();
                errorMap.put(id, value);
            } else if ("enumDescriptionList".equals(nodeName)) {
                read(reader, featureMap, errorMap, enums);
            } else if ("enumDescription".equals(nodeName)) {
                var id = mapHexId(reader.getAttribute("refENID"));
                var key = reader.getAttribute("enumKey");
                var values = new HashMap<Integer, String>();

                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if ("enumMember".equals(reader.getNodeName())) {
                        var enumValue = mapIntegerNullable(reader.getAttribute("refValue"));
                        var mappingValue = reader.getValue();
                        if (enumValue != null) {
                            values.put(enumValue, mappingValue);
                        }
                    }
                    reader.moveUp();
                }

                var enumDescription = new EnumDescription(id, key, values);
                enums.put(id, key, enumDescription);
            }

            reader.moveUp();
        }
    }
}
