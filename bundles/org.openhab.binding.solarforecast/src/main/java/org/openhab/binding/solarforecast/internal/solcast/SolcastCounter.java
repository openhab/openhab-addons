/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast.internal.solcast;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Instant;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.storage.Storage;

/**
 * The {@link SolcastCounter} counts API response statuses for Solcast API calls. They are stored in Storage to persist
 * them across restarts. Reset happens each day at 00:00 UTC time for all time zones.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastCounter {
    private final Storage<String> storage;
    private final String identifier;
    private JSONObject counter = new JSONObject();
    private Instant lastCounterReset = Utils.now();

    public SolcastCounter(String identifier, Storage<String> storage) {
        this.identifier = identifier;
        this.storage = storage;
        restore();
    }

    public synchronized JSONObject get() {
        // before each get check regarding day switch
        checkCounterReset();
        return new JSONObject(counter.toString());
    }

    public synchronized void count(int status) {
        switch (status) {
            case 200 -> counter.put(HTTP_OK, counter.getInt(HTTP_OK) + 1);
            case 429 -> counter.put(HTTP_TOO_MANY_REQUESTS, counter.getInt(HTTP_TOO_MANY_REQUESTS) + 1);
            default -> counter.put(HTTP_OTHER, counter.getInt(HTTP_OTHER) + 1);
        }
        storeCounter();
    }

    private void restore() {
        String counterString = storage.get(identifier + CALL_COUNT_APPENDIX);
        String lastResetString = storage.get(identifier + CALL_COUNT_DATE_APPENDIX);
        if (counterString != null && lastResetString != null) {
            lastCounterReset = Instant.parse(lastResetString);
            counter = new JSONObject(counterString);
        } else {
            counter = getNewCounter();
            lastCounterReset = Utils.now();
        }
    }

    private JSONObject getNewCounter() {
        return new JSONObject("{\"200\":0,\"429\":0,\"other\":0}");
    }

    /**
     * Solcast API counter is reseted daily at 00:00 UTC for all timezones
     */
    private void checkCounterReset() {
        Instant now = Utils.now();
        if (lastCounterReset.atZone(ZoneId.of("UTC")).getDayOfMonth() != now.atZone(ZoneId.of("UTC")).getDayOfMonth()) {
            counter = getNewCounter();
            storeCounter();
            lastCounterReset = now;
        }
    }

    private void storeCounter() {
        storage.put(identifier + CALL_COUNT_DATE_APPENDIX, lastCounterReset.toString());
        storage.put(identifier + CALL_COUNT_APPENDIX, counter.toString());
    }

    @Override
    public String toString() {
        return "SolcastCounter [identifier=" + identifier + ", counter=" + counter + ", lastCounterReset="
                + lastCounterReset + "]";
    }
}
