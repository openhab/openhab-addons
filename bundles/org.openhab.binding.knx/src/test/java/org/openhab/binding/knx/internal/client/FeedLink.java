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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.FrameEvent;
import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXAddress;
import io.calimero.KNXTimeoutException;
import io.calimero.Priority;
import io.calimero.cemi.CEMILData;
import io.calimero.link.KNXLinkClosedException;
import io.calimero.link.NetworkLinkListener;
import io.calimero.link.medium.KNXMediumSettings;

/**
 * A stand-in link that hands made-up received frames to its listeners and records what is sent instead of looping
 * it back. A send can be made to fail, and a read request can be answered.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class FeedLink extends DummyKNXNetworkLink {
    private final IndividualAddress address;
    private final List<NetworkLinkListener> listeners = new CopyOnWriteArrayList<>();
    private final List<byte[]> sent = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentLinkedQueue<Exception> failures = new ConcurrentLinkedQueue<>();
    private volatile @Nullable Answer answer;
    public volatile boolean open = true;

    /** Says what a device answers to a read request, sent from within the send call as a gateway would. */
    interface Answer {
        @Nullable
        CEMILData to(GroupAddress destination, byte[] request);
    }

    public FeedLink(IndividualAddress address) {
        this.address = address;
    }

    @Override
    public KNXMediumSettings getKNXMedium() {
        return KNXMediumSettings.create(KNXMediumSettings.MEDIUM_TP1, address);
    }

    @Override
    public void addLinkListener(@Nullable NetworkLinkListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeLinkListener(@Nullable NetworkLinkListener l) {
        listeners.remove(l);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    /** The next send throws this instead of going out. */
    public void failNextSend(Exception failure) {
        failures.add(failure);
    }

    void answerReads(Answer answer) {
        this.answer = answer;
    }

    @Override
    public void sendRequestWait(@Nullable KNXAddress dst, @Nullable Priority p, byte @Nullable... nsdu)
            throws KNXTimeoutException, KNXLinkClosedException {
        Exception failure = failures.poll();
        if (failure instanceof KNXTimeoutException timeout) {
            throw timeout;
        } else if (failure instanceof KNXLinkClosedException closed) {
            throw closed;
        } else if (failure instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (nsdu == null) {
            return;
        }
        sent.add(nsdu.clone());
        Answer currentAnswer = answer;
        if (currentAnswer != null && dst instanceof GroupAddress group) {
            CEMILData response = currentAnswer.to(group, nsdu.clone());
            if (response != null) {
                receive(response);
            }
        }
    }

    /** Hands a frame to the listeners as the link does for a frame from the bus. */
    void receive(CEMILData frame) {
        FrameEvent event = new FrameEvent(this, frame);
        listeners.forEach(l -> l.indication(event));
    }

    void receive(IndividualAddress source, GroupAddress destination, byte... apdu) {
        receive(new CEMILData(CEMILData.MC_LDATA_IND, source, destination, apdu, Priority.LOW));
    }

    public List<byte[]> sent() {
        synchronized (sent) {
            return new ArrayList<>(sent);
        }
    }
}
