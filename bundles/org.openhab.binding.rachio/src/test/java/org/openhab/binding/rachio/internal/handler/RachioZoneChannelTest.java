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
package org.openhab.binding.rachio.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests zone channel declarations and simple state conversions.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioZoneChannelTest {
    private static final List<String> THING_XML_FILES = List.of("cloud.xml", "device.xml", "zone.xml", "schedule.xml",
            "flex-schedule.xml", "base-station.xml", "valve.xml", "valve-program.xml");

    @Test
    void thingXmlFilesAreWellFormed()
            throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        for (String file : THING_XML_FILES) {
            factory.newDocumentBuilder().parse(resource("/OH-INF/thing/" + file).toFile());
        }
    }

    @Test
    void channelTypeIdsAreUnique() throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Set<String> channelTypeIds = new HashSet<>();

        for (String file : THING_XML_FILES) {
            NodeList channelTypes = factory.newDocumentBuilder().parse(resource("/OH-INF/thing/" + file).toFile())
                    .getElementsByTagNameNS("https://openhab.org/schemas/thing-description/v1.0.0", "channel-type");
            for (int i = 0; i < channelTypes.getLength(); i++) {
                String id = ((Element) channelTypes.item(i)).getAttribute("id");
                assertThat("Duplicate channel type ID '" + id + "' in " + file, channelTypeIds.add(id), is(true));
            }
        }
    }

    @Test
    void thingXmlFilesDeclareThingTypeVersion() throws IOException, URISyntaxException {
        assertThingTypeVersion("device.xml");
        assertThingTypeVersion("zone.xml");
        assertThingTypeVersion("schedule.xml");
        assertThingTypeVersion("flex-schedule.xml");
        assertThingTypeVersion("valve.xml");
        assertThingTypeVersion("valve-program.xml");
    }

    @Test
    void zoneThingDeclaresTelemetryAndImageChannels() throws IOException, URISyntaxException {
        String xml = readThingXml("zone.xml");

        assertThat(xml, containsString("id=\"available-water\""));
        assertThat(xml, containsString("id=\"depth-of-water\""));
        assertThat(xml, containsString("id=\"saturated-depth-of-water\""));
        assertThat(xml, containsString("id=\"management-allowed-depletion\""));
        assertThat(xml, containsString("id=\"root-zone-depth\""));
        assertThat(xml, containsString("id=\"efficiency\""));
        assertThat(xml, containsString("id=\"yard-area-square-feet\""));
        assertThat(xml, containsString("id=\"last-watered-date\""));
        assertThat(xml, containsString("id=\"fixed-runtime\""));
        assertThat(xml, containsString("id=\"max-runtime\""));
        assertThat(xml, containsString("id=\"runtime-no-multiplier\""));
        assertThat(xml, containsString("id=\"schedule-data-modified\""));
        assertThat(xml, containsString("id=\"image\" typeId=\"zone-image\""));
        assertThat(xml, containsString("<item-type>Image</item-type>"));
    }

    @Test
    void deviceThingDeclaresActiveZoneChannels() throws IOException, URISyntaxException {
        String xml = readThingXml("device.xml");

        assertThat(xml, containsString("id=\"active-zone-number\""));
        assertThat(xml, containsString("id=\"active-zone-name\""));
        assertThat(xml, containsString("id=\"active-zone-id\""));
    }

    @Test
    void scheduleThingsDeclareScheduleRuleServiceCommandChannels() throws IOException, URISyntaxException {
        assertScheduleCommandChannels(readThingXml("schedule.xml"));
        assertScheduleCommandChannels(readThingXml("flex-schedule.xml"));
    }

    private void assertScheduleCommandChannels(String xml) {
        assertThat(xml, containsString("<channel id=\"start\" typeId=\"schedule-rule-start\"/>"));
        assertThat(xml, containsString("<channel id=\"skip\" typeId=\"schedule-rule-skip\"/>"));
        assertThat(xml, containsString(
                "<channel id=\"skip-forward-zone-run\" typeId=\"schedule-rule-skip-forward-zone-run\"/>"));
        assertThat(xml,
                containsString("<channel id=\"seasonal-adjustment\" typeId=\"schedule-rule-seasonal-adjustment\"/>"));
    }

    @Test
    void quantityChannelItemTypesAreDeclared() throws IOException, URISyntaxException {
        String zoneXml = readThingXml("zone.xml");
        String deviceXml = readThingXml("device.xml");
        String valveXml = readThingXml("valve.xml");
        String scheduleXml = readThingXml("schedule.xml");
        String valveProgramXml = readThingXml("valve-program.xml");

        assertThat(zoneXml, containsString("<item-type unitHint=\"s\">Number:Time</item-type>"));
        assertThat(zoneXml, containsString("<item-type unitHint=\"in\">Number:Length</item-type>"));
        assertThat(zoneXml, containsString("<item-type unitHint=\"ft²\">Number:Area</item-type>"));
        assertThat(zoneXml, containsString("<item-type unitHint=\"mm\">Number:Length</item-type>"));
        assertThat(deviceXml, containsString("<item-type>Number:Temperature</item-type>"));
        assertThat(deviceXml, containsString("<item-type>Number:Length</item-type>"));
        assertThat(deviceXml, containsString("<item-type unitHint=\"one\">Number:Dimensionless</item-type>"));
        assertThat(deviceXml, containsString("<item-type>Number:Speed</item-type>"));
        assertThat(valveXml, containsString("<item-type unitHint=\"%\">Number:Dimensionless</item-type>"));
        assertThat(scheduleXml, containsString("<item-type unitHint=\"one\">Number:Dimensionless</item-type>"));
        assertThat(valveProgramXml, containsString("<item-type unitHint=\"s\">Number:Time</item-type>"));
        assertThat(valveProgramXml, containsString("<item-type unitHint=\"d\">Number:Time</item-type>"));
    }

    @Test
    void fixedUnitQuantityChannelsDeclareUnitHints() throws IOException, URISyntaxException {
        for (String file : List.of("zone.xml", "schedule.xml", "valve.xml", "valve-program.xml")) {
            assertAllQuantityChannelsHaveUnitHint(readThingXml(file));
        }

        String deviceXml = readThingXml("device.xml");
        assertAllQuantityChannelsHaveUnitHint(deviceXml.replace("<item-type>Number:Temperature</item-type>", "")
                .replace("<item-type>Number:Length</item-type>", "")
                .replace("<item-type>Number:Speed</item-type>", ""));
    }

    @Test
    void semanticTagsAreDeclaredForReviewReadyDefaults() throws IOException, URISyntaxException {
        String zoneXml = readThingXml("zone.xml");
        String valveXml = readThingXml("valve.xml");
        String cloudXml = readThingXml("cloud.xml");

        assertThat(zoneXml, containsString("<semantic-equipment-tag>Irrigation</semantic-equipment-tag>"));
        assertThat(zoneXml, containsString("<tag>Measurement</tag>"));
        assertThat(zoneXml, containsString("<tag>Water</tag>"));
        assertThat(valveXml, containsString("<tag>StateOfCharge</tag>"));
        assertThat(cloudXml, containsString("<semantic-equipment-tag>WebService</semantic-equipment-tag>"));
    }

    @Test
    void missingLastWateredDateIsPublishedAsNull() {
        assertThat(RachioZoneHandler.epochMillisOrNull(-1), is(UnDefType.NULL));
    }

    @Test
    void validLastWateredDateIsPublishedAsDateTime() {
        State state = RachioZoneHandler.epochMillisOrNull(1_523_129_743_000L);

        assertThat(state, instanceOf(DateTimeType.class));
    }

    private String readThingXml(String fileName) throws IOException, URISyntaxException {
        return readResource("/OH-INF/thing/" + fileName);
    }

    private String readResource(String resourcePath) throws IOException, URISyntaxException {
        return Files.readString(resource(resourcePath), StandardCharsets.UTF_8);
    }

    private Path resource(String resourcePath) throws URISyntaxException {
        return Path.of(Objects.requireNonNull(getClass().getResource(resourcePath)).toURI());
    }

    private void assertAllQuantityChannelsHaveUnitHint(String xml) {
        Matcher matcher = Pattern.compile("<item-type(?![^>]*unitHint)[^>]*>Number:[^<]+</item-type>").matcher(xml);
        assertThat(matcher.find(), is(false));
    }

    private void assertThingTypeVersion(String fileName) throws IOException, URISyntaxException {
        assertThat(readThingXml(fileName), containsString("<property name=\"thingTypeVersion\">1</property>"));
    }
}
