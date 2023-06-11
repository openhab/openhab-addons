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
package org.openhab.binding.mielecloud.internal.webservice.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Used as a notification to close SSE connections.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class MieleWebserviceDisconnectSseException extends RuntimeException {
    private static final long serialVersionUID = 607435177026345387L;
}
