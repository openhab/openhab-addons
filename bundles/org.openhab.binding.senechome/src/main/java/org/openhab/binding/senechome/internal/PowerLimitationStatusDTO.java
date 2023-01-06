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
package org.openhab.binding.senechome.internal;

import java.util.Date;

/**
 * The {@link PowerLimitationStatusDTO} class is used as a POJO to
 * in-memory persist the last limitation state change.
 *
 * @author Steven Schwarznau - Initial contribution
 *
 */
public class PowerLimitationStatusDTO {

    public boolean state = false;

    public long time = new Date().getTime();
}
