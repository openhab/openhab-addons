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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioFlexScheduleRuleResponse;
import org.openhab.core.thing.Thing;

/**
 * Handles Rachio FlexScheduleRule Things.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioFlexScheduleHandler extends AbstractRachioScheduleHandler<RachioFlexScheduleRuleResponse> {
    protected volatile String flexScheduleRuleId = "";

    public RachioFlexScheduleHandler(Thing thing) {
        super(thing, FLEX_SCHEDULE, new RachioFlexScheduleRuleResponse());
    }

    @Override
    protected RachioFlexScheduleRuleResponse createRule() {
        return new RachioFlexScheduleRuleResponse();
    }

    @Override
    protected String getRuleId() {
        return flexScheduleRuleId;
    }

    @Override
    protected void setRuleId(String ruleId) {
        flexScheduleRuleId = ruleId;
    }

    @Override
    protected RachioFlexScheduleRuleResponse loadRule(RachioBridgeHandler handler, String requestedRuleId)
            throws RachioApiException {
        return loadFlexScheduleRule(handler, requestedRuleId);
    }

    protected RachioFlexScheduleRuleResponse loadFlexScheduleRule(RachioBridgeHandler handler,
            String requestedFlexScheduleRuleId) throws RachioApiException {
        return handler.getFlexScheduleRuleForInitialization(requestedFlexScheduleRuleId);
    }

    @Override
    protected boolean refreshRuleForOnline() {
        return refreshFlexScheduleRule();
    }

    protected boolean refreshFlexScheduleRule() {
        return refreshRule();
    }
}
