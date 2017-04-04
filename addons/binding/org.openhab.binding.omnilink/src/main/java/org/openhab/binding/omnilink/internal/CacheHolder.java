package org.openhab.binding.omnilink.internal;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class CacheHolder<T> implements FutureCallback<T> {

    private Logger logger = LoggerFactory.getLogger(CacheHolder.class);

    private volatile long lastUpdated = 0;
    private volatile T item;
    private long expiry;

    private Callable<ListenableFuture<T>> loader;
    private volatile ListenableFuture<T> currentLoad;

    public CacheHolder(long expiry, Loader<T> loader) {
        this.loader = loader;
    }

    public synchronized ListenableFuture<T> get() {
        if (!isExpired()) {
            return Futures.immediateFuture(item);
        } else {
            final ListenableFuture<T> possibleCurrentLoad = currentLoad;
            if (possibleCurrentLoad != null) {
                return possibleCurrentLoad;
            } else {
                try {
                    currentLoad = loader.call();
                    Futures.addCallback(currentLoad, this);
                    return currentLoad;
                } catch (Exception e) {
                    return Futures.immediateFailedFuture(e);
                }
            }
        }
    }

    @Override
    public void onFailure(Throwable arg0) {
        // Booo!
    }

    @Override
    public void onSuccess(T newItem) {
        item = newItem;
        lastUpdated = System.currentTimeMillis();
        currentLoad = null;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - lastUpdated) > expiry || item == null;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public static interface Loader<T> extends Callable<ListenableFuture<T>> {

    }

}