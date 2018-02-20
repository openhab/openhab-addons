/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ipp;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Lists;


/**
 * The {@link IppBindingConstants} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Tobias Braeutigam - Initial contribution
 */
public class IppBindingConstants {

    public static final String BINDING_ID = "ipp";
    
    // List of all Thing Type UIDs
    public static final ThingTypeUID PRINTER_THING_TYPE = new ThingTypeUID(BINDING_ID, "printer");

    // List of all Channel ids
    public static final String JOBS_CHANNEL = "jobs";
    public static final String WAITING_JOBS_CHANNEL = "waitingJobs";
    public static final String DONE_JOBS_CHANNEL = "doneJobs";

    public static final String PRINTER_PARAMETER_URL = "url";
    public static final String PRINTER_PARAMETER_NAME = "name";
    public static final String PRINTER_PARAMETER_REFRESH_INTERVAL = "refresh";
    
    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(IppBindingConstants.PRINTER_THING_TYPE);
}
