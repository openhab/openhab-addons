/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener.handler;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gruenbecksoftener.data.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerInputData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerXmlResponse;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the periodic read and the editing of parameters from the softener device.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class SoftenerHandler {

    private final Logger logger = LoggerFactory.getLogger(SoftenerHandler.class);

    private static final int MAX_READ_PERIOD = 15000;
    private static final int DEFAULT_REFRESH_PERIOD = 30;

    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private ScheduledExecutorService executor;
    private @NonNullByDefault({}) Runnable runnable;
    private int refreshInterval;
    private long lastRun;

    public SoftenerHandler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Starts the automatic refresh of the softener data. The result is emitted asynchronously.
     *
     * @param config The configuration for the refresh.
     * @param executeFunction The handler for reading out the data.
     * @param responseParserFunction The {@link Function} which converts a {@link String} result to an
     *            {@link SoftenerXmlResponse}.
     * @param channelHandler The {@link Consumer} which will be called for every emitted result.
     * @param errorHandler If any error occurred, it will be emitted to this {@link Consumer}.
     * @param inputData The data which is supplied to be read.
     */
    public void startAutomaticRefresh(SoftenerConfiguration config, ResponseFunction executeFunction,
            Function<String, SoftenerXmlResponse> responseParserFunction, Consumer<SoftenerXmlResponse> channelHandler,
            Consumer<Exception> errorHandler, Supplier<Stream<SoftenerInputData>> inputData) {

        BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> responseFunction = executeFunction
                .getResponseFunction(responseParserFunction, channelHandler);

        if (refreshJob == null || refreshJob.isCancelled()) {

            runnable = () -> {
                lastRun = System.currentTimeMillis();
                try {
                    responseFunction.accept(config, inputData.get());
                    // // Request new softener data
                    // // Group the data to collect at once by the group
                    // inputData.get().collect(Collectors.groupingBy(SoftenerInputData::getGroup)).entrySet().stream()
                    // .forEach(input -> responseFunction.accept(config, input.getValue().stream()));

                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                    errorHandler.accept(e);
                }
            };
            this.refreshInterval = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            scheduleRefreshJob();

        } else {
            throw new IllegalStateException("Refresh execution is already running. Please first stop it!");
        }
    }

    /**
     * Stops the automatic refresh if it was running.
     *
     */
    public void stopRefresh() {
        logger.debug("Stopping periodic refresh");
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
            runnable = null;
        }
    }

    private void scheduleRefreshJob() {
        if (runnable != null) {
            logger.debug("Scheduling periodic refresh with interval {} ", refreshInterval);
            refreshJob = executor.scheduleWithFixedDelay(runnable, getDelay(), refreshInterval, TimeUnit.SECONDS);
        }
    }

    private Callable<SoftenerXmlResponse> getEditRunnable(ResponseFunction executeFunction,
            SoftenerConfiguration config, SoftenerEditData edit,
            Function<String, SoftenerXmlResponse> responseConsumer) {
        return () -> {
            lastRun = System.currentTimeMillis();
            return executeFunction.editParameter(config, edit, responseConsumer);
        };
    }

    private int getDelay() {
        long a = (System.currentTimeMillis() - lastRun) - MAX_READ_PERIOD;
        if (a < 0) {
            logger.debug("Delaying next read out by {} ms", a * -1);
            return (int) a * -1;
        }
        return 0;
    }

    /**
     * Edits a paramater. An running automatic refresh job will be canceled and an appropriate delay will happen in case
     * a reading was done shortly, to not overload the softener http interface.
     *
     * @param executeFunction The handler to edit a parameter.
     * @param config The configuration which shall be applied.
     * @param edit The data which shall be edited.
     * @param responseConsumer The {@link Function} which converts a {@link String} result to a
     *            {@link SoftenerXmlResponse}.
     * @return The {@link SoftenerXmlResponse} which was written.
     * @throws IOException In case any reading error occurred.
     */
    public SoftenerXmlResponse editParameter(ResponseFunction executeFunction, SoftenerConfiguration config,
            SoftenerEditData edit, Function<String, SoftenerXmlResponse> responseConsumer) throws IOException {
        stopRefresh();
        try {
            return executor.schedule(getEditRunnable(executeFunction, config, edit, responseConsumer), getDelay(),
                    TimeUnit.MILLISECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException("Failed to edit parameter " + edit.getDatapointId(), e);
            }
        } finally {
            scheduleRefreshJob();
        }
    }
}
