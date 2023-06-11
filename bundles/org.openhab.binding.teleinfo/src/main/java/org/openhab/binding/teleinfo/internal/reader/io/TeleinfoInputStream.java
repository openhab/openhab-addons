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
package org.openhab.binding.teleinfo.internal.reader.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.FrameUtil;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.binding.teleinfo.internal.serial.TeleinfoTicMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InputStream for Teleinfo {@link Frame} in serial port format.
 */
/**
 * The {@link TeleinfoInputStream} class is an {@link InputStream} to decode/read Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoInputStream extends InputStream {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoInputStream.class);

    private BufferedReader bufferedReader;
    private @Nullable String groupLine;
    private boolean autoRepairInvalidADPSgroupLine;
    private final TeleinfoTicMode ticMode;
    private final boolean verifyChecksum;
    private final Frame frame = new Frame();

    public TeleinfoInputStream(final InputStream teleinfoInputStream, TeleinfoTicMode ticMode) {
        this(teleinfoInputStream, false, ticMode, true);
    }

    public TeleinfoInputStream(final InputStream teleinfoInputStream, boolean autoRepairInvalidADPSgroupLine,
            TeleinfoTicMode ticMode) {
        this(teleinfoInputStream, autoRepairInvalidADPSgroupLine, ticMode, true);
    }

    public TeleinfoInputStream(final InputStream teleinfoInputStream, TeleinfoTicMode ticMode, boolean verifyChecksum) {
        this(teleinfoInputStream, false, ticMode, verifyChecksum);
    }

    public TeleinfoInputStream(final @Nullable InputStream teleinfoInputStream, boolean autoRepairInvalidADPSgroupLine,
            TeleinfoTicMode ticMode, boolean verifyChecksum) {
        if (teleinfoInputStream == null) {
            throw new IllegalArgumentException("Teleinfo inputStream is null");
        }

        this.autoRepairInvalidADPSgroupLine = autoRepairInvalidADPSgroupLine;
        this.ticMode = ticMode;
        this.verifyChecksum = verifyChecksum;
        this.bufferedReader = new BufferedReader(new InputStreamReader(teleinfoInputStream, StandardCharsets.US_ASCII));

        groupLine = null;
    }

    @Override
    public void close() throws IOException {
        logger.debug("close() [start]");
        bufferedReader.close();
        super.close();
        logger.debug("close() [end]");
    }

    /**
     * Returns the next frame.
     *
     * @return the next frame or null if end of stream
     * @throws TimeoutException
     * @throws IOException
     * @throws InvalidFrameException
     * @throws Exception
     */
    public synchronized @Nullable Frame readNextFrame() throws InvalidFrameException, IOException {
        // seek the next header frame
        while (!isHeaderFrame(groupLine)) {
            groupLine = bufferedReader.readLine();
            if (logger.isTraceEnabled()) {
                logger.trace("groupLine = {}", groupLine);
            }
            if (groupLine == null) { // end of stream
                logger.trace("end of stream reached !");
                return null;
            }
        }

        frame.clear();
        while ((groupLine = bufferedReader.readLine()) != null && !isHeaderFrame(groupLine)) {
            logger.trace("groupLine = {}", groupLine);
            String groupLineRef = groupLine;
            if (groupLineRef != null) {
                String[] groupLineTokens = groupLineRef.split(ticMode.getSeparator());
                if (ticMode == TeleinfoTicMode.HISTORICAL && groupLineTokens.length != 2 && groupLineTokens.length != 3
                        || ticMode == TeleinfoTicMode.STANDARD && groupLineTokens.length != 3
                                && groupLineTokens.length != 4) {
                    final String error = String.format("The groupLine '%1$s' is incomplete", groupLineRef);
                    throw new InvalidFrameException(error);
                }
                String labelStr = groupLineTokens[0];
                String valueString;
                String timestampString = null;
                switch (ticMode) {
                    default:
                        valueString = groupLineTokens[1];
                        break;
                    case STANDARD:
                        if (groupLineTokens.length == 3) {
                            valueString = groupLineTokens[1];
                        } else {
                            timestampString = groupLineTokens[1];
                            valueString = groupLineTokens[2];
                        }
                        break;
                }

                // verify integrity (through checksum)
                if (verifyChecksum) {
                    char checksum = groupLineRef.charAt(groupLineRef.length() - 1);
                    char computedChecksum = FrameUtil
                            .computeGroupLineChecksum(groupLineRef.substring(0, groupLineRef.length() - 2), ticMode);
                    if (computedChecksum != checksum) {
                        logger.trace("computedChecksum = {}", computedChecksum);
                        logger.trace("checksum = {}", checksum);
                        final String error = String.format(
                                "The groupLine '%s' is corrupted (integrity not checked). Actual checksum: '%s' / Expected checksum: '%s'",
                                groupLineRef, checksum, computedChecksum);
                        throw new InvalidFrameException(error);
                    }
                }

                Label label;
                try {
                    label = Label.getEnum(labelStr);
                } catch (IllegalArgumentException e) {
                    if (autoRepairInvalidADPSgroupLine && labelStr.startsWith(Label.ADPS.name())) {
                        // in this hardware issue, label variable is composed by label name and value. E.g:
                        // ADPS032
                        logger.warn("Try to auto repair malformed ADPS groupLine '{}'", labelStr);
                        label = Label.ADPS;
                        valueString = labelStr.substring(Label.ADPS.name().length());
                    } else {
                        final String error = String.format("The label '%s' is unknown", labelStr);
                        throw new InvalidFrameException(error);
                    }
                }

                frame.put(label, valueString);
                if (timestampString != null) {
                    frame.putTimestamp(label, timestampString);
                }
            }
        }

        return frame;
    }

    public boolean isAutoRepairInvalidADPSgroupLine() {
        return autoRepairInvalidADPSgroupLine;
    }

    public void setAutoRepairInvalidADPSgroupLine(boolean autoRepairInvalidADPSgroupLine) {
        this.autoRepairInvalidADPSgroupLine = autoRepairInvalidADPSgroupLine;
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("The 'read()' is not supported");
    }

    public static boolean isHeaderFrame(final @Nullable String line) {
        // A new teleinfo trame begin with '3' and '2' bytes (END OF TEXT et START OF TEXT)
        return (line != null && line.length() > 1 && line.codePointAt(0) == 3 && line.codePointAt(1) == 2);
    }
}
