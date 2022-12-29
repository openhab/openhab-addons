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
package org.openhab.binding.boschshc.internal.services.smokedetectorcheck;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.smokedetectorcheck.dto.SmokeDetectorCheckServiceState;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * Returns the result of the last smoke test and is used to request a new smoke test.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class SmokeDetectorCheckService extends BoschSHCService<SmokeDetectorCheckServiceState> {

    public SmokeDetectorCheckService() {
        super("SmokeDetectorCheck", SmokeDetectorCheckServiceState.class);
    }

    @Override
    public SmokeDetectorCheckServiceState handleCommand(Command command) throws BoschSHCException {
        if (command instanceof StringType) {
            var stringCommand = (StringType) command;
            var state = new SmokeDetectorCheckServiceState();
            state.value = SmokeDetectorCheckState.from(stringCommand.toString());
            return state;
        }

        if (command instanceof PlayPauseType) {
            var playPauseCommand = (PlayPauseType) command;
            if (playPauseCommand.equals(PlayPauseType.PLAY)) {
                var state = new SmokeDetectorCheckServiceState();
                state.value = SmokeDetectorCheckState.SMOKE_TEST_REQUESTED;
                return state;
            }
        }

        return super.handleCommand(command);
    }
}
