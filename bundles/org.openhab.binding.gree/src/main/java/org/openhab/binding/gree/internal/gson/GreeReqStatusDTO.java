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
 * The GreeReqStatus4Gson class is used by Gson to hold values to be send to
 * the Air Conditioner during requests for Status Updates to the
 * Air Conditioner.
 *
 * @author John Cunha - Initial contribution
 */
public class GreeReqStatusDTO {
    public String cid = null;
    public int i = 0;
    public String t = null;
    public int uid = 0;
    public String pack = null;
    public String tcid = null;
}
