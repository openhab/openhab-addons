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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXIllegalArgumentException;
import io.calimero.secure.Keyring;
import io.calimero.secure.Keyring.Interface.Type;

/**
 * What a loaded ETS keyring says about interfaces and secured group addresses. Everything here is plain data in the
 * file, so it can be read without the keyring password; keys and passwords are never exposed.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class KeyringFacts {

    /**
     * One interface entry of the keyring.
     *
     * @param address individual address of the interface
     * @param type tunnelling, backbone or USB
     * @param host individual address of the device hosting the interface, null if the keyring gives none
     * @param userId tunnel user id, 0 if there is none
     * @param linkedSecuredGroups secured group addresses ETS linked to this interface
     */
    public record InterfaceFacts(IndividualAddress address, Type type, @Nullable IndividualAddress host, int userId,
            Set<GroupAddress> linkedSecuredGroups) {
    }

    private final Keyring keyring;
    private final List<InterfaceFacts> interfaces;
    private final Map<IndividualAddress, InterfaceFacts> byAddress;
    private final Set<GroupAddress> securedGroupAddresses;
    private final boolean backboneKey;

    public KeyringFacts(Keyring keyring) {
        this.keyring = keyring;
        this.securedGroupAddresses = Set.copyOf(keyring.groups().keySet());
        var backbone = keyring.backbone();
        this.backboneKey = backbone.isPresent() && backbone.get().groupKey().isPresent();

        Map<IndividualAddress, InterfaceFacts> merged = new HashMap<>();
        for (var entry : keyring.interfaces().entrySet()) {
            // the library keeps no host for interfaces that have none and files them under 0.0.0
            IndividualAddress host = entry.getKey().getRawAddress() == 0 ? null : entry.getKey();
            for (Keyring.Interface iface : entry.getValue()) {
                Type type;
                try {
                    type = iface.type();
                } catch (KNXIllegalArgumentException e) {
                    // a type this library does not know cannot be described
                    continue;
                }
                Set<GroupAddress> linked = new HashSet<>(iface.groups().keySet());
                linked.retainAll(securedGroupAddresses);
                merged.merge(iface.address(),
                        new InterfaceFacts(iface.address(), type, host, iface.user(), Set.copyOf(linked)), (a, b) -> {
                            Set<GroupAddress> both = new HashSet<>(a.linkedSecuredGroups());
                            both.addAll(b.linkedSecuredGroups());
                            return new InterfaceFacts(a.address(), a.type(), a.host(), a.userId(), Set.copyOf(both));
                        });
            }
        }
        this.byAddress = Map.copyOf(merged);
        this.interfaces = merged.values().stream().sorted(Comparator.comparingInt(i -> i.address().getRawAddress()))
                .toList();
    }

    /** {@return the interface entries, ordered by address} */
    public List<InterfaceFacts> interfaces() {
        return interfaces;
    }

    public Optional<InterfaceFacts> interfaceOf(IndividualAddress address) {
        return Optional.ofNullable(byAddress.get(address));
    }

    /** {@return the group addresses that have a key in the keyring} */
    public Set<GroupAddress> securedGroupAddresses() {
        return securedGroupAddresses;
    }

    /** {@return true if ETS linked the secured group address to the interface with this address} */
    public boolean isLinked(IndividualAddress interfaceAddress, GroupAddress groupAddress) {
        InterfaceFacts iface = byAddress.get(interfaceAddress);
        return iface != null && iface.linkedSecuredGroups().contains(groupAddress);
    }

    /** {@return true if the keyring holds a backbone key for secure routing; the key itself is not exposed} */
    public boolean hasBackboneKey() {
        return backboneKey;
    }

    /** {@return true if the keyring's signature was made with this password} */
    public boolean signatureMatches(char[] password) {
        return keyring.verifySignature(password);
    }
}
