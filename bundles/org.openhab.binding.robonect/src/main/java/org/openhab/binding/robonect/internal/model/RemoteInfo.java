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

    /**
     * Returns remote-start point 1.
     *
     * @return remote-start point 1, or {@code null} if not reported
     */
    public @Nullable Remote getRemoteStart1() {
        return remotestart_1;
    }

    /**
     * Returns remote-start point 2.
     *
     * @return remote-start point 2, or {@code null} if not reported
     */
    public @Nullable Remote getRemoteStart2() {
        return remotestart_2;
    }

    /**
     * Returns remote-start point 3.
     *
     * @return remote-start point 3, or {@code null} if not reported
     */
    public @Nullable Remote getRemoteStart3() {
        return remotestart_3;
    }

    /**
     * Returns remote-start point 4.
     *
     * @return remote-start point 4, or {@code null} if not reported
     */
    public @Nullable Remote getRemoteStart4() {
        return remotestart_4;
    }

    /**
     * Returns remote-start point 5.
     *
     * @return remote-start point 5, or {@code null} if not reported
     */
    public @Nullable Remote getRemoteStart5() {
        return remotestart_5;
    }
}
