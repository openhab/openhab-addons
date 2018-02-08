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
public class AutomationDetails extends Response {

    private final Logger logger = LoggerFactory.getLogger(AutomationDetails.class);

    @Override
    protected boolean check(@NonNull String message) {
        return message.matches("\\*#2\\*[0-9]+#9*10\\*1[0-2]\\*[0-9]+\\*000\\*0##");
    }

    @Override
    public void process(@NonNull String message, @NonNull ResponseListener e) {
        int where;
        int status;
        int level;
        String[] segments = message.split("[\\*#]");

        where = Integer.parseInt(segments[3]);

        status = Integer.parseInt(segments[6]);

        level = Integer.parseInt(segments[7]);

        logger.debug("Automation Details @ {} => status={}, level={}", where, status, level);
        e.onAutomationDetails(where, status, level);
    }

}
