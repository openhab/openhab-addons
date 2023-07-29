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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.servlet.WebhookServlet;

/**
 * {@link HomeSecurityThingCapability} is the ancestor of capabilities hosted by a security home
 * e.g. person and camera capabilities
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeSecurityThingCapability extends Capability {
    protected final NetatmoDescriptionProvider descriptionProvider;
    protected final EventChannelHelper eventHelper;

    private Optional<WebhookServlet> webhook = Optional.empty();
    protected Optional<SecurityCapability> securityCapability = Optional.empty();
    protected Optional<HomeCapability> homeCapability = Optional.empty();

    public HomeSecurityThingCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler);
        this.descriptionProvider = descriptionProvider;
        this.eventHelper = (EventChannelHelper) channelHelpers.stream().filter(c -> c instanceof EventChannelHelper)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "HomeSecurityThingCapability must find an EventChannelHelper, please file a bug report."));
        eventHelper.setModuleType(moduleType);
    }

    @Override
    public void initialize() {
        super.initialize();
        securityCapability = handler.getHomeCapability(SecurityCapability.class);
        homeCapability = handler.getHomeCapability(HomeCapability.class);
        ApiBridgeHandler accountHandler = handler.getAccountHandler();
        if (accountHandler != null) {
            webhook = accountHandler.getWebHookServlet();
            webhook.ifPresent(servlet -> servlet.registerDataListener(handler.getId(), this));
        }
    }

    @Override
    public void dispose() {
        webhook.ifPresent(servlet -> servlet.unregisterDataListener(handler.getId()));
        super.dispose();
    }
}
