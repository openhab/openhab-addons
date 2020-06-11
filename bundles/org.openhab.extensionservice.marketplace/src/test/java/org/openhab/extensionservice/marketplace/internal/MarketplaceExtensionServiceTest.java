/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.extensionservice.marketplace.internal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openhab.extensionservice.marketplace.internal.model.Node;

/**
 * Tests {@link MarketplaceExtensionService}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class MarketplaceExtensionServiceTest {

    private static final String BASE_PATH = "http://marketplace.eclipse.org/marketplace-client-intro";

    private MarketplaceExtensionService marketplaceService;
    private MarketplaceProxy proxy;

    @Before
    public void setup() {
        marketplaceService = new MarketplaceExtensionService();

        proxy = mock(MarketplaceProxy.class);
        when(proxy.getNodes()).thenReturn(createMockNodes());

        marketplaceService.proxy = proxy;
    }

    @Test
    public void shouldParseBindingExtensionIdFromValidURI() throws Exception {
        String url = BASE_PATH + "?mpc_install=3305842";
        String extensionId = marketplaceService.getExtensionId(new URI(url));

        assertThat(extensionId, is("market:binding-3305842"));
    }

    @Test
    public void shouldParseRuleExtensionIdFromValidURI() throws Exception {
        String url = BASE_PATH + "?mpc_install=3459873";
        String extensionId = marketplaceService.getExtensionId(new URI(url));

        assertThat(extensionId, is("market:ruletemplate-3459873"));
    }

    @Test
    public void shouldParseVoiceExtensionIdFromValidURI() throws Exception {
        String url = BASE_PATH + "?mpc_install=396962433";
        String extensionId = marketplaceService.getExtensionId(new URI(url));

        assertThat(extensionId, is("market:voice-396962433"));
    }

    @Test
    public void shouldParseExtensionIdFromValidURIWithMultipleQueryParams() throws Exception {
        String url = BASE_PATH + "?p1&p2=foo&mpc_install=3305842&p3=bar";
        String extensionId = marketplaceService.getExtensionId(new URI(url));

        assertThat(extensionId, is("market:binding-3305842"));
    }

    @Test
    public void shouldReturnNullFormInvalidQueryParam() throws Exception {
        String url = BASE_PATH + "?extensionId=3305842";
        String extensionId = marketplaceService.getExtensionId(new URI(url));

        assertThat(extensionId, nullValue());
    }

    @Test
    public void shouldReturnNullFromInvalidHost() throws Exception {
        String url = "http://m.eclipse.org/marketplace-client-intro?extensionId=3305842";
        String extensionId = marketplaceService.getExtensionId(new URI(url));

        assertThat(extensionId, nullValue());
    }

    @Test
    public void shouldReturnNullFromEmptyId() throws Exception {
        String url = BASE_PATH + "?p1&p2=foo&mpc_install=&p3=bar";
        String extensionId = marketplaceService.getExtensionId(new URI(url));

        assertThat(extensionId, nullValue());
    }

    private List<Node> createMockNodes() {
        List<Node> nodes = new ArrayList<>(2);
        nodes.add(createBindingNode());
        nodes.add(createRuleNode());
        nodes.add(createVoiceNode());
        return nodes;
    }

    private Node createRuleNode() {
        Node node = new Node();
        node.id = "3459873";
        node.packagetypes = "rule_template";
        node.packageformat = "json";
        return node;
    }

    private Node createBindingNode() {
        Node node = new Node();
        node.id = "3305842";
        node.packagetypes = "binding";
        return node;
    }

    private Node createVoiceNode() {
        Node node = new Node();
        node.id = "396962433";
        node.packagetypes = "voice";
        return node;
    }
}
