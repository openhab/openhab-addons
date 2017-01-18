package org.openhab.binding.zoneminder.internal;

import java.io.IOException;
import java.security.GeneralSecurityException;

import name.eskildsen.zoneminder.IZoneMinderConnectionInfo;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;

public abstract class DataFetchRunnable implements Runnable {

    IZoneMinderConnectionInfo connection = null;;

    public DataFetchRunnable(IZoneMinderConnectionInfo conn) {
        connection = conn;
    }

    protected boolean isConnected() {

        if (connection == null) {
            return false;
        }

        return ZoneMinderFactory.validateConnection(connection);
    }

    protected void connect()
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException {
        if (!isConnected()) {
            ZoneMinderFactory.Initialize(connection, 5);

        }
    }

    @Override
    public abstract void run();

}
