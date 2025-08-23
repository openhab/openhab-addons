/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.exec.internal.ExecWhitelistWatchService;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.generic.ChannelTransformation;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.StringUtils;
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

    private static final String EXEC_HANDLER_THREADPOOL_NAME = "execBinding";

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

    // List of Configurations constants
    public static final String INTERVAL = "interval";
    public static final String TIME_OUT = "timeout";
    public static final String COMMAND = "command";
    public static final String TRANSFORM = "transform";
    public static final String AUTORUN = "autorun";
    public static final String CHARSET = "charset";

    private ExecutorService executor;
    private @Nullable ScheduledFuture<?> scheduledTask;
    private volatile @Nullable Future<?> lastTriggeredTask;
    private @Nullable String lastInput;

    private static Runtime rt = Runtime.getRuntime();

    private @Nullable ChannelTransformation channelTransformation;

    public ExecHandler(Thing thing, ExecWhitelistWatchService execWhitelistWatchService) {
        super(thing);
        this.execWhitelistWatchService = execWhitelistWatchService;
        this.executor = ThreadPoolManager.getPool(EXEC_HANDLER_THREADPOOL_NAME);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Placeholder for later refinement
        } else {
            if (channelUID.getId().equals(RUN)) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        executor.execute(this::execute);
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
                            executor.execute(this::execute);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        channelTransformation = new ChannelTransformation((List<String>) getConfig().get(TRANSFORM));

        ScheduledFuture<?> task = scheduledTask;
        if (task == null || task.isCancelled()) {
            if ((getConfig().get(INTERVAL)) != null && ((BigDecimal) getConfig().get(INTERVAL)).intValue() > 0) {
                int pollingInterval = ((BigDecimal) getConfig().get(INTERVAL)).intValue();
                scheduledTask = scheduler.scheduleWithFixedDelay(this::triggerExecution, 0, pollingInterval,
                        TimeUnit.SECONDS);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        Future<?> task = scheduledTask;
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            scheduledTask = null;
        }
        task = lastTriggeredTask;
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            lastTriggeredTask = null;
        }
        channelTransformation = null;
    }

    private void triggerExecution() {
        lastTriggeredTask = executor.submit(this::execute);
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

        Charset charset = null;
        String charsetValue = (String) getConfig().get(CHARSET);
        if (charsetValue != null && !charsetValue.isBlank()) {
            try {
                charset = Charset.forName(charsetValue);
            } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
                logger.warn("Invalid or unsupported character encoding '{}', falling back to UTF-8", charsetValue);
            }
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        if (commandLine != null && !commandLine.isEmpty()) {
            updateState(RUN, OnOffType.ON);

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
                updateState(STDOUT, new StringType());
                updateState(STDERR, new StringType(e.getMessage()));
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
                    updateState(STDOUT, new StringType());
                    updateState(STDERR, new StringType(e.getMessage()));
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
                        updateState(STDOUT, new StringType());
                        updateState(STDERR, new StringType("OS not supported, please manually split commands!"));
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
                updateState(STDOUT, new StringType());
                updateState(STDERR, new StringType(e.getMessage()));
                return;
            }

            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();

            TextOutputConsumer errConsumer = new TextOutputConsumer();
            Future<List<String>> stdErrFuture = errConsumer.consume(proc.getErrorStream(), charset,
                    Thread.currentThread().getName() + "-stderr-consumer");

            try (InputStreamReader isr = new InputStreamReader(proc.getInputStream(), charset);
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

            try {
                List<String> stdErr = stdErrFuture.get(timeOut, TimeUnit.MILLISECONDS);
                for (String line : stdErr) {
                    errorBuilder.append(line).append("\n");
                    logger.debug("Exec [{}]: '{}'", "ERROR", line);
                }
            } catch (InterruptedException e) {
                logger.debug("Interrupted while waiting for process ('{}') stderr content", commandLine);
            } catch (TimeoutException e) {
                logger.warn("Timed out while waiting for process ('{}') stderr content", commandLine);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                logger.warn("An exception occurred while reading the stderr when executing '{}' : '{}'", commandLine,
                        cause != null ? cause.getMessage() : e.getMessage());
            }

            updateState(RUN, OnOffType.OFF);
            updateState(EXIT, new DecimalType(proc.exitValue()));

            ChannelTransformation transformation = channelTransformation;
            String transformedStdout = Objects.requireNonNull(StringUtils.chomp(outputBuilder.toString()));
            String transformedStderr = Objects.requireNonNull(StringUtils.chomp(errorBuilder.toString()));
            if (transformation != null) {
                transformedStdout = transformation.apply(transformedStdout).orElse(transformedStdout);
                transformedStderr = transformation.apply(transformedStderr).orElse(transformedStderr);
            }
            updateState(STDOUT, new StringType(transformedStdout));
            updateState(STDERR, new StringType(transformedStderr));

            outputBuilder.append(errorBuilder.toString());

            outputBuilder.append(errorBuilder.toString());

            String transformedResponse = Objects.requireNonNull(StringUtils.chomp(outputBuilder.toString()));

            if (transformation != null) {
                transformedResponse = transformation.apply(transformedResponse).orElse(transformedResponse);
            }

            updateState(OUTPUT, new StringType(transformedResponse));
            updateState(LAST_EXECUTION, new DateTimeType());
        }
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
                updateState(STDOUT, new StringType());
                updateState(STDERR, new StringType(e.getMessage()));
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
            String operSys = System.getProperty("os.name");
            if (operSys == null) {
                os = OS.UNKNOWN;
                return os;
            }
            operSys = operSys.toLowerCase();
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
