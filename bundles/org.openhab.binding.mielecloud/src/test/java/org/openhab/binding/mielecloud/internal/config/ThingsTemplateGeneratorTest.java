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
package org.openhab.binding.mielecloud.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingTestConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class ThingsTemplateGeneratorTest {
    private static final String BRIDGE_ID = "genesis";
    private static final String ALTERNATIVE_BRIDGE_ID = "mielebridge";

    private static final String LOCALE = "en";
    private static final String ALTERNATIVE_LOCALE = "de";

    private static final String EMAIL = "openhab@openhab.org";
    private static final String ALTERNATIVE_EMAIL = "everyone@openhab.org";

    @Test
    public void whenBridgeIdAndAccessTokenAndLocaleAreProvidedThenAValidBridgeConfigurationTemplateIsGenerated() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        // when:
        String template = templateGenerator.createBridgeConfigurationTemplate(BRIDGE_ID, EMAIL, LOCALE);

        // then:
        assertEquals("Bridge mielecloud:account:genesis [ email=\"openhab@openhab.org\", locale=\"en\" ]", template);
    }

    @Test
    public void whenAnAlternativeBridgeIdIsProvidedThenAValidBridgeConfigurationTemplateWithThatIdIsGenerated() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        // when:
        String template = templateGenerator.createBridgeConfigurationTemplate(ALTERNATIVE_BRIDGE_ID, EMAIL, LOCALE);

        // then:
        assertEquals("Bridge mielecloud:account:mielebridge [ email=\"openhab@openhab.org\", locale=\"en\" ]",
                template);
    }

    @Test
    public void whenAnAlternativeAccessTokenIsProvidedThenAValidBridgeConfigurationTemplateWithThatAccessTokenIsGenerated() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        // when:
        String template = templateGenerator.createBridgeConfigurationTemplate(BRIDGE_ID, EMAIL, LOCALE);

        // then:
        assertEquals("Bridge mielecloud:account:genesis [ email=\"openhab@openhab.org\", locale=\"en\" ]", template);
    }

    @Test
    public void whenAnAlternativeLocaleIsProvidedThenAValidBridgeConfigurationTemplateWithThatLocaleIsGenerated() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        // when:
        String template = templateGenerator.createBridgeConfigurationTemplate(BRIDGE_ID, EMAIL, ALTERNATIVE_LOCALE);

        // then:
        assertEquals("Bridge mielecloud:account:genesis [ email=\"openhab@openhab.org\", locale=\"de\" ]", template);
    }

    @Test
    public void whenAnAlternativeEmailIsProvidedThenAValidBridgeConfigurationTemplateWithThatEmailIsGenerated() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        // when:
        String template = templateGenerator.createBridgeConfigurationTemplate(BRIDGE_ID, ALTERNATIVE_EMAIL, LOCALE);

        // then:
        assertEquals("Bridge mielecloud:account:genesis [ email=\"everyone@openhab.org\", locale=\"en\" ]", template);
    }

    private Bridge createBridgeMock(String id, String locale, String email) {
        Configuration configuration = mock(Configuration.class);
        when(configuration.get(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE)).thenReturn(locale);
        when(configuration.get(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL)).thenReturn(email);

        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID(MieleCloudBindingConstants.THING_TYPE_BRIDGE, id));
        when(bridge.getConfiguration()).thenReturn(configuration);

        return bridge;
    }

    private Thing createThingMock(ThingTypeUID thingTypeUid, String deviceIdentifier, @Nullable String label,
            String bridgeId) {
        Configuration configuration = mock(Configuration.class);
        when(configuration.get(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER)).thenReturn(deviceIdentifier);

        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(thingTypeUid);
        when(thing.getUID()).thenReturn(new ThingUID(thingTypeUid, deviceIdentifier, bridgeId));
        when(thing.getLabel()).thenReturn(label);
        when(thing.getConfiguration()).thenReturn(configuration);
        return thing;
    }

    private DiscoveryResult createDiscoveryResultMock(ThingTypeUID thingTypeUid, String id, String label,
            String bridgeId) {
        DiscoveryResult discoveryResult = mock(DiscoveryResult.class);
        when(discoveryResult.getLabel()).thenReturn(label);
        when(discoveryResult.getThingTypeUID()).thenReturn(thingTypeUid);
        when(discoveryResult.getThingUID()).thenReturn(new ThingUID(thingTypeUid, id, bridgeId));
        when(discoveryResult.getProperties())
                .thenReturn(Map.of(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER, id));
        return discoveryResult;
    }

    @Test
    public void whenNoThingsArePairedAndNoInboxEntriesAreAvailableThenAnEmptyConfigurationTemplateIsGenerated() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        Bridge bridge = createBridgeMock(MieleCloudBindingTestConstants.BRIDGE_ID, LOCALE, EMAIL);

        // when:
        String template = templateGenerator.createBridgeAndThingConfigurationTemplate(bridge, Collections.emptyList(),
                Collections.emptyList());

        // then:
        assertEquals("Bridge mielecloud:account:genesis [ email=\"openhab@openhab.org\", locale=\"en\" ] {\n}",
                template);
    }

    @Test
    public void whenPairedThingsArePresentThenTheyArePresentInTheConfigurationTemplate() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        Bridge bridge = createBridgeMock(ALTERNATIVE_BRIDGE_ID, ALTERNATIVE_LOCALE, ALTERNATIVE_EMAIL);

        Thing thing1 = createThingMock(MieleCloudBindingConstants.THING_TYPE_OVEN, "000137439123", "Oven H7860XY",
                ALTERNATIVE_BRIDGE_ID);
        Thing thing2 = createThingMock(MieleCloudBindingConstants.THING_TYPE_HOB, "000160106123", null,
                ALTERNATIVE_BRIDGE_ID);

        List<Thing> pairedThings = Arrays.asList(thing1, thing2);

        // when:
        String template = templateGenerator.createBridgeAndThingConfigurationTemplate(bridge, pairedThings,
                Collections.emptyList());

        // then:
        assertEquals("""
                Bridge mielecloud:account:mielebridge [ email="everyone@openhab.org", locale="de" ] {
                    Thing oven 000137439123 "Oven H7860XY" [ deviceIdentifier="000137439123" ]
                    Thing hob 000160106123 [ deviceIdentifier="000160106123" ]
                }\
                """, template);
    }

    @Test
    public void whenDiscoveryResultsAreInTheInboxThenTheyArePresentInTheConfigurationTemplate() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        Bridge bridge = createBridgeMock(ALTERNATIVE_BRIDGE_ID, ALTERNATIVE_LOCALE, ALTERNATIVE_EMAIL);

        DiscoveryResult discoveryResult1 = createDiscoveryResultMock(
                MieleCloudBindingConstants.THING_TYPE_FRIDGE_FREEZER, "000154106123", "Fridge-Freezer Kitchen",
                ALTERNATIVE_BRIDGE_ID);
        DiscoveryResult discoveryResult2 = createDiscoveryResultMock(
                MieleCloudBindingConstants.THING_TYPE_WASHING_MACHINE, "000189106123", "Washing Machine",
                ALTERNATIVE_BRIDGE_ID);

        List<DiscoveryResult> discoveredThings = Arrays.asList(discoveryResult1, discoveryResult2);

        // when:
        String template = templateGenerator.createBridgeAndThingConfigurationTemplate(bridge, Collections.emptyList(),
                discoveredThings);

        // then:
        assertEquals("""
                Bridge mielecloud:account:mielebridge [ email="everyone@openhab.org", locale="de" ] {
                    Thing fridge_freezer 000154106123 "Fridge-Freezer Kitchen" [ deviceIdentifier="000154106123" ]
                    Thing washing_machine 000189106123 "Washing Machine" [ deviceIdentifier="000189106123" ]
                }\
                """, template);
    }

    @Test
    public void whenThingsArePresentAndDiscoveryResultsAreInTheInboxThenTheyArePresentInTheConfigurationTemplate() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        Bridge bridge = createBridgeMock(ALTERNATIVE_BRIDGE_ID, ALTERNATIVE_LOCALE, EMAIL);

        Thing thing1 = createThingMock(MieleCloudBindingConstants.THING_TYPE_OVEN, "000137439123", "Oven H7860XY",
                ALTERNATIVE_BRIDGE_ID);
        Thing thing2 = createThingMock(MieleCloudBindingConstants.THING_TYPE_HOB, "000160106123", null,
                ALTERNATIVE_BRIDGE_ID);

        List<Thing> pairedThings = Arrays.asList(thing1, thing2);

        DiscoveryResult discoveryResult1 = createDiscoveryResultMock(
                MieleCloudBindingConstants.THING_TYPE_FRIDGE_FREEZER, "000154106123", "Fridge-Freezer Kitchen",
                ALTERNATIVE_BRIDGE_ID);
        DiscoveryResult discoveryResult2 = createDiscoveryResultMock(
                MieleCloudBindingConstants.THING_TYPE_WASHING_MACHINE, "000189106123", "Washing Machine",
                ALTERNATIVE_BRIDGE_ID);

        List<DiscoveryResult> discoveredThings = Arrays.asList(discoveryResult1, discoveryResult2);

        // when:
        String template = templateGenerator.createBridgeAndThingConfigurationTemplate(bridge, pairedThings,
                discoveredThings);

        // then:
        assertEquals("""
                Bridge mielecloud:account:mielebridge [ email="openhab@openhab.org", locale="de" ] {
                    Thing oven 000137439123 "Oven H7860XY" [ deviceIdentifier="000137439123" ]
                    Thing hob 000160106123 [ deviceIdentifier="000160106123" ]
                    Thing fridge_freezer 000154106123 "Fridge-Freezer Kitchen" [ deviceIdentifier="000154106123" ]
                    Thing washing_machine 000189106123 "Washing Machine" [ deviceIdentifier="000189106123" ]
                }\
                """, template);
    }

    @Test
    public void whenNoLocaleIsConfiguredThenTheDefaultIsUsed() {
        // given:
        ThingsTemplateGenerator templateGenerator = new ThingsTemplateGenerator();

        Configuration configuration = mock(Configuration.class);
        when(configuration.get(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE)).thenReturn(null);
        when(configuration.get(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL)).thenReturn(EMAIL);

        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(
                new ThingUID(MieleCloudBindingConstants.THING_TYPE_BRIDGE, MieleCloudBindingTestConstants.BRIDGE_ID));
        when(bridge.getConfiguration()).thenReturn(configuration);

        // when:
        String template = templateGenerator.createBridgeAndThingConfigurationTemplate(bridge, Collections.emptyList(),
                Collections.emptyList());

        // then:
        assertEquals("Bridge mielecloud:account:genesis [ email=\"openhab@openhab.org\", locale=\"en\" ] {\n}",
                template);
    }
}
