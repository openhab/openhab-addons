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
package org.openhab.binding.xmppclient.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This is the automation engine action handler service for the publishXMPP action.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof XMPPActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link IXMPPActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface IXMPPActions {

    public void publishXMPP(@Nullable String to, @Nullable String text);
}
