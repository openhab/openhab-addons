/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.parser.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.listener.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public final class LightStatusChanged extends Response {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(LightStatusChanged.class);

    @Override
    public boolean check(String message) {
        return message.matches("\\*1\\*[0-9]+\\*[0-9]+#9##");
    }

    @Override
    public void process(String message, ResponseListener e) {
        logger.trace("Process \"{}\".", message);
        String[] splittedString = message.split("\\*");
        int where;
        int state;
        if (splittedString.length != 4) {
            logger.warn("String \"{}\" is not a correct MsgStatusChanged (length = {}).", message,
                    splittedString.length);
            return;
        }
        String text = splittedString[2];
        try {
            state = Integer.parseInt(text);
            /*
             * if (state == null) {
             * logger.warn("String \"{}\" is not a correct MsgStatusChanged (State invalid \"{}\").", message, text);
             * return;
             * }
             */
        } catch (NumberFormatException exception) {
            logger.warn("String \"{}\" is not a correct MsgStatusChanged (State is not a number \"{}\").", message,
                    text);
            return;
        }
        try {
            text = splittedString[3].split("#")[0];
            where = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            logger.warn("String \"{}\" is not a correct MsgStatusChanged (Where is not a number \"{}\").", message,
                    text);
            return;
        }
        logger.trace("=> Call StatusChanged on {} to state {}.", where, state);
        e.onLightStatusChange(where, state);
    }

}
