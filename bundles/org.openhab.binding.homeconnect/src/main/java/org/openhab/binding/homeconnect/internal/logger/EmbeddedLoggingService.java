/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.logger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * Central logging service. Use default logging system and also persist in separate storage.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
@Component(service = EmbeddedLoggingService.class, scope = ServiceScope.SINGLETON, configurationPid = "binding.homeconnect")
public class EmbeddedLoggingService {

    private static final String STORAGE_NAME = "homeconnect";
    private static final String KEY_EMBEDDED_LOGGING = "embeddedLogging";
    private static final int CLEANUP_INITIAL_DELAY = 10;
    private static final int CLEANUP_PERIOD = 3600;
    private static final int CLEANUP_MAX_AGE_IN_HOURS = 49;

    private final ScheduledExecutorService scheduler;
    private final Storage<String> storage;
    private final Gson gson;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(EmbeddedLoggingService.class);
    private final AtomicLong atomicLong;

    private @Nullable ScheduledFuture<?> cleanupFuture;

    private boolean loggingEnabled;

    @Activate
    public EmbeddedLoggingService(@Reference StorageService storageService) {
        storage = storageService.getStorage(STORAGE_NAME);
        gson = new GsonBuilder().create();
        scheduler = ThreadPoolManager.getScheduledPool(getClass().getSimpleName());
        atomicLong = new AtomicLong();
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        Object value = componentContext.getProperties().get(KEY_EMBEDDED_LOGGING);
        loggingEnabled = (value == null || (boolean) value);

        if (loggingEnabled) {
            logger.debug("Schedule log cleanup task.");
            this.cleanupFuture = scheduler.scheduleWithFixedDelay(() -> {
                removeOldEntries();
            }, CLEANUP_INITIAL_DELAY, CLEANUP_PERIOD, TimeUnit.SECONDS);
        }

        logger.debug("Activated embedded logging service. file-logging-enabled: {}", loggingEnabled);
    }

    @Deactivate
    protected void dispose() {
        ScheduledFuture<?> cleanupFuture = this.cleanupFuture;
        if (cleanupFuture != null) {
            logger.debug("Dispose log cleanup task.");
            cleanupFuture.cancel(true);
        }
    }

    public LogWriter getLogger(Class<?> clazz) {
        return new LogWriter(clazz, loggingEnabled, storage, atomicLong);
    }

    public List<Log> getLogEntries() {
        try {
            return storage.stream().sorted((o1, o2) -> {
                String[] keys0 = o1.getKey().split("-");
                String[] keys1 = o2.getKey().split("-");
                Long ps0 = Long.valueOf(keys0[0]);
                Long ps1 = Long.valueOf(keys1[0]);

                int result = ps0.compareTo(ps1);
                if (result != 0) {
                    return result;
                }
                Integer ss0 = 0;
                Integer ss1 = 0;
                if (keys0.length > 1 && keys1.length > 1) {
                    ss0 = Integer.valueOf(keys0[1]);
                    ss1 = Integer.valueOf(keys1[1]);
                }
                return ss0.compareTo(ss1);
            }).map(e -> {
                String serializedObject = e.getValue();
                if (serializedObject == null) {
                    throw new FatalLoggerException("Empty object in log storage");
                }
                return deserialize(serializedObject);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            clear();
            return new ArrayList<Log>();
        }
    }

    public void clear() {
        storage.getKeys().forEach(key -> storage.remove(key));
    }

    private Log deserialize(String serialized) {
        try {
            return gson.fromJson(serialized, Log.class);
        } catch (Exception e) {
            logger.error("Could not deserialize log entry. error={}\n{}", e.getMessage(), serialized);
            throw e;
        }
    }

    private void removeOldEntries() {
        ZonedDateTime minDateTime = ZonedDateTime.now().minusHours(CLEANUP_MAX_AGE_IN_HOURS);
        long min = minDateTime.toInstant().toEpochMilli();

        logger.debug("Remove old log entries (< {})", minDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        storage.getKeys().stream().filter(key -> (Long.valueOf(key.split("-")[0]) < min))
                .forEach(key -> storage.remove(key));
    }
}
