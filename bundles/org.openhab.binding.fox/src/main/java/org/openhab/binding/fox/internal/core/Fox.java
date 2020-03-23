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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Fox} is responsible for openHAB handlers access to Fox system.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class Fox {

    private FoxMessenger messenger;

    public Fox(FoxMessenger messenger) {
        this.messenger = messenger;
    }

    public FoxMessenger getMessenger() {
        return messenger;
    }

    public void doTask(int id) throws FoxException {
        FoxMessageDoTask msg = new FoxMessageDoTask();
        msg.setTaskId(id);
        write(msg);
    }

    public String noticeResult() throws FoxException {
        FoxMessageNoticeResult msg = new FoxMessageNoticeResult();
        read(msg);
        return msg.getResult();
    }

    void write(FoxMessage msg) throws FoxException {
        messenger.write(msg.prepare().trim());
    }

    void read(FoxMessage msg) throws FoxException {
        msg.interpret(messenger.read().trim());
    }
}
