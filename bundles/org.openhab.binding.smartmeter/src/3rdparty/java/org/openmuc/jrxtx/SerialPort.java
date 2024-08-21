package org.openmuc.jrxtx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serial port for communication using UARTs. Can be used for communication protocols such as RS-232 and RS-485.
 * <p>
 * A SerialPort is created using {@link SerialPortBuilder}. Once closed it cannot be opened again but has to be
 * recreated.
 */
public interface SerialPort extends Closeable {

    /**
     * Returns the input stream for this serial port.
     * <p>
     * Closing the returned InputStream will close the associated serial port.
     * 
     * @return the InputStream object that can be used to read from the port.
     * @throws IOException
     *             if an I/O error occurred
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the output stream for this serial port.
     * 
     * @return the OutputStream object that can be used to write to the port.
     * @throws IOException
     *             if an I/O error occurred.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Closes the serial port.
     * <p>
     * Also closes the associated input and output streams.
     * 
     * @throws IOException
     *             if an I/O error occurred.
     */
    @Override
    void close() throws IOException;

    /**
     * Returns whether the serial port is currently open and available for communication.
     * 
     * @return true if the serial port is closed.
     */
    boolean isClosed();

    /**
     * Get the name of the serial port.
     * 
     * @return the serial port name.
     */
    String getPortName();

    /**
     * Get the current data bits config.
     * 
     * @return the dataBits the data bits.
     */
    DataBits getDataBits();

    /**
     * Set the data bits.
     * 
     * @param dataBits
     *            the new dataBits.
     * @throws IOException
     *             if an I/O exception occurred when setting the new data bits..
     */
    void setDataBits(DataBits dataBits) throws IOException;

    /**
     * Get the parity.
     * 
     * @return the new parity.
     */
    Parity getParity();

    /**
     * Set the new parity.
     * 
     * @param parity
     *            the new parity.
     * @throws IOException
     *             if an I/O exception occurred when setting the new parity.
     */
    void setParity(Parity parity) throws IOException;

    /**
     * Get the current stop bits settings.
     * 
     * @return the stopBits the stop bits.
     */
    StopBits getStopBits();

    /**
     * Set the stop bits.
     * 
     * @param stopBits
     *            the stopBits to set
     * @throws IOException
     *             if an I/O exception occurred when setting the new stop bits.
     */
    void setStopBits(StopBits stopBits) throws IOException;

    /**
     * @return the baudRate setting.
     * 
     * @see #setBaudRate(int)
     */
    int getBaudRate();

    /**
     * Sets the baud rate of the system.
     * 
     * @param baudRate
     *            the new baud rate.
     * @throws IOException
     *             if an I/O exception occurred when setting the new baud rate.
     * 
     * @see #getBaudRate()
     */
    void setBaudRate(int baudRate) throws IOException;

    /**
     * Returns setting for serial port timeout. <code>0</code> returns implies that the option is disabled (i.e.,
     * timeout of infinity).
     * 
     * @return the serialPortTimeout.
     * 
     * @see #setSerialPortTimeout(int)
     */
    int getSerialPortTimeout();

    /**
     * Enable/disable serial port timeout with the specified timeout, in milliseconds. With this option set to a
     * non-zero timeout, a read() call on the InputStream associated with this serial port will block for only this
     * amount of time. If the timeout expires, a org.openmuc.jrxtx.SerialPortTimeoutExcepption is raised, though the
     * serial port is still valid. The option must be enabled prior to entering the blocking operation to have effect.
     * The timeout must be <code>&gt; 0</code>. A timeout of zero is interpreted as an infinite timeout.
     * 
     * @param serialPortTimeout
     *            the specified timeout, in milliseconds.
     * @throws IOException
     *             if there is an error in the underlying protocol.
     * 
     * @see #getSerialPortTimeout()
     */
    void setSerialPortTimeout(int serialPortTimeout) throws IOException;

    /**
     * Set the flow control type.
     * 
     * @param flowControl
     *            the flow control.
     * @throws IOException
     *             if an I/O exception occurred when setting the new baud rate.
     */
    void setFlowControl(FlowControl flowControl) throws IOException;

    /**
     * Get the current flow control settings.
     * 
     * @return the flow control.
     */
    FlowControl getFlowControl();
}
