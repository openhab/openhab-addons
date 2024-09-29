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
package org.openhab.binding.pulseaudio.internal;

import static org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants.MODULE_SIMPLE_PROTOCOL_TCP_NAME;
import static org.openhab.binding.pulseaudio.internal.cli.Parser.extractArgumentFromLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.cli.Parser;
import org.openhab.binding.pulseaudio.internal.handler.DeviceIdentifier;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig.State;
import org.openhab.binding.pulseaudio.internal.items.Module;
import org.openhab.binding.pulseaudio.internal.items.SimpleProtocolTCPModule;
import org.openhab.binding.pulseaudio.internal.items.Sink;
import org.openhab.binding.pulseaudio.internal.items.SinkInput;
import org.openhab.binding.pulseaudio.internal.items.Source;
import org.openhab.binding.pulseaudio.internal.items.SourceOutput;
import org.openhab.core.audio.AudioFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client connects to a pulseaudio server via TCP. It reads the current state of the
 * pulseaudio server (available sinks, sources,...) and can send commands to the server.
 * The syntax of the commands is the same as for the pactl command line tool provided by pulseaudio.
 *
 * On the pulseaudio server the module-cli-protocol-tcp has to be loaded.
 *
 * @author Tobias Bräutigam - Initial contribution
 * @author Miguel Álvarez - changes for loading audio source module and nullability annotations
 */
@NonNullByDefault
public class PulseaudioClient {

    private final Logger logger = LoggerFactory.getLogger(PulseaudioClient.class);

    private String host;
    private int port;
    private @Nullable Socket client;

    private List<AbstractAudioDeviceConfig> items;
    private List<Module> modules;

    /**
     * Corresponding to the global binding configuration
     */
    private PulseAudioBindingConfiguration configuration;

    /**
     * corresponding name to execute actions on sink items
     */
    private static final String ITEM_SINK = "sink";

    /**
     * corresponding name to execute actions on source items
     */
    private static final String ITEM_SOURCE = "source";

    /**
     * corresponding name to execute actions on sink-input items
     */
    private static final String ITEM_SINK_INPUT = "sink-input";

    /**
     * corresponding name to execute actions on source-output items
     */
    private static final String ITEM_SOURCE_OUTPUT = "source-output";
    /**
     * command to list the loaded modules
     */
    private static final String CMD_LIST_MODULES = "list-modules";

    /**
     * command to list the sinks
     */
    private static final String CMD_LIST_SINKS = "list-sinks";

    /**
     * command to list the sources
     */
    private static final String CMD_LIST_SOURCES = "list-sources";

    /**
     * command to list the sink-inputs
     */
    private static final String CMD_LIST_SINK_INPUTS = "list-sink-inputs";

    /**
     * command to list the source-outputs
     */
    private static final String CMD_LIST_SOURCE_OUTPUTS = "list-source-outputs";

    /**
     * command to load a module
     */
    private static final String CMD_LOAD_MODULE = "load-module";

    /**
     * command to unload a module
     */
    private static final String CMD_UNLOAD_MODULE = "unload-module";

    /**
     * name of the module-combine-sink
     */
    private static final String MODULE_COMBINE_SINK = "module-combine-sink";

    public PulseaudioClient(String host, int port, PulseAudioBindingConfiguration configuration) {
        this.host = host;
        this.port = port;
        this.configuration = configuration;

        items = new ArrayList<>();
        modules = new ArrayList<>();
    }

    public boolean isConnected() {
        Socket clientSocket = client;
        return clientSocket != null ? clientSocket.isConnected() : false;
    }

    /**
     * updates the item states and their relationships
     */
    public synchronized void update() {
        // one-step copy
        modules = new ArrayList<>(Parser.parseModules(listModules()));

        List<AbstractAudioDeviceConfig> newItems = new ArrayList<>(); // prepare new list before assigning it
        if (configuration.sink) {
            logger.debug("reading sinks");
            newItems.addAll(Parser.parseSinks(listSinks(), this));
        }
        if (configuration.source) {
            logger.debug("reading sources");
            newItems.addAll(Parser.parseSources(listSources(), this));
        }
        if (configuration.sinkInput) {
            logger.debug("reading sink-inputs");
            newItems.addAll(Parser.parseSinkInputs(listSinkInputs(), this));
        }
        if (configuration.sourceOutput) {
            logger.debug("reading source-outputs");
            newItems.addAll(Parser.parseSourceOutputs(listSourceOutputs(), this));
        }
        logger.debug("Pulseaudio server {}: {} modules and {} items updated", host, modules.size(), newItems.size());
        items = newItems;
    }

    private String listModules() {
        return this.sendRawRequest(CMD_LIST_MODULES);
    }

    private String listSinks() {
        return this.sendRawRequest(CMD_LIST_SINKS);
    }

    private String listSources() {
        return this.sendRawRequest(CMD_LIST_SOURCES);
    }

    private String listSinkInputs() {
        return this.sendRawRequest(CMD_LIST_SINK_INPUTS);
    }

    private String listSourceOutputs() {
        return this.sendRawRequest(CMD_LIST_SOURCE_OUTPUTS);
    }

    /**
     * retrieves a module by its id
     *
     * @param id
     * @return the corresponding {@link Module} to the given <code>id</code>
     */
    public synchronized @Nullable Module getModule(int id) {
        for (Module module : modules) {
            if (module.getId() == id) {
                return module;
            }
        }
        return null;
    }

    /**
     * Retrieves a list of {@link SimpleProtocolTCPModule} for the provided item in the provided port range.
     *
     * @param item Sink/Source item.
     * @param minPort min port to include.
     * @param maxPort max port to include.
     * @return a list of {@link SimpleProtocolTCPModule} instances
     */
    public List<SimpleProtocolTCPModule> getSimpleProtocolTCPModulesByDevice(AbstractAudioDeviceConfig item,
            int minPort, int maxPort) {
        String itemType = getItemCommandName(item);
        if (itemType == null) {
            return List.of();
        }
        return filterSimpleProtocolTCPModules((spModule) -> spModule.getPort() >= minPort && //
                spModule.getPort() <= maxPort && //
                extractArgumentFromLine(itemType, spModule.getArgument()) // extract sick|source
                        .map(name -> name.equals(item.getPaName())).orElse(false))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a filtered stream of {@link SimpleProtocolTCPModule}
     * 
     * @param predicate a filter to apply
     * @return a stream of {@link SimpleProtocolTCPModule} filtered by provided predicate
     */
    private Stream<SimpleProtocolTCPModule> filterSimpleProtocolTCPModules(
            Predicate<SimpleProtocolTCPModule> predicate) {
        List<Module> modulesCopy = new ArrayList<>(modules);
        return modulesCopy.stream() //
                .filter(SimpleProtocolTCPModule.class::isInstance) //
                .map(SimpleProtocolTCPModule.class::cast) //
                .filter(predicate);
    }

    /**
     * send the command directly to the pulseaudio server
     * for a list of available commands please take a look at
     * http://www.freedesktop.org/wiki/Software/PulseAudio/Documentation/User/CLI
     *
     * @param command
     */
    public void sendCommand(String command) {
        sendRawCommand(command);
    }

    /**
     * retrieves a {@link Sink} by its name
     *
     * @return the corresponding {@link Sink} to the given <code>name</code>
     */
    public @Nullable Sink getSink(String name) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getPaName().equalsIgnoreCase(name) && item instanceof Sink sink) {
                return sink;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link Sink} by its id
     *
     * @return the corresponding {@link Sink} to the given <code>id</code>
     */
    public @Nullable Sink getSink(int id) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getId() == id && item instanceof Sink sink) {
                return sink;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link Source} by its id
     *
     * @return the corresponding {@link Source} to the given <code>id</code>
     */
    public @Nullable Source getSource(int id) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getId() == id && item instanceof Source source) {
                return source;
            }
        }
        return null;
    }

    /**
     * retrieves an {@link AbstractAudioDeviceConfig} by its identifier
     * If several devices correspond to the deviceIdentifier, returns the first one (aphabetical order)
     *
     * @param deviceIdentifier The device identifier to match against
     * @return the corresponding {@link AbstractAudioDeviceConfig} to the given <code>name</code>
     */
    public @Nullable AbstractAudioDeviceConfig getGenericAudioItem(DeviceIdentifier deviceIdentifier) {
        List<AbstractAudioDeviceConfig> matchingDevices = items.stream()
                .filter(device -> device.matches(deviceIdentifier))
                .sorted(Comparator.comparing(AbstractAudioDeviceConfig::getPaName)).collect(Collectors.toList());
        if (matchingDevices.size() == 1) {
            return matchingDevices.get(0);
        } else if (matchingDevices.size() > 1) {
            logger.debug(
                    "Cannot select exactly one audio device, so choosing the first. To choose without ambiguity between the {} devices matching the identifier {}, you can maybe use a more restrictive 'additionalFilter' parameter",
                    matchingDevices.size(), deviceIdentifier.getNameOrDescription());
            return matchingDevices.get(0);
        }
        return null;
    }

    /**
     * Get all items previously parsed from the pulseaudio server.
     *
     * @return All items parsed from the pulseaudio server
     */
    public List<AbstractAudioDeviceConfig> getItems() {
        return items;
    }

    /**
     * changes the <code>mute</code> state of the corresponding {@link Sink}
     *
     * @param item the {@link Sink} to handle
     * @param mute mutes the sink if true, unmutes if false
     */
    public void setMute(@Nullable AbstractAudioDeviceConfig item, boolean mute) {
        if (item == null) {
            return;
        }
        String itemCommandName = getItemCommandName(item);
        if (itemCommandName == null) {
            return;
        }
        String muteString = mute ? "1" : "0";
        sendRawCommand("set-" + itemCommandName + "-mute " + item.getId() + " " + muteString);
        // update internal data
        item.setMuted(mute);
    }

    /**
     * change the volume of an {@link AbstractAudioDeviceConfig}
     *
     * @param item the {@link AbstractAudioDeviceConfig} to handle
     * @param vol the new volume value the {@link AbstractAudioDeviceConfig} should be changed to (possible values from
     *            0 - 65536)
     */
    public void setVolume(AbstractAudioDeviceConfig item, int vol) {
        String itemCommandName = getItemCommandName(item);
        if (itemCommandName == null) {
            return;
        }
        sendRawCommand("set-" + itemCommandName + "-volume " + item.getId() + " " + vol);
        item.setVolume(Math.round(100f / 65536f * vol));
    }

    /**
     * Creates a new Simple Protocol TCP module instance on the server or return the provided one if still available.
     * The module loading (if needed) will be tried several times, on a new random port each time.
     *
     * @param item the sink we are searching for
     * @return the module representation
     * @throws InterruptedException
     */
    public Optional<SimpleProtocolTCPModule> loadModuleSimpleProtocolTcpIfNeeded(AbstractAudioDeviceConfig item,
            AudioFormat format, int minPort, int maxPort, @Nullable SimpleProtocolTCPModule spModule)
            throws InterruptedException {
        int currentTry = 0;
        String paFormat = getPAFormatString(format);
        long rate = Objects.requireNonNull(format.getFrequency());
        int channels = Objects.requireNonNull(format.getChannels());
        if (spModule != null) {
            // check if cached module is still available, it should be
            var module = findSimpleProtocolTcpModule(item, spModule.getId(), spModule.getPort(), paFormat, rate,
                    channels);
            if (module.isPresent()) {
                logger.debug("reusing simple protocol tcp module {}", module.get().getId());
                return module;
            }
            logger.warn("previous module instance not found or incompatible, creating a new one");
        }
        String itemType = getItemCommandName(item);
        do {
            int simpleTcpPortToTry = new Random().nextInt(minPort, maxPort + 1);
            if (filterSimpleProtocolTCPModules(m -> m.getPort() == simpleTcpPortToTry).findAny().isPresent()) {
                currentTry++;
                logger.warn("port {} already in use in the server, retrying random port generation",
                        simpleTcpPortToTry);
                continue;
            }
            logger.debug("trying to load simple protocol tcp module at port {}", simpleTcpPortToTry);
            String moduleOptions = String.format(" %s=%s port=%d format=%s rate=%d channels=%d", itemType,
                    item.getPaName(), simpleTcpPortToTry, paFormat, rate, channels);
            if (item instanceof Source) {
                moduleOptions = moduleOptions + " record=true";
            }
            sendRawCommand("load-module " + MODULE_SIMPLE_PROTOCOL_TCP_NAME + moduleOptions);
            try {
                do {
                    Thread.sleep(100);
                    update();
                    Optional<SimpleProtocolTCPModule> simpleProtocolModule = findSimpleProtocolTcpModule(item, null,
                            simpleTcpPortToTry, paFormat, rate, channels);
                    if (simpleProtocolModule.isPresent()) {
                        return simpleProtocolModule;
                    }
                    currentTry++;
                } while (currentTry < 3);
            } catch (NumberFormatException e) {
                logger.warn("simple protocol module load failed");
            }
            currentTry++;
        } while (currentTry < 3);

        logger.warn("""
                The pulseaudio binding tried 3 times to load the module-simple-protocol-tcp\
                 on random port on the pulseaudio server and give up trying\
                """);
        return Optional.empty();
    }

    public String getPAFormatString(AudioFormat format) {
        assert AudioFormat.CODEC_PCM_SIGNED.equals(format.getCodec());
        switch (Objects.requireNonNull(format.getBitDepth())) {
            case 16:
                return "s16" + (Objects.requireNonNull(format.isBigEndian()) ? "be" : "le");
            case 24:
                return "s24" + (Objects.requireNonNull(format.isBigEndian()) ? "be" : "le");
            case 32:
                return "s32" + (Objects.requireNonNull(format.isBigEndian()) ? "be" : "le");
            default:
                throw new IllegalArgumentException("Unsupported format : " + format.getBitDepth());
        }
    }

    /**
     * Find a simple protocol module corresponding to the given sink in argument
     * and returns the port it listens to
     *
     * @param item
     * @return
     */
    private Optional<SimpleProtocolTCPModule> findSimpleProtocolTcpModule(AbstractAudioDeviceConfig item,
            @Nullable Integer id, @Nullable Integer port, @Nullable String format, @Nullable Long rate,
            @Nullable Integer channels) {
        String itemType = getItemCommandName(item);
        if (itemType == null) {
            return Optional.empty();
        }
        boolean isSource = item instanceof Source;
        return filterSimpleProtocolTCPModules(spModule -> {
            if (id != null && spModule.getId() != id) {
                return false;
            }
            if (port != null && spModule.getPort() != port) {
                return false;
            }
            boolean nameMatch = extractArgumentFromLine(itemType, spModule.getArgument()) // extract sick|source
                    .map(name -> name.equals(item.getPaName())).orElse(false);
            if (nameMatch) {
                if (isSource) {
                    boolean recordStream = extractArgumentFromLine("record", spModule.getArgument()).map("true"::equals)
                            .orElse(false);
                    if (!recordStream) {
                        return false;
                    }
                }
                if (format != null) {
                    boolean rateMatch = extractArgumentFromLine("format", spModule.getArgument()).map(format::equals)
                            .orElse(false);
                    if (!rateMatch) {
                        return false;
                    }
                }
                if (rate != null) {
                    boolean rateMatch = extractArgumentFromLine("rate", spModule.getArgument())
                            .map(value -> Long.parseLong(value) == rate).orElse(false);
                    if (!rateMatch) {
                        return false;
                    }
                }
                if (channels != null) {
                    boolean channelsMatch = extractArgumentFromLine("channels", spModule.getArgument())
                            .map(value -> Integer.parseInt(value) == channels.intValue()).orElse(false);
                    if (!channelsMatch) {
                        return false;
                    }
                }
            }
            return nameMatch;
        }).findAny();
    }

    public void unloadModule(Module module) {
        sendCommand("unload-module " + module.getId());
    }

    /**
     * returns the item names that can be used in commands
     *
     * @param item
     * @return
     */
    private @Nullable String getItemCommandName(AbstractAudioDeviceConfig item) {
        if (item instanceof Sink) {
            return ITEM_SINK;
        } else if (item instanceof Source) {
            return ITEM_SOURCE;
        } else if (item instanceof SinkInput) {
            return ITEM_SINK_INPUT;
        } else if (item instanceof SourceOutput) {
            return ITEM_SOURCE_OUTPUT;
        }
        return null;
    }

    /**
     * change the volume of an {@link AbstractAudioDeviceConfig}
     *
     * @param item the {@link AbstractAudioDeviceConfig} to handle
     * @param vol the new volume percent value the {@link AbstractAudioDeviceConfig} should be changed to (possible
     *            values from 0 - 100)
     */
    public void setVolumePercent(AbstractAudioDeviceConfig item, int vol) {
        int volumeToSet = vol;
        if (volumeToSet <= 100) {
            volumeToSet = toAbsoluteVolume(volumeToSet);
        }
        setVolume(item, volumeToSet);
    }

    /**
     * transform a percent volume to a value that can be send to the pulseaudio server (0-65536)
     *
     * @param percent
     * @return
     */
    private int toAbsoluteVolume(int percent) {
        return (int) Math.round(65536f / 100f * Double.valueOf(percent));
    }

    /**
     * changes the combined sinks slaves to the given <code>sinks</code>
     *
     * @param combinedSink the combined sink which slaves should be changed
     * @param sinks the list of new slaves
     */
    public void setCombinedSinkSlaves(@Nullable Sink combinedSink, List<Sink> sinks) {
        if (combinedSink == null || !combinedSink.isCombinedSink()) {
            return;
        }
        List<String> slaves = new ArrayList<>();
        for (Sink sink : sinks) {
            slaves.add(sink.getPaName());
        }
        // 1. delete old combined-sink
        Module lastModule = combinedSink.getModule();
        if (lastModule != null) {
            sendRawCommand(CMD_UNLOAD_MODULE + " " + lastModule.getId());
        }
        // 2. add new combined-sink with same name and all slaves
        sendRawCommand(CMD_LOAD_MODULE + " " + MODULE_COMBINE_SINK + " sink_name=" + combinedSink.getPaName()
                + " slaves=" + String.join(",", slaves));
        // 3. update internal data structure because the combined sink has a new number + other slaves
        update();
    }

    /**
     * sets the sink a sink-input should be routed to
     *
     * @param sinkInput the sink-input to be rerouted
     * @param sink the new sink the sink-input should be routed to
     */
    public void moveSinkInput(@Nullable SinkInput sinkInput, @Nullable Sink sink) {
        if (sinkInput == null || sink == null) {
            return;
        }
        sendRawCommand("move-sink-input " + sinkInput.getId() + " " + sink.getId());
        sinkInput.setSink(sink);
    }

    /**
     * sets the sink a source-output should be routed to
     *
     * @param sourceOutput the source-output to be rerouted
     * @param source the new source the source-output should be routed to
     */
    public void moveSourceOutput(@Nullable SourceOutput sourceOutput, @Nullable Source source) {
        if (sourceOutput == null || source == null) {
            return;
        }
        sendRawCommand("move-sink-input " + sourceOutput.getId() + " " + source.getId());
        sourceOutput.setSource(source);
    }

    /**
     * suspend a source
     *
     * @param source the source which state should be changed
     * @param suspend suspend it or not
     */
    public void suspendSource(@Nullable Source source, boolean suspend) {
        if (source == null) {
            return;
        }
        if (suspend) {
            sendRawCommand("suspend-source " + source.getId() + " 1");
            source.setState(State.SUSPENDED);
        } else {
            sendRawCommand("suspend-source " + source.getId() + " 0");
            // unsuspending the source could result in different states (RUNNING,IDLE,...)
            // update to get the new state
            update();
        }
    }

    /**
     * suspend a sink
     *
     * @param sink the sink which state should be changed
     * @param suspend suspend it or not
     */
    public void suspendSink(@Nullable Sink sink, boolean suspend) {
        if (sink == null) {
            return;
        }
        if (suspend) {
            sendRawCommand("suspend-sink " + sink.getId() + " 1");
            sink.setState(State.SUSPENDED);
        } else {
            sendRawCommand("suspend-sink " + sink.getId() + " 0");
            // unsuspending the sink could result in different states (RUNNING,IDLE,...)
            // update to get the new state
            update();
        }
    }

    /**
     * changes the combined sinks slaves to the given <code>sinks</code>
     *
     * @param combinedSinkName the combined sink which slaves should be changed
     * @param sinks the list of new slaves
     */
    public void setCombinedSinkSlaves(String combinedSinkName, List<Sink> sinks) {
        if (getSink(combinedSinkName) != null) {
            return;
        }
        List<String> slaves = new ArrayList<>();
        for (Sink sink : sinks) {
            slaves.add(sink.getPaName());
        }
        // add new combined-sink with same name and all slaves
        sendRawCommand(CMD_LOAD_MODULE + " " + MODULE_COMBINE_SINK + " sink_name=" + combinedSinkName + " slaves="
                + String.join(",", slaves));
        // update internal data structure because the combined sink is new
        update();
    }

    private synchronized void sendRawCommand(String command) {
        checkConnection();
        Socket clientSocket = client;
        if (clientSocket != null && clientSocket.isConnected()) {
            try {
                PrintStream out = new PrintStream(clientSocket.getOutputStream(), true);
                logger.trace("sending command {} to pa-server {}", command, host);
                out.print(command + "\r\n");
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
    }

    private String sendRawRequest(String command) {
        logger.trace("_sendRawRequest({})", command);
        checkConnection();
        String result = "";
        Socket clientSocket = client;
        if (clientSocket != null && clientSocket.isConnected()) {
            try {
                PrintStream out = new PrintStream(clientSocket.getOutputStream(), true);
                out.print(command + "\r\n");

                InputStream instr = clientSocket.getInputStream();

                try {
                    byte[] buff = new byte[1024];
                    int retRead = 0;
                    int lc = 0;
                    do {
                        retRead = instr.read(buff);
                        lc++;
                        if (retRead > 0) {
                            String line = new String(buff, 0, retRead);
                            if (line.endsWith(">>> ") && lc > 1) {
                                result += line.substring(0, line.length() - 4);
                                break;
                            }
                            result += line.trim();
                        }
                    } while (retRead > 0);
                } catch (SocketTimeoutException e) {
                    // Timeout -> as newer PA versions (>=5.0) do not send the >>> we have no chance
                    // to detect the end of the answer, except by this timeout
                } catch (SocketException e) {
                    logger.warn("Socket exception while sending pulseaudio command: {}", e.getMessage());
                } catch (IOException e) {
                    logger.warn("Exception while reading socket: {}", e.getMessage());
                }
                instr.close();
                out.close();
                clientSocket.close();
                return result;
            } catch (IOException e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
        return result;
    }

    private void checkConnection() {
        try {
            connect();
        } catch (IOException e) {
            logger.debug("{}", e.getMessage(), e);
        }
    }

    /**
     * Connects to the pulseaudio server (timeout 500ms)
     */
    public void connect() throws IOException {
        Socket clientSocket = client;
        if (clientSocket == null || clientSocket.isClosed() || !clientSocket.isConnected()) {
            logger.trace("Try to connect...");
            try {
                var clientFinal = new Socket(host, port);
                clientFinal.setSoTimeout(500);
                client = clientFinal;
                logger.trace("connected");
            } catch (UnknownHostException e) {
                client = null;
                throw new IOException("Unknown host", e);
            } catch (IllegalArgumentException e) {
                client = null;
                throw new IOException("Invalid port", e);
            } catch (SecurityException | SocketException e) {
                client = null;
                throw new IOException(
                        String.format("Cannot connect socket: %s", e.getMessage() != null ? e.getMessage() : ""), e);
            } catch (IOException e) {
                client = null;
                throw e;
            }
        }
    }

    /**
     * Disconnects from the pulseaudio server
     */
    public void disconnect() {
        Socket clientSocket = client;
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.debug("{}", e.getMessage(), e);
            }
        }
    }
}
