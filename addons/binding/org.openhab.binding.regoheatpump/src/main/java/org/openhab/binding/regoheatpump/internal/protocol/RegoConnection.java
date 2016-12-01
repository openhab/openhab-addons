package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;

public interface RegoConnection {
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
     * Writes data bytes through the channel.
     *
     * @return
     */
    public void write(byte[] data) throws IOException;

    /**
     * Reads a single byte from channel.
     *
     * @return
     */
    public int read() throws IOException;
}
