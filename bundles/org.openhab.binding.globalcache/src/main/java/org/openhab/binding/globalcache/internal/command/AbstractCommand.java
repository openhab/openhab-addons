/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.globalcache.internal.command;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants;
import org.openhab.binding.globalcache.internal.GlobalCacheBindingConstants.CommandType;
import org.openhab.binding.globalcache.internal.handler.GlobalCacheHandler;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractCommand} class implements the basic functionality needed for all GlobalCache commands.
 *
 * @author Mark Hilbush - Initial contribution
 */
public abstract class AbstractCommand implements CommandInterface {
    private final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);

    private LinkedBlockingQueue<RequestMessage> requestQueue;

    private final int RESPONSE_QUEUE_MAX_DEPTH = 1;
    private final int RESPONSE_QUEUE_TIMEOUT = 3000;

    protected Thing thing;

    protected String ipAddress;

    // Actual command strings sent to/received from the device (without CR)
    protected String deviceCommand;
    protected String deviceReply;

    // Short human-readable name of the command
    protected String commandName;

    // Determines which TCP port will be used for the command
    private CommandType commandType;

    protected String module;
    protected String connector;

    protected String errorModule;
    protected String errorConnector;
    protected String errorCode;
    protected String errorMessage;

    private boolean isQuiet;

    /*
     * The {@link AbstractCommand} abstract class is the basis for all GlobalCache device command classes.
     *
     * @author Mark Hilbush - Initial contribution
     */
    public AbstractCommand(Thing t, LinkedBlockingQueue<RequestMessage> q, String n, CommandType c) {
        thing = t;
        requestQueue = q;
        commandName = n;
        commandType = c;
        setQuiet(false);

        module = null;
        connector = null;
        deviceCommand = null;
        deviceReply = null;
        errorCode = null;
        errorMessage = null;

        ipAddress = ((GlobalCacheHandler) thing.getHandler()).getIP();
    }

    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getModule() {
        return module;
    }

    public void setModule(String m) {
        module = m;
    }

    @Override
    public String getConnector() {
        return connector;
    }

    public void setConnector(String c) {
        connector = c;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public boolean isGC100Model12() {
        return thing.getThingTypeUID().equals(GlobalCacheBindingConstants.THING_TYPE_GC_100_12);
    }

    @Override
    public abstract void parseSuccessfulReply();

    public boolean isSuccessful() {
        return errorCode == null ? true : false;
    }

    public String successAsString() {
        return errorCode == null ? "succeeded" : "failed";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorModule() {
        return errorModule;
    }

    public String getErrorConnector() {
        return errorConnector;
    }

    protected boolean isQuiet() {
        return isQuiet;
    }

    private void setQuiet(boolean quiet) {
        isQuiet = quiet;
    }

    /*
     * Execute a GlobalCache device command
     */
    public void executeQuiet() {
        setQuiet(true);
        execute();
    }

    public void execute() {
        if (requestQueue == null) {
            createGenericError("Execute method was called with a null requestQueue");
            return;
        }

        if (deviceCommand == null) {
            createGenericError("Execute method was called with a null deviceCommand");
            return;
        }

        if (thing == null) {
            createGenericError("Execute method was called with a null thing");
            return;
        }

        // Send command & get response
        if (sendCommand()) {
            parseSuccessfulReply();
            if (!isQuiet()) {
                logSuccess();
            }
        } else {
            if (!isQuiet()) {
                logFailure();
            }
        }
        return;
    }

    /*
     * Place a request message onto the request queue, then wait on the response queue for the
     * response message. The CommandHandler private class in GlobalCacheHandler.java
     * is responsible for the actual device interaction.
     */
    private boolean sendCommand() {
        // Create a response queue. The command processor will use this queue to return the device's reply.
        LinkedBlockingQueue<ResponseMessage> responseQueue = new LinkedBlockingQueue<>(RESPONSE_QUEUE_MAX_DEPTH);

        // Create the request message
        RequestMessage requestMsg = new RequestMessage(commandName, commandType, deviceCommand, responseQueue);

        try {
            // Put the request message on the request queue
            requestQueue.put(requestMsg);
            logger.trace("Put request on queue (depth={}), sent command '{}'", requestQueue.size(), deviceCommand);

            // Wait on the response queue for the response message
            ResponseMessage responseMsg = responseQueue.poll(RESPONSE_QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);

            if (responseMsg == null) {
                createGenericError("Timed out waiting on response queue for message");
                return false;
            }

            deviceReply = responseMsg.getDeviceReply();
            logger.trace("Got response message off response queue, received reply '{}'", deviceReply);

            if (isErrorReply(deviceReply)) {
                return false;
            }

        } catch (InterruptedException e) {
            createGenericError("Poll of response queue was interrupted");
            return false;
        }

        return true;
    }

    /*
     * Parse the reply and set error values if an error occurred.
     */
    private boolean isErrorReply(String reply) {
        logger.trace("Checking device reply for error condition: {}", reply);

        Pattern pattern;
        Matcher matcher;

        // Generic (generated by binding) errors are of the form
        // ERROR: message
        if (reply.startsWith("ERROR:")) {
            createGenericError(reply);
            return true;
        }

        // iTach response for unknown command are of the form
        // unknowncommand,eee, where eee is the error number
        if (reply.startsWith("unknowncommand,") && reply.length() >= 16) {
            String eCode = reply.substring(15);
            createGenericError("Device does not understand command, error code is " + eCode);
            errorCode = eCode;
            return true;
        }

        // GC-100 response for unknown command are of the form
        // unknowncommand ee, where ee is the error number
        if (reply.startsWith("unknowncommand ") && reply.length() >= 16) {
            errorModule = "";
            errorConnector = "";
            errorCode = reply.substring(15);
            errorMessage = lookupErrorMessage(errorCode, GC100_ERROR_MESSAGES);
            logger.debug("Device reply indicates GC-100 error condition");
            return true;
        }

        // iTach error replies are of the form ERR_m:c,eee, where m is the module number,
        // c is the connector number, and eee is the error number
        pattern = Pattern.compile("ERR_[0-9]:[0-3],\\d\\d\\d");
        matcher = pattern.matcher(reply);
        if (matcher.find()) {
            errorModule = reply.substring(4, 5);
            errorConnector = reply.substring(6, 7);
            errorCode = reply.substring(8, 11);
            errorMessage = lookupErrorMessage(errorCode, ITACH_ERROR_MESSAGES);
            logger.debug("Device reply indicates iTach error condition");
            return true;
        }

        // Flex general error replies are of the form ERR eee, where eee is the error number
        pattern = Pattern.compile("ERR \\d\\d\\d");
        matcher = pattern.matcher(reply);
        if (matcher.find()) {
            errorModule = "";
            errorConnector = "";
            errorCode = reply.substring(4);
            errorMessage = lookupErrorMessage(errorCode, FLEX_ERROR_MESSAGES);
            logger.debug("Device reply indicates Flex general error condition");
            return true;
        }

        // Flex infrared error replies are of the form ERR IReee, where eee is the error number
        pattern = Pattern.compile("ERR IR\\d\\d\\d");
        matcher = pattern.matcher(reply);
        if (matcher.find()) {
            errorModule = "";
            errorConnector = "";
            errorCode = reply.substring(6);
            errorMessage = lookupErrorMessage(errorCode, FLEX_IR_ERROR_MESSAGES);
            logger.debug("Device reply indicates Flex IR error condition");
            return true;
        }

        // Flex serial error replies are of the form ERR SLeee, where eee is the error number
        pattern = Pattern.compile("ERR SL\\d\\d\\d");
        matcher = pattern.matcher(reply);
        if (matcher.find()) {
            errorModule = "";
            errorConnector = "";
            errorCode = reply.substring(6);
            errorMessage = lookupErrorMessage(errorCode, FLEX_SL_ERROR_MESSAGES);
            logger.debug("Device reply indicates Flex SL error condition");
            return true;
        }

        errorCode = null;
        return false;
    }

    private void createGenericError(String s) {
        errorModule = "N/A";
        errorConnector = "N/A";
        errorCode = "N/A";
        errorMessage = s;
    }

    private String lookupErrorMessage(String errorCode, String[] errorMessageArray) {
        int eCode;
        try {
            eCode = Integer.parseInt(errorCode);

        } catch (NumberFormatException e) {
            eCode = 0;
            logger.debug("Badly formatted error code '{}' received from device: {}", errorCode, e.getMessage());
        }

        if (eCode < 1 || eCode > errorMessageArray.length) {
            eCode = 0;
        }
        return errorMessageArray[eCode];
    }

    /*
     * Errors returned by GlobalCache iTach devices
     */
    private static final String[] ITACH_ERROR_MESSAGES = {
            // 0
            "Unknown error",
            // 1
            "Invalid command. Command not found.",
            // 2
            "Invalid module address (does not exist).",
            // 3
            "Invalid connector address (does not exist).",
            // 4
            "Invalid ID value.",
            // 5
            "Invalid frequency value",
            // 6
            "Invalid repeat value.",
            // 7
            "Invalid offset value.",
            // 8
            "Invalid pulse count.",
            // 9
            "Invalid pulse data.",
            // 10
            "Uneven amount of <on|off> statements.",
            // 11
            "No carriage return found.",
            // 12
            "Repeat count exceeded.",
            // 13
            "IR command sent to input connector.",
            // 14
            "Blaster command sent to non-blaster connector.",
            // 15
            "No carriage return before buffer full.",
            // 16
            "No carriage return.",
            // 17
            "Bad command syntax.",
            // 18
            "Sensor command sent to non-input connector.",
            // 19
            "Repeated IR transmission failure.",
            // 20
            "Above designated IR <on|off> pair limit.",
            // 21
            "Symbol odd boundary.",
            // 22
            "Undefined symbol.",
            // 23
            "Unknown option.",
            // 24
            "Invalid baud rate setting.",
            // 25
            "Invalid flow control setting.",
            // 26
            "Invalid parity setting.",
            // 27
            "Settings are locked."
            //
    };

    /*
     * Errors returned by GlobalCache GC-100 devices
     */
    private static final String[] GC100_ERROR_MESSAGES = {
            // 0
            "Unknown error.",
            // 1
            "Time out occurred because <CR> not received. Request not processed",
            // 2
            "Invalid module address (module does not exist) received with getversion.",
            // 3
            "Invalid module address (module does not exist).",
            // 4
            "Invalid connector address.",
            // 5
            "Connector address 1 is set up as “sensor in” when attempting IR command.",
            // 6
            "Connector address 2 is set up as “sensor in” when attempting IR command.",
            // 7
            "Connector address 3 is set up as “sensor in” when attempting IR command.",
            // 8
            "Offset is set to an even transition number, but should be odd.",
            // 9
            "Maximum number of transitions exceeded (256 total allowed).",
            // 10
            "Number of transitions in the IR command is not even.",
            // 11
            "Contact closure command sent to a module that is not a relay.",
            // 12
            "Missing carriage return. All commands must end with a carriage return.",
            // 13
            "State was requested of an invalid connector address.",
            // 14
            "Command sent to the unit is not supported by the GC-100.",
            // 15
            "Maximum number of IR transitions exceeded. (SM_IR_INPROCESS)",
            // 16
            "Invalid number of IR transitions (must be an even number).",
            // 17
            "Unknown error.",
            // 18
            "Unknown error.",
            // 19
            "Unknown error.",
            // 20
            "Unknown error.",
            // 21
            "Attempted to send an IR command to a non-IR module.",
            // 22
            "Unknown error.",
            // 23
            "Command sent is not supported by this type of module."
            //
    };

    /*
     * General errors returned by Flex devices
     */
    private static final String[] FLEX_ERROR_MESSAGES = {
            // 0
            "Unknown error.",
            // 1
            "Invalid command.  Command not found.",
            // 2
            "Bad command syntax used with a known command.",
            // 3
            "Invalid connector address (does not exist).",
            // 4
            "No carriage return found.",
            // 5
            "Command not supported by current Flex Link Port setting.",
            // 6
            "Settings are locked.",
            //
    };

    /*
     * Infrared errors returned by Flex devices
     */
    private static final String[] FLEX_IR_ERROR_MESSAGES = {
            // 0
            "Unknown error.",
            // 1
            "Invalid ID value.",
            // 2
            "Invalid frequency.",
            // 3
            "Invalid repeat.",
            // 4
            "Invalid offset.",
            // 5
            "Invalid IR pulse value.",
            // 6
            "Odd amount of IR pulse values (must be even).",
            // 7
            "Maximum pulse pair limit exceeded.",
            //
    };

    /*
     * Serial errors returned by Flex devices
     */
    private static final String[] FLEX_SL_ERROR_MESSAGES = {
            // 0
            "Unknown error.",
            // 1
            "Invalid baud rate.",
            // 2
            "Invalid flow control setting.",
            // 3
            "Invalid parity setting.",
            //
    };
}
