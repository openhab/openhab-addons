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
package org.openhab.binding.mielecloud.internal.config.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.config.ThingsTemplateGenerator;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.thing.Bridge;
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
    private final ThingsTemplateGenerator templateGenerator;

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
        this.templateGenerator = new ThingsTemplateGenerator();
    }

    @Override
    protected String handleGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws MieleHttpException, IOException {
        String skeleton = getResourceLoader().loadResourceAsString(ACCOUNTS_SKELETON);
        skeleton = renderBridges(skeleton);
        skeleton = renderSslWarning(request, skeleton);
        return skeleton;
    }

    private String renderBridges(String skeleton) {
        List<Thing> bridges = thingRegistry.stream().filter(this::isMieleCloudBridge).collect(Collectors.toList());
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

        return skeleton.replace(BRIDGES_TITLE_PLACEHOLDER, "The following bridges are paired")
                .replace(BRIDGES_PLACEHOLDER, builder.toString());
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
        builder.append(" ");
        builder.append(bridge.getConfiguration().get(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL).toString());
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

        builder.append("                        <input class=\"trigger\" id=\"mielecloud-account-");
        builder.append(thingId);
        builder.append("\" type=\"checkbox\" name=\"things-file\" />\n");

        builder.append("                        <label for=\"mielecloud-account-");
        builder.append(thingId);
        builder.append("\">&lt; &gt;</label>\n");

        builder.append("                        <div class=\"things\">\n");
        builder.append(
                "                            <span class=\"legend\">You can use this things-file template to pair all available devices:</span>\n");
        builder.append("                            <div class=\"code-container\">\n");
        builder.append(
                "                                <a href=\"#\" onclick=\"copyCodeToClipboard(event, this);\" class=\"btn btn-outline-info btn-sm copy\">Copy</a>\n");
        builder.append("                                <textarea readonly>");
        builder.append(generateConfigurationTemplate((Bridge) bridge));
        builder.append("</textarea>\n");
        builder.append("                            </div>\n");
        builder.append("                        </div>\n");
        builder.append("                    </li>");

        return builder.toString();
    }

    private String generateConfigurationTemplate(Bridge bridge) {
        List<Thing> pairedThings = thingRegistry.stream().filter(thing -> isConnectedVia(thing, bridge))
                .collect(Collectors.toList());
        List<DiscoveryResult> discoveryResults = inbox.stream()
                .filter(discoveryResult -> willConnectVia(discoveryResult, bridge)).collect(Collectors.toList());

        return templateGenerator.createBridgeAndThingConfigurationTemplate(bridge, pairedThings, discoveryResults);
    }

    private boolean isConnectedVia(Thing thing, Bridge bridge) {
        return bridge.getUID().equals(thing.getBridgeUID());
    }

    private boolean willConnectVia(DiscoveryResult discoveryResult, Bridge bridge) {
        return bridge.getUID().equals(discoveryResult.getBridgeUID());
    }

    private boolean isMieleCloudBridge(Thing thing) {
        return MieleCloudBindingConstants.THING_TYPE_BRIDGE.equals(thing.getThingTypeUID());
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
