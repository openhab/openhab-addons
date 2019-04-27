/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.elkm1.internal.elk.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * The zone definition request to get the details about the zone.
 *
 * @author David Bennett - Initial Contribution
 */
@NonNullByDefault
public class ZoneDefinition extends ElkMessage {
    public ZoneDefinition() {
        super(ElkCommand.ZoneDefintionRequest);
    }

    @Override
    protected String getData() {
        return "";
    }
}
