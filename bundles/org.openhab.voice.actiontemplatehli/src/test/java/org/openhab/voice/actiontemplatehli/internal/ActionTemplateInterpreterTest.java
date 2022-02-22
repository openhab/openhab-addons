/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.voice.actiontemplatehli.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.SERVICE_ID;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.io.rest.LocaleService;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.voice.text.HumanLanguageInterpreter;
import org.openhab.core.voice.text.InterpretationException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ActionTemplateInterpreterTest} class contains the tests for the interpreter
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateInterpreterTest {
    private @Mock @NonNullByDefault({}) ItemRegistry itemRegistryMock;
    private @Mock @NonNullByDefault({}) MetadataRegistry metadataRegistryMock;
    private @Mock @NonNullByDefault({}) LocaleService localeServiceMock;
    private @Mock @NonNullByDefault({}) EventPublisher eventPublisherMock;
    private @Mock @NonNullByDefault({}) HumanLanguageInterpreter fallbackHLIMock;
    private @NonNullByDefault({}) ActionTemplateInterpreter interpreter;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        ObjectMapper mapper = new ObjectMapper();
        // Prepare Switch
        var switchItem = new SwitchItem("testSwitch");
        switchItem.setState(OnOffType.OFF);
        switchItem.setLabel("bedroom light");
        switchItem.addTag("Light");
        Mockito.when(itemRegistryMock.get(switchItem.getName())).thenReturn(switchItem);
        // Prepare Switch Write action
        var switchNPLWriteAction = new ActionTemplateConfiguration();
        switchNPLWriteAction.template = "$onOff $itemLabel";
        switchNPLWriteAction.value = "$onOff";
        var onOffPlaceholder = new ActionTemplateConfiguration.ActionTemplatePlaceholder();
        onOffPlaceholder.label = "onOff";
        onOffPlaceholder.nerStaticValues = new String[] { "turn on", "turn off" };
        onOffPlaceholder.posStaticValues = Map.of("turn__on", "ON", "turn__off", "OFF");
        switchNPLWriteAction.placeholders = List.of(onOffPlaceholder);
        // Prepare Switch Read action
        var switchNPLReadAction = new ActionTemplateConfiguration();
        switchNPLReadAction.read = true;
        switchNPLReadAction.template = "how is the $itemLabel";
        switchNPLReadAction.value = "$itemLabel is $state";
        // Prepare Group
        var groupItem = new GroupItem("testGroup");
        groupItem.setLabel("bedroom");
        groupItem.addTag("Location");
        Mockito.when(itemRegistryMock.get(groupItem.getName())).thenReturn(groupItem);
        // Prepare Group Write action
        var groupNPLWriteAction = new ActionTemplateConfiguration();
        groupNPLWriteAction.template = "turn on $itemLabel lights";
        groupNPLWriteAction.requiredItemTags = new String[] { "Location" };
        groupNPLWriteAction.value = "ON";
        groupNPLWriteAction.memberTargets = new ActionTemplateConfiguration.ActionTemplateGroupTargets();
        groupNPLWriteAction.memberTargets.itemType = "Switch";
        groupNPLWriteAction.memberTargets.requiredItemTags = new String[] { "Light" };
        // Prepare Group Read action
        var groupNPLReadAction = new ActionTemplateConfiguration();
        groupNPLReadAction.read = true;
        groupNPLReadAction.requiredItemTags = new String[] { "Location" };
        groupNPLReadAction.template = "how is the light in the $itemLabel";
        groupNPLReadAction.value = "$itemLabel in $groupLabel is $state";
        var statePlaceholder = new ActionTemplateConfiguration.ActionTemplatePlaceholder();
        statePlaceholder.label = "state";
        statePlaceholder.posStaticValues = Map.of("ON", "on", "OFF", "off");
        groupNPLReadAction.placeholders = List.of(statePlaceholder);
        groupNPLReadAction.memberTargets = new ActionTemplateConfiguration.ActionTemplateGroupTargets();
        groupNPLReadAction.memberTargets.itemName = switchItem.getName();
        groupNPLReadAction.memberTargets.requiredItemTags = new String[] { "Light" };
        // Add switch member to group
        groupItem.addMember(switchItem);
        // Prepare string
        var stringItem = new StringItem("testString");
        stringItem.setLabel("message example");
        Mockito.when(itemRegistryMock.get(stringItem.getName())).thenReturn(stringItem);
        // Prepare string write action
        var stringNPLWriteAction = new ActionTemplateConfiguration();
        stringNPLWriteAction.template = "send message $* to $contact";
        stringNPLWriteAction.value = "$contact:$*";
        stringNPLWriteAction.ruleMode = true;
        var contactPlaceholder = new ActionTemplateConfiguration.ActionTemplatePlaceholder();
        contactPlaceholder.label = "contact";
        contactPlaceholder.nerStaticValues = new String[] { "Mark", "Andrea" };
        contactPlaceholder.posStaticValues = Map.of("Mark", "+34000000000", "Andrea", "+34000000001");
        stringNPLWriteAction.placeholders = List.of(contactPlaceholder);
        var stringConfig = mapper.readValue(mapper.writeValueAsString(stringNPLWriteAction), Map.class);
        // Mock metadata for 'testString'
        Mockito.when(metadataRegistryMock.get(new MetadataKey(SERVICE_ID, stringItem.getName())))
                .thenReturn(new Metadata(new MetadataKey(SERVICE_ID, stringItem.getName()), "", stringConfig));
        // Mock items
        Mockito.when(itemRegistryMock.getAll()).thenReturn(List.of(switchItem, stringItem, groupItem));

        interpreter = new ActionTemplateInterpreter(itemRegistryMock, localeServiceMock, metadataRegistryMock,
                eventPublisherMock) {
            @Override
            protected ActionTemplateConfiguration[] getTypeActionConfigs(String itemType) {
                // mock type actions for testing
                if ("Switch".equals(itemType)) {
                    return new ActionTemplateConfiguration[] { switchNPLWriteAction, switchNPLReadAction };
                }
                if ("Group".equals(itemType)) {
                    return new ActionTemplateConfiguration[] { groupNPLWriteAction, groupNPLReadAction };
                }
                return new ActionTemplateConfiguration[] {};
            }
        };
    }

    /**
     * Test type write action
     */
    @Test
    public void switchItemOnOffTest() throws InterpretationException {
        var response = interpreter.interpret(Locale.ENGLISH, "turn on bedroom light");
        assertThat(response, is(interpreter.config.commandSentMessage));
        Mockito.verify(eventPublisherMock).post(ItemEventFactory.createCommandEvent("testSwitch", OnOffType.ON));
        response = interpreter.interpret(Locale.ENGLISH, "turn off bedroom light");
        assertThat(response, is(interpreter.config.commandSentMessage));
        Mockito.verify(eventPublisherMock).post(ItemEventFactory.createCommandEvent("testSwitch", OnOffType.OFF));
    }

    /**
     * Test type read action
     */
    @Test
    public void switchItemReadTest() throws InterpretationException {
        var response = interpreter.interpret(Locale.ENGLISH, "how is the bedroom light");
        assertThat(response, is("bedroom light is OFF"));
    }

    /**
     * Test group write action targeting members
     */
    @Test
    public void groupItemMemberOnTest() throws InterpretationException {
        var response = interpreter.interpret(Locale.ENGLISH, "turn on bedroom lights");
        assertThat(response, is("Done"));
        Mockito.verify(eventPublisherMock).post(ItemEventFactory.createCommandEvent("testSwitch", OnOffType.ON));
    }

    /**
     * Test group read action targeting members
     */
    @Test
    public void groupItemMemberReadTest() throws InterpretationException {
        var response = interpreter.interpret(Locale.ENGLISH, "how is the light in the bedroom");
        assertThat(response, is("bedroom light in bedroom is off"));
    }

    /**
     * Test group read action targeting members
     */
    @Test
    public void fallbackInterpreterTest() throws InterpretationException {
        var exampleUnhandledText = "this is an unknown sample";
        interpreter.config.fallbackHLI = "mocked_interpreter";
        Mockito.when(fallbackHLIMock.getId()).thenReturn(interpreter.config.fallbackHLI);
        Mockito.when(fallbackHLIMock.interpret(Locale.ENGLISH, exampleUnhandledText))
                .thenReturn("mocked fallback response");
        interpreter.addHumanLanguageInterpreter(fallbackHLIMock);
        var response = interpreter.interpret(Locale.ENGLISH, exampleUnhandledText);
        assertThat(response, is("mocked fallback response"));
    }

    /**
     * Test write action using the dynamic label
     */
    @Test
    public void messageTest() throws InterpretationException {
        var response = interpreter.interpret(Locale.ENGLISH, "send message please turn off the bedroom light to mark");
        // rule mode is enabled so no response
        assertThat(response, is(""));
        Mockito.verify(eventPublisherMock).post(ItemEventFactory.createCommandEvent("testString",
                new StringType("+34000000000:please turn off the bedroom light")));
    }
}
