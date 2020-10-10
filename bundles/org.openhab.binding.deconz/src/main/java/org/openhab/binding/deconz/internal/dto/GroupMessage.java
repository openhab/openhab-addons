/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.types.GroupType;

/**
 * The REST interface and websocket connection are using the same fields.
 * The REST data contains more descriptive info like the manufacturer and name.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GroupMessage extends DeconzBaseMessage {
    public @Nullable GroupAction action;
    public String @Nullable [] devicemembership;
    public @Nullable Boolean hidden;
    public @Nullable String id;
    public String @Nullable [] lights;
    public String @Nullable [] lightsequence;
    public String @Nullable [] multideviceids;
    public @Nullable String name;
    public Scene @Nullable [] scenes;
    public @Nullable GroupState state;
    public @Nullable GroupType type;
}
