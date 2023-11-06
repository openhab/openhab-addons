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
package org.openhab.binding.nikobus.internal.protocol;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusCommand} class holds a command that can be send to Nikobus installation.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusCommand {
    public static class Result {
        private final Callable<String> callable;

        private Result(String result) {
            callable = () -> result;
        }

        private Result(Exception exception) {
            callable = () -> {
                throw exception;
            };
        }

        public String get() throws Exception {
            return callable.call();
        }
    }

    public static class ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);
        private final Consumer<Result> resultConsumer;
        private final int responseLength;
        private final int addressStart;
        private final String responseCode;
        private final AtomicBoolean isCompleted = new AtomicBoolean();

        private ResponseHandler(int responseLength, int addressStart, String responseCode,
                Consumer<Result> resultConsumer) {
            this.responseLength = responseLength;
            this.addressStart = addressStart;
            this.responseCode = responseCode;
            this.resultConsumer = resultConsumer;
        }

        public boolean isCompleted() {
            return isCompleted.get();
        }

        public boolean complete(String result) {
            return complete(new Result(result));
        }

        public boolean completeExceptionally(Exception exception) {
            return complete(new Result(exception));
        }

        private boolean complete(Result result) {
            if (isCompleted.getAndSet(true)) {
                return false;
            }

            try {
                resultConsumer.accept(result);
            } catch (RuntimeException e) {
                logger.warn("Processing result {} failed with {}", result, e.getMessage(), e);
            }

            return true;
        }

        public int getResponseLength() {
            return responseLength;
        }

        public int getAddressStart() {
            return addressStart;
        }

        public String getResponseCode() {
            return responseCode;
        }
    }

    private final String payload;
    private final @Nullable ResponseHandler responseHandler;

    public NikobusCommand(String payload) {
        this.payload = payload + '\r';
        this.responseHandler = null;
    }

    public NikobusCommand(String payload, int responseLength, int addressStart, String responseCode,
            Consumer<Result> resultConsumer) {
        this.payload = payload + '\r';
        this.responseHandler = new ResponseHandler(responseLength, addressStart, responseCode, resultConsumer);
    }

    public String getPayload() {
        return payload;
    }

    public @Nullable ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}
