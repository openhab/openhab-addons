/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.casokitchen.internal.dto;

import static org.openhab.binding.casokitchen.internal.CasoKitchenBindingConstants.EMPTY;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link CallResponse} class wraps response values of an API call.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CallResponse {
    public int status = -1;
    public String responseString = EMPTY;
}
