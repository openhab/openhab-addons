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
package org.openhab.binding.exec.internal.handler;

import static org.openhab.binding.exec.internal.ExecBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.exec.internal.ExecWhitelistWatchService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExecHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @author Constantin Piber - Added better argument support (delimiter and pass to shell)
 * @author Jan N. Klug - Add command whitelist check
 */
@NonNullByDefault
public class ExecHandler extends BaseThingHandler {
    /**
     * Use this to separate between command and parameter, and also between parameters.
     */
    public static final String CMD_LINE_DELIMITER = "@@";

    /**
     * Shell executables
     */
    public static final String[] SHELL_WINDOWS = new String[] { "cmd" };
    public static final String[] SHELL_NIX = new String[] { "sh", "bash", "zsh", "csh" };
    private final ExecWhitelistWatchService execWhitelistWatchService;

    private Logger logger = LoggerFactory.getLogger(ExecHandler.class);

    private final BundleContext bundleContext;

    // List of Configurations constants
    public static final String INTERVAL = "interval";
    public static final String TIME_OUT = "timeout";
    public static final String COMMAND = "command";
    public static final String TRANSFORM = "transform";
    public static final String AUTORUN = "autorun";

    // RegEx to extract a parse a function String <code>'(.*?)\((.*)\)'</code>
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(.*?)\\((.*)\\)");

    private @Nullable ScheduledFuture<?> executionJob;
    private @Nullable String lastInput;

    private static Runtime rt = Runtime.getRuntime();

    public ExecHandler(Thing thing, ExecWhitelistWatchService execWhitelistWatchService) {
        super(thing);
        this.bundleContext = FrameworkUtil.getBundle(ExecHandler.class).getBundleContext();
        this.execWhitelistWatchService = execWhitelistWatchService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Placeholder for later refinement
        } else {
            if (channelUID.getId().equals(RUN)) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        scheduler.schedule(this::execute, 0, TimeUnit.SECONDS);
                    }
                }
            } else if (channelUID.getId().equals(INPUT)) {
                if (command instanceof StringType) {
                    String previousInput = lastInput;
                    lastInput = command.toString();
                    if (lastInput != null && !lastInput.equals(previousInput)) {
                        if (getConfig().get(AUTORUN) != null && ((Boolean) getConfig().get(AUTORUN))) {
                            logger.trace("Executing command '{}' after a change of the input channel to '{}'",
                                    getConfig().get(COMMAND), lastInput);
                            scheduler.schedule(this::execute, 0, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        if (executionJob == null || executionJob.isCancelled()) {
            if ((getConfig().get(INTERVAL)) != null && ((BigDecimal) getConfig().get(INTERVAL)).intValue() > 0) {
                int pollingInterval = ((BigDecimal) getConfig().get(INTERVAL)).intValue();
                executionJob = scheduler.scheduleWithFixedDelay(this::execute, 0, pollingInterval, TimeUnit.SECONDS);
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

    public void execute() {
        String commandLine = (String) getConfig().get(COMMAND);
        if (!execWhitelistWatchService.isWhitelisted(commandLine)) {
            logger.warn("Tried to execute '{}', but it is not contained in whitelist.", commandLine);
            return;
        }

        int timeOut = 60000;
        if ((getConfig().get(TIME_OUT)) != null) {
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

            Date date = Calendar.getInstance().getTime();
            try {
                if (lastInput != null) {
                    commandLine = String.format(commandLine, date, lastInput);
                } else {
                    commandLine = String.format(commandLine, date);
                }
            } catch (IllegalFormatException e) {
                logger.warn(
                        "An exception occurred while formatting the command line '{}' with the current time '{}' and input value '{}': {}",
                        commandLine, date, lastInput, e.getMessage());
                updateState(RUN, OnOffType.OFF);
                updateState(OUTPUT, new StringType(e.getMessage()));
                return;
            }

            String[] cmdArray;
            String[] shell;
            if (commandLine.contains(CMD_LINE_DELIMITER)) {
                logger.debug("Splitting by '{}'", CMD_LINE_DELIMITER);
                try {
                    cmdArray = commandLine.split(CMD_LINE_DELIMITER);
                } catch (PatternSyntaxException e) {
                    logger.warn("An exception occurred while splitting '{}' : '{}'", commandLine, e.getMessage());
                    updateState(RUN, OnOffType.OFF);
                    updateState(OUTPUT, new StringType(e.getMessage()));
                    return;
                }
            } else {
                // Invoke shell with 'c' option and pass string
                logger.debug("Passing to shell for parsing command.");
                switch (getOperatingSystemType()) {
                    case WINDOWS:
                        shell = SHELL_WINDOWS;
                        logger.debug("OS: WINDOWS ({})", getOperatingSystemName());
                        cmdArray = createCmdArray(shell, "/c", commandLine);
                        break;
                    case LINUX:
                    case MAC:
                    case BSD:
                    case SOLARIS:
                        // assume sh is present, should all be POSIX-compliant
                        shell = SHELL_NIX;
                        logger.debug("OS: *NIX ({})", getOperatingSystemName());
                        cmdArray = createCmdArray(shell, "-c", commandLine);
                        break;
                    default:
                        logger.debug("OS: Unknown ({})", getOperatingSystemName());
                        logger.warn("OS {} not supported, please manually split commands!", getOperatingSystemName());
                        updateState(RUN, OnOffType.OFF);
                        updateState(OUTPUT, new StringType("OS not supported, please manually split commands!"));
                        return;
                }
            }

            if (cmdArray.length == 0) {
                logger.trace("Empty command received, not executing");
                return;
            }

            logger.trace("The command to be executed will be '{}'", Arrays.asList(cmdArray));

            Process proc;
            try {
                proc = rt.exec(cmdArray);
            } catch (Exception e) {
                logger.warn("An exception occurred while executing '{}' : '{}'", Arrays.asList(cmdArray),
                        e.getMessage());
                updateState(RUN, OnOffType.OFF);
                updateState(OUTPUT, new StringType(e.getMessage()));
                return;
            }

            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();

            try (InputStreamReader isr = new InputStreamReader(proc.getInputStream());
                    BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                    logger.debug("Exec [{}]: '{}'", "OUTPUT", line);
                }
            } catch (IOException e) {
                logger.warn("An exception occurred while reading the stdout when executing '{}' : '{}'", commandLine,
                        e.getMessage());
            }

            try (InputStreamReader isr = new InputStreamReader(proc.getErrorStream());
                    BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    errorBuilder.append(line).append("\n");
                    logger.debug("Exec [{}]: '{}'", "ERROR", line);
                }
            } catch (IOException e) {
                logger.warn("An exception occurred while reading the stderr when executing '{}' : '{}'", commandLine,
                        e.getMessage());
            }

            boolean exitVal = false;
            try {
                exitVal = proc.waitFor(timeOut, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("An exception occurred while waiting for the process ('{}') to finish : '{}'", commandLine,
                        e.getMessage());
            }

            if (!exitVal) {
                logger.warn("Forcibly termininating the process ('{}') after a timeout of {} ms", commandLine, timeOut);
                proc.destroyForcibly();
            }

            updateState(RUN, OnOffType.OFF);
            updateState(EXIT, new DecimalType(proc.exitValue()));

            outputBuilder.append(errorBuilder.toString());

            outputBuilder.append(errorBuilder.toString());

            String transformedResponse = Objects.requireNonNull(StringUtils.chomp(outputBuilder.toString()));
            String transformation = (String) getConfig().get(TRANSFORM);

            if (transformation != null && transformation.length() > 0) {
                transformedResponse = transformResponse(transformedResponse, transformation);
            }

            updateState(OUTPUT, new StringType(transformedResponse));

            DateTimeType stampType = new DateTimeType(ZonedDateTime.now());
            updateState(LAST_EXECUTION, stampType);
        }
    }

    protected @Nullable String transformResponse(String response, String transformation) {
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
            logger.warn("An exception occurred while transforming '{}' with '{}' : '{}'", response, transformation,
                    te.getMessage());

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

    /**
     * Transforms the command string into an array.
     * Either invokes the shell and passes using the "c" option
     * or (if command already starts with one of the shells) splits by space.
     *
     * @param shell (path), picks to first one to execute the command
     * @param cOption "c"-option string
     * @param commandLine to execute
     * @return command array
     */
    protected String[] createCmdArray(String[] shell, String cOption, String commandLine) {
        boolean startsWithShell = false;
        for (String sh : shell) {
            if (commandLine.startsWith(sh + " ")) {
                startsWithShell = true;
                break;
            }
        }

        if (!startsWithShell) {
            return new String[] { shell[0], cOption, commandLine };
        } else {
            logger.debug("Splitting by spaces");
            try {
                return commandLine.split(" ");
            } catch (PatternSyntaxException e) {
                logger.warn("An exception occurred while splitting '{}' : '{}'", commandLine, e.getMessage());
                updateState(RUN, OnOffType.OFF);
                updateState(OUTPUT, new StringType(e.getMessage()));
                return new String[] {};
            }
        }
    }

    /**
     * Contains information about which operating system openHAB is running on.
     * Found on https://stackoverflow.com/a/31547504/7508309, slightly modified
     *
     * @author Constantin Piber (for Memin) - Initial contribution
     */
    public enum OS {
        WINDOWS,
        LINUX,
        BSD,
        MAC,
        SOLARIS,
        UNKNOWN,
        NOT_SET
    }

    private static OS os = OS.NOT_SET;

    public static OS getOperatingSystemType() {
        if (os == OS.NOT_SET) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux") || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.endsWith("bsd")) {
                os = OS.BSD;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            } else {
                os = OS.UNKNOWN;
            }
        }
        return os;
    }

    public static String getOperatingSystemName() {
        String osname = System.getProperty("os.name");
        return osname != null ? osname : "unknown";
    }
}
