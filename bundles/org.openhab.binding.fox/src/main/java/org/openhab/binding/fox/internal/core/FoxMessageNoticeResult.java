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
package org.openhab.binding.fox.internal.core;

import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoxMessageBoot} is a message of system result notice.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
class FoxMessageNoticeResult extends FoxMessage {

    String result;

    public FoxMessageNoticeResult() {
        super();
        result = "";
    }

    @Override
    protected void prepareMessage() {
    }

    @Override
    protected void interpretMessage() {
        result = "";
        if (message.matches("do [R|T][0-9]+")) {
            Scanner scanner = new Scanner(message);
            scanner.next();
            result = scanner.next();
            scanner.close();
        }
    }

    String getResult() {
        return result;
    }
}
