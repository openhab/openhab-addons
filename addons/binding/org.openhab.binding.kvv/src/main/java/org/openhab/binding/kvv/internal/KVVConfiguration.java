/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kvv.internal;

/**
 * The {@link KVVConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Maximilian Hess - Initial contribution
 */
public class KVVConfiguration {

    /**
     * the id of the station
     */
    public String stationId;

    /**
     * the update interval
     */
    public int updateInterval;

    /**
     * the (human-readable) id of the station
     */
    public String name;

}
