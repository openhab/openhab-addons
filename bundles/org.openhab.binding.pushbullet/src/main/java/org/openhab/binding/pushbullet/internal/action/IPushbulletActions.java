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
package org.openhab.binding.pushbullet.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IPushbulletActions} interface defines rule actions for sending notifications
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface IPushbulletActions {

    public Boolean sendPushbulletNote(@Nullable String recipient, @Nullable String title, @Nullable String message);

    public Boolean sendPushbulletNote(@Nullable String recipient, @Nullable String message);
}
