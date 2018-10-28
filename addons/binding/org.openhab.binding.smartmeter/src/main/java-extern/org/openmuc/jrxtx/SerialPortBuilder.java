package org.openmuc.jrxtx;

import java.io.IOException;

/**
 * Builder class for SerialPorts. Provides a convenient way to set the various fields of a SerialPort.
 *
 * Example:
 *
 * <pre>
 * <code>
 * SerialPort port = newBuilder("/dev/ttyS0")
 *                   .setBaudRate(19200)
 *                   .setParity(Parity.EVEN)
 *                   .build();
 * InputStream is = port.getInputStream();
 * ..
 * </code>
 * </pre>
 */
@SuppressWarnings("deprecation")
public class SerialPortBuilder {

    private String portName;
    private int baudRate;
    private DataBits dataBits;
    private Parity parity;
    private StopBits stopBits;
    private FlowControl flowControl;

    private SerialPortBuilder(String portName) {
        this.portName = portName;
        this.baudRate = 9600;
        this.dataBits = DataBits.DATABITS_8;
        this.parity = Parity.EVEN;
        this.stopBits = StopBits.STOPBITS_1;
        this.flowControl = FlowControl.NONE;
    }

    /**
     * Constructs a new SerialPortBuilder with the default values.
     *
     * @param portName
     *            the serial port name. E.g. on Unix systems: <code>"/dev/ttyUSB0"</code> and on Unix
     * @return returns the new builder.
     */
    public static SerialPortBuilder newBuilder(String portName) {
        return new SerialPortBuilder(portName);
    }

    /**
     * Set the serial port name.
     *
     * @param portName
     *            the serial port name e.g. <code>"/dev/ttyUSB0"</code>
     * @return the serial port builder.
     */
    public SerialPortBuilder setPortName(String portName) {
        this.portName = portName;
        return this;
    }

    /**
     * Set the baud rate for the serial port. Values such as 9600 or 115200.
     *
     * @param baudRate
     *            the baud rate.
     * @return the serial port builder.
     *
     * @see SerialPortBuilder#setBaudRate(int)
     */
    public SerialPortBuilder setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        return this;
    }

    /**
     * Set the number of data bits transfered with the serial port.
     *
     * @param dataBits
     *            the number of dataBits.
     * @return the serial port builder.
     * @see SerialPort#setDataBits(DataBits)
     */
    public SerialPortBuilder setDataBits(DataBits dataBits) {
        this.dataBits = dataBits;
        return this;
    }

    /**
     * Set the parity of the serial port.
     *
     * @param parity
     *            the parity.
     * @return the serial port builder.
     * @see SerialPort#setParity(Parity)
     */
    public SerialPortBuilder setParity(Parity parity) {
        this.parity = parity;
        return this;
    }

    /**
     * Set the number of stop bits after each data bits.
     *
     * @param stopBits
     *            the number of stop bits.
     * @return the serial port builder.
     *
     * @see SerialPort#setStopBits(StopBits)
     */
    public SerialPortBuilder setStopBits(StopBits stopBits) {
        this.stopBits = stopBits;
        return this;
    }

    /**
     * Set the flow control type.
     *
     * @param flowControl
     *            the flow control.
     *
     * @return the serial port builder.
     *
     * @see SerialPort#setFlowControl(FlowControl)
     */
    public SerialPortBuilder setFlowControl(FlowControl flowControl) {
        this.flowControl = flowControl;
        return this;
    }

    /**
     * Combine all of the options that have been set and return a new SerialPort object.
     *
     * @return a new serial port object.
     * @throws IOException
     *             if an I/O exception occurred while opening the serial port.
     */
    public SerialPort build() throws IOException {
        return JRxTxPort.openSerialPort(portName, baudRate, parity, dataBits, stopBits, flowControl);
    }
}
