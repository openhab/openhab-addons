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
package org.openhab.misc.addonsuggestionfinder.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.addon.AddonDiscoveryMethod;
import org.openhab.core.addon.AddonDiscoveryServiceType;
import org.openhab.core.addon.AddonInfo;
import org.openhab.core.addon.AddonMatchProperty;
import org.openhab.misc.addonsuggestionfinder.AddonSuggestionInfoProvider;
import org.openhab.misc.addonsuggestionfinder.internal.AddonInfoList;
import org.openhab.misc.addonsuggestionfinder.internal.AddonListSerializer;

/**
 * JUnit tests for {@link SuggestedAddonInfoProvider}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class AddonSuggestionInfoProviderTest {

    private final AddonListSerializer serializer = new AddonListSerializer();

    // @formatter:off
    private final String testXml =
            "<addons>"
            + "    <addon:addon id=\"groovyscripting\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "        xmlns:addon=\"https://openhab.org/schemas/addon/v1.0.0\""
            + "        xsi:schemaLocation=\"https://openhab.org/schemas/addon/v1.0.0 https://openhab.org/schemas/addon-1.0.0.xsd\">"
            + "        <type>automation</type>"
            + "        <name>Groovy Scripting</name>"
            + "        <description>This adds a Groovy script engine.</description>"
            + "        <connection>none</connection>"
            + "        <discovery-method>"
            + "            <service-type>mdns</service-type>"
            + "            <match-property>"
            + "                <name>rp</name>"
            + "                <regex>.*</regex>"
            + "            </match-property>"
            + "            <match-property>"
            + "                <name>ty</name>"
            + "                <regex>hp (.*)</regex>"
            + "            </match-property>"
            + "            <mdns-service-type>_printer._tcp.local.</mdns-service-type>"
            + "        </discovery-method>"
            + "        <discovery-method>"
            + "            <service-type>upnp</service-type>"
            + "            <match-property>"
            + "                <name>modelName</name>"
            + "                <regex>Philips hue bridge</regex>"
            + "            </match-property>"
            + "        </discovery-method>"
            + "    </addon:addon>"
            + "</addons>";
    // @formatter:on

    @Test
    void testAddonInfosXml() {
        AddonInfoList addons = serializer.fromXML(testXml);
        List<AddonInfo> addonsInfos = addons.getAddonInfos();
        assertEquals(1, addonsInfos.size());
        AddonInfo addon = addonsInfos.get(0);
        assertNotNull(addon);
        List<AddonDiscoveryMethod> discoveryMethods = addon.getDiscoveryMethods();
        assertNotNull(discoveryMethods);
        assertEquals(2, discoveryMethods.size());

        AddonDiscoveryMethod method = discoveryMethods.get(0);
        assertNotNull(method);
        assertEquals(AddonDiscoveryServiceType.MDNS, method.getServiceType());
        assertEquals("_printer._tcp.local.", method.getMdnsServiceType());
        List<AddonMatchProperty> matchProperties = method.getMatchProperties();
        assertNotNull(matchProperties);
        assertEquals(2, matchProperties.size());
        AddonMatchProperty property = matchProperties.get(0);
        assertNotNull(property);
        assertEquals("rp", property.getName());
        assertEquals(".*", property.getRegex());

        method = discoveryMethods.get(1);
        assertNotNull(method);
        assertEquals(AddonDiscoveryServiceType.UPNP, method.getServiceType());
        assertEquals("", method.getMdnsServiceType());
        matchProperties = method.getMatchProperties();
        assertNotNull(matchProperties);
        assertEquals(1, matchProperties.size());
        property = matchProperties.get(0);
        assertNotNull(property);
        assertEquals("modelName", property.getName());
        assertEquals("Philips hue bridge", property.getRegex());
    }

    @Test
    void testAddonSuggestionAddonInfoProvider() {
        AddonSuggestionInfoProvider provider = new AddonSuggestionInfoProvider();
        assertNotNull(provider);
        Set<AddonInfo> addons = provider.getAddonInfos(Locale.US);
        assertNotNull(addons);
        // TODO un-comment the following line when at least one addon has officially added a 'discovery-method'
        // assertFalse(addons.isEmpty());
    }
}
