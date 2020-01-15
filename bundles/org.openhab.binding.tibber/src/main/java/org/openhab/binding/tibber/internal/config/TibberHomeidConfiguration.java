/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal.config;

/**
 * The {@link TibberHomeidConfiguration} class contains fields mapping home id parameters.
 *
 * @author Stian Kjoglum - Initial contribution
 */
public class TibberHomeidConfiguration {
    public String id;

    public String getHomeid() {
        return id;
    }

    public void setHomeid(String my_id) {
        this.id = my_id;
        return;
    }
}
