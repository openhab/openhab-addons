/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.gree.internal.gson;

/**
 *
 * The GreeExecResponsePack4Gson class is used by Gson to hold values returned from
 * the Air Conditioner during requests for Execution of Commands to the
 * Air Conditioner.
 *
 * @author John Cunha - Initial contribution
 */
public class GreeExecResponsePackDTO {
    public String t = null;
    public String mac = null;
    public int r = 0;
    public String[] opt = null;
    public Integer[] p = null;
    public Integer[] val = null;
}
