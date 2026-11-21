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
package org.openhab.binding.tibberpulselocal.internal.stream;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Why a stream attempt ended. Control flow and the status mapping use {@link #getKind()}, never the message text.
 * The message only names the kind and never contains request data, so it is safe to log.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public final class StreamFailure extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * The failure categories. The status each one maps to is defined by the bridge handler.
     */
    public enum Kind {
        /** HTTP 401 or 403: the password was refused. Final, no retry until the configuration changes. */
        REJECTED,
        /** HTTP 404: the firmware has no stream endpoint. Retried slowly. */
        ENDPOINT_MISSING,
        /** Any other unexpected HTTP status, see {@link StreamFailure#getHttpStatus()}. */
        HTTP_STATUS,
        /** Name not resolvable, connection refused, connect timeout, or a failed write of the request. */
        UNREACHABLE,
        /** Close frame, end of stream, or reset after the request was sent. Another client may hold the slot. */
        CLOSED_BY_BRIDGE,
        /** No frame within the liveness limit. */
        SILENT,
        /** Websocket protocol violation, oversized message or HTTP head, or an unexpected internal error. */
        PROTOCOL
    }

    private final Kind kind;
    private final int httpStatus;

    public StreamFailure(Kind kind) {
        this(kind, 0, null);
    }

    public StreamFailure(Kind kind, @Nullable Throwable cause) {
        this(kind, 0, cause);
    }

    public StreamFailure(int httpStatus) {
        this(Kind.HTTP_STATUS, httpStatus, null);
    }

    private StreamFailure(Kind kind, int httpStatus, @Nullable Throwable cause) {
        super(httpStatus > 0 ? kind.name() + " " + httpStatus : kind.name(), cause);
        this.kind = kind;
        this.httpStatus = httpStatus;
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * @return the HTTP status for {@link Kind#HTTP_STATUS}, otherwise 0
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * @return true if the client does not retry after this failure
     */
    public boolean isFinal() {
        return kind == Kind.REJECTED;
    }
}
