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
package org.openhab.binding.homematic.internal.communicator.message;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles BIN-RPC request and response messages to communicate with a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BinRpcMessage implements RpcRequest<byte[]>, RpcResponse {
    private final Logger logger = LoggerFactory.getLogger(BinRpcMessage.class);

    public enum TYPE {
        REQUEST,
        RESPONSE
    }

    private Object[] messageData;
    private byte[] binRpcData;
    private int offset;

    private String methodName;
    private TYPE type;
    private int args;
    private Charset encoding;

    public BinRpcMessage(String methodName, Charset encoding) {
        this(methodName, TYPE.REQUEST, encoding);
    }

    /**
     * Creates a new request with the specified methodName.
     */
    public BinRpcMessage(String methodName, TYPE type, Charset encoding) {
        this.methodName = methodName;
        this.type = type;
        this.encoding = encoding;
        createHeader();
    }

    /**
     * Decodes a BIN-RPC message from the given InputStream.
     */
    public BinRpcMessage(InputStream is, boolean methodHeader, Charset encoding) throws IOException {
        this.encoding = encoding;
        byte[] sig = new byte[8];
        int length = is.read(sig, 0, 4);
        if (length != 4) {
            throw new EOFException("Only " + length + " bytes received reading signature");
        }
        validateBinXSignature(sig);
        length = is.read(sig, 4, 4);
        if (length != 4) {
            throw new EOFException("Only " + length + " bytes received reading message length");
        }
        int datasize = (new BigInteger(Arrays.copyOfRange(sig, 4, 8))).intValue();
        byte[] payload = new byte[datasize];
        int offset = 0;
        int currentLength;

        while (offset < datasize && (currentLength = is.read(payload, offset, datasize - offset)) != -1) {
            offset += currentLength;
        }
        if (offset != datasize) {
            throw new EOFException("Only " + offset + " bytes received while reading message payload, expected "
                    + datasize + " bytes");
        }
        byte[] message = new byte[sig.length + payload.length];
        System.arraycopy(sig, 0, message, 0, sig.length);
        System.arraycopy(payload, 0, message, sig.length, payload.length);

        decodeMessage(message, methodHeader);
    }

    private void validateBinXSignature(byte[] sig) throws UnsupportedEncodingException {
        if (sig[0] != 'B' || sig[1] != 'i' || sig[2] != 'n') {
            throw new UnsupportedEncodingException("No BinX signature");
        }
    }

    /**
     * Decodes a BIN-RPC message from the given byte array.
     */
    public BinRpcMessage(byte[] message, boolean methodHeader, Charset encoding) throws IOException, ParseException {
        this.encoding = encoding;
        if (message.length < 8) {
            throw new EOFException("Only " + message.length + " bytes received");
        }
        validateBinXSignature(message);
        decodeMessage(message, methodHeader);
    }

    private void decodeMessage(byte[] message, boolean methodHeader) throws IOException {
        binRpcData = message;

        offset = 8;

        if (methodHeader) {
            methodName = readString();
            readInt();
        }
        generateResponseData();
    }

    public void setType(TYPE type) {
        binRpcData[3] = type == TYPE.RESPONSE ? (byte) 1 : (byte) 0;
    }

    private void generateResponseData() throws IOException {
        offset = 8 + (methodName != null ? methodName.length() + 8 : 0);
        List<Object> values = new ArrayList<>();
        while (offset < binRpcData.length) {
            values.add(readRpcValue());
        }
        messageData = values.toArray();
        values.clear();
    }

    private void createHeader() {
        binRpcData = new byte[256];
        addString("Bin ");
        setType(type);
        addInt(0); // placeholder content length
        if (methodName != null) {
            addInt(methodName.length());
            addString(methodName);
            addInt(0); // placeholder arguments
        }
        setInt(4, offset - 8);
    }

    /**
     * Adds arguments to the method.
     */
    @Override
    public void addArg(Object argument) {
        addObject(argument);
        setInt(4, offset - 8);

        if (methodName != null) {
            setInt(12 + methodName.length(), ++args);
        }
    }

    public int getArgCount() {
        return args;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public byte[] createMessage() {
        trimBinRpcData();
        return binRpcData;
    }

    private void trimBinRpcData() {
        byte[] trimmed = new byte[offset];
        System.arraycopy(binRpcData, 0, trimmed, 0, offset);
        binRpcData = trimmed;
    }

    @Override
    public Object[] getResponseData() {
        return messageData;
    }

    // read rpc values
    private int readInt() {
        byte[] bi = new byte[4];
        System.arraycopy(binRpcData, offset, bi, 0, 4);
        offset += 4;
        return (new BigInteger(bi)).intValue();
    }

    private long readInt64() {
        byte[] bi = new byte[8];
        System.arraycopy(binRpcData, offset, bi, 0, 8);
        offset += 8;
        return (new BigInteger(bi)).longValue();
    }

    private String readString() {
        int len = readInt();
        offset += len;
        return new String(binRpcData, offset - len, len, encoding);
    }

    private Object readRpcValue() throws IOException {
        int type = readInt();
        switch (type) {
            case 1:
                return Integer.valueOf(readInt());
            case 2:
                return binRpcData[offset++] != 0 ? Boolean.TRUE : Boolean.FALSE;
            case 3:
                return readString();
            case 4:
                int mantissa = readInt();
                int exponent = readInt();
                BigDecimal bd = new BigDecimal((double) mantissa / (double) (1 << 30) * Math.pow(2, exponent));
                return bd.setScale(6, RoundingMode.HALF_DOWN).doubleValue();
            case 5:
                return new Date(readInt() * 1000);
            case 0xD1:
                // Int64
                return Long.valueOf(readInt64());
            case 0x100:
                // Array
                int numElements = readInt();
                Collection<Object> array = new ArrayList<>();
                while (numElements-- > 0) {
                    array.add(readRpcValue());
                }
                return array.toArray();
            case 0x101:
                // Struct
                numElements = readInt();
                Map<String, Object> struct = new TreeMap<>();
                while (numElements-- > 0) {
                    String name = readString();
                    struct.put(name, readRpcValue());
                }
                return struct;

            default:
                for (int i = 0; i < binRpcData.length; i++) {
                    logger.info("{} {}", Integer.toHexString(binRpcData[i]), (char) binRpcData[i]);
                }
                throw new IOException("Unknown data type " + type);
        }
    }

    private void setInt(int position, int value) {
        int temp = offset;
        offset = position;
        addInt(value);
        offset = temp;
    }

    private void addByte(byte b) {
        if (offset == binRpcData.length) {
            byte[] newdata = new byte[binRpcData.length * 2];
            System.arraycopy(binRpcData, 0, newdata, 0, binRpcData.length);
            binRpcData = newdata;
        }
        binRpcData[offset++] = b;
    }

    private void addInt(int value) {
        addByte((byte) (value >> 24));
        addByte((byte) (value >> 16));
        addByte((byte) (value >> 8));
        addByte((byte) (value));
    }

    private void addDouble(double value) {
        double tmp = Math.abs(value);
        int exp = 0;
        if (tmp != 0 && tmp < 0.5) {
            while (tmp < 0.5) {
                tmp *= 2;
                exp--;
            }
        } else {
            while (tmp >= 1) {
                tmp /= 2;
                exp++;
            }
        }
        if (value < 0) {
            tmp *= -1;
        }
        int mantissa = (int) Math.round(tmp * 0x40000000);
        addInt(mantissa);
        addInt(exp);
    }

    private void addString(String string) {
        byte[] sd = string.getBytes(encoding);
        for (byte ch : sd) {
            addByte(ch);
        }
    }

    private void addList(Collection<?> collection) {
        for (Object object : collection) {
            addObject(object);
        }
    }

    private void addObject(Object object) {
        if (object.getClass() == String.class) {
            addInt(3);
            String string = (String) object;
            addInt(string.length());
            addString(string);
        } else if (object.getClass() == Boolean.class) {
            addInt(2);
            addByte(((Boolean) object).booleanValue() ? (byte) 1 : (byte) 0);
        } else if (object.getClass() == Integer.class) {
            addInt(1);
            addInt(((Integer) object).intValue());
        } else if (object.getClass() == Double.class) {
            addInt(4);
            addDouble(((Double) object).doubleValue());
        } else if (object.getClass() == Float.class) {
            addInt(4);
            BigDecimal bd = new BigDecimal((Float) object);
            addDouble(bd.setScale(6, RoundingMode.HALF_DOWN).doubleValue());
        } else if (object.getClass() == BigDecimal.class) {
            addInt(4);
            addDouble(((BigDecimal) object).setScale(6, RoundingMode.HALF_DOWN).doubleValue());
        } else if (object.getClass() == BigInteger.class) {
            addInt(4);
            addDouble(((BigInteger) object).doubleValue());
        } else if (object.getClass() == Date.class) {
            addInt(5);
            addInt((int) ((Date) object).getTime() / 1000);
        } else if (object instanceof List<?> list) {
            addInt(0x100);
            addInt(list.size());
            addList(list);
        } else if (object instanceof Map<?, ?> map) {
            addInt(0x101);
            addInt(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = (String) entry.getKey();
                if (key != null) {
                    addInt(key.length());
                    addString(key);
                    addList(Collections.singleton(entry.getValue()));
                }
            }
        }
    }

    public String toBinString() {
        return Arrays.toString(createMessage());
    }

    @Override
    public String toString() {
        try {
            trimBinRpcData();
            generateResponseData();
            return RpcUtils.dumpRpcMessage(methodName, messageData);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
