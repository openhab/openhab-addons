/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

/**
 * The result of a service location reading
 * This will list the detected appliances and actuators (plugs)
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeServiceLocationInfo {

    public int serviceLocationId;
    public String name;
    public String timezone;
    public String lon;
    public String lat;
    public String electricityCost;
    public String electricityCurrency;
    public SmappeeServiceLocationInfoAppliance[] appliances;
    public SmappeeServiceLocationInfoActuator[] actuators;
    public SmappeeServiceLocationInfoSensor[] sensors;
}