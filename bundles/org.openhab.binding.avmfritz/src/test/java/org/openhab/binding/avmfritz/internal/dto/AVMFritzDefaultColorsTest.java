/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link DeviceListModel}.
 *
 * @author Joshua Bacher -  Initial contribution
 */
@NonNullByDefault
public class AVMFritzDefaultColorsTest {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzDefaultColorsTest.class);

    private @NonNullByDefault({})
    ColorDefaultsModel defaultColors;

    @BeforeEach
    public void setUp() {
        String xml = "<colordefaults>\n" +
                "<hsdefaults>\n" +
                "<hs hue_index=\"1\">\n" +
                " <name enum=\"5569\">Rot</name>\n" +
                " <color sat_index=\"1\" hue=\"358\" sat=\"179\" val=\"227\"/>\n" +
                " <color sat_index=\"2\" hue=\"358\" sat=\"112\" val=\"237\"/>\n" +
                " <color sat_index=\"3\" hue=\"358\" sat=\"54\" val=\"245\"/>\n" +
                "</hs>\n" +
                "<hs hue_index=\"2\">\n" +
                " <name enum=\"5570\">Orange</name>\n" +
                " <color sat_index=\"1\" hue=\"35\" sat=\"214\" val=\"255\"/>\n" +
                " <color sat_index=\"2\" hue=\"35\" sat=\"140\" val=\"255\"/>\n" +
                " <color sat_index=\"3\" hue=\"35\" sat=\"72\" val=\"255\"/>\n" +
                "</hs>\n" +
                "<hs hue_index=\"3\"><name enum=\"5571\">Gelb</name><color sat_index=\"1\" hue=\"52\" sat=\"153\"\n" +
                "val=\"252\"/><color sat_index=\"2\" hue=\"52\" sat=\"102\" val=\"252\"/><color sat_index=\"3\" hue=\"52\"\n" +
                "sat=\"51\" val=\"255\"/></hs>\n" +
                "<hs hue_index=\"4\"><name enum=\"5572\">Grasgrün</name><color sat_index=\"1\" hue=\"92\"\n" +
                "sat=\"123\" val=\"248\"/><color sat_index=\"2\" hue=\"92\" sat=\"79\" val=\"250\"/><color sat_index=\"3\"\n" +
                "hue=\"92\" sat=\"38\" val=\"252\"/></hs>\n" +
                "<hs hue_index=\"5\"><name enum=\"5573\">Grün</name><color sat_index=\"1\" hue=\"107\" sat=\"130\"\n" +
                "val=\"220\"/><color sat_index=\"2\" hue=\"107\" sat=\"82\" val=\"232\"/><color sat_index=\"3\" hue=\"107\"\n" +
                "sat=\"38\" val=\"242\"/></hs>\n" +
                "<hs hue_index=\"6\"><name enum=\"5574\">Türkis</name><color sat_index=\"1\" hue=\"155\" sat=\"133\"\n" +
                "val=\"235\"/><color sat_index=\"2\" hue=\"155\" sat=\"84\" val=\"242\"/><color sat_index=\"3\" hue=\"155\"\n" +
                "sat=\"41\" val=\"248\"/></hs>\n" +
                "<hs hue_index=\"7\"><name enum=\"5575\">Cyan</name><color sat_index=\"1\" hue=\"191\" sat=\"179\"\n" +
                "val=\"255\"/><color sat_index=\"2\" hue=\"191\" sat=\"118\" val=\"255\"/><color sat_index=\"3\" hue=\"191\"\n" +
                "sat=\"59\" val=\"255\"/></hs>\n" +
                "<hs hue_index=\"8\"><name enum=\"5576\">Himmelblau</name><color sat_index=\"1\" hue=\"218\"\n" +
                "sat=\"169\" val=\"252\"/><color sat_index=\"2\" hue=\"218\" sat=\"110\" val=\"252\"/><color sat_index=\"3\"\n" +
                "hue=\"218\" sat=\"56\" val=\"255\"/></hs>\n" +
                "<hs hue_index=\"9\"><name enum=\"5577\">Blau</name><color sat_index=\"1\" hue=\"225\" sat=\"204\"\n" +
                "val=\"255\"/><color sat_index=\"2\" hue=\"225\" sat=\"135\" val=\"255\"/><color sat_index=\"3\" hue=\"225\"\n" +
                "sat=\"67\" val=\"255\"/></hs>\n" +
                "<hs hue_index=\"10\"><name enum=\"5578\">Violett</name><color sat_index=\"1\" hue=\"266\" sat=\"169\"\n" +
                "val=\"250\"/><color sat_index=\"2\" hue=\"266\" sat=\"110\" val=\"250\"/><color sat_index=\"3\" hue=\"266\"\n" +
                "sat=\"54\" val=\"252\"/></hs>\n" +
                "<hs hue_index=\"11\"><name enum=\"5579\">Magenta</name><color sat_index=\"1\" hue=\"296\"\n" +
                "sat=\"140\" val=\"250\"/><color sat_index=\"2\" hue=\"296\" sat=\"92\" val=\"252\"/><color sat_index=\"3\"\n" +
                "hue=\"296\" sat=\"46\" val=\"255\"/></hs>\n" +
                "<hs hue_index=\"12\"><name enum=\"5580\">Pink</name><color sat_index=\"1\" hue=\"335\" sat=\"163\"\n" +
                "val=\"242\"/><color sat_index=\"2\" hue=\"335\" sat=\"107\" val=\"248\"/><color sat_index=\"3\" hue=\"335\"\n" +
                "sat=\"51\" val=\"250\"/></hs>\n" +
                "</hsdefaults>\n" +
                "<temperaturedefaults><temp value=\"2700\" /><temp value=\"3000\" /><temp value=\"3400\" /><temp\n" +
                "value=\"3800\" /><temp value=\"4200\" /><temp value=\"4700\" /><temp value=\"5300\" /><temp\n" +
                "value=\"5900\" /><temp value=\"6500\" />\n" +
                "</temperaturedefaults>\n" +
                "</colordefaults>\n";

        try {
            Unmarshaller u = JAXBUtils.JAXBCONTEXT_COLORS.createUnmarshaller();
            defaultColors = (ColorDefaultsModel) u.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
        }
    }

    @Test
    public void validateColorDefaultModels() {
        assertNotNull(defaultColors);
        assertEquals(12, defaultColors.hsDefaults.hueSaturationModelList.size());
        defaultColors.hsDefaults.hueSaturationModelList.stream().map(el ->
                DynamicTest.dynamicTest("testing if empty", () -> {
                    assertNotNull(el.colorDescriptionModels);
                    assertNotNull(el.colorNameModel);
                    assertNotNull(el.colorNameModel);
                })
        );
        HueSaturationModel sel = defaultColors.hsDefaults.hueSaturationModelList.stream().filter(el -> el.index == 11).findAny().orElseThrow();
        assertEquals("Magenta", sel.colorNameModel.name);
        assertEquals(3, sel.colorDescriptionModels.size());
        assertNotNull(defaultColors.temperatureDefaultsModel.temperatureModelList);
        assertEquals(9, defaultColors.temperatureDefaultsModel.temperatureModelList.size());
        defaultColors.temperatureDefaultsModel.temperatureModelList.stream().map(
                el -> DynamicTest.dynamicTest("testing if null", () -> assertNotNull(el))
        ).count(); // count forces every el in stream to be evaluated.
    }

}
