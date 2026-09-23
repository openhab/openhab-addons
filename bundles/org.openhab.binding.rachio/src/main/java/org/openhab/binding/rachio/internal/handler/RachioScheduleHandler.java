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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_UPDATE;
import static org.openhab.binding.rachio.internal.RachioUtils.firstNonBlank;
import static org.openhab.binding.rachio.internal.RachioUtils.getTimestamp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Rachio ScheduleRule Things.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioScheduleHandler extends AbstractRachioScheduleHandler<RachioScheduleRuleResponse> {
    private final Logger logger = LoggerFactory.getLogger(RachioScheduleHandler.class);
    protected volatile String scheduleRuleId = "";

    public RachioScheduleHandler(Thing thing) {
        super(thing, FIXED_SCHEDULE, new RachioScheduleRuleResponse());
    }

    @Override
    protected RachioScheduleRuleResponse createRule() {
        return new RachioScheduleRuleResponse();
    }

    @Override
    protected String getRuleId() {
        return scheduleRuleId;
    }

    @Override
    protected void setRuleId(String ruleId) {
        scheduleRuleId = ruleId;
    }

    @Override
    protected RachioScheduleRuleResponse loadRule(RachioBridgeHandler handler, String requestedRuleId)
            throws RachioApiException {
        return loadScheduleRule(handler, requestedRuleId);
    }

    protected RachioScheduleRuleResponse loadScheduleRule(RachioBridgeHandler handler, String requestedScheduleRuleId)
            throws RachioApiException {
        return handler.getScheduleRuleForInitialization(requestedScheduleRuleId);
    }

    @Override
    protected boolean refreshRuleForOnline() {
        return refreshScheduleRule();
    }

    protected boolean refreshScheduleRule() {
        return refreshRule();
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        synchronized (this) {
            if (!"SCHEDULE_STATUS".equals(event.type) || scheduleRuleId.isBlank()
                    || !scheduleRuleId.equalsIgnoreCase(event.scheduleId)) {
                return false;
            }

            scheduleRule.id = scheduleRuleId;
            scheduleRule.name = firstNonBlank(event.scheduleName, scheduleRule.name);
            scheduleRule.type = firstNonBlank(event.scheduleType, scheduleRule.type);
            if ("SCHEDULE_STARTED".equals(event.subType)) {
                scheduleRule.startTime = firstNonBlank(event.startTime);
            } else if ("SCHEDULE_STOPPED".equals(event.subType) || "SCHEDULE_COMPLETED".equals(event.subType)) {
                scheduleRule.lastRun = firstNonBlank(event.endTime, event.timestamp);
            }
        }

        logger.debug("{}: Schedule webhook event received: {}.{}", thingId, event.type, event.subType);
        postChannelData();
        updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
        return true;
    }
}
