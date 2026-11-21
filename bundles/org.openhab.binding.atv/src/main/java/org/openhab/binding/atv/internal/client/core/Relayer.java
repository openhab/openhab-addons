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
package org.openhab.binding.atv.internal.client.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * Relays method calls to interfaces of multiple protocol implementations.
 *
 * <p>
 * A {@code Relayer} keeps track of multiple implementations of one capability interface (one per protocol) and
 * selects which instance a call is forwarded to based on a priority list. The target is resolved by the explicit
 * {@link Capability} registry: an instance provides a capability when its {@link CapabilitySource#capabilities()}
 * declaration contains it (capability honesty — declaration matching actual overrides — is enforced by the
 * protocol test suites).
 *
 * <p>
 * A temporary takeover ({@link #takeover(Protocol)}) puts one protocol first in the priority order until
 * {@link #release()} is called; only a single takeover may be active at a time, otherwise
 * {@link IllegalStateException} is thrown.
 *
 * <p>
 * All methods are synchronized since relay methods may be entered from any caller thread.
 *
 * @param <T> capability interface type relayed by this instance
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Relayer<T> {

    private final Class<T> baseInterface;
    private final List<Protocol> priorities;
    private final boolean requireCapabilitySource;
    private final Map<Protocol, T> interfaces = new LinkedHashMap<>();
    private @Nullable Protocol takeoverProtocol;

    /**
     * Creates a relayer requiring registered instances to implement {@link CapabilitySource}.
     *
     * @param baseInterface capability interface relayed by this instance
     * @param protocolPriority priority order (highest priority first); injectable so relays can override the
     *            default order (e.g. power management favoring Companion)
     */
    public Relayer(Class<T> baseInterface, List<Protocol> protocolPriority) {
        this(baseInterface, protocolPriority, true);
    }

    /**
     * Creates a relayer.
     *
     * @param baseInterface capability interface relayed by this instance
     * @param protocolPriority priority order (highest priority first)
     * @param requireCapabilitySource if {@code true}, {@link #register(Object, Protocol)} rejects instances that do
     *            not implement {@link CapabilitySource}; relayers used only for instance selection (push updaters,
     *            features) pass {@code false} since their instances are never capability-relayed
     */
    public Relayer(Class<T> baseInterface, List<Protocol> protocolPriority, boolean requireCapabilitySource) {
        this.baseInterface = Objects.requireNonNull(baseInterface, "baseInterface");
        this.priorities = List.copyOf(protocolPriority);
        this.requireCapabilitySource = requireCapabilitySource;
    }

    /**
     * Returns the capability interface relayed by this instance.
     *
     * @return base interface class
     */
    public Class<T> baseInterface() {
        return baseInterface;
    }

    /**
     * Returns the number of registered instances.
     *
     * @return instance count
     */
    public synchronized int count() {
        return interfaces.size();
    }

    /**
     * Returns the main instance based on priority (takeover protocol first).
     *
     * @return main instance
     * @throws NotSupportedError if no instance is registered
     */
    public synchronized T mainInstance() {
        for (Protocol protocol : effectivePriorities(priorities)) {
            T instance = interfaces.get(protocol);
            if (instance != null) {
                return instance;
            }
        }
        throw new NotSupportedError();
    }

    /**
     * Returns the protocol of the main instance (takeover protocol first).
     *
     * @return main protocol, or empty if no instance is registered
     */
    public synchronized Optional<Protocol> mainProtocol() {
        for (Protocol protocol : effectivePriorities(priorities)) {
            if (interfaces.containsKey(protocol)) {
                return Optional.of(protocol);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all instances added to this relayer.
     *
     * @return registered instances (snapshot)
     */
    public synchronized List<T> instances() {
        return new ArrayList<>(interfaces.values());
    }

    /**
     * Registers a new instance for a protocol, replacing any previous instance of the same protocol.
     *
     * @param instance instance to register
     * @param protocol protocol the instance belongs to
     * @throws IllegalArgumentException if the protocol is not in the priority list, or if the instance does not
     *             implement {@link CapabilitySource} while required to
     */
    public synchronized void register(T instance, Protocol protocol) {
        Objects.requireNonNull(instance, "instance");
        if (!priorities.contains(protocol)) {
            throw new IllegalArgumentException(protocol + " not in priority list");
        }
        if (requireCapabilitySource && !(instance instanceof CapabilitySource)) {
            throw new IllegalArgumentException(instance.getClass().getName() + " does not implement CapabilitySource");
        }
        interfaces.put(protocol, instance);
    }

    /**
     * Returns the instance registered for a protocol.
     *
     * @param protocol protocol to look up
     * @return registered instance, or empty if none
     */
    public synchronized Optional<T> get(Protocol protocol) {
        return Optional.ofNullable(interfaces.get(protocol));
    }

    /**
     * Returns the highest-priority instance providing a capability, honoring an active takeover first.
     *
     * @param capability capability that shall be called
     * @return instance to forward the call to
     * @throws IllegalArgumentException if the capability does not belong to this relayer's interface (programming
     *             error)
     * @throws NotSupportedError if no registered instance declares the capability
     */
    public T relay(Capability capability) {
        return relay(capability, priorities);
    }

    /**
     * Returns the highest-priority instance providing a capability using an explicit priority order. An active
     * takeover still comes first.
     *
     * @param capability capability that shall be called
     * @param priority priority order to use instead of the relayer's own
     * @return instance to forward the call to
     * @throws IllegalArgumentException if the capability does not belong to this relayer's interface
     * @throws NotSupportedError if no registered instance declares the capability
     */
    public synchronized T relay(Capability capability, List<Protocol> priority) {
        if (capability.interfaceClass() != baseInterface) {
            throw new IllegalArgumentException(capability + " not in " + baseInterface.getSimpleName());
        }
        for (Protocol protocol : effectivePriorities(priority)) {
            T instance = interfaces.get(protocol);
            // Protocol in priority list but no instance for it: ignored, as no implementation exists
            if (instance == null) {
                continue;
            }
            // An instance not declaring the capability means it doesn't provide that method
            if (instance instanceof CapabilitySource source && source.capabilities().contains(capability)) {
                return instance;
            }
        }
        // An existing capability not provided by any instance is "not supported"
        throw new NotSupportedError(capability.methodName() + " is not supported");
    }

    /**
     * Temporarily overrides the priority list with a specific protocol.
     *
     * @param protocol protocol taking over
     * @throws IllegalStateException if another takeover is already active
     */
    public synchronized void takeover(Protocol protocol) {
        if (takeoverProtocol != null) {
            throw new IllegalStateException(takeoverProtocol + " has already done takeover");
        }
        takeoverProtocol = Objects.requireNonNull(protocol, "protocol");
    }

    /**
     * Releases a temporary takeover. No-op when no takeover is active.
     */
    public synchronized void release() {
        takeoverProtocol = null;
    }

    private List<Protocol> effectivePriorities(List<Protocol> priority) {
        Protocol takeover = takeoverProtocol;
        if (takeover == null) {
            return priority;
        }
        List<Protocol> chain = new ArrayList<>(priority.size() + 1);
        chain.add(takeover);
        chain.addAll(priority);
        return chain;
    }
}
