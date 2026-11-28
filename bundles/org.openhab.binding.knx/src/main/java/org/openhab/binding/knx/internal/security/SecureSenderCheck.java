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
package org.openhab.binding.knx.internal.security;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Bookkeeping for one connection: a secured device obeys only senders that ETS linked to the group address, so this
 * remembers where the address openHAB sends from is not linked. It only reports; sending is never blocked.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class SecureSenderCheck {
    private static final int MAX_LISTED = 4;

    private enum Problem {
        NONE,
        NOT_IN_KEYRING,
        LINKED_TO_NOTHING
    }

    private final KeyringFacts facts;
    private final IndividualAddress own;
    private final Problem problem;
    private final Set<GroupAddress> unlinked = new ConcurrentSkipListSet<>(
            Comparator.comparingInt(GroupAddress::getRawAddress));

    public SecureSenderCheck(KeyringFacts facts, IndividualAddress ownAddress) {
        this.facts = facts;
        this.own = ownAddress;
        var iface = facts.interfaceOf(ownAddress);
        if (iface.isEmpty()) {
            problem = Problem.NOT_IN_KEYRING;
        } else if (iface.get().linkedSecuredGroups().isEmpty()) {
            problem = Problem.LINKED_TO_NOTHING;
        } else {
            problem = Problem.NONE;
        }
    }

    public IndividualAddress ownAddress() {
        return own;
    }

    /** {@return true if the own address has no interface entry in the keyring} */
    public boolean ownAddressNotInKeyring() {
        return problem == Problem.NOT_IN_KEYRING;
    }

    /** {@return true if the keyring has an entry for the own address, but ETS linked it to no secured address} */
    public boolean ownAddressLinkedToNothing() {
        return problem == Problem.LINKED_TO_NOTHING;
    }

    /**
     * Notes that openHAB sends a write, read request or read response to a group address.
     *
     * @return true the first time the address turns out to be secured and not linked to the own address
     */
    public boolean noteSend(GroupAddress groupAddress) {
        if (!facts.securedGroupAddresses().contains(groupAddress) || facts.isLinked(own, groupAddress)) {
            return false;
        }
        return unlinked.add(groupAddress);
    }

    /**
     * {@return the text for the bridge status, as a key for the thing status translation, or null if there is
     * nothing to say}
     */
    public @Nullable String statusDescription() {
        List<GroupAddress> groups = List.copyOf(unlinked);
        String key;
        if (problem == Problem.NONE) {
            if (groups.isEmpty()) {
                return null;
            }
            key = "warning.knx-secure-link-groups-unlinked";
        } else {
            key = problem == Problem.NOT_IN_KEYRING ? "warning.knx-secure-link-address-unknown"
                    : "warning.knx-secure-link-address-unlinked";
            if (!groups.isEmpty()) {
                key += "-groups";
            }
        }
        // core's status translation splits the arguments at every comma, so none may hold one
        StringBuilder arguments = new StringBuilder("\"").append(own).append('"');
        if (!groups.isEmpty()) {
            String listed = groups.stream().limit(MAX_LISTED).map(GroupAddress::toString)
                    .collect(Collectors.joining("; "));
            if (groups.size() > MAX_LISTED) {
                listed += "; ...";
            }
            arguments.append(",\"").append(listed).append('"');
        }
        return "@text/" + key + " [" + arguments + "]";
    }
}
