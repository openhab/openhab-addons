/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.presence.internal.ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.presence.internal.BaseHandler;
import org.openhab.binding.presence.internal.binding.PresenceBindingConfiguration;
import org.openhab.binding.presence.internal.binding.PresenceBindingConstants;
import org.openhab.binding.presence.internal.dhcp.DHCPListener;
import org.openhab.binding.presence.internal.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
public class PingHandler extends BaseHandler {

    private final Logger logger = LoggerFactory.getLogger(PingHandler.class);

    private @NonNullByDefault({}) PingConfiguration config;

    private @NonNullByDefault({}) String dottedAddress;

    /*
     * Don't do too much here other than some basic static initialization. Things get reused when their config changes
     */
    public PingHandler(Thing thing, PresenceBindingConfiguration bindingConfiguration) {
        super(thing, 2, bindingConfiguration);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void initialize() {
        config = getConfigAs(PingConfiguration.class);
        logger.debug("Start initializing {}", config.hostname);

        // Look up and cache our IP address
        try {
            InetAddress addr = InetAddress.getByName(config.hostname);
            dottedAddress = addr.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("Invalid hostname: {} (Won't resolve to an InetAddress)", config.hostname);
        }

        super.initialize();

        logger.debug("Updating status to online");
        updateStatus(ThingStatus.ONLINE);

        // If allowDHCPListen is true, register with the listener
        if (bindingConfiguration.allowDHCPListen) {
            DHCPListener.register(dottedAddress, () -> {
                logger.debug("Got a DHCP packet for {}", config.hostname);
                updateStateIfChanged(OnOffType.ON);
            });
        }

        logger.debug("Finished initializing");
    }

    @Override
    public void dispose() {
        super.dispose();

        // Unregister with DHCP listener. Its ok to call if it was not enabled
        DHCPListener.unregister(dottedAddress);
    }

    /*
     * This method creates a CompletableFuture that only collects the output of a process.
     * It is mostly used for debugging but the output of the process is also stored in one of
     * the properties of the Thing so the user can see it if desired.
     */
    private CompletableFuture<String> getOutputCollectingFuture(Process p, String name) {

        StringBuffer arPingOutout = new StringBuffer(
                System.lineSeparator() + name + " output:" + System.lineSeparator());
        return CompletableFuture.supplyAsync(() -> p).thenApplyAsync(process -> {
            try (BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br1.readLine()) != null) {
                    line = line.replaceAll("Timeout\\s*", "");
                    arPingOutout.append(line).append(System.lineSeparator());
                }
            } catch (IOException e1) {
            }
            return arPingOutout.toString();
        }, bindingConfiguration.executor);
    }

    /*
     * This is the main process for detecting the presence of a device via ping. It launches multiple
     * background Process objects, then waits for them all to complete, and finally reduces the results
     * down to a single ON/OFF status. The output of the processes are stored in the properties of the Thing
     * so the user can troubleshoot. If the underlying Future that this method is running under is
     * cancelled, then we will get an InterruptedException. If this occurs, we can forcibly destroy all
     * of the Processes we launched and clean up nicely. This only happens when the Thing is being disposed of.
     */
    @Override
    protected void getStatus() {
        try {
            logger.debug("starting detection process for {}", config.hostname);

            Map<String, Process> processes = new HashMap<>();

            Map<String, String> properties = editProperties();

            String preferredMethod = properties.get(PresenceBindingConstants.PROPERTY_PREFERRED_METHOD);

            // If we have no preferred method , or it is ping, then start a ping process
            if ("ping".equals(preferredMethod) || preferredMethod.length() == 0) {
                processes.put("ping", NetworkUtils.getPingProcess(bindingConfiguration.ipPingMethod,
                        bindingConfiguration.pingToolPath, config.hostname, (int) config.timeout));
            }

            // If we have no preferred method , or it is arping, then start arping processes
            if ("arping".equals(preferredMethod) || preferredMethod.length() == 0) {
                List<String> ifcs;
                // If we have a preferred interface, then just use that
                String preferredInterfaces = properties.get(PresenceBindingConstants.PROPERTY_PREFERRED_INTERFACE);
                if (preferredInterfaces.length() == 0) {
                    ifcs = NetworkUtils.getInterfaceNames();
                } else {
                    ifcs = Arrays.asList(preferredInterfaces.split(","));
                }

                for (String ifc : ifcs) {
                    processes.put("arping-" + ifc, NetworkUtils.getArpProcess(bindingConfiguration.arpPingUtilMethod,
                            bindingConfiguration.arpPingToolPath, ifc, config.hostname, (int) config.timeout));
                }
            }

            Future<Boolean> javaPingFuture = null;
            try {
                logger.debug("waiting for detection of {}", config.hostname);

                // Send the bonjour packet to the address
                NetworkUtils.wakeUpIOS(config.hostname);

                // Create an outputCollectingFuture for each process so we capture the output
                Map<String, CompletableFuture<String>> m = processes.entrySet().stream().collect(
                        Collectors.toMap(e -> e.getKey(), e -> getOutputCollectingFuture(e.getValue(), e.getKey())));

                // Create a java ping future if have no preferred method
                if (preferredMethod.length() == 0) {
                    try {
                        javaPingFuture = bindingConfiguration.executor.submit(() -> {
                            return NetworkUtils.performJavaPing(config.hostname, (int) config.timeout);
                        });
                    } catch (RejectedExecutionException e) {
                        logger.debug("Unable to execute java ping process");
                    }
                }

                // Wait for the processes to complete
                for (Process p : processes.values()) {
                    p.waitFor();
                }

                if (logger.isTraceEnabled()) {
                    // log the output of each process
                    m.entrySet().stream().forEach(e -> {
                        try {
                            logger.trace("{} {}", e.getKey(), e.getValue().get());
                        } catch (InterruptedException | ExecutionException e1) {
                        }
                    });
                }

                Set<String> newIfcs = new HashSet<String>();
                boolean f = javaPingFuture != null ? javaPingFuture.get() : false;

                logger.trace("javaPingOutput (if started): {}", f);
                logger.debug("detection complete for {}", config.hostname);

                // Check all processes for a zero exit code and save the interface if found via arping
                for (Entry<String, Process> e : processes.entrySet()) {
                    int i = e.getValue().exitValue();
                    if (!f && i == 0) {
                        f = true;
                    }
                    if (i == 0 && !e.getKey().equals("ping")) {
                        newIfcs.add(e.getKey().substring(e.getKey().indexOf('-') + 1));
                    }
                }

                // Update our state
                updateStateIfChanged(f ? OnOffType.ON : OnOffType.OFF);

                // Update our properties
                properties.put(PresenceBindingConstants.PROPERTY_PREFERRED_INTERFACE, String.join(",", newIfcs));

                if (f) {
                    if (newIfcs.size() > 0) {
                        properties.put(PresenceBindingConstants.PROPERTY_PREFERRED_METHOD, "arping");
                    } else {
                        properties.put(PresenceBindingConstants.PROPERTY_PREFERRED_METHOD, "ping");
                    }
                }

                properties.put(PresenceBindingConstants.PROPERTY_DHCP_STATE,
                        DHCPListener.instance != null ? "listening on port " + DHCPListener.port : "disabled");

                // Save the output of the processes
                properties.put(PresenceBindingConstants.PROPERTY_ARP_STATE,
                        m.entrySet().stream().filter(e -> e.getKey().startsWith("arping")).map(e -> {
                            try {
                                return e.getValue().get();
                            } catch (InterruptedException | ExecutionException e1) {
                                return "";
                            }
                        }).collect(Collectors.joining(System.lineSeparator())));
                properties.put(PresenceBindingConstants.PROPERTY_ICMP_STATE,
                        m.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase("ping")).findAny().map(e -> {
                            try {
                                return e.getValue().get();
                            } catch (InterruptedException | ExecutionException e1) {
                                return "";
                            }
                        }).orElse(""));

                // Clear the "preferred" properties if we're not seen so we check all the things next time
                if (!f) {
                    properties.put(PresenceBindingConstants.PROPERTY_PREFERRED_INTERFACE, "");
                    properties.put(PresenceBindingConstants.PROPERTY_PREFERRED_METHOD, "");
                }
                updateProperties(properties);

            } catch (InterruptedException e) {
                // If we get here, our over-arching Future was cancelled.
                // We need to go UNDEFined and gracefully clean up all of our background Processes
                logger.debug("waitFor was interrupted");
                if (javaPingFuture != null) {
                    javaPingFuture.cancel(true);
                }
                processes.values().forEach(p -> p.destroyForcibly());
            }

        } catch (Exception e1) {
            // This is just a general exception catch in case something went wrong while creating a Process
            // os limits and such
            logger.debug("starting process threw excpetion: ", e1);
            updateStateIfChanged(UnDefType.UNDEF);
        }
    }
}
