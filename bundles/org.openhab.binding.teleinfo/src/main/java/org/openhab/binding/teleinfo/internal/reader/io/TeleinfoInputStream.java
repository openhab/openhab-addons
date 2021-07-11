/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
    private final Frame frame = new Frame();

    public TeleinfoInputStream(final InputStream teleinfoInputStream) {
        this(teleinfoInputStream, false);
    }

    public TeleinfoInputStream(final @Nullable InputStream teleinfoInputStream,
            boolean autoRepairInvalidADPSgroupLine) {
        if (teleinfoInputStream == null) {
            throw new IllegalArgumentException("Teleinfo inputStream is null");
        }

        this.autoRepairInvalidADPSgroupLine = autoRepairInvalidADPSgroupLine;
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
                String[] groupLineTokens = groupLineRef.split("\\s");
                if (groupLineTokens.length != 2 && groupLineTokens.length != 3) {
                    final String error = String.format("The groupLine '%1$s' is incomplete", groupLineRef);
                    throw new InvalidFrameException(error);
                }
                String labelStr = groupLineTokens[0];
                String valueString = groupLineTokens[1];

                // verify integrity (through checksum)
                char checksum = (groupLineTokens.length == 3 ? groupLineTokens[2].charAt(0) : ' ');
                char computedChecksum = FrameUtil.computeGroupLineChecksum(labelStr, valueString);
                if (computedChecksum != checksum) {
                    logger.trace("computedChecksum = {}", computedChecksum);
                    logger.trace("checksum = {}", checksum);
                    final String error = String.format(
                            "The groupLine '%s' is corrupted (integrity not checked). Actual checksum: '%s' / Expected checksum: '%s'",
                            groupLineRef, checksum, computedChecksum);
                    throw new InvalidFrameException(error);
                }

                Label label;
                try {
                    label = Label.valueOf(labelStr);
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

    private boolean isHeaderFrame(final @Nullable String line) {
        // A new teleinfo trame begin with '3' and '2' bytes (END OF TEXT et START OF TEXT)
        return (line != null && line.length() > 1 && line.codePointAt(0) == 3 && line.codePointAt(1) == 2);
    }
}
