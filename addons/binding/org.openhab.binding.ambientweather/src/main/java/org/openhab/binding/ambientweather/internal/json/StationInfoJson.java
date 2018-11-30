/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.json;

/**
 * The {@link StationInfoJson} is the JSON object
 * returned by the Ambient Weather API that describes the
 * user-provided name and location of the weather station.
 *
 * @author Mark Hilbush - Initial Contribution
 */
public class StationInfoJson {
    /*
     * The name given to the station by the user
     */
    public String name;

    /*
     * The location given to the station by the user
     */
    public String location;
}
