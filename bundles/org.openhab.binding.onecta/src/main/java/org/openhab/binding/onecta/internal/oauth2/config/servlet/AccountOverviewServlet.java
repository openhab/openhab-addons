/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.oauth2.config.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onecta.internal.constants.OnectaBridgeConstants;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;

/**
 * Servlet showing the account overview page.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class AccountOverviewServlet extends AbstractShowPageServlet {
    private static final long serialVersionUID = -4551210904923220429L;
    private static final String ACCOUNTS_SKELETON = "index.html";

    private static final String BRIDGES_TITLE_PLACEHOLDER = "<!-- BRIDGES TITLE -->";
    private static final String BRIDGES_PLACEHOLDER = "<!-- BRIDGES -->";
    private static final String NO_SSL_WARNING_PLACEHOLDER = "<!-- NO SSL WARNING -->";

    private final ThingRegistry thingRegistry;
    private final Inbox inbox;

    /**
     * Creates a new {@link AccountOverviewServlet}.
     *
     * @param resourceLoader Loader to use for resources.
     * @param thingRegistry openHAB thing registry.
     * @param inbox openHAB inbox for discovery results.
     */
    public AccountOverviewServlet(ResourceLoader resourceLoader, ThingRegistry thingRegistry, Inbox inbox) {
        super(resourceLoader);
        this.thingRegistry = thingRegistry;
        this.inbox = inbox;
    }

    @Override
    protected String handleGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws OnectaHttpException, IOException {
        String skeleton = getResourceLoader().loadResourceAsString(ACCOUNTS_SKELETON);
        skeleton = renderBridges(skeleton);
        skeleton = renderSslWarning(request, skeleton);
        return skeleton;
    }

    private String renderBridges(String skeleton) {
        List<Thing> bridges = thingRegistry.stream().filter(this::isOnectaBridge).collect(Collectors.toList());
        if (bridges.isEmpty()) {
            return renderNoBridges(skeleton);
        } else {
            return renderBridgesIntoSkeleton(skeleton, bridges);
        }
    }

    private String renderNoBridges(String skeleton) {
        return skeleton.replace(BRIDGES_TITLE_PLACEHOLDER, "There is no account paired at the moment.")
                .replace(BRIDGES_PLACEHOLDER, "");
    }

    private String renderBridgesIntoSkeleton(String skeleton, List<Thing> bridges) {
        StringBuilder builder = new StringBuilder();

        int index = 0;
        Iterator<Thing> bridgeIterator = bridges.iterator();
        while (bridgeIterator.hasNext()) {
            builder.append(renderBridge(bridgeIterator.next(), index));
            index++;
        }

        return skeleton.replace(BRIDGES_PLACEHOLDER, builder.toString());
    }

    private String renderBridge(Thing bridge, int index) {
        StringBuilder builder = new StringBuilder();
        builder.append("                    <li>\n");

        String thingUid = bridge.getUID().getAsString();
        String thingId = bridge.getUID().getId();
        builder.append("                        ");
        builder.append(thingUid.substring(0, thingUid.length() - thingId.length()));
        builder.append(" ");
        builder.append(thingId);

        builder.append("\n");

        builder.append("                        <span class=\"status ");
        final ThingStatus status = bridge.getStatus();
        if (status == ThingStatus.ONLINE) {
            builder.append("online");
        } else {
            builder.append("offline");
        }
        builder.append("\">");
        builder.append(status.toString());
        builder.append("</span>\n");

        builder.append("                    </li>");

        return builder.toString();
    }

    private boolean isOnectaBridge(Thing thing) {
        return OnectaBridgeConstants.THING_TYPE_BRIDGE.equals(thing.getThingTypeUID());
    }

    private String renderSslWarning(HttpServletRequest request, String skeleton) {
        if (!request.isSecure()) {
            return skeleton.replace(NO_SSL_WARNING_PLACEHOLDER, "<div class=\"alert alert-danger\" role=\"alert\">\n"
                    + "                    Warning: We strongly advice to proceed only with SSL enabled for a secure data exchange.\n"
                    + "                    See <a href=\"https://www.openhab.org/docs/installation/security.html\">Securing access to openHAB</a> for details.\n"
                    + "                </div>");
        } else {
            return skeleton.replace(NO_SSL_WARNING_PLACEHOLDER, "");
        }
    }
}
