package org.openhab.binding.ivtheatpump.internal.protocol;

import java.io.IOException;

public interface IVRConnection {
    /**
     * Connect to the receiver. Return true if the connection has succeeded or if already connected.
     *
     **/
    public void connect() throws IOException;

    /**
     * Return true if this manager is connected to the AVR.
     *
     * @return
     */
    public boolean isConnected();

    /**
     * Closes the connection.
     **/
    public void close();

    /**
     * TODO
     *
     * @return
     */
    public void write(byte[] data) throws IOException;

    /**
     * TODO
     *
     * @return
     */
    public int read() throws IOException;
}
