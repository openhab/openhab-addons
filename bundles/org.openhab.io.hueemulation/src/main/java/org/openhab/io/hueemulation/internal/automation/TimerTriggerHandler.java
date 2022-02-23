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
package org.openhab.io.hueemulation.internal.automation;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseTriggerModuleHandler;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This trigger module time allows a trigger that is setup with a time (hours:minutes:seconds).
 * As soon as that time has run up, it will trigger.
 * <p>
 * A random factor and repeat times can also be configured.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class TimerTriggerHandler extends BaseTriggerModuleHandler implements Callable<Duration> {

    private final Logger logger = LoggerFactory.getLogger(TimerTriggerHandler.class);

    public static final String MODULE_TYPE_ID = "timer.TimerTrigger";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    public static final String CFG_REPEAT = "repeat";
    public static final String CFG_TIME = "time";
    public static final String CFG_TIME_RND = "randomizeTime";

    private final Scheduler scheduler;
    private final Duration duration;
    private @Nullable ScheduledCompletableFuture<?> schedule;

    private static class Config {
        int repeat = 1;
        String time = "";
        String randomizeTime = "";
    }

    Config config;

    public TimerTriggerHandler(Trigger module, Scheduler scheduler) {
        super(module);
        this.scheduler = scheduler;
        config = module.getConfiguration().as(Config.class);

        String[] fields = config.time.split(":");
        Duration d1 = Duration.parse(String.format("P%dH%dM%sS", fields[0], fields[1], fields[2]));

        // Take optional random time (a range-like parameter) into account
        if (!config.randomizeTime.isEmpty()) {
            fields = config.randomizeTime.split(":");
            Duration d2 = Duration.parse(String.format("P%dH%dM%sS", fields[0], fields[1], fields[2]));
            // The random time must be later a bigger value than time
            if (d1.compareTo(d2) >= 0) {
                throw new IllegalArgumentException();
            }
            // Compute the difference, turn in to seconds, get a random second value between 0 and that upper bound
            // and then add it to the base time
            Duration difference = d2.minus(d1);
            duration = d1.plus(Duration.ofSeconds(randomSeconds(difference.getSeconds())));
        } else {
            duration = d1;
        }
    }

    protected long randomSeconds(long maximum) {
        return Math.abs(new Random().nextLong()) % maximum;
    }

    @Override
    public synchronized void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        if (config.repeat != 0) {
            scheduleJob();
        }
    }

    private void scheduleJob() {
        schedule = scheduler.after(this, duration);
        logger.debug("Scheduled timer to expire in '{}' for trigger '{}'.", duration, module.getId());
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        ScheduledCompletableFuture<?> future = schedule;
        if (future != null) {
            future.cancel(true);
            logger.debug("cancelled job for trigger '{}'.", module.getId());
        }
    }

    @Override
    public Duration call() {
        ((TriggerHandlerCallback) callback).triggered(module, Map.of());
        config.repeat -= 1;
        if (config.repeat == 0) {
            schedule = null;
        } else {
            scheduleJob();
        }
        return duration;
    }
}
