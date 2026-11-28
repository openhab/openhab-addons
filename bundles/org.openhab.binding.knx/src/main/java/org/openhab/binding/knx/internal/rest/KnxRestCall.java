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
package org.openhab.binding.knx.internal.rest;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.rest.dto.ErrorDTO;
import org.slf4j.LoggerFactory;

/**
 * Turns the result of a call of {@link KnxRestService} into a status and an entity, so that every failure is
 * answered with the one error body and the JAX-RS class only builds the response.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class KnxRestCall {
    static final int OK = 200;
    static final int NO_CONTENT = 204;

    /** What to answer: the status and the answer object, null for no body. */
    record Answer(int status, @Nullable Object entity) {
    }

    private KnxRestCall() {
    }

    /** Answers 200 with what the call returns. */
    static Answer run(String endpoint, Supplier<Object> call) {
        try {
            return new Answer(OK, call.get());
        } catch (KnxRestException e) {
            return failed(e);
        } catch (RuntimeException e) {
            return internal(endpoint, e);
        }
    }

    /** Answers 204 without a body when the call ends normally. */
    static Answer runWithoutBody(String endpoint, Runnable call) {
        try {
            call.run();
            return new Answer(NO_CONTENT, null);
        } catch (KnxRestException e) {
            return failed(e);
        } catch (RuntimeException e) {
            return internal(endpoint, e);
        }
    }

    static Answer failed(KnxRestException e) {
        return new Answer(e.status(), error(e.code(), e.text()));
    }

    /** Logs the class of the exception only: its message may hold a path or a value. */
    static Answer internal(String endpoint, RuntimeException e) {
        LoggerFactory.getLogger(KnxRestCall.class).warn("KNX REST {}: internal error {}", endpoint,
                e.getClass().getName());
        KnxRestError internal = KnxRestError.INTERNAL;
        return new Answer(internal.status(), error(internal.code(), internal.text()));
    }

    private static ErrorDTO error(String code, String text) {
        ErrorDTO error = new ErrorDTO();
        error.code = code;
        error.text = text;
        return error;
    }
}
