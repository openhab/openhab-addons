/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.commands;

/**
 * Used to indicate the status of a command being sent to a KLF200 unit as it
 * transitions through its life-cycle.
 *
 * @author MFK - Initial Contribution
 */
public enum CommandStatus {

    /**
     * Command has been created, but has not yet been added to a processing
     * queue. Commands with this status may be incomplete in terms of their
     * internal data.
     */
    CREATED,

    /**
     * Indicates that the command has been added to a processing queue and is
     * waiting to be processed.
     */
    QUEUED,

    /**
     * Indicates that processing has begun on this command. Specifically that a
     * request has been sent to the KLF200 unit. Depending on the nature of the
     * command, responses may have also been received from the unit, but the
     * command is still waiting for further responses and therefore remains in
     * this state.
     */
    PROCESSING,

    /**
     * Indicates that a command has finished processing. Specifically, that it
     * has received all expected response(s) from the KLF200 unit and that the
     * responses have been correctly interpreted as valid. A command in this
     * state has effectively completed processing as has been determined to be
     * valid.
     */
    COMPLETE,

    /**
     * Indicates that a command is in an error state. The reason for the error
     * or point in the commands lifecycle where the error occurred is determined
     * elsewhere. A command in this state should be consider to have failed and
     * therefore any data within the command to be invalid and incomplete.
     */
    ERROR;

    /** Holds any additional information that is available in respect of an ERROR status. */
    private String errorDetail;

    /**
     * Gets the error detail.
     *
     * @return the error detail
     */
    public String getErrorDetail() {
        return this.errorDetail;
    }

    /**
     * Sets the error detail.
     *
     * @param err
     *                the err
     * @return the command status
     */
    public CommandStatus setErrorDetail(String err) {
        this.errorDetail = err;
        return this;
    }
}
