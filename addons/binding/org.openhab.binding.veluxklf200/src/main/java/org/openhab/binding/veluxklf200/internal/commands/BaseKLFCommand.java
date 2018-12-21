/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.commands;

import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandStructure;
import org.openhab.binding.veluxklf200.internal.engine.KLFCommandProcessor;
import org.openhab.binding.veluxklf200.internal.utility.KLFSession;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class that all KLF commands should extend. Provides both abstraction and
 * common functions
 *
 * @author MFK - Initial Contribution
 */
public abstract class BaseKLFCommand {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KLFCommandProcessor.class);

    /** Constant to indicate that originator of the command was a user. */
    public static final byte CMD_ORIGINATOR_USER = 1;

    /** Constant to indicate that the 'priority' of the command being sent is 'normal'. */
    public static final byte CMD_PRIORITY_NORMAL = 3;

    /** Constant to indicate the velocity a scene should be executed at. */
    public static final byte CMD_VELOCITY_DEFAULT = 0;

    /** Constant position of the first byte of user data in a response from a KLF unit. */
    public static final int FIRSTBYTE = 4;

    /** Constant to indicate that a NODE ID has not been set or captured by the particular command */
    public static final byte NOT_REQUIRED = (byte) 250;

    /**
     * Denotes the status of a given command. Typically the main usable states
     * of note are COMPLETE and ERROR
     */
    protected CommandStatus commandStatus;

    /**
     * The unique session identifier for this particular command instance.
     */
    private short sessionId;

    /**
     * Records the main / first node that a command is being executed for. This is used when reconciling responses
     * recieved from the KLF200 in cases where there is no session defined by the KLF200 specification for the
     * particular API command.
     */
    private byte mainNode;

    /**
     * The KLF200 specification currently only supports a protocol version
     * denoted by 0.
     */
    public static final byte SUPPORTED_PROTOCOL = 0;

    /**
     * Constructor should always be called by sub-classes during their own
     * constructor using super().
     */
    protected BaseKLFCommand() {
        this.commandStatus = CommandStatus.CREATED;
        if (this.getKLFCommandStructure().isSessionRequired()) {
            // A session is required, so generate a pseudo-unique session ID
            this.sessionId = KLFSession.getInstance().getSessionIdentifier();
        } else {
            // No session required
            this.sessionId = 0;
        }
        // Defaulted to NOT_REQUIRED, sub-class must specify by calling setMainNode() where relevant.
        this.mainNode = NOT_REQUIRED;
    }

    /**
     * Returns the session Identifier that has been assigned to this command. If
     * 0 (zero), none was assigned as it is not required by this type of
     * command.
     *
     * @return Returns the session Identifier that has been assigned to this
     *         command. If 0 (zero), none was assigned as it is not required by
     *         this type of command.
     */
    public short getSessionID() {
        return this.sessionId;
    }

    /**
     * Returns a formatted session ID. Use for simplifying debug logging
     *
     * @return
     */
    public String formatSessionID() {
        if (this.sessionId == 0) {
            return "NOT REQUIRED";
        }
        return "(" + this.sessionId + ")";
    }

    /**
     * Throughout the life-cycle of a command, it may be in various states
     * ranging from "CREATED" when it is first instantiated, but has not yet
     * been QUEUED for execution, to COMPLETE or ERROR when processing has
     * completed.
     *
     * @return The current processing status of the command.
     */
    public CommandStatus getCommandStatus() {
        return this.commandStatus;
    }

    /**
     * Throughout the life-cycle of a command, it may be in various states
     * ranging from "CREATED" when it is first instantiated, but has not yet
     * been QUEUED for execution, to COMPLETE or ERROR when processing has
     * completed.
     *
     * Typically this should never be called explicitly as it is up to the
     * KLFCommandProcessor to call this method as the Command progresses through
     * its lifecycle.
     *
     * @param status
     *                   The current processing status of the command.
     */
    public void setCommandStatus(CommandStatus status) {
        this.commandStatus = status;
    }

    /**
     * Used to determine if a given KLF response code (command code received in
     * a response from the KLF unit) can be handled by this command. The
     * determination is based on the {@link KLFCommandStructure} definitions and
     * the sub-classes implementation of the {@link getKLFCommandStructure}.
     *
     * @param responseCode The response code (Command code) returned by the KLF200 unit
     *                         in response to a previous command.
     * @param data         the data
     * @return true, if this command is capable of handling the response, false
     *         otherwise.
     */
    public boolean canHandleResponse(short responseCode, byte[] data) {
        if (getKLFCommandStructure().canHandle(responseCode)) {
            if (getKLFCommandStructure().isSessionRequired()) {
                if (this.getSessionID() == extractSession(responseCode, data)) {
                    return true;
                }
            } else {
                if (getKLFCommandStructure().isNodeSpecific()) {
                    return canHandleNode(responseCode, data);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the main node that this command is being executed for.
     *
     * @return id of the main node (first node) this command is being executed for or NOT_REQUIRED if this command is
     *         not node specific.
     */
    public byte getMainNode() {
        return this.mainNode;
    }

    /**
     * Returns a formatted version of the mainNode to simplify debug logging
     *
     * @return formatted mainNode value as string.
     */
    public String formatMainNode() {
        if (this.mainNode == NOT_REQUIRED) {
            return "NOT NODE SPECIFIC";
        }
        return "(" + this.mainNode + ")";
    }

    /**
     * Sets the main node that this command relates to. Should be set by a sub-class in cases where the sub-classes
     * specific command implementation is node specific. In cases where the command pertains to multiple nodes, the
     * mainNode should be set to the first node in the list.
     *
     * Should not be called if a command is not node specific and/or if the commands attributes are configured to be
     * non-node specific.
     *
     * @param mainNode main node that the command relates to.
     */
    protected void setMainNode(byte mainNode) {
        this.mainNode = mainNode;
    }

    /**
     * Called when handling responses from the KLF200 to determine which request the particular response pertains to.
     * Certain KLF200 commands utilise a session to help with this determination. However, there are some that do node
     * utilise a session and therefore the only way to reconcile is to examine which node the command was executed for.
     *
     * @param responseCode The response code (command code) returned from the KLF200
     * @param data         The payload returned in the command
     * @returns True if this command instance reconciles with the data recieved, false otherwise.
     */
    protected boolean canHandleNode(short responseCode, byte[] data) {
        byte responseNode = extractNode(responseCode, data);
        if (NOT_REQUIRED == responseNode) {
            // This should never happen. If it does, it means that a particular command class has not correctly
            // overridden this method.
            logger.error(
                    "An invalid node identifier was detected. This indicates a mis-configuration or coding error in the binding.");
            return false;
        }
        if (responseNode == this.getMainNode()) {
            return true;
        }
        return false;
    }

    /**
     * Extracts the node id from the data returned from a KLF command. In cases where the attributes of a command
     * contain {@link KLFCommandProcessor.MASK_CMD_SPECIFIC}, it is required that this method be overriden by the
     * command sub-class. The sub-class should parse out the relevant data to find the nodeId and return it.
     *
     * @return nodeId contained in the response data stream, or 255 if not overridden
     */
    protected byte extractNode(short responseCode, byte[] data) {
        // This should never be called by a command that has the KLFCommandProcessor.MASK_CMD_SPECIFIC attribute set.
        // That particular command should always override this method.
        return (byte) 250;
    }

    /**
     * Validate a response received from a KLF200 unit to ensure that the
     * response payload is structured correctly. Specifically, this validates
     * that the packet is well formed and in conformance with the KLF
     * specification. It does not validate that the user data within the
     * response is valid. The recipient must do this.
     *
     * @param data
     *                 The data returned by the KLF200 unit. Note, it is assumed that
     *                 the data has been 'Slip RFC1055' decoded beforehand.
     * @return true, if the data structure is valid, false otherwise.
     */
    public static boolean validateKLFResponse(byte[] data) {
        if ((null == data) || (data.length < 5)) {
            LoggerFactory.getLogger(KLFCommandProcessor.class)
                    .error("KLF Response data invalid. Packet is too short: {}", KLFUtils.formatBytes(data));
            return false;
        }
        if ((short) 0 != data[0]) {
            LoggerFactory.getLogger(KLFCommandProcessor.class)
                    .error("KLF Response contains invalid protocol identifier: {}", data[0]);
            return false;
        }
        if ((data[1] & 0xFF) != data.length - 2) {
            LoggerFactory.getLogger(KLFCommandProcessor.class).error(
                    "KLF Response length of {} does not match data recieved: {}", data[1], KLFUtils.formatBytes(data));
            return false;
        }
        byte checksum = 0;
        for (int i = 0; i < data.length - 1; ++i) {
            checksum = (byte) (checksum ^ data[i]);
        }
        if (checksum != data[data.length - 1]) {
            LoggerFactory.getLogger(KLFCommandProcessor.class).error("KLF Response checksum is invalid: {}",
                    KLFUtils.formatBytes(data));
            return false;
        }
        return true;
    }

    /**
     * Creates a 'Slip RFC1055' encoded byte array that is ready to be sent to a
     * KLF200 unit. The command and parameter data for the request are assembled
     * by the sub-classes implementation of the {@link pack} method, while this
     * method then applies surrounding protocol structures such as package
     * length, protocol version and checksum data.
     *
     * @return A 'Slip RFC1055' data packet that is ready to be send to a KLF200
     *         unit. In the event that there is an error, returns null.
     */
    public byte[] getRawKLFCommand() {
        byte data[] = pack();
        if (null == data) {
            logger.error("Invalid data packet. A null data packet is not valid.");
            return null;
        }
        if (data.length > 250) {
            logger.error("Invalid data packet size. Max permissable is 250 bytes, recieved {} bytes: {}", data.length,
                    KLFUtils.formatBytes(data));
            return null;
        } else {
            byte checksum = 0;
            byte[] message = new byte[data.length + 5];
            message[0] = SUPPORTED_PROTOCOL;
            message[1] = (byte) (3 + data.length);
            message[2] = (byte) (getKLFCommandStructure().getCommandCode() >>> 8);
            message[3] = (byte) getKLFCommandStructure().getCommandCode();
            message[4 + data.length] = 0;
            System.arraycopy(data, 0, message, 4, data.length);
            for (byte b : message) {
                checksum = (byte) (checksum ^ b);
            }
            message[4 + data.length] = checksum;
            return KLFUtils.slipRFC1055encode(message);
        }
    }

    /**
     * Checks to ensure that the commands internal data is valid. Specifically,
     * the nature of the validation should be limited to the parameter data that
     * is being supplied in the command. For example, if the command expects a
     * password, then it may be appropriate to validate that the password is not
     * null and meets whatever requirements the API may have for that parameter.
     * A sub-class should override this method to perform validation as
     * required. If not overridden, no validation is performed here.
     *
     * @return true, if is valid, false otherwise.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Must be implemented by a sub-class so as to enable this command to be
     * able to handle any response payload received from the KLF200 unit in
     * response to this command being sent to the unit. In many cases, an
     * individual request 'REQ' has a single confirmation response 'CFM'.
     * However, many commands may result in more than one response payload.
     *
     * @param data
     *                 Data received from the KLF unit. The implementing class must
     *                 look at the command code {@link KLFUtils.decodeKLFCommand} to
     *                 identify the particular response variation and handle
     *                 appropriately.
     */
    public abstract void handleResponse(byte[] data);

    /**
     * Must be implemented by a sub-class so as to specify the relationship with
     * a {@link getKLFCommandStructure}. Specifically, this determines which
     * KLF200 requests and set of associated responses this command is
     * applicable for.
     *
     * @return The {@link getKLFCommandStructure} represented by this command.
     */
    public abstract KLFCommandStructure getKLFCommandStructure();

    /**
     * Must be implemented by a sub-class to assemble is own internal data
     * representation in the a byte array that is suitable for sending to a
     * KLF200 unit. This method is typically called by {@link getRawKLFCommand},
     * which then applies surrounding protocol structures such as package
     * length, protocol version and checksum data.
     *
     * @return A byte array representation of the request to be sent to the
     *         KLF200 unit. If there is no data required for the command, an
     *         empty array should be returned. A null return is considered to be
     *         invalid.
     */
    protected abstract byte[] pack();

    /**
     * Must be implemented by a sub-class to retrieve a session parameter from a
     * data response. Not all commands require a session, as such, an
     * implementing command can simply return zero to indicate that there is no
     * session required.
     *
     * @param responseCode the response code
     * @param data         the data
     * @return The pseudo-unique session ID contained within the data if
     *         applicable or 0 if there is no session necessary for the
     *         particular command.
     */
    protected abstract int extractSession(short responseCode, byte[] data);
}
