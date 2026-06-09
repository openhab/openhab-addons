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
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_RUN_TIME;
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
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_NAME, "flex_schedule_name");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_ENABLED, "flex_schedule_enabled");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_TYPE, "flex_schedule_type");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_START_TIME, "flex_schedule_start_time");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_LAST_RUN, "flex_schedule_last_run");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_NEXT_RUN, "flex_schedule_next_run");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_ZONES, "flex_schedule_zones");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT, "flex_schedule_seasonal_adjustment");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_START, "schedule-rule-start");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_SKIP, "schedule-rule-skip");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN, "schedule-rule-skip-forward-zone-run");
        assertChannel(xml, CHANNEL_FLEX_SCHEDULE_LAST_UPDATE, "flex_schedule_last_update");
        assertThat(xml, containsString("<channel-type id=\"flex_schedule_seasonal_adjustment\">"));
        assertThat(xml, containsString("<item-type unitHint=\"one\">Number:Dimensionless</item-type>"));
    }

    @Test
    void publicChannelIdsMatchWorkingBaselineWithFlexCommands() throws IOException, URISyntaxException {
        assertChannelIds("base-station.xml", "name", "online", "lastUpdate");
        assertChannelIds("device.xml", "name", "active", "online", "paused", "pauseTime", "sleepMode", "stop", "run",
                "runZones", "runTime", "rainDelay", "rainSensorTripped", "activeZoneNumber", "activeZoneName",
                "activeZoneId", "currentScheduleId", "currentScheduleName", "currentScheduleType",
                "currentScheduleStartTime", "currentScheduleEndTime", "currentScheduleDuration",
                "currentScheduleRunning", "lastApiEventType", "lastApiEventTime", "lastApiEventSummary",
                "forecastSummary", "forecastTodayHigh", "forecastTodayLow", "forecastPrecipitation",
                "forecastPrecipitationProbability", "forecastWind", "forecastUpdated", "lastSkipType",
                "lastSkipScheduleId", "lastSkipStartTime", "lastSkipReason", "lastUpdate", "lastEvent", "lastEventTime",
                "scheduleName", "scheduleInfo", "scheduleStart", "scheduleEnd");
        assertChannelIds("zone.xml", "name", "number", "enabled", "run", "runTime", "runTotal", "availableWater",
                "imageUrl", "image", "depthOfWater", "saturatedDepthOfWater", "managementAllowedDepletion",
                "rootZoneDepth", "efficiency", "yardAreaSquareFeet", "lastWateredDate", "fixedRuntime", "maxRuntime",
                "runtimeNoMultiplier", "scheduleDataModified", "moistureLevel", "moisturePercent", "lastUpdate",
                "lastEvent", "lastEventTime");
        assertChannelIds("schedule.xml", "name", "enabled", "type", "startTime", "lastRun", "nextRun", "zones",
                "seasonalAdjustment", "start", "skip", "skipForwardZoneRun", "lastUpdate");
        assertChannelIds("flex-schedule.xml", "name", "enabled", "type", "start-time", "last-run", "next-run", "zones",
                "seasonal-adjustment", "start", "skip", "skip-forward-zone-run", "last-update");
        assertChannelIds("valve.xml", "name", "online", "run", "runTime", "defaultRuntime", "stateMatches",
                "flowDetected", "batteryLevel", "serialNumber", "lastRunType", "lastEndReason", "nextPlannedRunTime",
                "nextPlannedRunDuration", "nextPlannedRunProgramId", "nextPlannedRunSkipped", "lastCompletedRunTime",
                "lastCompletedRunDuration", "lastRunStatus", "skipNextPlannedRun", "cancelNextPlannedRunSkip",
                "lastUpdate", "lastEvent", "lastEventTime");
        assertChannelIds("valve-program.xml", "name", "enabled", "programType", "valveId", "startTime", "nextRunTime",
                "lastRunTime", "duration", "daysOfWeek", "intervalDays", "seasonalAdjustment", "updatedAt",
                "nextProgramRunSkipped", "skipNextPlannedRun", "cancelNextPlannedRunSkip",
                "lastRainSkipPlannedRunStartTime", "lastRainSkipCanceledPlannedRunStartTime", "lastUpdate", "lastEvent",
                "lastEventTime");
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
    void zoneThingDeclaresTelemetryAndImageChannels() throws IOException, URISyntaxException {
        String xml = readThingXml("zone.xml");

        assertThat(xml, containsString("id=\"availableWater\""));
        assertThat(xml, containsString("id=\"depthOfWater\""));
        assertThat(xml, containsString("id=\"saturatedDepthOfWater\""));
        assertThat(xml, containsString("id=\"managementAllowedDepletion\""));
        assertThat(xml, containsString("id=\"rootZoneDepth\""));
        assertThat(xml, containsString("id=\"efficiency\""));
        assertThat(xml, containsString("id=\"yardAreaSquareFeet\""));
        assertThat(xml, containsString("id=\"lastWateredDate\""));
        assertThat(xml, containsString("id=\"fixedRuntime\""));
        assertThat(xml, containsString("id=\"maxRuntime\""));
        assertThat(xml, containsString("id=\"runtimeNoMultiplier\""));
        assertThat(xml, containsString("id=\"scheduleDataModified\""));
        assertThat(xml, containsString("id=\"image\" typeId=\"zone_image\""));
        assertThat(xml, containsString("<item-type>Image</item-type>"));
    }

    @Test
    void zoneMoistureChannelsAreDocumentedAsCommandOnlyAdjustments() throws IOException, URISyntaxException {
        String xml = readThingXml("zone.xml");

        assertThat(xml, containsString("id=\"moistureLevel\""));
        assertThat(xml, containsString("id=\"moisturePercent\""));
        assertThat(xml, containsString("Command-only soil moisture adjustment input for zone/setMoistureLevel"));
        assertThat(xml, containsString("Command-only soil moisture adjustment input for zone/setMoisturePercent"));
    }

    @Test
    void deviceThingDeclaresActiveZoneChannels() throws IOException, URISyntaxException {
        String xml = readThingXml("device.xml");

        assertThat(xml, containsString("id=\"activeZoneNumber\""));
        assertThat(xml, containsString("id=\"activeZoneName\""));
        assertThat(xml, containsString("id=\"activeZoneId\""));
    }

    @Test
    void existingDeviceZoneAndScheduleChannelIdsRemainCompatible() throws IOException, URISyntaxException {
        String deviceXml = readThingXml("device.xml");
        String zoneXml = readThingXml("zone.xml");
        String scheduleXml = readThingXml("schedule.xml");

        assertThat(deviceXml, containsString("<channel id=\"pauseTime\" typeId=\"dev_pauseTime\"/>"));
        assertThat(deviceXml, containsString("<channel id=\"lastUpdate\" typeId=\"lastUpdate\"/>"));
        assertThat(zoneXml, containsString("<channel id=\"runTime\" typeId=\"zone_runTime\"/>"));
        assertThat(scheduleXml,
                containsString("<channel id=\"seasonalAdjustment\" typeId=\"schedule_seasonalAdjustment\"/>"));
    }

    @Test
    void existingItemChannelUidsRetainPublishedChannelIds() {
        ThingUID device = new ThingUID(THING_TYPE_DEVICE, "bridge", "controller");
        ThingUID zone = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        ThingUID schedule = new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule");

        assertThat(new ChannelUID(device, CHANNEL_DEVICE_PAUSE_TIME).getAsString(),
                is("rachio:device:controller:bridge:pauseTime"));
        assertThat(new ChannelUID(zone, CHANNEL_ZONE_RUN_TIME).getAsString(), is("rachio:zone:zone:bridge:runTime"));
        assertThat(new ChannelUID(schedule, CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT).getAsString(),
                is("rachio:schedule:schedule:bridge:seasonalAdjustment"));
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
        assertThat(deviceXml, containsString("<item-type>Number:Length</item-type>"));
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

    private void assertChannelIds(String fileName, String... expectedIds) throws IOException, URISyntaxException {
        Matcher matcher = Pattern.compile("<channel id=\"([^\"]+)\"").matcher(readThingXml(fileName));
        List<String> actualIds = matcher.results().map(result -> result.group(1)).toList();
        assertThat(fileName, actualIds, is(List.of(expectedIds)));
    }

    private void assertChannel(String xml, String id, String typeId) {
        assertThat(xml, containsString("<channel id=\"" + id + "\" typeId=\"" + typeId + "\"/>"));
        assertThat(xml, containsString("<channel-type id=\"" + typeId + "\">"));
    }
}
