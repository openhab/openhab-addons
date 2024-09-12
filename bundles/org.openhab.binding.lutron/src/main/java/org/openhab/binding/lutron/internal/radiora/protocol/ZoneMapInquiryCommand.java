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
package org.openhab.binding.lutron.internal.radiora.protocol;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Requests an updated ZoneMap.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class ZoneMapInquiryCommand extends RadioRACommand {

    @Override
    public String getCommand() {
        return "ZMPI";
    }

    @Override
    public List<String> getArgs() {
        return Collections.emptyList();
    }
}
