/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
@NonNullByDefault
public abstract class ConnectorBase<T> implements IMeterReaderConnector<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * The name of the port where the device is connected as defined in openHAB configuration.
     */
    private String portName;
    private static final int NUMBER_OF_RETRIES = 3;

    /**
     * Contructor for basic members.
     *
     * This constructor has to be called from derived classes!
     *
     * @throws IOException
     */
    protected ConnectorBase(String portName) {
        this.portName = portName;
    }

    /**
     * Reads a new IO message.
     *
     * @param initMessage
     * @return
     * @throws IOException
     */
    protected abstract T readNext(byte @Nullable [] initMessage) throws IOException;

    /**
     * Whether to periodically emit values.
     *
     * @return
     */
    protected boolean applyPeriod() {
        return false;
    }

    /**
     * Whether to apply a retry handling whenever the read out failed.
     *
     * @return
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
     * @return
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
                    Duration additionalDelay = Duration.ofSeconds(i);
                    logger.warn("Delaying retry by {}", additionalDelay);
                    return Flowable.timer(additionalDelay.toMillis(), TimeUnit.MILLISECONDS);
                });
    }

    @Override
    public Publisher<T> getMeterValues(byte @Nullable [] initMessage, Duration period, ExecutorService executor)
            throws IOException {
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

    protected void emitValues(byte @Nullable [] initMessage, FlowableEmitter<@Nullable T> emitter) throws IOException {
        if (!emitter.isCancelled()) {
            emitter.onNext(readNext(initMessage));
            emitter.onComplete();
        }
    }

    public String getPortName() {
        return portName;
    }
}
