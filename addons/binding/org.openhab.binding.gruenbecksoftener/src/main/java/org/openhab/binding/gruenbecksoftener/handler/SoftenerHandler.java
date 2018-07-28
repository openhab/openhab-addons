package org.openhab.binding.gruenbecksoftener.handler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.gruenbecksoftener.internal.SoftenerConfiguration;
import org.openhab.binding.gruenbecksoftener.json.SoftenerXmlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftenerHandler {

    private final Logger logger = LoggerFactory.getLogger(SoftenerHandler.class);

    private ScheduledFuture<?> refreshJob;

    private static final int DEFAULT_REFRESH_PERIOD = 30;

    public Supplier<Boolean> startAutomaticRefresh(SoftenerConfiguration config, ResponseFunction executeFunction,
            Function<String, SoftenerXmlResponse> responseParserFunction, ScheduledExecutorService scheduler,
            Consumer<SoftenerXmlResponse> channelHandler, Consumer<Exception> errorHandler,
            Supplier<Stream<SoftenerInputData>> inputData) {

        BiConsumer<SoftenerConfiguration, Stream<SoftenerInputData>> responseFunction = executeFunction
                .getResponseFunction(responseParserFunction, channelHandler);

        if (refreshJob == null || refreshJob.isCancelled()) {

            Runnable runnable = () -> {
                try {
                    // Request new softener data
                    inputData.get().collect(Collectors.groupingBy(SoftenerInputData::getGroup)).entrySet().stream()
                            .forEach(input -> responseFunction.accept(config, input.getValue().stream()));

                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                    errorHandler.accept(e);
                }
            };

            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        }
        return () -> refreshJob.cancel(true);
    }

}
