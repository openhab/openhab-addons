/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.dwdunwetter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.test.java.JavaTest;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.dwdunwetter.internal.DwdUnwetterBindingConstants;
import org.openhab.binding.dwdunwetter.internal.handler.DwdUnwetterHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test cases for {@link DwdUnwetterHandler}. The tests provide mocks for supporting entities using Mockito.
 *
 * @author Martin Koehler - Initial contribution
 */
public class DwdUnwetterHandlerTest extends JavaTest {

    private ThingHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Before
    public void setUp() {
        initMocks(this);
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
        InputStream stream = getClass().getResourceAsStream("/ESH-INF/thing/thing-types.xml");
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
            if (StringUtils.equals(nodeId.getTextContent(), uuid.getId())) {
                return getLabel(node.getChildNodes());
            }
        }
        return null;
    }

    private String getLabel(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeName().equals("label")) {
                return node.getTextContent();
            }
        }
        return null;
    }

}
