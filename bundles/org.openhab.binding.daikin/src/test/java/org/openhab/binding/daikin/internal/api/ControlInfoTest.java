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
package org.openhab.binding.daikin.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.daikin.internal.api.Enums.FanMovement;

/**
 * This class provides tests for deconz lights
 *
 * @author Leo Siepel - Initial contribution
 *
 */

@NonNullByDefault
public class ControlInfoTest {

    @BeforeEach
    public void initialize() {
    }

    @Test
    public void separateUpAndDown() {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir_ud=S,f_dir_lr=0,ndfd1=00,ndfd2=00,ndfd3=S0,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.VERTICAL, info.fanMovement);
    }

    @Test
    public void separateLeftAndRightTest() {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir_ud=0,f_dir_lr=S,ndfd1=00,ndfd2=00,ndfd3=0S,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.HORIZONTAL, info.fanMovement);
    }

    @Test
    public void separateStoppedTest() {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir_ud=0,f_dir_lr=0,ndfd1=00,ndfd2=00,ndfd3=00,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.STOPPED, info.fanMovement);
    }

    @Test
    public void separateTwoDimensionalTest() {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir_ud=S,f_dir_lr=S,ndfd1=00,ndfd2=00,ndfd3=SS,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.VERTICAL_AND_HORIZONTAL, info.fanMovement);
    }

    @Test
    public void combinedUpAndDown() {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir=1,ndfd1=00,ndfd2=00,ndfd3=S0,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.VERTICAL, info.fanMovement);
    }

    @Test
    public void combinedLeftAndRightTest() throws IOException {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir=2,ndfd1=00,ndfd2=00,ndfd3=0S,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.HORIZONTAL, info.fanMovement);
    }

    @Test
    public void combinedStoppedTest() throws IOException {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir=0,ndfd1=00,ndfd2=00,ndfd3=00,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.STOPPED, info.fanMovement);
    }

    @Test
    public void combinedTwoDimensionalTest() throws IOException {
        // arrange
        String incomingMessage = "ret=OK,pow=0,mode=3,stemp=21.0,shum=0,adv=13,dt1=21.0,dt2=M,dt3=21.0,dt4=25.0,dh1=0,dh2=0,dh3=0,dh4=0,dhh=0,alert=16,f_rate=3,dfr1=A,dfr2=A,dfr3=3,dfr4=A,dfr6=A,dfrh=0,f_dir=3,ndfd1=00,ndfd2=00,ndfd3=SS,ndfd4=00,ndfd6=00,ndfdh=00";

        // act
        ControlInfo info = ControlInfo.parse(incomingMessage);

        // assert
        assertEquals(FanMovement.VERTICAL_AND_HORIZONTAL, info.fanMovement);
    }
}
