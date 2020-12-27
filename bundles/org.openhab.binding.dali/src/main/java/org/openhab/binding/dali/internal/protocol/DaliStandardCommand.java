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
package org.openhab.binding.dali.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DaliStandardCommand} represents different types of commands for
 * controlling DALI equipment.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliStandardCommand extends DaliGearCommandBase {

    private DaliStandardCommand(DaliAddress target, int cmdval, int param, boolean sendTwice) {
        super(target.addToFrame(new DaliForwardFrame(16, new byte[] { 0x1, (byte) (cmdval | (param & 0b1111)) })),
                sendTwice);
    }

    public static DaliStandardCommand Off(DaliAddress target) {
        return new DaliStandardCommand(target, 0x00, 0, false);
    }

    public static DaliStandardCommand Up(DaliAddress target) {
        return new DaliStandardCommand(target, 0x01, 0, false);
    }

    public static DaliStandardCommand Down(DaliAddress target) {
        return new DaliStandardCommand(target, 0x02, 0, false);
    }

    public static DaliStandardCommand StepUp(DaliAddress target) {
        return new DaliStandardCommand(target, 0x03, 0, false);
    }

    public static DaliStandardCommand StepDown(DaliAddress target) {
        return new DaliStandardCommand(target, 0x04, 0, false);
    }

    public static DaliStandardCommand RecallMaxLevel(DaliAddress target) {
        return new DaliStandardCommand(target, 0x05, 0, false);
    }

    public static DaliStandardCommand RecallMinLevel(DaliAddress target) {
        return new DaliStandardCommand(target, 0x06, 0, false);
    }

    public static DaliStandardCommand StepDownAndOff(DaliAddress target) {
        return new DaliStandardCommand(target, 0x07, 0, false);
    }

    public static DaliStandardCommand OnAndStepUp(DaliAddress target) {
        return new DaliStandardCommand(target, 0x08, 0, false);
    }

    public static DaliStandardCommand EnableDAPCSequence(DaliAddress target) {
        return new DaliStandardCommand(target, 0x09, 0, false);
    }

    public static DaliStandardCommand GoToScene(DaliAddress target, int scene) {
        return new DaliStandardCommand(target, 0x10, scene, false);
    }

    public static DaliStandardCommand Reset(DaliAddress target) {
        return new DaliStandardCommand(target, 0x20, 0, true);
    }

    public static DaliStandardCommand QueryStatus(DaliAddress target) {
        return new DaliStandardCommand(target, 0x90, 0, false);
    }

    public static DaliStandardCommand QueryActualLevel(DaliAddress target) {
        return new DaliStandardCommand(target, 0xa0, 0, false);
    }
}
