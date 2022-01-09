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
package org.openhab.binding.bmwconnecteddrive.internal.dto.compat;

import java.util.List;

/**
 * The {@link VehicleMessages} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @param <CBSMessage>
 */
public class VehicleMessages {
    public List<CCMMessageCompat> ccmMessages;
    public List<CBSMessageCompat> cbsMessages;
}
