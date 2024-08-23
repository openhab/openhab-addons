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
package org.openhab.binding.networkupstools.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.CoreItemFactory;

/**
 * Test class that reads the README.md and matches it with the OH-INF thing channel definitions.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class NutNameChannelsTest {

    private static final String THING_TYPES_XML = "thing-types.xml";
    private static final String CHANNELS_XML = "channels.xml";

    private static final int EXPECTED_NUMBER_OF_CHANNELS = 21;
    private static final int EXPECTED_NUMMBER_OF_CHANNEL_XML_LINES = EXPECTED_NUMBER_OF_CHANNELS * 6;

    // README table is: | Channel Name | Item Type | Unit | Description | Advanced
    private static final Pattern README_PATTERN = Pattern
            .compile("^\\|\\s+([\\w\\.]+)\\s+\\|\\s+([:\\w]+)\\s+\\|\\s+([^\\|]+)\\|\\s+([^\\|]+)\\|\\s+([^\\s]+)");
    private static final Pattern CHANNEL_PATTERN = Pattern.compile("<channel id");
    private static final Pattern CHANNEL_TYPE_PATTERN = Pattern
            .compile("(<channel-type|<item-type|<label|<description|<state|</channel-type)");

    private static final String TEMPLATE_CHANNEL_TYPE = "<channel-type id=\"%s\"%s>";
    private static final String TEMPLATE_ADVANCED = " advanced=\"true\"";
    private static final String TEMPLATE_ITEM_TYPE = "<item-type>%s</item-type>";
    private static final String TEMPLATE_LABEL = "<label>%s</label>";
    private static final String TEMPLATE_DESCRIPTION = "<description>%s</description>";
    private static final String TEMPLATE_STATE = "<state pattern=\"%s\" readOnly=\"true\"/>";
    private static final String TEMPLATE_STATE_NO_PATTERN = "<state readOnly=\"true\"/>";
    private static final String TEMPLATE_STATE_OPTIONS = "<state readOnly=\"true\">";
    private static final String TEMPLATE_CHANNEL_TYPE_END = "</channel-type>";
    private static final String TEMPLATE_CHANNEL = "<channel id=\"%s\" typeId=\"%s\"/>";

    private static final String README_IS_ADVANCED = "yes";

    /**
     * Test if README matches with the channels in the things xml.
     */
    @Test
    public void testReadmeMatchingChannels() {
        final Map<NutName, String> readMeNutNames = readReadme();
        final List<String> list = new ArrayList<>();

        for (final Entry<NutName, String> entry : readMeNutNames.entrySet()) {
            final Matcher matcher = README_PATTERN.matcher(entry.getValue());

            assertNotNull(entry.getKey(), "Could not find NutName in readme for : " + entry.getValue());
            if (matcher.find()) {
                list.add(String.format(TEMPLATE_CHANNEL, entry.getKey().getChannelId(),
                        nutNameToChannelType(entry.getKey())));
            } else {
                fail("Could not match line from readme: " + entry.getValue());
            }
        }
        assertThat("Expected number created channels from readme doesn't match with source code", list.size(),
                is(EXPECTED_NUMBER_OF_CHANNELS));
        final List<String> channelsFromXml = readThingsXml(CHANNEL_PATTERN, THING_TYPES_XML);
        final List<String> channelsFromReadme = list.stream().map(String::trim).sorted().collect(Collectors.toList());
        for (int i = 0; i < channelsFromXml.size(); i++) {
            assertThat(channelsFromXml.get(i), is(channelsFromReadme.get(i)));
        }
    }

    /**
     * Test is the channel-type matches with the description in the README.
     * This test is a little verbose as it generates the channel-type description as in the xml is specified.
     * This is for easy adding more channels, by simply adding them to the readme and copy-paste the generated xml to
     * the channels xml.
     */
    @Test
    public void testNutNameMatchingReadme() {
        final Map<NutName, String> readMeNutNames = readReadme();
        final List<String> list = new ArrayList<>();

        for (final NutName nn : NutName.values()) {
            buildChannel(list, nn, readMeNutNames.get(nn));
        }
        assertThat("Expected number created channel data from readme doesn't match with source code", list.size(),
                is(EXPECTED_NUMMBER_OF_CHANNEL_XML_LINES));
        final List<String> channelsFromXml = readThingsXml(CHANNEL_TYPE_PATTERN, CHANNELS_XML);
        final List<String> channelsFromReadme = list.stream().map(String::trim).sorted().collect(Collectors.toList());

        for (int i = 0; i < channelsFromXml.size(); i++) {
            assertThat(channelsFromXml.get(i), is(channelsFromReadme.get(i)));
        }
    }

    private Map<NutName, String> readReadme() {
        try {
            final String path = Path.of(getClass().getProtectionDomain().getClassLoader().getResource(".").toURI())
                    .toString();
            final List<String> lines = Files.readAllLines(Path.of(path, "..", "..", "README.md"));

            return lines.stream().filter(line -> README_PATTERN.matcher(line).find())
                    .collect(Collectors.toMap(this::lineToNutName, Function.identity()));
        } catch (final IOException | URISyntaxException e) {
            fail("Could not read README.md");
            return null;
        }
    }

    private List<String> readThingsXml(final Pattern pattern, final String filename) {
        try {
            final String path = Path.of(getClass().getProtectionDomain().getClassLoader().getResource(".").toURI())
                    .toString();
            final List<String> lines = Files
                    .readAllLines(Path.of(path, "..", "..", "src", "main", "resources", "OH-INF", "thing", filename));
            return lines.stream().filter(line -> pattern.matcher(line).find()).map(String::trim).sorted()
                    .collect(Collectors.toList());
        } catch (final IOException | URISyntaxException e) {
            fail("Could not read things xml");
            return null;
        }
    }

    private NutName lineToNutName(final String line) {
        final Matcher matcher = README_PATTERN.matcher(line);
        assertTrue(matcher.find(), "Could not match readme line: " + line);
        final String name = matcher.group(1);
        final NutName channelIdToNutName = NutName.channelIdToNutName(name);
        assertNotNull(channelIdToNutName, "Name should not match null: '" + name + "' ->" + line);
        return channelIdToNutName;
    }

    private void buildChannel(final List<String> list, final NutName nn, final String readmeLine) {
        if (readmeLine == null) {
            fail("Readme line is null for: " + nn);
        } else {
            final Matcher matcher = README_PATTERN.matcher(readmeLine);

            if (matcher.find()) {
                final String advanced = README_IS_ADVANCED.equals(matcher.group(5)) ? TEMPLATE_ADVANCED : "";

                list.add(String.format(TEMPLATE_CHANNEL_TYPE, nutNameToChannelType(nn), advanced));
                final String itemType = matcher.group(2);

                list.add(String.format(TEMPLATE_ITEM_TYPE, itemType));
                list.add(String.format(TEMPLATE_LABEL, nutNameToLabel(nn)));
                list.add(String.format(TEMPLATE_DESCRIPTION, matcher.group(4).trim()));
                final String pattern = nutNameToPattern(itemType);

                list.add(pattern.isEmpty()
                        ? NutName.UPS_STATUS == nn ? TEMPLATE_STATE_OPTIONS : TEMPLATE_STATE_NO_PATTERN
                        : String.format(TEMPLATE_STATE, pattern));
            } else {
                fail("Could not parse the line from README:" + readmeLine);
            }
            list.add(TEMPLATE_CHANNEL_TYPE_END);
        }
    }

    private String nutNameToLabel(final NutName nn) {
        final String[] labelWords = nn.getName().replace("ups", "UPS").split("\\.");
        return Stream.of(labelWords).map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String nutNameToChannelType(final NutName nn) {
        return nn.getName().replace('.', '-');
    }

    private String nutNameToPattern(final String itemType) {
        final String pattern;
        switch (itemType) {
            case CoreItemFactory.STRING:
                pattern = "";
                break;
            case CoreItemFactory.NUMBER:
                pattern = "%d";
                break;
            case "Number:Dimensionless":
                pattern = "%.1f %%";
                break;
            case "Number:Time":
                pattern = "%d %unit%";
                break;
            case "Number:Power":
            case "Number:ElectricPotential":
                pattern = "%.0f %unit%";
                break;
            case "Number:Temperature":
            case "Number:ElectricCurrent":
            case "Number:Frequency":
            case "Number:Angle":
                pattern = "%.1f %unit%";
                break;
            default:
                fail("itemType not supported:" + itemType);
                pattern = "";
                break;
        }
        return pattern;
    }
}
