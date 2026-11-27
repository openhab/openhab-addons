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
package org.openhab.binding.homeconnect.internal.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants;
import org.openhab.binding.homeconnect.internal.client.HomeConnectEventSourceClient;
import org.openhab.binding.homeconnect.internal.configuration.ApiBridgeConfiguration;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Renders the servlet templates with the embedded Thymeleaf and OGNL libraries, so an incompatible library update
 * fails at build time instead of when the servlet page is opened.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class HomeConnectTemplatesTest {

    private final TemplateEngine templateEngine = new TemplateEngine();

    @BeforeEach
    void setUp() {
        // same template setup as HomeConnectServlet, but resolved from the classpath instead of the servlet context
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
        // context-relative links (@{/...}) need a web context by default; resolve them against an empty context path
        templateEngine.setLinkBuilder(new StandardLinkBuilder() {
            @Override
            @NonNullByDefault({})
            protected String computeContextPath(IExpressionContext context, String base,
                    Map<String, Object> parameters) {
                return "";
            }
        });
    }

    @Test
    void bridgesPageWithoutBridgesShowsHint() {
        String html = render("bridges", List.of());

        assertThat(html, containsString("No Home Connect bridge found"));
    }

    @Test
    void appliancesPageWithoutBridgesShowsHint() {
        String html = render("appliances", List.of());

        assertThat(html, containsString("No Home Connect bridge found"));
    }

    @Test
    void bridgesPageShowsBridgeDetails() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID("homeconnect", "api_bridge", "test"));
        when(bridge.getLabel()).thenReturn("Home Connect <API>");
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        HomeConnectBridgeHandler bridgeHandler = mock(HomeConnectBridgeHandler.class);
        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridgeHandler.getConfiguration()).thenReturn(new ApiBridgeConfiguration());
        when(bridgeHandler.getEventSourceClient()).thenReturn(mock(HomeConnectEventSourceClient.class));

        String html = render("bridges", List.of(bridgeHandler));

        assertThat(html, containsString("Home Connect &lt;API&gt;"));
        assertThat(html, containsString("homeconnect:api_bridge:test"));
        assertThat(html, containsString(HomeConnectBindingConstants.API_BASE_URL));
        assertThat(html, containsString("badge-success"));
    }

    private String render(String template, List<HomeConnectBridgeHandler> bridgeHandlers) {
        Context context = new Context();
        context.setVariable("bridgeHandlers", bridgeHandlers);
        return templateEngine.process(template, context);
    }
}
