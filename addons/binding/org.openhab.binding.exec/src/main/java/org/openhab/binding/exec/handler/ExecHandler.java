/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.exec.handler;

import static org.openhab.binding.exec.ExecBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExecHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class ExecHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(ExecHandler.class);

    // List of Configurations constants
    public static final String INTERVAL = "interval";
    public static final String TIME_OUT = "timeout";
    public static final String COMMAND = "command";
    public static final String TRANSFORM = "transform";
    public static final String RUN_ON_INPUT = "runOnInput";
    public static final String REPEAT_ENABLED = "repeatEnabled";

    // RegEx to extract a parse a function String <code>'(.*?)\((.*)\)'</code>
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    private ScheduledFuture<?> periodicExecutionJob;
    private ItemRegistry itemRegistry;
    private String currentInput = "";
    private String previousInput = null;
    private StrSubstitutor substitutor;
    private ReentrantLock lock;

    private static Runtime rt = Runtime.getRuntime();

    public ExecHandler(Thing thing, ItemRegistry itemRegistry) {
        super(thing);

        lock = new ReentrantLock();

        this.itemRegistry = itemRegistry;

        substitutor = new StrSubstitutor(new ExecStrLookup());
        substitutor.setEnableSubstitutionInVariables(true);
    }

    @Override
    public void initialize() {

        if (periodicExecutionJob == null || periodicExecutionJob.isCancelled()) {
            if (((BigDecimal) getConfig().get(INTERVAL)) != null
                    && ((BigDecimal) getConfig().get(INTERVAL)).intValue() > 0) {
                int polling_interval = ((BigDecimal) getConfig().get(INTERVAL)).intValue();
                periodicExecutionJob = scheduler.scheduleWithFixedDelay(new PeriodicExecutionRunnable(), 0,
                        polling_interval, TimeUnit.SECONDS);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        if (periodicExecutionJob != null && !periodicExecutionJob.isCancelled()) {
            periodicExecutionJob.cancel(true);
            periodicExecutionJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            // Placeholder for later refinement
        } else {
            if (channelUID.getId().equals(RUN)) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        scheduler.schedule(new ExecutionRunnable(currentInput), 0, TimeUnit.SECONDS);
                    }
                }
            } else if (channelUID.getId().equals(INPUT)) {
                currentInput = command.toString();
                if (getConfig().get(RUN_ON_INPUT) != null && ((Boolean) getConfig().get(RUN_ON_INPUT)).booleanValue()) {
                    if (currentInput != null && currentInput.equals(previousInput)
                            && getConfig().get(REPEAT_ENABLED) != null
                            && ((Boolean) getConfig().get(REPEAT_ENABLED)).booleanValue()) {
                        logger.trace("Executing command '{}' after because of a repitition on the input channel ('{}')",
                                getConfig().get(COMMAND), command.toString());
                        scheduler.schedule(new ExecutionRunnable(currentInput), 0, TimeUnit.SECONDS);
                    } else {
                        if (currentInput != null && previousInput != null && !currentInput.equals(previousInput)
                                || (previousInput == null && currentInput != null)) {
                            logger.trace("Executing command '{}' after a change of the input channel to '{}'",
                                    getConfig().get(COMMAND), command.toString());
                            scheduler.schedule(new ExecutionRunnable(currentInput), 0, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        }
    }

    class ExecStrLookup extends StrLookup {

        String execInput = "";

        public void setInput(String input) {
            this.execInput = input;
        }

        @Override
        public String lookup(String key) {
            try {
                String transform = null;
                String format = null;
                String subkey = "";
                if (key.contains(":")) {
                    String parts[] = key.split(":");
                    subkey = parts[0];

                    if (parts.length == 2) {
                        format = parts[1];
                    } else if (parts.length == 3) {
                        transform = parts[1];
                        format = parts[2];
                    }
                } else {
                    subkey = key;
                }

                if (subkey.equals("exec-time")) {
                    try {
                        // Transform is not relevant here, as our source is a Date
                        if (format != null) {
                            return String.format(format, Calendar.getInstance().getTime());
                        } else {
                            return null;
                        }
                    } catch (PatternSyntaxException e) {
                        logger.warn("Invalid substitution key '{}'", key);
                        return null;
                    }
                } else if (subkey.equals("exec-input") && !execInput.equals("")) {
                    try {
                        String transformedInput = execInput;
                        if (transform != null) {
                            transformedInput = transformString(transformedInput, transform);
                        }
                        if (format != null) {
                            return String.format(format, transformedInput);
                        } else {
                            return execInput;
                        }
                    } catch (PatternSyntaxException e) {
                        logger.warn("Invalid substitution key '{}'", key);
                        return null;
                    }
                } else {
                    try {
                        String transformedInput = itemRegistry.getItem(subkey).getState().toString();
                        if (transform != null) {
                            transformedInput = transformString(transformedInput, transform);
                        }
                        if (format != null) {
                            return String.format(format, transformedInput);
                        } else {
                            return transformedInput;
                        }
                    } catch (PatternSyntaxException e) {
                        logger.warn("Invalid substitution key '{}'", key);
                        return null;
                    }
                }
            } catch (ItemNotFoundException e) {
                return null;
            }
        }
    }

    protected class PeriodicExecutionRunnable extends ExecutionRunnable {

        public PeriodicExecutionRunnable() {
            super("");
        }

        @Override
        public void run() {
            this.input = currentInput;
            super.run();
        }
    }

    protected class ExecutionRunnable implements Runnable {

        protected String input;

        public ExecutionRunnable(String input) {
            this.input = input;
        }

        @Override
        public void run() {

            try {
                lock.lock();

                String commandLine = (String) getConfig().get(COMMAND);

                int timeOut = 60000;
                if (((BigDecimal) getConfig().get(TIME_OUT)) != null) {
                    timeOut = ((BigDecimal) getConfig().get(TIME_OUT)).intValue() * 1000;
                }

                if (commandLine != null && !commandLine.isEmpty()) {

                    updateState(RUN, OnOffType.ON);

                    // For some obscure reason, when using Apache Common Exec, or using a straight implementation of
                    // Runtime.Exec(), on Mac OS X (Yosemite and El Capitan), there seems to be a lock race condition
                    // randomly appearing (on UNIXProcess) *when* one tries to gobble up the stdout and sterr output of
                    // the
                    // subprocess in separate threads. It seems to be common "wisdom" to do that in separate threads,
                    // but
                    // only when keeping everything between .exec() and .waitfor() in the same thread, this lock race
                    // condition seems to go away. This approach of not reading the outputs in separate threads *might*
                    // be a
                    // problem for external commands that generate a lot of output, but this will be dependent on the
                    // limits
                    // of the underlying operating system.

                    try {
                        ((ExecStrLookup) substitutor.getVariableResolver()).setInput(input);
                        commandLine = substitutor.replace(commandLine);
                    } catch (Exception e) {
                        logger.error("An exception occurred while formatting the command line : '{}'", e.getMessage(),
                                e);
                        updateState(RUN, OnOffType.OFF);
                        return;
                    }

                    // try {
                    // if (input != null) {
                    // commandLine = String.format(commandLine, Calendar.getInstance().getTime(), input);
                    // } else {
                    // commandLine = String.format(commandLine, Calendar.getInstance().getTime());
                    // }
                    // } catch (IllegalFormatException e) {
                    // logger.error(
                    // "An exception occurred while formatting the command line with the current time and input values :
                    // '{}'",
                    // e.getMessage());
                    // updateState(RUN, OnOffType.OFF);
                    // return;
                    // }

                    logger.trace("The command to be executed will be '{}'", commandLine);

                    Process proc = null;
                    try {
                        proc = rt.exec(commandLine.toString());
                    } catch (Exception e) {
                        logger.error("An exception occurred while executing '{}' : '{}'",
                                new Object[] { commandLine.toString(), e.getMessage() });
                        updateState(RUN, OnOffType.OFF);

                        updateState(OUTPUT, new StringType(e.getMessage()));
                        return;
                    }

                    StringBuilder outputBuilder = new StringBuilder();
                    StringBuilder errorBuilder = new StringBuilder();

                    try (InputStreamReader isr = new InputStreamReader(proc.getInputStream());
                            BufferedReader br = new BufferedReader(isr);) {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            outputBuilder.append(line).append("\n");
                            logger.debug("Exec [{}]: '{}'", "OUTPUT", line);
                        }
                        isr.close();
                    } catch (IOException e) {
                        logger.error("An exception occurred while reading the stdout when executing '{}' : '{}'",
                                new Object[] { commandLine.toString(), e.getMessage() });
                    }

                    try (InputStreamReader isr = new InputStreamReader(proc.getErrorStream());
                            BufferedReader br = new BufferedReader(isr);) {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            errorBuilder.append(line).append("\n");
                            logger.debug("Exec [{}]: '{}'", "ERROR", line);
                        }
                        isr.close();
                    } catch (IOException e) {
                        logger.error("An exception occurred while reading the stderr when executing '{}' : '{}'",
                                new Object[] { commandLine.toString(), e.getMessage() });
                    }

                    boolean exitVal = false;
                    try {
                        exitVal = proc.waitFor(timeOut, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        logger.error("An exception occurred while waiting for the process ('{}') to finish : '{}'",
                                new Object[] { commandLine.toString(), e.getMessage() });
                    }

                    if (!exitVal) {
                        logger.warn("Forcibly termininating the process ('{}') after a timeout of {} ms",
                                new Object[] { commandLine.toString(), timeOut });
                        proc.destroyForcibly();
                    }

                    updateState(RUN, OnOffType.OFF);
                    updateState(EXIT, new DecimalType(proc.exitValue()));

                    outputBuilder.append(errorBuilder.toString());

                    String transformedResponse = StringUtils.chomp(outputBuilder.toString());
                    String transformation = (String) getConfig().get(TRANSFORM);

                    if (transformation != null && transformation.length() > 0) {
                        transformedResponse = transformString(transformedResponse, transformation);
                    }

                    updateState(OUTPUT, new StringType(transformedResponse));

                    DateTimeType stampType = new DateTimeType(Calendar.getInstance());
                    updateState(LAST_EXECUTION, stampType);

                }

            } catch (Exception e) {
                logger.error("An exception occurred while executing a command : '{}'", e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }

    };

    protected String transformString(String response, String transformation) {
        String transformedResponse;

        try {
            String[] parts = splitTransformationConfig(transformation);
            String transformationType = parts[0];
            String transformationFunction = parts[1];

            TransformationService transformationService = TransformationHelper.getTransformationService(bundleContext,
                    transformationType);
            if (transformationService != null) {
                transformedResponse = transformationService.transform(transformationFunction, response);
            } else {
                transformedResponse = response;
                logger.warn("Couldn't transform response because transformationService of type '{}' is unavailable",
                        transformationType);
            }
        } catch (TransformationException te) {
            logger.error("An exception occurred while transforming '{}' with '{}' : '{}'",
                    new Object[] { response, transformation, te.getMessage() });

            // in case of an error we return the response without any transformation
            transformedResponse = response;
        }

        logger.debug("Transformed response is '{}'", transformedResponse);
        return transformedResponse;
    }

    /**
     * Splits a transformation configuration string into its two parts - the
     * transformation type and the function/pattern to apply.
     *
     * @param transformation the string to split
     * @return a string array with exactly two entries for the type and the function
     */
    protected String[] splitTransformationConfig(String transformation) {
        Matcher matcher = EXTRACT_FUNCTION_PATTERN.matcher(transformation);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("given transformation function '" + transformation
                    + "' does not follow the expected pattern '<function>(<pattern>)'");
        }
        matcher.reset();

        matcher.find();
        String type = matcher.group(1);
        String pattern = matcher.group(2);

        return new String[] { type, pattern };
    }

}
