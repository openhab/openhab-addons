/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;
import java.util.List;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;

/**
 * The {@link IParadoxCommunicator} is representing the functionality of communication implementation.
 * If another Paradox alarm system is used this interface must be implemented.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public interface IParadoxCommunicator extends IParadoxGenericCommunicator {

    public void refreshMemoryMap() throws ParadoxBindingException, IOException, InterruptedException;

    public List<byte[]> readPartitionFlags();

    public ZoneStateFlags readZoneStateFlags();

    public List<String> readPartitionLabels();

    public List<String> readZoneLabels();

    public void executeCommand(String commandAsString);

}
