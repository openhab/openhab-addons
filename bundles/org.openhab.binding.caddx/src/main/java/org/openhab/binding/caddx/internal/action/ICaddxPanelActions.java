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
    public void turnOffAnySounderOrAlarmOnPanel(@Nullable String pin);

    public void disarmOnPanel(@Nullable String pin);

    public void armInAwayModeOnPanel(@Nullable String pin);

    public void armInStayModeOnPanel(@Nullable String pin);

    public void cancelOnPanel(@Nullable String pin);

    public void initiateAutoArmOnPanel(@Nullable String pin);

    public void startWalkTestModeOnPanel(@Nullable String pin);

    public void stopWalkTestModeOnPanel(@Nullable String pin);

    public void stayOnPanel();

    public void chimeOnPanel();

    public void exitOnPanel();

    public void bypassInteriorsOnPanel();

    public void firePanicOnPanel();

    public void medicalPanicOnPanel();

    public void policePanicOnPanel();

    public void smokeDetectorResetOnPanel();

    public void autoCallbackDownloadOnPanel();

    public void manualPickupDownloadOnPanel();

    public void enableSilentExitOnPanel();

    public void performTestOnPanel();

    public void groupBypassOnPanel();

    public void auxiliaryFunction1OnPanel();

    public void auxiliaryFunction2OnPanel();

    public void startKeypadSounderOnPanel();
}
