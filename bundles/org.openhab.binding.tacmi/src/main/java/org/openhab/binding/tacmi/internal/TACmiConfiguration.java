/**
 * Copyright (c) 2015-2020 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal;

/**
 * The {@link TACmiConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Niessner (marvkis) - Initial contribution
 */
public class TACmiConfiguration {

    /**
     * host address of the C.M.I.
     */
    public String host;

    /**
     * CoE / CAN node ID we are representing
     */
    public int node;
}
