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
package org.openhab.binding.huesync.internal.handler.tasks;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDtoDetailed;
import org.openhab.binding.huesync.internal.api.dto.execution.HueSyncExecutionDto;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmiDto;

/**
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncUpdateInfo {
    public @Nullable HueSyncDeviceDtoDetailed deviceStatus;
    public @Nullable HueSyncHdmiDto hdmiStatus;
    public @Nullable HueSyncExecutionDto execution;
}
