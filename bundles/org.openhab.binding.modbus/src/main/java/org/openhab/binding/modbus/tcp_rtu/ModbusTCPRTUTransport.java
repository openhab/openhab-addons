/**
 * Copyright 2002-2010 jamod development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***/

package org.openhab.binding.modbus.tcp_rtu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.io.BytesInputStream;
import net.wimpi.modbus.io.BytesOutputStream;
import net.wimpi.modbus.msg.ModbusMessage;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.util.ModbusUtil;

/**
 * Class that implements the Modbus transport flavor.
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 * @author John Charlton - ModbusRTUTransport on which this code is based
 * @author Dieter Wimberger - ModbusRTUTransport and ModbusTCPTransport on which this code is based
 */
public class ModbusTCPRTUTransport extends ModbusTCPTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusTCPRTUTransport.class);

    private byte[] m_InBuffer;
    private BytesOutputStream m_ByteOut;
    private BytesOutputStream m_ByteInOut;
    private byte[] lastRequest = null;

    /**
     * Constructs a new <tt>ModbusTransport</tt> instance,
     * for a given <tt>Socket</tt>.
     * <p>
     *
     * @param socket the <tt>Socket</tt> used for message transport.
     */
    public ModbusTCPRTUTransport(Socket socket) {
        super(socket);
    }// constructor

    @Override
    public void writeMessage(ModbusMessage msg) throws ModbusIOException {
        try {
            int len;
            synchronized (m_ByteOut) {
                // write message to byte out
                m_ByteOut.reset();
                msg.setHeadless();
                msg.writeTo(m_ByteOut);
                len = m_ByteOut.size();
                int[] crc = ModbusUtil.calculateCRC(m_ByteOut.getBuffer(), 0, len);
                m_ByteOut.writeByte(crc[0]);
                m_ByteOut.writeByte(crc[1]);
                // write message
                len = m_ByteOut.size();
                byte buf[] = m_ByteOut.getBuffer();
                m_Output.write(buf, 0, len); // PDU + CRC
                m_Output.flush();
                logger.debug("Sent: {}", ModbusUtil.toHex(buf, 0, len));
                lastRequest = new byte[len];
                System.arraycopy(buf, 0, lastRequest, 0, len);
            }

        } catch (Exception ex) {
            throw new ModbusIOException("I/O failed to write");
        }
    }// writeMessage

    // This is required for the slave that is not supported
    @Override
    public ModbusRequest readRequest() throws ModbusIOException {
        throw new RuntimeException("Operation not supported.");
    } // readRequest

    @Override
    public ModbusResponse readResponse() throws ModbusIOException {
        ModbusResponse response = null;
        int dlength = 0;

        try {
            // read to function code, create request and read function specific bytes
            synchronized (m_ByteIn) {
                int uid = m_Input.read();
                logger.trace("Managed to read at least one byte");
                if (uid != -1) {
                    int fc = m_Input.read();
                    m_ByteInOut.reset();
                    m_ByteInOut.writeByte(uid);
                    m_ByteInOut.writeByte(fc);

                    // create response to acquire length of message
                    response = ModbusResponse.createModbusResponse(fc);
                    response.setHeadless();

                    getResponse(fc, m_ByteInOut);

                    dlength = m_ByteInOut.size() - 2; // less the crc
                    logger.debug("Response: {}", ModbusUtil.toHex(m_ByteInOut.getBuffer(), 0, dlength + 2));

                    m_ByteIn.reset(m_InBuffer, dlength);

                    // check CRC
                    int[] crc = ModbusUtil.calculateCRC(m_InBuffer, 0, dlength); // does not include CRC
                    if (ModbusUtil.unsignedByteToInt(m_InBuffer[dlength]) != crc[0]
                            || ModbusUtil.unsignedByteToInt(m_InBuffer[dlength + 1]) != crc[1]) {
                        throw new IOException("CRC Error in received frame: " + dlength + " bytes: "
                                + ModbusUtil.toHex(m_ByteIn.getBuffer(), 0, dlength));
                    }
                } else {
                    throw new IOException("Error reading response (EOF)");
                }

                // read response
                m_ByteIn.reset(m_InBuffer, dlength);
                response.readFrom(m_ByteIn);
            } // synchronized
            return response;
        } catch (Exception ex) {
            final String errMsg = "failed to read";
            logger.debug("Last request: {}", ModbusUtil.toHex(lastRequest));
            logger.debug("{}: {}", errMsg, ex.getMessage());
            throw new ModbusIOException(
                    String.format("I/O exception: %s %s", ex.getClass().getSimpleName(), ex.getMessage()));
        }
    }// readResponse

    @Override
    protected void prepareStreams(Socket socket) throws IOException {
        m_Input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        m_Output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        m_ByteOut = new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH);
        m_InBuffer = new byte[Modbus.MAX_MESSAGE_LENGTH];
        m_ByteIn = new BytesInputStream(m_InBuffer);
        m_ByteInOut = new BytesOutputStream(m_InBuffer);
    }// prepareStreams

    private void getResponse(int fn, BytesOutputStream out) throws IOException {
        int bc = -1, bc2 = -1, bcw = -1;
        int inpBytes = 0;
        byte inpBuf[] = new byte[256];

        switch (fn) {
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x0C:
            case 0x11: // report slave ID version and run/stop state
            case 0x14: // read log entry (60000 memory reference)
            case 0x15: // write log entry (60000 memory reference)
            case 0x17:
                // read the byte count;
                bc = m_Input.read();
                out.write(bc);
                // now get the specified number of bytes and the 2 CRC bytes
                inpBytes = m_Input.read(inpBuf, 0, bc + 2);
                out.write(inpBuf, 0, inpBytes);
                if (inpBytes != bc + 2) {
                    logger.debug("awaited {} bytes, but received {}", (bc + 2), inpBytes);
                }
                break;
            case 0x05:
            case 0x06:
            case 0x0B:
            case 0x0F:
            case 0x10:
                // read status: only the CRC remains after address and function code
                inpBytes = m_Input.read(inpBuf, 0, 6);
                out.write(inpBuf, 0, inpBytes);
                break;
            case 0x07:
            case 0x08:
                // read status: only the CRC remains after address and function code
                inpBytes = m_Input.read(inpBuf, 0, 3);
                out.write(inpBuf, 0, inpBytes);
                break;
            case 0x16:
                // eight bytes in addition to the address and function codes
                inpBytes = m_Input.read(inpBuf, 0, 8);
                out.write(inpBuf, 0, inpBytes);
                break;
            case 0x18:
                // read the byte count word
                bc = m_Input.read();
                out.write(bc);
                bc2 = m_Input.read();
                out.write(bc2);
                bcw = ModbusUtil.makeWord(bc, bc2);
                // now get the specified number of bytes and the 2 CRC bytes
                inpBytes = m_Input.read(inpBuf, 0, bcw + 2);
                out.write(inpBuf, 0, inpBytes);
                break;
        }
    }// getResponse

}// class ModbusTCPRTUTransport
