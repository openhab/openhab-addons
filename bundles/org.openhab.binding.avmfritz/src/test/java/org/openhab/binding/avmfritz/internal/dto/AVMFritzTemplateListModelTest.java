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
package org.openhab.binding.avmfritz.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.avmfritz.internal.dto.templates.TemplateListModel;
import org.openhab.binding.avmfritz.internal.dto.templates.TemplateModel;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;

/**
 * Tests for {@link TemplateListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class AVMFritzTemplateListModelTest {

    private @NonNullByDefault({}) TemplateListModel templates;

    @SuppressWarnings("null")
    @BeforeEach
    public void setUp() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<templatelist version=\"1\">" +
                    "<template identifier=\"tmpXXXXX-39DC738C5\" id=\"30103\" functionbitmask=\"6784\" applymask=\"64\"><name>Test template #1</name><devices><device identifier=\"YY:5D:AA-900\" /><device identifier=\"XX:5D:AA-900\" /></devices><applymask><relay_automatic /></applymask></template>" +
                    "<template identifier=\"tmpXXXXX-39722FC0F\" id=\"30003\" functionbitmask=\"6784\" applymask=\"64\"><name>Test template #2</name><devices><device identifier=\"YY:5D:AA-900\" /></devices><applymask><relay_automatic /></applymask></template>" +
                "</templatelist>";
        //@formatter:on
        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_TEMPLATES.createUnmarshaller();
        templates = u.unmarshal(xsr, TemplateListModel.class).getValue();
    }

    @Test
    public void validateDeviceListModel() {
        assertNotNull(templates);
        assertEquals(2, templates.getTemplates().size());
        assertEquals("1", templates.getVersion());
    }

    @Test
    public void validateTemplate1() {
        Optional<TemplateModel> optionalTemplate = findModelByIdentifier("tmpXXXXX-39DC738C5");
        assertTrue(optionalTemplate.isPresent());
        assertTrue(optionalTemplate.get() instanceof TemplateModel);

        TemplateModel template = optionalTemplate.get();
        assertEquals("30103", template.getTemplateId());
        assertEquals("Test template #1", template.getName());

        assertEquals(2, template.getDeviceList().getDevices().size());
    }

    @Test
    public void validateTemplate2() {
        Optional<TemplateModel> optionalTemplate = findModelByIdentifier("tmpXXXXX-39722FC0F");
        assertTrue(optionalTemplate.isPresent());
        assertTrue(optionalTemplate.get() instanceof TemplateModel);

        TemplateModel template = optionalTemplate.get();
        assertEquals("30003", template.getTemplateId());
        assertEquals("Test template #2", template.getName());

        assertEquals(1, template.getDeviceList().getDevices().size());
    }

    private Optional<TemplateModel> findModelByIdentifier(String identifier) {
        return templates.getTemplates().stream().filter(it -> identifier.equals(it.getIdentifier())).findFirst();
    }
}
