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
package org.openhab.binding.lgwebos.action;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ILGWebOSActions} defines the interface for all thing actions supported by the binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface ILGWebOSActions {

    public void showToast(String text) throws IOException;

    public void showToast(String icon, String text) throws IOException;

    public void launchBrowser(String url);

    public void launchApplication(String appId);

    public void launchApplication(String appId, String params);

    public void sendText(String text);

    public void sendButton(String button);

    public void increaseChannel();

    public void decreaseChannel();

    public void sendRCButton(String rcButton);
}
