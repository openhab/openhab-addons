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

import org.openhab.binding.gruenbecksoftener.data.SoftenerEditData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerInputData;
import org.openhab.binding.gruenbecksoftener.data.SoftenerXmlResponse;
import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftenerHandler {

    private final Logger logger = LoggerFactory.getLogger(SoftenerHandler.class);

    private static final int MAX_READ_PERIOD = 15000;
    private static final int DEFAULT_REFRESH_PERIOD = 30;

    private ScheduledFuture<?> refreshJob;
    private ScheduledExecutorService executor;
    private Runnable runnable;
    private int refreshInterval;
    private long lastRun;

    public SoftenerHandler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Refreshes the
     *
     * @param config
     * @param executeFunction
     * @param responseParserFunction
     * @param scheduler
     * @param channelHandler
     * @param errorHandler
     * @param inputData
     * @return
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
                throw new RuntimeException(e);
            }
        } finally {
            scheduleRefreshJob();
        }
    }
}
