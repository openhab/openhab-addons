/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.fromJson;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.SDMError.SDMErrorDetails;

/**
 * Tests deserialization of {@link org.openhab.binding.nest.internal.sdm.dto.SDMError}s from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
public class SDMErrorTest {

    @Test
    public void deserializeFailedPreconditionError() throws IOException {
        SDMError error = fromJson("failed-precondition-error.json", SDMError.class);
        assertThat(error, is(notNullValue()));

        SDMErrorDetails details = error.error;
        assertThat(details, is(notNullValue()));
        assertThat(details.code, is(400));
        assertThat(details.message, is("Thermostat fan unavailable."));
        assertThat(details.status, is("FAILED_PRECONDITION"));
    }

    @Test
    public void deserializeNotFoundError() throws IOException {
        SDMError error = fromJson("not-found-error.json", SDMError.class);
        assertThat(error, is(notNullValue()));

        SDMErrorDetails details = error.error;
        assertThat(details, is(notNullValue()));
        assertThat(details.code, is(404));
        assertThat(details.message, is("Device enterprises/project-id/devices/device-id not found."));
        assertThat(details.status, is("NOT_FOUND"));
    }

    @Test
    public void deserializeResponseWithoutError() throws IOException {
        SDMError error = fromJson("list-devices-response.json", SDMError.class);
        assertThat(error, is(notNullValue()));

        SDMErrorDetails details = error.error;
        assertThat(details, is(nullValue()));
    }
}
