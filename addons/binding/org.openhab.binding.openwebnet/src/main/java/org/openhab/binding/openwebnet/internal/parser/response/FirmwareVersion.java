/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.parser.response;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.openwebnet.internal.listener.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antoine Laydier
 *
 */
public class FirmwareVersion extends Response {

    private final Logger logger = LoggerFactory.getLogger(FirmwareVersion.class);

    @Override
    protected boolean check(@NonNull String message) {
        return message.matches("\\*#13\\*[0-9]*[#]?[9]?\\*16\\*[0-9]+\\*[0-9]+\\*[0-9]+##");
    }

    @Override
    public void process(@NonNull String message, @NonNull ResponseListener e) {
        int where;
        String[] segments = message.split("\\*");
        try {
            if ("".equals(segments[2])) {
                where = 0;
            } else {
                where = Integer.parseInt(segments[2].split("#")[0]);
            }
        } catch (NumberFormatException e2) {
            logger.warn("Firmware Version conversion problem ({})", message);
            return;
        }
        String version = segments[4] + "." + segments[5] + "." + segments[6].split("#")[0];
        logger.debug("FirmwareVersion of {} = {}", where, version);
        e.onFirmwareVersion(where, version);
    }

}
