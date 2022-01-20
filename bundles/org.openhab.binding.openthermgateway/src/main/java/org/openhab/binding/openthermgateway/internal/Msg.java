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
package org.openhab.binding.openthermgateway.internal;

/**
 * The {@link Msg} flag is used to indicate whether the message is sent for Reading, Writing
 * or both, based on the OpenTherm specification.
 * 
 * @author Arjen Korevaar - Initial contribution
 */
public enum Msg {
    READ,
    WRITE,
    READWRITE
}
