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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link NhcNotification2} represents a Niko Home Control II notification. It is used when parsing the notification
 * response json.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
class NhcNotification2 {
    String status = "";
    String type = "";
    String timeOccured = "";
    String uuid = "";
    String text = "";
}
