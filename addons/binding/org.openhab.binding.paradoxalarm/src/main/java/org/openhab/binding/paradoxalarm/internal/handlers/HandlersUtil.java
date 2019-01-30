/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HandlersUtil} Utility class where the common handlers logic resides.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class HandlersUtil {

    private final static Logger logger = LoggerFactory.getLogger(HandlersUtil.class);

    public static void cancelSchedule(@Nullable ScheduledFuture<?> schedule) {
        if (schedule != null) {
            boolean cancelingResult = schedule.cancel(true);
            String cancelingSuccessful = cancelingResult ? "successful" : "failed";
            logger.debug("Canceling schedule of {} is {}", schedule.toString(), cancelingSuccessful);
        }
    }
}
