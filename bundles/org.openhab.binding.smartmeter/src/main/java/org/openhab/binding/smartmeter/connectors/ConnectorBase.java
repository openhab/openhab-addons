/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.smartmeter.connectors;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.schedulers.Schedulers;

/**
 * Represents a basic implementation of a SML device connector.
 *
 * @author Matthias Steigenberger - Initial contribution
 * @author Mathias Gilhuber - Also-By
 */
@NonNullByDefault
public abstract class ConnectorBase<T> implements IMeterReaderConnector<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * The name of the port where the device is connected as defined in openHAB configuration.
     */
    private String portName;
    public static final int NUMBER_OF_RETRIES = 3;

    /**
     * Contructor for basic members.
     *
     * This constructor has to be called from derived classes!
     *
     */
    protected ConnectorBase(String portName) {
        this.portName = portName;
    }

    /**
     * Reads a new IO message.
     *
     * @param initMessage
     * @return The payload
     * @throws IOException Whenever there was a reading error.
     */
    protected abstract T readNext(byte @Nullable [] initMessage) throws IOException;

    /**
     * Whether to periodically emit values.
     *
     * @return whether periodically emit values or not
     */
    protected boolean applyPeriod() {
        return false;
    }

    /**
     * Whether to apply a retry handling whenever the read out failed.
     *
     * @return whether to use the retry handling or not.
     */
    protected boolean applyRetryHandling() {
        return false;
    }

    /**
     * If reading of meter values fail a retry handling shall be implemented here.
     * The provided publisher publishes the errors.
     * If a retry shall happen, the returned publisher shall emit an event-
     *
     * @param period
     * @param attempts
     * @return The publisher which emits events for a retry.
     */
    protected Publisher<?> getRetryPublisher(Duration period, Publisher<Throwable> attempts) {
        return Flowable.fromPublisher(attempts)
                .zipWith(Flowable.range(1, NUMBER_OF_RETRIES + 1), (throwable, attempt) -> {
                    if (throwable instanceof TimeoutException || attempt == NUMBER_OF_RETRIES + 1) {
                        throw new RuntimeException(throwable);
                    } else {
                        logger.warn("{}. reading attempt failed: {}. Retrying {}...", attempt, throwable.getMessage(),
                                getPortName());
                        return attempt;
                    }
                }).flatMap(i -> {
                    retryHook(i);
                    Duration additionalDelay = period;
                    logger.warn("Delaying retry by {}", additionalDelay);
                    return Flowable.timer(additionalDelay.toMillis(), TimeUnit.MILLISECONDS);
                });
    }

    /**
     * Called whenever a retry shall happen. Clients can do something here.
     *
     * @param retryCount The current number of retries
     */
    protected void retryHook(int retryCount) {
    }

    @Override
    public Publisher<T> getMeterValues(byte @Nullable [] initMessage, Duration period, ExecutorService executor) {
        Flowable<T> itemPublisher = Flowable.<T> create((emitter) -> {
            emitValues(initMessage, emitter);
        }, BackpressureStrategy.DROP);

        Flowable<T> result;
        if (applyPeriod()) {
            result = Flowable.timer(period.toMillis(), TimeUnit.MILLISECONDS, Schedulers.from(executor))
                    .flatMap(event -> itemPublisher).repeat();
        } else {
            result = itemPublisher;
        }
        if (applyRetryHandling()) {
            return result.retryWhen(attempts -> {
                return Flowable.fromPublisher(getRetryPublisher(period, attempts));
            });
        } else {
            return result;
        }
    }

    /**
     * Emitting of values shall happen here. If there is an event based emitting, this can be overriden.
     *
     * @param initMessage The message which shall be written before reading the values.
     * @param emitter The {@link FlowableEmitter} to emit the values to.
     * @throws IOException thrown if any reading error occurs.
     */
    protected void emitValues(byte @Nullable [] initMessage, FlowableEmitter<@Nullable T> emitter) throws IOException {
        if (!emitter.isCancelled()) {
            try {
                emitter.onNext(readNext(initMessage));
                emitter.onComplete();
            } catch (IOException e) {
                if (!emitter.isCancelled()) {
                    throw e;
                }
            }
        }
    }

    /**
     * Gets the name of the serial port.
     *
     * @return The actual name of the serial port.
     */
    public String getPortName() {
        return portName;
    }
}
