/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Response to a remote-start point request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class RemoteInfo extends RobonectAnswer {
    private @Nullable Remote remotestart_1;
    private @Nullable Remote remotestart_2;
    private @Nullable Remote remotestart_3;
    private @Nullable Remote remotestart_4;
    private @Nullable Remote remotestart_5;

    public @Nullable Remote getRemoteStart1() {
        return remotestart_1;
    }

    public @Nullable Remote getRemoteStart2() {
        return remotestart_2;
    }

    public @Nullable Remote getRemoteStart3() {
        return remotestart_3;
    }

    public @Nullable Remote getRemoteStart4() {
        return remotestart_4;
    }

    public @Nullable Remote getRemoteStart5() {
        return remotestart_5;
    }
}
