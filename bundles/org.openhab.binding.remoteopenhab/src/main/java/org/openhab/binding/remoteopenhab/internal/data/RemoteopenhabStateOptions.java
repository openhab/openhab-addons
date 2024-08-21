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
package org.openhab.binding.remoteopenhab.internal.data;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Match content of field value from {@link RemoteopenhabChannelDescriptionChangedEvent} event payload when event is for
 * STATE_OPTIONS
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabStateOptions {

    public List<RemoteopenhabStateOption> options = List.of();
}
