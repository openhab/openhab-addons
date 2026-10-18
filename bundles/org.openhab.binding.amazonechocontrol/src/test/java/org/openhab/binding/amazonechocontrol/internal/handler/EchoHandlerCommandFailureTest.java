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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.amazonechocontrol.internal.handler.EchoHandler.isCommandToIdlePlayer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;

/**
 * The {@link EchoHandlerCommandFailureTest} checks which failed player command is an expected one, because Amazon
 * plays nothing on that device
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class EchoHandlerCommandFailureTest {
    private static final String UNSUPPORTED_PROVIDER_ERROR_TYPE = "UnsupportedProviderException:"
            + "http://internal.amazon.com/coral/com.amazon.dee.web.coral.model.nowplaying/";

    @Test
    public void aPlayerCommandToADeviceWithoutAnAmazonPlayerIsExpected() {
        assertThat(isCommandToIdlePlayer(failure(404, UNSUPPORTED_PROVIDER_ERROR_TYPE)), is(true));
    }

    @Test
    public void anotherErrorTypeWithTheSameStatusIsNotExpected() {
        assertThat(
                isCommandToIdlePlayer(failure(404,
                        "ResourceNotFoundException:"
                                + "http://internal.amazon.com/coral/com.amazon.dee.web.coral.model.nowplaying/")),
                is(false));
    }

    @Test
    public void theSameErrorTypeFromAnotherAmazonModelIsNotExpected() {
        assertThat(
                isCommandToIdlePlayer(failure(404,
                        "UnsupportedProviderException:"
                                + "http://internal.amazon.com/coral/com.amazon.dee.web.coral.model.musicprovider/")),
                is(false));
    }

    @Test
    public void aFailureWithoutAnErrorTypeIsNotExpected() {
        assertThat(isCommandToIdlePlayer(failure(404, "")), is(false));
        assertThat(isCommandToIdlePlayer(new ConnectionException("failed with code 404", 404, null)), is(false));
        assertThat(isCommandToIdlePlayer(new ConnectionException("failed with code 404")), is(false));
    }

    @Test
    public void theSameErrorTypeWithAnotherStatusIsNotExpected() {
        assertThat(isCommandToIdlePlayer(failure(400, UNSUPPORTED_PROVIDER_ERROR_TYPE)), is(false));
    }

    @Test
    public void aFailureWithoutAStatusIsNotExpected() {
        assertThat(isCommandToIdlePlayer(new ConnectionException("Request failed", new IllegalStateException())),
                is(false));
    }

    private static ConnectionException failure(int httpStatus, String amazonErrorType) {
        return new ConnectionException("failed with code " + httpStatus, httpStatus, amazonErrorType);
    }
}
