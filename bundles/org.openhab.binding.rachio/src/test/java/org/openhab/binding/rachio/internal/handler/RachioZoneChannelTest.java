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
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_PAUSE_TIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_ENABLED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_LAST_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_LAST_UPDATE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_NEXT_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_START_TIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_TYPE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_ZONES;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_ZONE;

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
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.xml.sax.SAXException;

/**
 * Tests zone channel declarations and simple state conversions.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioZoneChannelTest {
    @Test
    void thingXmlFilesAreWellFormed()
            throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        for (String file : List.of("cloud.xml", "device.xml", "zone.xml", "schedule.xml", "flex-schedule.xml",
                "base-station.xml", "valve.xml", "valve-program.xml")) {
            factory.newDocumentBuilder().parse(resource("/OH-INF/thing/" + file).toFile());
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
    void flexScheduleThingXmlDefinesHandlerUpdatedChannels() throws IOException, URISyntaxException {
        String xml = readThingXml("flex-schedule.xml");

        assertThat(xml, containsString("<thing-type id=\"flex-schedule\">"));
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_NAME, "flex-schedule-name");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_ENABLED, "flex-schedule-enabled");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_TYPE, "flex-schedule-type");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_START_TIME, "flex-schedule-start-time");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_LAST_RUN, "flex-schedule-last-run");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_NEXT_RUN, "flex-schedule-next-run");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_ZONES, "flex-schedule-zones");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT, "flex-schedule-seasonal-adjustment");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_START, "schedule-rule-start");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_SKIP, "schedule-rule-skip");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN, "schedule-rule-skip-forward-zone-run");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_LAST_UPDATE, "flex-schedule-last-update");
        assertThat(xml, containsString("<channel-type id=\"flex-schedule-seasonal-adjustment\">"));
        assertThat(xml, containsString("<item-type unitHint=\"one\">Number:Dimensionless</item-type>"));
    }

    @Test
    void publicChannelIdsMatchLowerCaseHyphenBaseline() throws IOException, URISyntaxException {
        assertChannelIds("base-station.xml", "name", "online", "last-update");
        assertChannelIds("device.xml", "name", "active", "online", "paused", "pause-time", "sleep-mode", "stop", "run",
                "run-zones", "runtime", "rain-delay", "rain-sensor-tripped", "active-zone-number", "active-zone-name",
                "active-zone-id", "current-schedule-id", "current-schedule-name", "current-schedule-type",
                "current-schedule-start-time", "current-schedule-end-time", "current-schedule-duration",
                "current-schedule-running", "last-api-event-type", "last-api-event-time", "last-api-event-summary",
                "forecast-summary", "forecast-today-high", "forecast-today-low", "forecast-precipitation",
                "forecast-precipitation-probability", "forecast-wind", "forecast-updated", "last-skip-type",
                "last-skip-schedule-id", "last-skip-start-time", "last-skip-reason", "last-update", "last-event",
                "last-event-time", "schedule-name", "schedule-info", "schedule-start", "schedule-end");
        assertChannelIds("zone.xml", "name", "number", "enabled", "run", "runtime", "run-total", "available-water",
                "image-url", "image", "depth-of-water", "saturated-depth-of-water", "management-allowed-depletion",
                "root-zone-depth", "efficiency", "yard-area-square-feet", "last-watered-date", "fixed-runtime",
                "max-runtime", "runtime-no-multiplier", "schedule-data-modified", "moisture-level", "moisture-percent",
                "last-update", "last-event", "last-event-time");
        assertChannelIds("schedule.xml", "name", "enabled", "type", "start-time", "last-run", "next-run", "zones",
                "seasonal-adjustment", "start", "skip", "skip-forward-zone-run", "last-update");
        assertChannelIds("flex-schedule.xml", "name", "enabled", "type", "start-time", "last-run", "next-run", "zones",
                "seasonal-adjustment", "start", "skip", "skip-forward-zone-run", "last-update");
        assertChannelIds("valve.xml", "name", "online", "run", "runtime", "default-runtime", "state-matches",
                "flow-detected", "battery-level", "serial-number", "last-run-type", "last-end-reason",
                "next-planned-runtime", "next-planned-run-duration", "next-planned-run-program-id",
                "next-planned-run-skipped", "last-completed-runtime", "last-completed-run-duration", "last-run-status",
                "skip-next-planned-run", "cancel-next-planned-run-skip", "last-update", "last-event",
                "last-event-time");
        assertChannelIds("valve-program.xml", "name", "enabled", "program-type", "valve-id", "start-time",
                "next-runtime", "last-runtime", "duration", "days-of-week", "interval-days", "seasonal-adjustment",
                "updated-at", "next-program-run-skipped", "skip-next-planned-run", "cancel-next-planned-run-skip",
                "last-rain-skip-planned-run-start-time", "last-rain-skip-canceled-planned-run-start-time",
                "last-update", "last-event", "last-event-time");
    }

    @Test
    void channelTypeIdsAreUniqueAcrossThingXmlFiles() throws IOException, URISyntaxException {
        Set<String> channelTypeIds = new HashSet<>();
        Pattern pattern = Pattern.compile("<channel-type id=\"([^\"]+)\"");

        for (String file : List.of("cloud.xml", "device.xml", "zone.xml", "schedule.xml", "flex-schedule.xml",
                "base-station.xml", "valve.xml", "valve-program.xml")) {
            Matcher matcher = pattern.matcher(readThingXml(file));
            while (matcher.find()) {
                String channelTypeId = matcher.group(1);
                assertThat(file + " redefines channel-type " + channelTypeId, channelTypeIds.add(channelTypeId),
                        is(true));
            }
        }
    }

    @Test
    void xmlIdsFollowOpenHabNamingConventions() throws IOException, URISyntaxException {
        Pattern lowerCaseHyphenId = Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");
        Pattern lowerCamelCaseId = Pattern.compile("[a-z][A-Za-z0-9]*");

        for (String file : List.of("cloud.xml", "device.xml", "zone.xml", "schedule.xml", "flex-schedule.xml",
                "base-station.xml", "valve.xml", "valve-program.xml")) {
            String xml = readThingXml(file);
            assertMatchingIds(file, xml, Pattern.compile("<channel id=\"([^\"]+)\""), lowerCaseHyphenId);
            assertMatchingIds(file, xml, Pattern.compile("<channel-type id=\"([^\"]+)\""), lowerCaseHyphenId);
            assertMatchingIds(file, xml, Pattern.compile("<(?:thing-type|bridge-type) id=\"([^\"]+)\""),
                    lowerCaseHyphenId);
            assertMatchingIds(file, xml, Pattern.compile("<parameter name=\"([^\"]+)\""), lowerCamelCaseId);
            assertMatchingIds(file, xml, Pattern.compile("<property name=\"([^\"]+)\""), lowerCamelCaseId);
        }
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
    void zoneWaterDepthAndAreaChannelsDefaultToApiUnits() throws IOException, URISyntaxException {
        String xml = readThingXml("zone.xml");

        for (String channelType : List.of("zone-available-water", "zone-depth-of-water",
                "zone-saturated-depth-of-water")) {
            String declaration = channelTypeDeclaration(xml, channelType);
            assertThat(declaration, containsString("<item-type unitHint=\"in\">Number:Length</item-type>"));
            assertThat(declaration, containsString("<state readOnly=\"true\" pattern=\"%.2f %unit%\"/>"));
        }
        String rootZoneDepth = channelTypeDeclaration(xml, "zone-root-zone-depth");
        assertThat(rootZoneDepth, containsString("<item-type unitHint=\"in\">Number:Length</item-type>"));
        assertThat(rootZoneDepth, containsString("<state readOnly=\"true\" pattern=\"%.1f %unit%\"/>"));
        String yardArea = channelTypeDeclaration(xml, "zone-yard-area-square-feet");
        assertThat(yardArea, containsString("<item-type unitHint=\"ft²\">Number:Area</item-type>"));
        assertThat(yardArea, containsString("<state readOnly=\"true\" pattern=\"%.1f %unit%\"/>"));
    }

    @Test
    void zoneMoistureChannelsAreDocumentedAsCommandOnlyAdjustments() throws IOException, URISyntaxException {
        String xml = readThingXml("zone.xml");

        assertThat(xml, containsString("id=\"moisture-level\""));
        assertThat(xml, containsString("id=\"moisture-percent\""));
        assertThat(xml, containsString("Command-only soil moisture adjustment input for zone/setMoistureLevel"));
        assertThat(xml, containsString("Command-only soil moisture adjustment input for zone/setMoisturePercent"));
    }

    @Test
    void deviceThingDeclaresActiveZoneChannels() throws IOException, URISyntaxException {
        String xml = readThingXml("device.xml");

        assertThat(xml, containsString("id=\"active-zone-number\""));
        assertThat(xml, containsString("id=\"active-zone-name\""));
        assertThat(xml, containsString("id=\"active-zone-id\""));
    }

    @Test
    void forecastPrecipitationDeclaresMillimeterDisplayMetadata() throws IOException, URISyntaxException {
        String xml = readThingXml("device.xml");

        assertThat(xml, containsString("<item-type unitHint=\"mm\">Number:Length</item-type>"));
        assertThat(xml, containsString("<state readOnly=\"true\" pattern=\"%.1f %unit%\"/>"));
    }

    @Test
    void representativeChannelsUseLowerCaseHyphenIdsAndTypes() throws IOException, URISyntaxException {
        String deviceXml = readThingXml("device.xml");
        String zoneXml = readThingXml("zone.xml");
        String scheduleXml = readThingXml("schedule.xml");

        assertThat(deviceXml, containsString("<channel id=\"pause-time\" typeId=\"device-pause-time\"/>"));
        assertThat(deviceXml, containsString("<channel id=\"last-update\" typeId=\"last-update\"/>"));
        assertThat(zoneXml, containsString("<channel id=\"runtime\" typeId=\"zone-runtime\"/>"));
        assertThat(scheduleXml,
                containsString("<channel id=\"seasonal-adjustment\" typeId=\"schedule-seasonal-adjustment\"/>"));
    }

    @Test
    void itemChannelUidsUseLowerCaseHyphenIds() {
        ThingUID device = new ThingUID(THING_TYPE_DEVICE, "bridge", "controller");
        ThingUID zone = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        ThingUID schedule = new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule");

        assertThat(new ChannelUID(device, CHANNEL_DEVICE_PAUSE_TIME).getAsString(),
                is("rachio:device:controller:bridge:pause-time"));
        assertThat(new ChannelUID(zone, CHANNEL_ZONE_RUNTIME).getAsString(), is("rachio:zone:zone:bridge:runtime"));
        assertThat(new ChannelUID(schedule, CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT).getAsString(),
                is("rachio:schedule:schedule:bridge:seasonal-adjustment"));
    }

    @Test
    void quantityChannelItemTypesAreDeclared() throws IOException, URISyntaxException {
        String zoneXml = readThingXml("zone.xml");
        String deviceXml = readThingXml("device.xml");
        String valveXml = readThingXml("valve.xml");
        String scheduleXml = readThingXml("schedule.xml");
        String flexScheduleXml = readThingXml("flex-schedule.xml");
        String valveProgramXml = readThingXml("valve-program.xml");

        assertThat(zoneXml, containsString("<item-type unitHint=\"s\">Number:Time</item-type>"));
        assertThat(zoneXml, containsString("<item-type unitHint=\"in\">Number:Length</item-type>"));
        assertThat(zoneXml, containsString("<item-type unitHint=\"ft²\">Number:Area</item-type>"));
        assertThat(zoneXml, containsString("<item-type unitHint=\"mm\">Number:Length</item-type>"));
        assertThat(deviceXml, containsString("<item-type>Number:Temperature</item-type>"));
        assertThat(deviceXml, containsString("<item-type unitHint=\"mm\">Number:Length</item-type>"));
        assertThat(deviceXml, containsString("<item-type unitHint=\"one\">Number:Dimensionless</item-type>"));
        assertThat(deviceXml, containsString("<item-type>Number:Speed</item-type>"));
        assertThat(valveXml, containsString("<item-type unitHint=\"%\">Number:Dimensionless</item-type>"));
        assertThat(scheduleXml, containsString("<item-type unitHint=\"one\">Number:Dimensionless</item-type>"));
        assertThat(flexScheduleXml, containsString("<item-type unitHint=\"one\">Number:Dimensionless</item-type>"));
        assertThat(valveProgramXml, containsString("<item-type unitHint=\"s\">Number:Time</item-type>"));
        assertThat(valveProgramXml, containsString("<item-type unitHint=\"d\">Number:Time</item-type>"));
    }

    @Test
    void fixedUnitQuantityChannelsDeclareUnitHints() throws IOException, URISyntaxException {
        for (String file : List.of("zone.xml", "schedule.xml", "flex-schedule.xml", "valve.xml", "valve-program.xml")) {
            assertAllQuantityChannelsHaveUnitHint(readThingXml(file));
        }

        String deviceXml = readThingXml("device.xml");
        assertAllQuantityChannelsHaveUnitHint(deviceXml.replace("<item-type>Number:Temperature</item-type>", "")
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

    private String channelTypeDeclaration(String xml, String channelType) {
        int start = xml.indexOf("<channel-type id=\"" + channelType + "\">");
        int end = xml.indexOf("</channel-type>", start);
        assertThat(channelType + " declaration exists", start >= 0 && end > start, is(true));
        return xml.substring(start, end);
    }

    private void assertThingTypeVersion(String fileName) throws IOException, URISyntaxException {
        assertThat(readThingXml(fileName), containsString("<property name=\"thingTypeVersion\">1</property>"));
    }

    private void assertChannelIds(String fileName, String... expectedIds) throws IOException, URISyntaxException {
        Matcher matcher = Pattern.compile("<channel id=\"([^\"]+)\"").matcher(readThingXml(fileName));
        List<String> actualIds = matcher.results().map(result -> result.group(1)).toList();
        assertThat(fileName, actualIds, is(List.of(expectedIds)));
    }

    private void assertChannel(String xml, String id, String typeId) {
        assertThat(xml, containsString("<channel id=\"" + id + "\" typeId=\"" + typeId + "\"/>"));
        assertThat(xml, containsString("<channel-type id=\"" + typeId + "\">"));
    }

    private void assertMatchingIds(String fileName, String xml, Pattern idPattern, Pattern conventionPattern) {
        Matcher matcher = idPattern.matcher(xml);
        while (matcher.find()) {
            String id = matcher.group(1);
            assertThat(fileName + " contains invalid ID " + id, conventionPattern.matcher(id).matches(), is(true));
        }
    }
}
