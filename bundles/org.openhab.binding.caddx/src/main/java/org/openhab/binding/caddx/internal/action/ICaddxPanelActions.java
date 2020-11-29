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
package org.openhab.binding.caddx.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ICaddxPanelActions} defines the interface for all thing actions supported by the panel.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public interface ICaddxPanelActions {
    public void turnOffAnySounderOrAlarm(@Nullable String pin);

    public void disarm(@Nullable String pin);

    public void armInAwayMode(@Nullable String pin);

    public void armInStayMode(@Nullable String pin);

    public void cancel(@Nullable String pin);

    public void initiateAutoArm(@Nullable String pin);

    public void startWalkTestMode(@Nullable String pin);

    public void stopWalkTestMode(@Nullable String pin);

    public void stay();

    public void chime();

    public void exit();

    public void bypassInteriors();

    public void firePanic();

    public void medicalPanic();

    public void policePanic();

    public void smokeDetectorReset();

    public void autoCallbackDownload();

    public void manualPickupDownload();

    public void enableSilentExit();

    public void performTest();

    public void groupBypass();

    public void auxiliaryFunction1();

    public void auxiliaryFunction2();

    public void startKeypadSounder();
}
