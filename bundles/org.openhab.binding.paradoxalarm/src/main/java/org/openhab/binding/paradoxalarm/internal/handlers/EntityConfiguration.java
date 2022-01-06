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
package org.openhab.binding.paradoxalarm.internal.handlers;

/**
 * The {@link EntityConfiguration} Common configuration class used by all entities at the moment.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class EntityConfiguration {
    private int id;
    private boolean disarmEnabled;

    public int getId() {
        return id;
    }

    public boolean isDisarmEnabled() {
        return disarmEnabled;
    }
}
