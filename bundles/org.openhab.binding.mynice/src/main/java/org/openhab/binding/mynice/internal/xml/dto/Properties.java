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
    public enum DoorStatus {
        OPEN(false),
        CLOSED(false),
        OPENING(true),
        CLOSING(true),
        STOPPED(false);

        public final boolean moving;

        DoorStatus(boolean moving) {
            this.moving = moving;
        }
    }

    @XStreamAlias("DoorStatus")
    private String doorStatus;
    @XStreamAlias("Obstruct")
    private String obstruct;
    @XStreamAlias("T4_allowed")
    public Property t4allowed;

    public boolean obstructed() {
        return "1".equals(obstruct);
    }

    public DoorStatus status() {
        return DoorStatus.valueOf(doorStatus.toUpperCase());
    }
}
