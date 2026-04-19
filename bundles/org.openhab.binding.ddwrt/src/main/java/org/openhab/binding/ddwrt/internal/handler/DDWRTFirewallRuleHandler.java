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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_DESCRIPTION;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_ENABLED;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTFirewallRuleConfiguration;
import org.openhab.binding.ddwrt.internal.api.DDWRTFirewallRule;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetworkCache;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a DD-WRT Firewall Rule thing.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTFirewallRuleHandler extends DDWRTBaseHandler<DDWRTFirewallRule, DDWRTFirewallRuleConfiguration> {

    private final Logger logger = LoggerFactory.getLogger(DDWRTFirewallRuleHandler.class);

    private DDWRTFirewallRuleConfiguration config = new DDWRTFirewallRuleConfiguration();

    public DDWRTFirewallRuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(DDWRTFirewallRuleConfiguration config) {
        this.config = config;
        if (config.ruleId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ruleid");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable DDWRTFirewallRule getEntity(DDWRTNetworkCache cache) {
        return cache.getFirewallRule(config.ruleId);
    }

    @Override
    protected State getChannelState(DDWRTFirewallRule rule, String channelId) {
        return switch (channelId) {
            case CHANNEL_ENABLED -> OnOffType.from(rule.isEnabled());
            case CHANNEL_DESCRIPTION ->
                rule.getDescription().isEmpty() ? UnDefType.UNDEF : StringType.valueOf(rule.getDescription());
            default -> UnDefType.NULL;
        };
    }

    @Override
    protected boolean handleCommand(DDWRTNetwork network, DDWRTFirewallRule rule, ChannelUID channelUID,
            Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        if (CHANNEL_ENABLED.equals(channelId) && command instanceof OnOffType) {
            logger.debug("Firewall rule enable/disable not yet implemented for {}", rule.getRuleId());
            return false;
        }
        return false;
    }
}
