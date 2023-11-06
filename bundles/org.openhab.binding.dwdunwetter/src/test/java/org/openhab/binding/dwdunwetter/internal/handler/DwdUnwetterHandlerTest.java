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
package org.openhab.binding.dwdunwetter.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.dwdunwetter.internal.DwdUnwetterBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test cases for {@link DwdUnwetterHandler}. The tests provide mocks for supporting entities using Mockito.
 *
 * @author Martin Koehler - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DwdUnwetterHandlerTest extends JavaTest {

    private ThingHandler handler;

    private @Mock ThingHandlerCallback callback;
    private @Mock Thing thing;

    @BeforeEach
    public void setUp() {
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class)))
                .thenAnswer(invocation -> ChannelBuilder.create(invocation.getArgument(0, ChannelUID.class))
                        .withType(invocation.getArgument(1, ChannelTypeUID.class)));

        handler = new DwdUnwetterHandler(thing);
        handler.setCallback(callback);
        // mock getConfiguration to prevent NPEs
        when(thing.getUID()).thenReturn(new ThingUID(DwdUnwetterBindingConstants.BINDING_ID, "test"));
        Configuration configuration = new Configuration();
        configuration.put("refresh", Integer.valueOf("1"));
        configuration.put("warningCount", Integer.valueOf("1"));
        when(thing.getConfiguration()).thenReturn(configuration);
    }

    @Test
    public void testInitializeShouldCallTheCallback() {
        // we expect the handler#initialize method to call the callback during execution and
        // pass it the thing and a ThingStatusInfo object containing the ThingStatus of the thing.
        handler.initialize();

        // the argument captor will capture the argument of type ThingStatusInfo given to the
        // callback#statusUpdated method.
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        // verify the interaction with the callback and capture the ThingStatusInfo argument:
        waitForAssert(() -> {
            verify(callback, times(1)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        });

        // assert that the (temporary) UNKNOWN status was to the mocked thing first:
        assertThat(statusInfoCaptor.getAllValues().get(0).getStatus(), is(ThingStatus.UNKNOWN));
    }

    /**
     * Tests that the labels of the channels are equal to the ChannelType Definition
     */
    @Test
    public void testLabels() throws Exception {
        handler.initialize();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream stream = getClass().getResourceAsStream("/OH-INF/thing/thing-types.xml");
        Document document = builder.parse(stream);
        NodeList nodeList = document.getElementsByTagName("channel-type");

        thing = handler.getThing();
        List<Channel> channels = thing.getChannels();
        for (Channel channel : channels) {
            String label = getLabel(nodeList, channel.getChannelTypeUID());
            assertThat(channel.getLabel(), CoreMatchers.startsWith(label));
        }
    }

    private String getLabel(NodeList nodeList, ChannelTypeUID uuid) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node nodeId = node.getAttributes().getNamedItem("id");
            if (nodeId == null) {
                continue;
            }
            if (Objects.equals(nodeId.getTextContent(), uuid.getId())) {
                return getLabel(node.getChildNodes());
            }
        }
        return null;
    }

    private String getLabel(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if ("label".equals(node.getNodeName())) {
                return node.getTextContent();
            }
        }
        return null;
    }
}
