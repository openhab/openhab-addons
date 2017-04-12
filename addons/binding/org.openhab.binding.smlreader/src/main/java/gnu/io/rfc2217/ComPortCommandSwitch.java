
/*
 * Copyright (C) 2010 Archie L. Cobbs. All rights reserved.
 *
 * $Id: ComPortCommandSwitch.java 6 2010-11-20 23:37:06Z archie.cobbs $
 */

package gnu.io.rfc2217;

/**
 * Visitor pattern interface for {@link ComPortCommand} classes.
 *
 * @see ComPortCommand#visit
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

