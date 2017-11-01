/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package gnu.io.rfc2217;

/**
 * Adapter class for {@link ComPortCommandSwitch} implementations.
 *
 * @see ComPortCommandSwitch
 * @see ComPortCommand#visit
 * @author jserv
 */
public class AbstractComPortCommandSwitch implements ComPortCommandSwitch {

    /**
     * Visit method invoked by {@link SignatureCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseSignature(SignatureCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link BaudRateCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseBaudRate(BaudRateCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link DataSizeCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseDataSize(DataSizeCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link ParityCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseParity(ParityCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link StopSizeCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseStopSize(StopSizeCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link ControlCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseControl(ControlCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link NotifyLineStateCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseNotifyLineState(NotifyLineStateCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link NotifyModemStateCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseNotifyModemState(NotifyModemStateCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link FlowControlSuspendCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseFlowControlSuspend(FlowControlSuspendCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link FlowControlResumeCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseFlowControlResume(FlowControlResumeCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link LineStateMaskCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseLineStateMask(LineStateMaskCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link ModemStateMaskCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void caseModemStateMask(ModemStateMaskCommand command) {
        caseDefault(command);
    }

    /**
     * Visit method invoked by {@link PurgeDataCommand} instances.
     *
     *
     * The implementation in {@link AbstractComPortCommandSwitch} delegates to {@link #caseDefault}.
     *
     */
    @Override
    public void casePurgeData(PurgeDataCommand command) {
        caseDefault(command);
    }

    /**
     * Default handler.
     *
     *
     * All other methods in {@link AbstractComPortCommandSwitch} delegate to this method;
     * the implementation in {@link AbstractComPortCommandSwitch} does nothing.
     *
     */
    protected void caseDefault(ComPortCommand command) {
    }
}
