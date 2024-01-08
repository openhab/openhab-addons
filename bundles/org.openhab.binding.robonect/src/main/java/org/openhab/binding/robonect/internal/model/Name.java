/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

/**
 * Response holding the name of the mower used in the name command.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class Name extends RobonectAnswer {

    private String name;

    /**
     * @return - The mower name.
     */
    public String getName() {
        return name;
    }
}
