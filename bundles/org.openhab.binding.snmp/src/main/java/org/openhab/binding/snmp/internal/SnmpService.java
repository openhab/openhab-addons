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
package org.openhab.binding.snmp.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.snmp4j.CommandResponder;
import org.snmp4j.PDU;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseListener;

/**
 * The {@link SnmpService} is responsible for SNMP communication
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public interface SnmpService {

    public void addCommandResponder(CommandResponder listener);

    public void removeCommandResponder(CommandResponder listener);

    public void send(PDU pdu, Target target, @Nullable Object userHandle, ResponseListener listener) throws IOException;
}
