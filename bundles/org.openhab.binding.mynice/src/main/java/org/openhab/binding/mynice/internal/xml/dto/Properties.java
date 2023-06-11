/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal.xml.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@XStreamAlias("Properties")
public class Properties {
    @XStreamAlias("DoorStatus")
    public String doorStatus;
    @XStreamAlias("Obstruct")
    public String obstruct;
    @XStreamAlias("T4_allowed")
    public Property t4allowed;
}
