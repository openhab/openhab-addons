/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.message;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link MessageParser} class is a parser of Domintell messages
 *
 * @author Gabor Bicskei - Initial contribution
 */
public final class MessageParser {
    private static final List<Object[]> MESSAGE_PATTERNS = new ArrayList<>();

    static {
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^(FRO|ET2|Datasheet).*"), BaseMessage.Type.APPINFO});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^APPINFO.*"), BaseMessage.Type.START_APPINFO});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^END APPINFO.*"), BaseMessage.Type.END_APPINFO});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^[A-Z0-9]{3}[\\s0-9a-fA-F]{6}(-\\d){0,1}.*"), BaseMessage.Type.DATA});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("\\d{1,2}:\\d{1,2} \\d{1,2}/\\d{1,2}/\\d{1,2}"), BaseMessage.Type.SYSTEM_TIME, SystemTimeMessage.class});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^INFO:Session opened.*"), BaseMessage.Type.SESSION_OPENED});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^INFO:Auth failed.*"), BaseMessage.Type.AUTH_FAILED});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^INFO:Access denied.*"), BaseMessage.Type.ACCESS_DENIED});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^INFO:Session timeout.*"), BaseMessage.Type.SESSION_TIMEOUT});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^INFO:World.*"), BaseMessage.Type.WORLD});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^INFO:Session closed.*"), BaseMessage.Type.SESSION_CLOSED});
        MESSAGE_PATTERNS.add(new Object[]{Pattern.compile("^PONG.*"), BaseMessage.Type.PONG});
    }

    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(MessageParser.class);


    public MessageParser() {
    }

    public @Nullable BaseMessage parseMessage(String msg) {
        for (Object[] o: MESSAGE_PATTERNS) {
            Pattern p = (Pattern) o[0];
            Matcher matcher = p.matcher(msg);
            if (matcher.matches()) {
                BaseMessage.Type t = (BaseMessage.Type) o[1];
                switch (t) {
                    case DATA:
                        return new StatusMessage(msg);
                    case SYSTEM_TIME:
                        return new SystemTimeMessage(msg);
                    default:
                        return new BaseMessage(t, msg);
                }
            }
        }
        logger.debug("Unable to parse Domintell message: {}", msg);
        return null;
    }
}
