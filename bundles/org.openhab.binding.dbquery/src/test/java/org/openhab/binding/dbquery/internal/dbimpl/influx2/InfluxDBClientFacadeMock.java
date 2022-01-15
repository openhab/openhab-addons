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
package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dbquery.internal.error.DatabaseException;

import com.influxdb.Cancellable;
import com.influxdb.query.FluxRecord;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class InfluxDBClientFacadeMock implements InfluxDBClientFacade {
    public static final String INVALID_QUERY = "invalid";
    public static final String EMPTY_QUERY = "empty";
    public static final String SCALAR_QUERY = "scalar";
    public static final String MULTIPLE_ROWS_QUERY = "multiple";

    public static final String SCALAR_RESULT = "scalarResult";
    public static final int MULTIPLE_ROWS_SIZE = 3;
    public static final String VALUE_COLUMN = "_value";
    public static final String TIME_COLUMN = "_time";
    public static final String MULTIPLE_ROWS_VALUE_PREFIX = "value";

    boolean connected;

    @Override
    public boolean connect() {
        connected = true;
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean disconnect() {
        connected = false;
        return true;
    }

    @Override
    public void query(String queryString, BiConsumer<Cancellable, FluxRecord> onNext,
            Consumer<? super Throwable> onError, Runnable onComplete) {
        if (!connected) {
            throw new DatabaseException("Client not connected");
        }

        if (INVALID_QUERY.equals(queryString)) {
            onError.accept(new RuntimeException("Invalid query"));
        } else if (EMPTY_QUERY.equals(queryString)) {
            onComplete.run();
        } else if (SCALAR_QUERY.equals(queryString)) {
            FluxRecord scalar = new FluxRecord(0);
            scalar.getValues().put("result", "_result");
            scalar.getValues().put("table", 0);
            scalar.getValues().put(VALUE_COLUMN, SCALAR_RESULT);
            onNext.accept(mock(Cancellable.class), scalar);
            onComplete.run();
        } else if (MULTIPLE_ROWS_QUERY.equals(queryString)) {
            onNext.accept(mock(Cancellable.class), createRowRecord(0, MULTIPLE_ROWS_VALUE_PREFIX + 1));
            onNext.accept(mock(Cancellable.class), createRowRecord(0, MULTIPLE_ROWS_VALUE_PREFIX + 2));
            onNext.accept(mock(Cancellable.class), createRowRecord(1, MULTIPLE_ROWS_VALUE_PREFIX + 3));
            onComplete.run();
        }
    }

    private static FluxRecord createRowRecord(int table, String value) {
        FluxRecord record = new FluxRecord(0);
        record.getValues().put("result", "_result");
        record.getValues().put("table", table);
        record.getValues().put(VALUE_COLUMN, value);
        record.getValues().put(TIME_COLUMN, Instant.now());
        record.getValues().put("_start", Instant.now());
        record.getValues().put("_stop", Instant.now());
        record.getValues().put("_measurement", "measurementName");
        return record;
    }
}
