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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Command;

/**
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class FineOffsetGatewayQueryServiceTest {

    @Test
    void failedLiveDataReadIsReportedAsNull() {
        // distinguishes a failed poll from a valid empty response, so the handler does not age out channels
        FineOffsetGatewayQueryService service = new FineOffsetGatewayQueryService(new FineOffsetGatewayConfiguration(),
                null) {
            @Override
            protected byte @Nullable [] executeCommand(Command command) {
                return null;
            }
        };
        assertThat(service.getMeasuredValues(), nullValue());
    }
}
