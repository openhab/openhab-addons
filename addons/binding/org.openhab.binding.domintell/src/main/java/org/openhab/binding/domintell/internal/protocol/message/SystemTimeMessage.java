/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.openhab.binding.domintell.internal.protocol.message.BaseMessage.Type.SYSTEM_TIME;

/**
* The {@link SystemTimeMessage} class is responsible for parsing domintell system time messages
*
* @author Gabor Bicskei - Initial contribution
*/
public class SystemTimeMessage extends BaseMessage {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(SystemTimeMessage.class);

    /**
     * Date formatter for parsing Domintell system date
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    private Date dateTime;

    public SystemTimeMessage(String msg) {
        super(SYSTEM_TIME, msg);
        try {
            this.dateTime = DATE_FORMAT.parse(msg);
        } catch (ParseException e) {
            logger.debug("Unable to parse system date/time: {}", msg);
        }
    }

    public Date getDateTime() {
        return dateTime;
    }
}
