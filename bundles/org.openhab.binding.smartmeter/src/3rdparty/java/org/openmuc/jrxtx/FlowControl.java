package org.openmuc.jrxtx;

/**
 * The flow control.
 * 
 * @see SerialPort#setFlowControl(FlowControl)
 * @see SerialPortBuilder#setFlowControl(FlowControl)
 */
public enum FlowControl {
    /**
     * No flow control.
     */
    NONE,

    /**
     * Hardware flow control on input and output (RTS/CTS).
     * 
     * <p>
     * Sets <b>RFR</b> (ready for receiving) formally known as <b>RTS</b> and the <b>CTS</b> (clear to send) flag.
     * </p>
     */
    RTS_CTS,

    /**
     * Software flow control on input and output.
     */
    XON_XOFF

}
