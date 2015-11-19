/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.exec.handler;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
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
import org.openhab.binding.exec.ExecBindingConstants;
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

    /** RegEx to extract a parse a function String <code>'(.*?)\((.*)\)'</code> */
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    private static final String CMD_LINE_DELIMITER = "@@";
    private String returnValue;
    private ScheduledFuture<?> executionJob;
    // ByteArrayOutputStream stdout;

    public ExecHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (channelUID.getId().equals(ExecBindingConstants.EXECUTE)) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    scheduler.schedule(periodicExecutionRunnable, 0, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public void initialize() {

        if (executionJob == null || executionJob.isCancelled()) {
            if (((BigDecimal) getConfig().get(INTERVAL)) != null) {
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

                CommandLine cmdLine = null;
                if (commandLine.contains(CMD_LINE_DELIMITER)) {
                    String[] cmdArray = commandLine.split(CMD_LINE_DELIMITER);
                    cmdLine = new CommandLine(cmdArray[0]);

                    for (int i = 1; i < cmdArray.length; i++) {
                        cmdLine.addArgument(cmdArray[i], false);
                    }
                } else {
                    cmdLine = CommandLine.parse(commandLine);
                }

                ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
                DefaultExecuteResultHandler resultHandler = new ExecResultHandler(watchdog);
                DefaultExecutor executor = new DefaultExecutor();

                // ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                ExecLogOutputStream op = new ExecLogOutputStream(logger, 0);
                PumpStreamHandler psh = new PumpStreamHandler(op, new ExecLogOutputStream(logger, 1));

                executor.setExitValue(0);
                executor.setStreamHandler(psh);
                executor.setWatchdog(watchdog);
                // stdout = new ByteArrayOutputStream();

                updateState(new ChannelUID(getThing().getUID(), ExecBindingConstants.EXECUTE), OnOffType.ON);

                try {
                    executor.execute(cmdLine, resultHandler);
                } catch (Exception e) {
                    logger.error("An exception occured while executing '{}' : '{}'", cmdLine.toString(),
                            e.getMessage());
                    e.printStackTrace();
                }

                // some time later the result handler callback was invoked so we
                // can safely request the exit code
                try {
                    resultHandler.waitFor(timeOut);
                } catch (InterruptedException e) {
                    logger.error("A timeout occured while executing '{}' : '{}'", cmdLine.toString(), e.getMessage());
                } catch (Exception e) {
                    logger.error("An exception occured while executing '{}' : '{}'", cmdLine.toString(),
                            e.getMessage());
                } finally {
                    watchdog.destroyProcess();
                }

                updateState(new ChannelUID(getThing().getUID(), ExecBindingConstants.EXECUTE), OnOffType.OFF);

                if (resultHandler.hasResult()) {
                    int exitCode = resultHandler.getExitValue();
                    updateState(new ChannelUID(getThing().getUID(), ExecBindingConstants.EXIT),
                            new DecimalType(exitCode));
                } else {
                    logger.warn("Can not set the process exit value after executing '{}'", cmdLine.toString());
                }

                returnValue = StringUtils.chomp(op.builder.toString());

                String transformedResponse = returnValue;
                String transformation = (String) getConfig().get(TRANSFORM);
                // If transformation is needed
                if (transformation != null && transformation.length() > 0)
                    transformedResponse = transformResponse(returnValue, transformation);

                updateState(new ChannelUID(getThing().getUID(), ExecBindingConstants.OUTPUT),
                        new StringType(transformedResponse));

                DateTimeType stampType = new DateTimeType(Calendar.getInstance());
                updateState(new ChannelUID(getThing().getUID(), ExecBindingConstants.LAST_EXECUTION), stampType);
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
            logger.error("An exception occured while transforming '{}' with '{}' : '{}'",
                    new Object[] { response, transformation, te.getMessage() });

            // in case of an error we return the response without any
            // transformation
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

    class ExecLogOutputStream extends LogOutputStream {

        Logger log;
        int level;
        public StringBuilder builder = new StringBuilder();

        public ExecLogOutputStream(Logger log, int level) {
            super(level);
            this.log = log;
        }

        @Override
        protected void processLine(String line, int level) {
            switch (level) {
                case 0: {
                    logger.debug("Exec [DEBUG]: '{}'", line);
                    builder.append(line).append("\n");
                    break;
                }
                case 1: {
                    logger.error("Exec [ERROR]: '{}'", line);
                    builder.append(line).append("\n");
                    break;
                }
            }
        }
    }

    public class ExecResultHandler extends DefaultExecuteResultHandler {

        ExecuteWatchdog watchdog;

        public ExecResultHandler(ExecuteWatchdog watchdog) {
            this.watchdog = watchdog;
        }

        @Override
        public void onProcessComplete(int exitValue) {
            super.onProcessComplete(exitValue);
            logger.info("The command was executed properly with process exit value '{}'", exitValue);
        }

        @Override
        public void onProcessFailed(ExecuteException e) {
            super.onProcessFailed(e);
            if (!watchdog.killedProcess()) {
                logger.error("The command failed to execute with process exit value '{}' : '{}'", e.getExitValue(),
                        e.getMessage());
            } else {
                logger.error("The command execution was terminated by the watchdog with process exit value '{}' : '{}'",
                        e.getExitValue(), e.getMessage());
            }
        }
    }

}
