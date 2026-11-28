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
package org.openhab.io.eebus.internal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openmuc.jeebus.usecase.powerlimitation.controllablesystem.LimitationUseCaseImpl;
import org.openmuc.jeebus.usecase.powerlimitation.controllablesystem.states.StateMachine;

/**
 * Exposes the executor that jEEBus 1.1.0 leaves running when a power limitation use case closes.
 * Remove this workaround when the dependency includes upstream commit def1676fe.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class LimitationExecutorAccess {

    private final VarHandle stateMachine;
    private final VarHandle executor;

    LimitationExecutorAccess() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            stateMachine = MethodHandles.privateLookupIn(LimitationUseCaseImpl.class, lookup)
                    .findVarHandle(LimitationUseCaseImpl.class, "stateMachine", StateMachine.class);
            executor = MethodHandles.privateLookupIn(StateMachine.class, lookup).findVarHandle(StateMachine.class,
                    "executor", ScheduledExecutorService.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access the jEEBus power limitation executor", e);
        }
    }

    ScheduledExecutorService getExecutor(LimitationUseCaseImpl useCase) {
        StateMachine machine = (StateMachine) stateMachine.get(useCase);
        return (ScheduledExecutorService) executor.get(machine);
    }
}
