/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.job;

import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.AstroHandlerFactory;

/**
 * Calculates and publishes astro positional data.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public class PositionalJob extends AbstractBaseJob {

    @Override
    protected void executeJob(String thingUid) {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUid);
        if (astroHandler != null) {
            astroHandler.publishPositionalInfo();
        }
    }
}
