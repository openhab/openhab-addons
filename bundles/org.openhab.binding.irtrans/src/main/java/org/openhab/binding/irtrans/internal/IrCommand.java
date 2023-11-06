/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.irtrans.internal;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IrCommand} is a structure to store and manipulate infrared command
 * in various formats
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class IrCommand {

    private Logger logger = LoggerFactory.getLogger(IrCommand.class);

    /**
     *
     * Each infrared command is in essence a sequence of characters/pointers
     * that refer to pulse/pause timing pairs. So, in order to build an infrared
     * command one has to collate the pulse/pause timings as defined by the
     * sequence
     *
     * PulsePair is a small datastructure to capture each pulse/pair timing pair
     *
     */
    private class PulsePair {
        public int Pulse;
        public int Pause;
    }

    private String remote;
    private String command;
    private String sequence;
    private List<PulsePair> pulsePairs;
    private int numberOfRepeats;
    private int frequency;
    private int frameLength;
    private int pause;
    private boolean startBit;
    private boolean repeatStartBit;
    private boolean noTog;
    private boolean rc5;
    private boolean rc6;

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<PulsePair> getPulsePairs() {
        return pulsePairs;
    }

    public void setPulsePairs(ArrayList<PulsePair> pulsePairs) {
        this.pulsePairs = pulsePairs;
    }

    public int getNumberOfRepeats() {
        return numberOfRepeats;
    }

    public void setNumberOfRepeats(int numberOfRepeats) {
        this.numberOfRepeats = numberOfRepeats;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getFrameLength() {
        return frameLength;
    }

    public void setFrameLength(int frameLength) {
        this.frameLength = frameLength;
    }

    public int getPause() {
        return pause;
    }

    public void setPause(int pause) {
        this.pause = pause;
    }

    public boolean isStartBit() {
        return startBit;
    }

    public void setStartBit(boolean startBit) {
        this.startBit = startBit;
    }

    public boolean isRepeatStartBit() {
        return repeatStartBit;
    }

    public void setRepeatStartBit(boolean repeatStartBit) {
        this.repeatStartBit = repeatStartBit;
    }

    public boolean isNoTog() {
        return noTog;
    }

    public void setNoTog(boolean noTog) {
        this.noTog = noTog;
    }

    public boolean isRc5() {
        return rc5;
    }

    public void setRc5(boolean rc5) {
        this.rc5 = rc5;
    }

    public boolean isRc6() {
        return rc6;
    }

    public void setRc6(boolean rc6) {
        this.rc6 = rc6;
    }

    /**
     * Matches two IrCommands Commands match if they have the same remote and
     * the same command
     *
     * @param anotherCommand the another command
     * @return true, if successful
     */
    public boolean matches(IrCommand anotherCommand) {
        return (matchRemote(anotherCommand) && matchCommand(anotherCommand));
    }

    /**
     * Match remote fields of two IrCommands In everything we do in the IRtrans
     * binding, the "*" stands for a wilcard character and will match anything
     *
     * @param S the infrared command
     * @return true, if successful
     */
    private boolean matchRemote(IrCommand S) {
        if ("*".equals(getRemote()) || "*".equals(S.getRemote())) {
            return true;
        } else {
            return S.getRemote().equals(getRemote());
        }
    }

    /**
     * Match command fields of two IrCommands
     *
     * @param S the infrared command
     * @return true, if successful
     */
    private boolean matchCommand(IrCommand S) {
        if ("*".equals(command) || "*".equals(S.command)) {
            return true;
        } else {
            return S.command.equals(command);
        }
    }

    /**
     * Convert/Parse the IRCommand into a ByteBuffer that is compatible with the
     * IRTrans devices
     *
     * @return the byte buffer
     */
    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(44 + 210 + 1);

        // skip first byte for length - we will fill it in at the end
        byteBuffer.position(1);

        // Checksum - 1 byte - not used in the ethernet version of the device
        byteBuffer.put((byte) 0);

        // Command - 1 byte - not used
        byteBuffer.put((byte) 0);

        // Address - 1 byte - not used
        byteBuffer.put((byte) 0);

        // Mask - 2 bytes - not used
        byteBuffer.putShort((short) 0);

        // Number of pulse pairs - 1 byte
        byte[] byteSequence = sequence.getBytes(StandardCharsets.US_ASCII);
        byteBuffer.put((byte) (byteSequence.length));

        // Frequency - 1 byte
        byteBuffer.put((byte) frequency);

        // Mode / Flags - 1 byte
        byte modeFlags = 0;
        if (startBit) {
            modeFlags = (byte) (modeFlags | 1);
        }
        if (repeatStartBit) {
            modeFlags = (byte) (modeFlags | 2);
        }
        if (rc5) {
            modeFlags = (byte) (modeFlags | 4);
        }
        if (rc6) {
            modeFlags = (byte) (modeFlags | 8);
        }
        byteBuffer.put(modeFlags);

        // Pause timings - 8 Shorts = 16 bytes
        for (int i = 0; i < pulsePairs.size(); i++) {
            byteBuffer.putShort((short) Math.round(pulsePairs.get(i).Pause / 8));
        }
        for (int i = pulsePairs.size(); i <= 7; i++) {
            byteBuffer.putShort((short) 0);
        }

        // Pulse timings - 8 Shorts = 16 bytes
        for (int i = 0; i < pulsePairs.size(); i++) {
            byteBuffer.putShort((short) Math.round(pulsePairs.get(i).Pulse / 8));
        }
        for (int i = pulsePairs.size(); i <= 7; i++) {
            byteBuffer.putShort((short) 0);
        }

        // Time Counts - 1 Byte
        byteBuffer.put((byte) pulsePairs.size());

        // Repeats - 1 Byte
        byte repeat = (byte) 0;
        repeat = (byte) numberOfRepeats;
        if (frameLength > 0) {
            repeat = (byte) (repeat | 128);
        }
        byteBuffer.put(repeat);

        // Repeat Pause or Frame Length - 1 byte
        if ((repeat & 128) == 128) {
            byteBuffer.put((byte) frameLength);
        } else {
            byteBuffer.put((byte) pause);
        }

        // IR pulse sequence
        try {
            byteBuffer.put(sequence.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            logger.warn("An exception occurred while encoding the sequence : '{}'", e.getMessage(), e);
        }

        // Add <CR> (ASCII 13) at the end of the sequence
        byteBuffer.put((byte) ((char) 13));

        // set the length of the byte sequence
        byteBuffer.flip();
        byteBuffer.position(0);
        byteBuffer.put((byte) (byteBuffer.limit() - 1));
        byteBuffer.position(0);

        return byteBuffer;
    }

    /**
     * Convert the the infrared command to a Hexadecimal notation/string that
     * can be interpreted by the IRTrans device
     *
     * Convert the first 44 bytes to hex notation, then copy the remainder (= IR
     * command piece) as ASCII string
     *
     * @return the byte buffer in Hex format
     */
    public ByteBuffer toHEXByteBuffer() {
        byte[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        ByteBuffer byteBuffer = toByteBuffer();
        byte[] toConvert = new byte[byteBuffer.limit()];
        byteBuffer.get(toConvert, 0, byteBuffer.limit());

        byte[] converted = new byte[toConvert.length * 2];

        for (int k = 0; k < toConvert.length - 1; k++) {
            converted[2 * k] = hexDigit[(toConvert[k] >> 4) & 0x0f];
            converted[2 * k + 1] = hexDigit[toConvert[k] & 0x0f];

        }

        ByteBuffer convertedBuffer = ByteBuffer.allocate(converted.length);
        convertedBuffer.put(converted);
        convertedBuffer.flip();

        return convertedBuffer;
    }

    /**
     * Convert 'sequence' bit of the IRTrans compatible byte buffer to a
     * Hexidecimal string
     *
     * @return the string
     */
    public String sequenceToHEXString() {
        byte[] byteArray = toHEXByteArray();
        return new String(byteArray, 88, byteArray.length - 88 - 2);
    }

    /**
     * Convert the IRTrans compatible byte buffer to a string
     *
     * @return the string
     */
    public String toHEXString() {
        return new String(toHEXByteArray());
    }

    /**
     * Convert the IRTrans compatible byte buffer to a byte array.
     *
     * @return the byte[]
     */
    public byte[] toHEXByteArray() {
        return toHEXByteBuffer().array();
    }
}
