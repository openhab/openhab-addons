/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.influxdb.Cancellable;
import com.influxdb.query.FluxRecord;

/**
 * Facade to Influx2 client to facilitate testing
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public interface InfluxDBClientFacade {
    boolean connect();

    boolean isConnected();

    boolean disconnect();

    void query(String query, BiConsumer<Cancellable, FluxRecord> onNext, Consumer<? super Throwable> onError,
            Runnable onComplete);
}
