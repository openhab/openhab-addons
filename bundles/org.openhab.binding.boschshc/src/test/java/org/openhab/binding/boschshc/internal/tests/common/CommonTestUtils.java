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
package org.openhab.binding.boschshc.internal.tests.common;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;

/**
 * Common utilities used in unit tests.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public final class CommonTestUtils {

    public static final String TEST_EXCEPTION_MESSAGE = "Test exception";

    private CommonTestUtils() {
        // Utility Class
    }

    public static List<Exception> getExecutionExceptionAndInterruptedExceptionArguments() {
        return List.of(new ExecutionException(TEST_EXCEPTION_MESSAGE, null),
                new InterruptedException(TEST_EXCEPTION_MESSAGE));
    }

    public static List<Exception> getExceutionExceptionAndRuntimeExceptionArguments() {
        return List.of(new ExecutionException(TEST_EXCEPTION_MESSAGE, null),
                new RuntimeException(TEST_EXCEPTION_MESSAGE));
    }

    public static List<Exception> getBoschShcAndExecutionAndTimeoutExceptionArguments() {
        return List.of(new BoschSHCException(TEST_EXCEPTION_MESSAGE),
                new ExecutionException(TEST_EXCEPTION_MESSAGE, null), new TimeoutException(TEST_EXCEPTION_MESSAGE));
    }

    public static List<Exception> getBoschShcAndExecutionAndTimeoutAndInterruptedExceptionArguments() {
        return List.of(new BoschSHCException(TEST_EXCEPTION_MESSAGE),
                new ExecutionException(TEST_EXCEPTION_MESSAGE, null), new TimeoutException(TEST_EXCEPTION_MESSAGE),
                new InterruptedException(TEST_EXCEPTION_MESSAGE));
    }

    public static List<Exception> getExecutionAndTimeoutAndInterruptedExceptionArguments() {
        return List.of(new ExecutionException(TEST_EXCEPTION_MESSAGE, null),
                new TimeoutException(TEST_EXCEPTION_MESSAGE), new InterruptedException(TEST_EXCEPTION_MESSAGE));
    }

    public static List<Exception> getExecutionAndTimeoutExceptionArguments() {
        return List.of(new ExecutionException(TEST_EXCEPTION_MESSAGE, null),
                new TimeoutException(TEST_EXCEPTION_MESSAGE));
    }
}
