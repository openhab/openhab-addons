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
package org.openhab.binding.smsmodem.internal.smslib.callback;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IDeviceInformationListener} will receive informations
 * and statistics
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public interface IDeviceInformationListener {

    void setManufacturer(String manufacturer);

    void setModel(String string);

    void setSwVersion(String swVersion);

    void setSerialNo(String serialNo);

    void setImsi(String imsi);

    void setRssi(String rssi);

    void setMode(String mode);

    public void setTotalSent(String totalSent);

    public void setTotalFailed(String totalFailed);

    public void setTotalReceived(String totalReceived);

    public void setTotalFailures(String totalFailure);
}
