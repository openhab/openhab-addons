/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.transform.exec.internal;

import java.time.Duration;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.exec.ExecUtil;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by command line.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Jan N. Klug - added command whitelist service
 */
@NonNullByDefault
@Component(property = { "openhab.transform=EXEC" })
public class ExecTransformationService implements TransformationService {
    private static final Pattern SPLIT_ON_SPACE = Pattern.compile("(['])((?:\\\\\\1|.)+?)\\1|([^\\s']+)");
    private final Logger logger = LoggerFactory.getLogger(ExecTransformationService.class);
    private final ExecTransformationWhitelistWatchService execTransformationWhitelistWatchService;

    @Activate
    public ExecTransformationService(
            @Reference ExecTransformationWhitelistWatchService execTransformationWhitelistWatchService) {
        this.execTransformationWhitelistWatchService = execTransformationWhitelistWatchService;
    }

    /**
     * Transforms the input <code>source</code> by the command line.
     *
     * @param commandLine the command to execute. Command line should contain %s string, which will be replaced by the
     *            input data.
     * @param source the input to transform
     */
    @Override
    public @Nullable String transform(String commandLine, String source) throws TransformationException {
        if (commandLine == null || source == null) {
            throw new TransformationException("the given parameters 'commandLine' and 'source' must not be null");
        }

        if (!execTransformationWhitelistWatchService.isWhitelisted(commandLine)) {
            logger.warn("Tried to execute '{}', but it is not contained in whitelist.", commandLine);
            return null;
        }
        logger.debug("about to transform '{}' by the commandline '{}'", source, commandLine);

        long startTime = System.currentTimeMillis();

        String formattedCommandLine = String.format(commandLine, source);
        String[] cmdLineParts = SPLIT_ON_SPACE.matcher(formattedCommandLine).results()
                .map(mr -> mr.group(2) == null ? mr.group() : mr.group(2)).toArray(String[]::new);
        String result = ExecUtil.executeCommandLineAndWaitResponse(Duration.ofSeconds(5), cmdLineParts);
        logger.trace("command line execution elapsed {} ms", System.currentTimeMillis() - startTime);

        return result;
    }
}
