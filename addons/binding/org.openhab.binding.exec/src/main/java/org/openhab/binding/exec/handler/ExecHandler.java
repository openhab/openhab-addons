/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.util.IllegalFormatException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
    public static final String AUTORUN = "autorun";

    // RegEx to extract a parse a function String <code>'(.*?)\((.*)\)'</code>
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    private ScheduledFuture<?> executionJob;
    private String lastInput;

    private static Runtime rt = Runtime.getRuntime();

    public ExecHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            // Placeholder for later refinement
        } else {
            if (channelUID.getId().equals(RUN)) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        scheduler.schedule(periodicExecutionRunnable, 0, TimeUnit.SECONDS);
                    }
                }
            } else if (channelUID.getId().equals(INPUT)) {
                if (command instanceof StringType) {
                    String previousInput = lastInput;
                    lastInput = command.toString();
                    if (lastInput != null && !lastInput.equals(previousInput)) {
                        if (getConfig().get(AUTORUN) != null && ((Boolean) getConfig().get(AUTORUN)).booleanValue()) {
                            lastInput = command.toString();
                            logger.trace("Executing command '{}' after a change of the input channel to '{}'",
                                    getConfig().get(COMMAND), command.toString());
                            scheduler.schedule(periodicExecutionRunnable, 0, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {

        if (executionJob == null || executionJob.isCancelled()) {
            if (((BigDecimal) getConfig().get(INTERVAL)) != null && ((BigDecimal) getConfig().get(INTERVAL)).intValue() > 0) {
                int polling_interval = ((BigDecimal) getConfig().get(INTERVAL)).intValue();
                executionJob = scheduler.scheduleWithFixedDelay(periodicExecutionRunnable, 0, polling_interval,
                        TimeUnit.SECONDS);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        if (executionJob != null && !executionJob.isCancelled()) {
            executionJob.cancel(true);
            executionJob = null;
        }
    }

    protected Runnable periodicExecutionRunnable = new Runnable() {

        @Override
        public void run() {

            String commandLine = (String) getConfig().get(COMMAND);

            int timeOut = 60000;
            if (((BigDecimal) getConfig().get(TIME_OUT)) != null) {
                timeOut = ((BigDecimal) getConfig().get(TIME_OUT)).intValue() * 1000;
            }

            if (commandLine != null && !commandLine.isEmpty()) {

                updateState(RUN, OnOffType.ON);

                // For some obscure reason, when using Apache Common Exec, or using a straight implementation of
                // Runtime.Exec(), on Mac OS X (Yosemite and El Capitan), there seems to be a lock race condition
                // randomly appearing (on UNIXProcess) *when* one tries to gobble up the stdout and sterr output of the
                // subprocess in separate threads. It seems to be common "wisdom" to do that in separate threads, but
                // only when keeping everything between .exec() and .waitfor() in the same thread, this lock race
                // condition seems to go away. This approach of not reading the outputs in separate threads *might* be a
                // problem for external commands that generate a lot of output, but this will be dependent on the limits
                // of the underlying operating system.

                try {
                    if (lastInput != null) {
                        commandLine = String.format(commandLine, Calendar.getInstance().getTime(), lastInput);
                    } else {
                        commandLine = String.format(commandLine, Calendar.getInstance().getTime());
                    }
                } catch (IllegalFormatException e) {
                    logger.error(
                            "An exception occurred while formatting the command line with the current time and input values : '{}'",
                            e.getMessage());
                    updateState(RUN, OnOffType.OFF);
                    return;
                }

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

                outputBuilder.append(errorBuilder.toString());

                String transformedResponse = StringUtils.chomp(outputBuilder.toString());
                String transformation = (String) getConfig().get(TRANSFORM);

                if (transformation != null && transformation.length() > 0) {
                    transformedResponse = transformResponse(transformedResponse, transformation);
                }

                updateState(OUTPUT, new StringType(transformedResponse));

                DateTimeType stampType = new DateTimeType(Calendar.getInstance());
                updateState(LAST_EXECUTION, stampType);

            }
        }

    };

    protected String transformResponse(String response, String transformation) {
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
