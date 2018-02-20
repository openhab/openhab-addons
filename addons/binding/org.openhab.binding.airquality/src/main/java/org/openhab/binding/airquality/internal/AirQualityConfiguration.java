/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.internal;

/**
 * The {@link AirQualityConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class AirQualityConfiguration {

    public String apikey;

    public String location;

    public Integer stationId;

    public Integer refresh;

}