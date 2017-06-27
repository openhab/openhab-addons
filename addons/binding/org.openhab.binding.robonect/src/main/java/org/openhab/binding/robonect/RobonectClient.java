/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.B64Code;
import org.openhab.binding.robonect.model.ErrorList;
import org.openhab.binding.robonect.model.ModelParser;
import org.openhab.binding.robonect.model.MowerInfo;
import org.openhab.binding.robonect.model.Name;
import org.openhab.binding.robonect.model.RobonectAnswer;
import org.openhab.binding.robonect.model.VersionInfo;
import org.openhab.binding.robonect.model.cmd.Command;
import org.openhab.binding.robonect.model.cmd.ErrorCommand;
import org.openhab.binding.robonect.model.cmd.ModeCommand;
import org.openhab.binding.robonect.model.cmd.NameCommand;
import org.openhab.binding.robonect.model.cmd.StartCommand;
import org.openhab.binding.robonect.model.cmd.StatusCommand;
import org.openhab.binding.robonect.model.cmd.StopCommand;
import org.openhab.binding.robonect.model.cmd.VersionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Meyer - Initial contribution
 */
public class RobonectClient {

    private final Logger logger = LoggerFactory.getLogger(RobonectClient.class);

    private final HttpClient httpClient;

    private final ModelParser parser;

    public class JobSettings {
        private static final String TIME_REGEX = "^[012]\\d:\\d\\d$";
        private ModeCommand.RemoteStart remoteStart;
        private ModeCommand.Mode after;
        private String start;
        private String end;

        public ModeCommand.RemoteStart getRemoteStart() {
            if (remoteStart != null) {
                return remoteStart;
            } else {
                logger.debug("No explicit remote start set. Returnt STANDARD.");
                return ModeCommand.RemoteStart.STANDARD;
            }
        }

        public void setRemoteStart(ModeCommand.RemoteStart remoteStart) {
            this.remoteStart = remoteStart;
        }

        public ModeCommand.Mode getAfter() {
            return after;
        }

        public void setAfter(ModeCommand.Mode after) {
            this.after = after;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            if (start != null && start.matches(TIME_REGEX)) {
                this.start = start;
            } else {
                logger.debug("Got start value {} but expected something matching {}", start, TIME_REGEX);
            }
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            if (end != null && end.matches(TIME_REGEX)) {
                this.end = end;
            } else {
                logger.debug("Got end value {} but expected something matching {}", end, TIME_REGEX);
            }
        }
    }

    private static class BasicResult implements Authentication.Result {
        private final HttpHeader header;
        private final URI uri;
        private final String value;

        public BasicResult(HttpHeader header, URI uri, String value) {
            this.header = header;
            this.uri = uri;
            this.value = value;
        }

        public URI getURI() {
            return this.uri;
        }

        public void apply(Request request) {
            request.header(this.header, this.value);
        }

        public String toString() {
            return String.format("Basic authentication result for %s", this.uri);
        }
    }

    private JobSettings jobSettings;

    private final String baseUrl;

    public RobonectClient(HttpClient httpClient, RobonectEndpoint endpoint) {
        this.httpClient = httpClient;
        this.baseUrl = "http://" + endpoint.getIpAddress() + "/json";
        this.parser = new ModelParser();
        this.jobSettings = new JobSettings();
        if (endpoint.isUseAuthentication()) {
            addPreemptiveAuthentication(httpClient, endpoint);
        }
    }

    private void addPreemptiveAuthentication(HttpClient httpClient, RobonectEndpoint endpoint) {
        AuthenticationStore auth = httpClient.getAuthenticationStore();
        URI uri = URI.create(baseUrl);
        auth.addAuthenticationResult(new BasicResult(HttpHeader.AUTHORIZATION, uri, "Basic " + B64Code
                .encode(endpoint.getUser() + ":" + endpoint.getPassword(), StandardCharsets.ISO_8859_1)));
    }

    public MowerInfo getMowerInfo() throws InterruptedException {
        String responseString = sendCommand(new StatusCommand());
        return parser.parse(responseString, MowerInfo.class);
    }

    public RobonectAnswer start() throws InterruptedException {
        String responseString = sendCommand(new StartCommand());
        return parser.parse(responseString, RobonectAnswer.class);
    }

    public RobonectAnswer stop() throws InterruptedException {
        String responseString = sendCommand(new StopCommand());
        return parser.parse(responseString, RobonectAnswer.class);
    }

    public RobonectAnswer resetErrors() throws InterruptedException {
        String responseString = sendCommand(new ErrorCommand().withReset(true));
        return parser.parse(responseString, RobonectAnswer.class);
    }

    public ErrorList errorList() throws InterruptedException {
        String responseString = sendCommand(new ErrorCommand());
        return parser.parse(responseString, ErrorList.class);
    }

    public RobonectAnswer setMode(ModeCommand.Mode mode) throws InterruptedException {
        String responseString = sendCommand(createCommand(mode));
        return parser.parse(responseString, RobonectAnswer.class);
    }

    private ModeCommand createCommand(ModeCommand.Mode mode) {
        if (mode != ModeCommand.Mode.JOB) {
            return new ModeCommand(mode);
        } else {
            return new ModeCommand(mode).withRemoteStart(jobSettings.remoteStart).withAfter(jobSettings.after)
                    .withStart(jobSettings.start).withEnd(jobSettings.end);
        }
    }

    public Name getName() throws InterruptedException {
        String responseString = sendCommand(new NameCommand());
        return parser.parse(responseString, Name.class);
    }

    public Name setName(String name) throws InterruptedException {
        String responseString = sendCommand(new NameCommand().withNewName(name));
        return parser.parse(responseString, Name.class);
    }

    private String sendCommand(Command command) throws InterruptedException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("send HTTP GET to: {} ", command.toCommandURL(baseUrl));
            }
            String responseString = httpClient.newRequest(command.toCommandURL(baseUrl)).method(HttpMethod.GET)
                    .timeout(30000, TimeUnit.MILLISECONDS).send().getContentAsString();
            if (logger.isDebugEnabled()) {
                logger.debug("Response body was: {} ", responseString);
            }
            return responseString;
        } catch (ExecutionException | TimeoutException e) {
            throw new RobonectCommunicationException("Could not send command " + command.toCommandURL(baseUrl), e);
        }
    }

    public VersionInfo getVersionInfo() throws InterruptedException {
        String versionResponse = sendCommand(new VersionCommand());
        return parser.parse(versionResponse, VersionInfo.class);
    }

    public JobSettings getJobSettings() {
        return jobSettings;
    }
}
