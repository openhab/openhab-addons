/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.parser;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.listener.ResponseListener;
import org.openhab.binding.openwebnet.internal.parser.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The {@link Parser} class parses the received Bytes from serial line, generate callback.
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public class Parser {

    private static final String OPENWEBNET_SEPARATOR = "##";

    private StringBuilder buffer;

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(Parser.class);

    private @Nullable InputStream currentStream; // only used to check if the InputStream has changed (no read from it)
    private ResponseListener listener;

    /**
     * Define the packet separator of OpenWebNet protocol
     *
     * @param listener listener to for callback
     *
     */
    public Parser(ResponseListener listener) {
        this.buffer = new StringBuilder();
        this.currentStream = null;
        this.listener = listener;
    }

    /**
     * Parse a received buffer for messages
     *
     * @param newData buffer to parse
     */
    // Only needed for Maven
    // @SuppressWarnings("null")
    public void parse(ByteBuffer newData) {
        buffer.append(StandardCharsets.US_ASCII.decode(newData));
        int position;
        logger.debug("Input buffer (<{}>, length={})", buffer.toString(), buffer.length());
        while ((position = buffer.indexOf(Parser.OPENWEBNET_SEPARATOR)) >= 0) {
            position += Parser.OPENWEBNET_SEPARATOR.length();
            @Nullable
            String message = buffer.substring(0, position);
            if (message != null) {
                logger.debug("{} message {} received", this, message);
                buffer.delete(0, position);
                Response answer = Response.find(message);
                if (answer == null) {
                    logger.warn("\"{}\" received but not understood", message);
                } else {
                    logger.debug("\"{}\" received ", answer.getClass().getSimpleName());
                    answer.process(message, listener);
                }
            }
            logger.debug("Input buffer deleted {} -> (<{}>, length={})", position, buffer.toString(), buffer.length());
        }
        logger.debug("No more message to parse (<{}>, length={})", buffer.toString(), buffer.length());
    }

    /**
     * Check is stream provided is the same as current one, if not reset the input buffer.
     * This method need to be called prior to can any parse().
     */
    public void checkInput(InputStream stream) {
        if (currentStream == null) {
            currentStream = stream;
        }
        if (!stream.equals(currentStream)) {
            logger.info("Stream changed ... Flush {} character(s) <{}>", buffer.length(), buffer.toString());
            buffer.delete(0, buffer.length());
        }
    }

}
