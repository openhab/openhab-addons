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
package org.openhab.binding.hydrawise.internal.api.local.dto;

import java.util.List;

/**
 * The {@link Sensor} class models a sensor
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Sensor {

    public Integer input;

    public Integer type;

    public Integer mode;

    public Integer timer;

    public Integer offtimer;

    public String name;

    public Integer offlevel;

    public Integer active;

    public List<Object> relays = null;
}
