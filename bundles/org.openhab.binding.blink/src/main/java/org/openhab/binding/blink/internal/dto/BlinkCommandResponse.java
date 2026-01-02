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
package org.openhab.binding.blink.internal.dto;

/**
 * The {@link BlinkCommandResponse} class is the DTO for all async api calls responses (thumbnail, arm/disarm,
 * motiondection on/off).
 *
 * @author Matthias Oesterheld - Initial contribution
 */
public class BlinkCommandResponse {

    public boolean complete;
    public int status;
    public String status_msg;
    public int status_code;
}
