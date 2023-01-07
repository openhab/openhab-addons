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
package org.openhab.binding.mielecloud.internal.webservice.retry;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceTransientException;

/**
 * {@link RetryStrategy} retrying a failing operation for a number of times.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class NTimesRetryStrategy implements RetryStrategy {
    private final int numberOfRetries;

    /**
     * Creates a new {@link NTimesRetryStrategy}.
     *
     * @param numberOfRetries The number of retries to make.
     * @throws IllegalArgumentException if {@code numberOfRetries} is smaller than zero.
     */
    public NTimesRetryStrategy(int numberOfRetries) {
        if (numberOfRetries < 0) {
            throw new IllegalArgumentException("Number of retries must not be negative.");
        }

        this.numberOfRetries = numberOfRetries;
    }

    @Override
    public <@Nullable T> T performRetryableOperation(Supplier<T> operation, Consumer<Exception> onException) {
        boolean obtainedReturnValue = false;
        T returnValue = null;
        MieleWebserviceTransientException lastException = null;
        for (int i = 0; !obtainedReturnValue && i < numberOfRetries + 1; i++) {
            try {
                returnValue = operation.get();
                obtainedReturnValue = true;
            } catch (MieleWebserviceTransientException e) {
                lastException = e;
                if (i < numberOfRetries) {
                    onException.accept(e);
                }
            }
        }

        if (!obtainedReturnValue) {
            throw new MieleWebserviceException(
                    "Unable to perform operation. Operation failed " + (numberOfRetries + 1) + " times.", lastException,
                    lastException == null ? ConnectionError.UNKNOWN : lastException.getConnectionError());
        } else {
            return returnValue;
        }
    }
}
