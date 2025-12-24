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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private @Nullable WebhookServlet webhookServlet;
    private @Nullable SecurityCapability securityCapability;
    private @Nullable HomeCapability homeCapability;

    public HomeSecurityThingCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler);
        this.descriptionProvider = descriptionProvider;
        this.eventHelper = channelHelpers.stream().filter(EventChannelHelper.class::isInstance)
                .map(EventChannelHelper.class::cast).findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "HomeSecurityThingCapability must find an EventChannelHelper, please file a bug report."));
        eventHelper.setModuleType(moduleType);
    }

    protected Optional<SecurityCapability> getSecurityCapability() {
        if (securityCapability == null) {
            handler.getHomeCapability(SecurityCapability.class).ifPresent(cap -> securityCapability = cap);
            ApiBridgeHandler accountHandler = handler.getAccountHandler();
            if (accountHandler != null) {
                webhookServlet = null;
                accountHandler.getWebHookServlet().ifPresent(servlet -> {
                    webhookServlet = servlet;
                    servlet.registerDataListener(handler.getId(), this);
                });
            }
        }
        return Optional.ofNullable(securityCapability);
    }

    protected Optional<HomeCapability> getHomeCapability() {
        if (homeCapability == null) {
            handler.getHomeCapability(HomeCapability.class).ifPresent(cap -> homeCapability = cap);
        }
        return Optional.ofNullable(homeCapability);
    }

    @Override
    public void dispose() {
        WebhookServlet webhook = this.webhookServlet;
        if (webhook != null) {
            webhook.unregisterDataListener(handler.getId());
        }
        super.dispose();
    }
}
