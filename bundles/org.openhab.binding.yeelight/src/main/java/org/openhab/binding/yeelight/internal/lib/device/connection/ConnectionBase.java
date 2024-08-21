/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.yeelight.internal.lib.device.connection;

import org.openhab.binding.yeelight.internal.lib.device.DeviceMethod;

/**
 * Created by jiang on 16/10/21.
 *
 * @author Coaster Li - Initial contribution
 */
public interface ConnectionBase {

    boolean invoke(DeviceMethod method);

    boolean invokeCustom(DeviceMethod method);

    boolean connect();

    boolean disconnect();
}
