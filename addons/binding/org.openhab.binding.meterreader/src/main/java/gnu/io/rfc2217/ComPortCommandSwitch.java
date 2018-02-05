/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.rfc2217;

/**
 * Visitor pattern interface for {@link ComPortCommand} classes.
 *
 * @see ComPortCommand#visit
 * @author jserv
 */
public interface ComPortCommandSwitch {

    void caseSignature(SignatureCommand command);

    void caseBaudRate(BaudRateCommand command);

    void caseDataSize(DataSizeCommand command);

    void caseParity(ParityCommand command);

    void caseStopSize(StopSizeCommand command);

    void caseControl(ControlCommand command);

    void caseNotifyLineState(NotifyLineStateCommand command);

    void caseNotifyModemState(NotifyModemStateCommand command);

    void caseFlowControlSuspend(FlowControlSuspendCommand command);

    void caseFlowControlResume(FlowControlResumeCommand command);

    void caseLineStateMask(LineStateMaskCommand command);

    void caseModemStateMask(ModemStateMaskCommand command);

    void casePurgeData(PurgeDataCommand command);
}
