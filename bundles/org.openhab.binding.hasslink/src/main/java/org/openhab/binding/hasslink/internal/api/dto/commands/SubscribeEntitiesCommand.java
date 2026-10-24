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
package org.openhab.binding.hasslink.internal.api.dto.commands;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SubscribeEntitiesCommand} registers openHAB for native entity state updates.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class SubscribeEntitiesCommand extends BaseCommand {
    public final @Nullable Collection<String> entityIds;

    public SubscribeEntitiesCommand(int id) {
        this(id, null);
    }

    public SubscribeEntitiesCommand(int id, @Nullable Collection<String> entityIds) {
        super(id, "subscribe_entities");
        this.entityIds = entityIds != null ? Set.copyOf(entityIds) : null;
    }
}
