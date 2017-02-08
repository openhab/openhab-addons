/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.job;

import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.quartz.JobDataMap;

/**
 * Calculates and publishes astro positional data.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PositionalJob extends AbstractBaseJob {

    @Override
    protected void executeJob(String thingUid, JobDataMap jobDataMap) {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUid);
        if (astroHandler != null) {
            astroHandler.publishPositionalInfo();
        }
    }
}
