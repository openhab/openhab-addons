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
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.GroupAddress;
import io.calimero.KNXException;
import io.calimero.KNXTimeoutException;
import io.calimero.Priority;
import io.calimero.datapoint.Datapoint;
import io.calimero.dptxlator.DPTXlator;
import io.calimero.link.KNXLinkClosedException;
import io.calimero.link.KNXNetworkLink;
import io.calimero.process.ProcessCommunicatorImpl;
import io.calimero.secure.SecureApplicationLayer;

/**
 * The library's process communicator, telling the caller of a read when the request has gone out. The library
 * sends the request and waits for the answer inside one call, and a missing confirmation and a missing answer both
 * end it with a {@link KNXTimeoutException}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ReadTrackingCommunicator extends ProcessCommunicatorImpl {
    private final ThreadLocal<@Nullable Runnable> requestSent = new ThreadLocal<>();

    ReadTrackingCommunicator(KNXNetworkLink link, SecureApplicationLayer sal) throws KNXLinkClosedException {
        // secure group addresses are written with their group key; GO diagnostics would need device tool keys
        super(link, sal, false);
    }

    /**
     * Reads like {@link #read(Datapoint)}.
     *
     * @param requestSent run on the calling thread once the link has taken the request, before the wait for the
     *            answer begins; it must not throw
     */
    String read(Datapoint datapoint, Runnable requestSent) throws KNXException, InterruptedException {
        this.requestSent.set(requestSent);
        try {
            return read(datapoint);
        } finally {
            this.requestSent.remove();
        }
    }

    @Override
    @NonNullByDefault({})
    protected void send(GroupAddress dst, Priority p, int service, DPTXlator t)
            throws KNXTimeoutException, KNXLinkClosedException, InterruptedException {
        super.send(dst, p, service, t);
        Runnable sent = requestSent.get();
        if (sent != null) {
            sent.run();
        }
    }
}
