package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.influxdb.Cancellable;
import com.influxdb.query.FluxRecord;

@NonNullByDefault
public interface InfluxDBClientFacade {
    boolean connect();

    boolean isConnected();

    boolean disconnect();

    void query(String query, BiConsumer<Cancellable, FluxRecord> onNext, Consumer<? super Throwable> onError,
            Runnable onComplete);
}
