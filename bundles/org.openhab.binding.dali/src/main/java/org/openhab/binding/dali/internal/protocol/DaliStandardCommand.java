/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.dali.internal.handler.DaliException;

/**
 * The {@link DaliStandardCommand} represents different types of commands for
 * controlling DALI equipment.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliStandardCommand extends DaliGearCommandBase {

    private DaliStandardCommand(DaliAddress target, int cmdval, int param, boolean sendTwice) throws DaliException {
        super(target.addToFrame(new DaliForwardFrame(16, new byte[] { 0x1, (byte) (cmdval | (param & 0b1111)) })),
                sendTwice);
    }

    public static DaliStandardCommand createOffCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x00, 0, false);
    }

    public static DaliStandardCommand createUpCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x01, 0, false);
    }

    public static DaliStandardCommand createDownCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x02, 0, false);
    }

    public static DaliStandardCommand createStepUpCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x03, 0, false);
    }

    public static DaliStandardCommand createStepDownCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x04, 0, false);
    }

    public static DaliStandardCommand createRecallMaxLevelCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x05, 0, false);
    }

    public static DaliStandardCommand createRecallMinLevelCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x06, 0, false);
    }

    public static DaliStandardCommand createStepDownAndOffCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x07, 0, false);
    }

    public static DaliStandardCommand createOnAndStepUpCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x08, 0, false);
    }

    public static DaliStandardCommand createEnableDAPCSequenceCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x09, 0, false);
    }

    public static DaliStandardCommand createGoToSceneCommand(DaliAddress target, int scene) throws DaliException {
        return new DaliStandardCommand(target, 0x10, scene, false);
    }

    public static DaliStandardCommand createResetCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x20, 0, true);
    }

    public static DaliStandardCommand createQueryStatusCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x90, 0, false);
    }

    public static DaliStandardCommand createQueryContentDTR0Command(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0x98, 0, false);
    }

    public static DaliStandardCommand createQueryActualLevelCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0xa0, 0, false);
    }

    public static DaliStandardCommand createActivateCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0xe2, 0, false);
    }

    public static DaliStandardCommand createQueryColorValueCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0xfa, 0, false);
    }

    // Global commands sent to special addresses

    public static DaliStandardCommand createSetDTR0Command(int value) throws DaliException {
        return new DaliStandardCommand(DaliAddress.createRawAddress(0xa3), value, 0, false);
    }

    public static DaliStandardCommand createSetDTR1Command(int value) throws DaliException {
        return new DaliStandardCommand(DaliAddress.createRawAddress(0xc3), value, 0, false);
    }

    public static DaliStandardCommand createSetDTR2Command(int value) throws DaliException {
        return new DaliStandardCommand(DaliAddress.createRawAddress(0xc5), value, 0, false);
    }

    public static DaliStandardCommand createSetDeviceTypeCommand(int value) throws DaliException {
        return new DaliStandardCommand(DaliAddress.createRawAddress(0xc1), value, 0, false);
    }

    // DT8 (color temperature and single-channel RGB) commands

    public static DaliStandardCommand createSetRgbDimlevelCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0xeb, 0, false);
    }

    public static DaliStandardCommand createSetColorTemperatureCommand(DaliAddress target) throws DaliException {
        return new DaliStandardCommand(target, 0xe7, 0, false);
    }
}
