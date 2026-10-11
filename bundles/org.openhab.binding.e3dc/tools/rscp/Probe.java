/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.e3dc.internal.rscp.RscpClient;
import org.openhab.binding.e3dc.internal.rscp.RscpError;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * RSCP capability probe.
 *
 * <p>
 * Connects to an E3/DC device, authenticates and then requests a list of tags one at a time, printing
 * the decoded response or the error code the device answered with. This determines which data points
 * a concrete device implements instead of assuming them, which is how model specific capabilities are
 * established.
 *
 * <p>
 * The request tags are derived from the extracted catalogue rather than written out by hand: every
 * {@code *_REQ_*} tag of a namespace the binding cares about is swept. A hand written list silently
 * rots as the catalogue grows, and tags that do not exist would be reported as unsupported and
 * mistaken for a device that lacks them.
 *
 * <p>
 * The probe uses the binding's own wire implementation, so a successful run against hardware also
 * confirms the encryption: a wrong cipher fails on the very first response.
 *
 * <p>
 * Usage: {@code Probe <host> <rscpPassword> <user> <password> [tagFile] [port]}
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class Probe {

    /** Namespaces swept for request tags. */
    private static final List<String> NAMESPACES = List.of("RSCP", "INFO", "EMS", "PVI", "BAT", "PM", "WB", "DB",
            "HA", "DCDC", "SGR", "SE");

    /**
     * Tags that are queried without a container. These are the data points the binding itself reads, so
     * a probe run also validates the channel model against the device.
     */
    private static final List<String> DIRECT_TAGS = List.of(//
            "INFO_SW_RELEASE", "INFO_SERIAL_NUMBER", "INFO_PRODUCTION_DATE", "INFO_TIME_ZONE", //
            "EMS_POWER_PV", "EMS_POWER_BAT", "EMS_POWER_HOME", "EMS_POWER_GRID", "EMS_POWER_ADD", //
            "EMS_BAT_SOC", "EMS_INSTALLED_PEAK_POWER", "EMS_BALANCED_PHASES", "EMS_EMERGENCY_POWER_STATUS", //
            "EMS_MODE", "EMS_COUPLING_MODE", "EMS_REMAINING_BAT_CHARGE_POWER", "EMS_REMAINING_BAT_DISCHARGE_POWER", //
            "PVI_AC_POWER", "PVI_AC_VOLTAGE", "PVI_AC_CURRENT", "PVI_AC_FREQUENCY", "PVI_DC_POWER", "PVI_DC_VOLTAGE",
            "PVI_DC_CURRENT", "PVI_TEMPERATURE", "PVI_ON_GRID", "PVI_USED_STRING_COUNT", "PVI_AC_ENERGY_ALL",
            "PVI_AC_ENERGY_DAY", "PVI_INVERTER_COUNT", //
            "WB_EXTERN_DATA", "BAT_AVAILABLE_BATTERIES", //
            "DB_AUTARKY", "DB_CONSUMED_PRODUCTION", "DB_BAT_CHARGE_LEVEL", "DB_DC_POWER", "DB_CONSUMPTION",
            "DB_GRID_POWER_IN", "DB_GRID_POWER_OUT", "DB_BAT_POWER_IN", "DB_BAT_POWER_OUT");

    /** Containers whose nested values are requested as well. */
    private static final List<String> CONTAINER_TAGS = List.of(//
            "EMS_REQ_GET_POWER_SETTINGS", "EMS_REQ_GET_IDLE_PERIODS", "EMS_REQ_GET_MANUAL_CHARGE", //
            "PVI_REQ_DATA", "BAT_REQ_DATA", "PM_REQ_DATA", "WB_REQ_DATA", "HA_REQ_ACTUATOR_STATES", //
            "DB_REQ_HISTORY_DATA_DAY");

    /**
     * Whether to sweep every request tag of the namespaces instead of only the curated list. A full
     * sweep is thorough but takes a long time, because each tag is a separate round trip.
     */
    private static final String FULL_SWEEP_ARGUMENT = "--full";

    public static void main(String[] args) throws Exception {
        List<String> arguments = new ArrayList<>(List.of(args));
        boolean fullSweep = arguments.remove(FULL_SWEEP_ARGUMENT);
        args = arguments.toArray(new String[0]);

        if (args.length < 4) {
            System.err.println("usage: Probe <host> <rscpPassword> <user> <password> [tagFile] [port] ["
                    + FULL_SWEEP_ARGUMENT + "]");
            System.err.println("  without " + FULL_SWEEP_ARGUMENT + " only the data points the binding uses are probed");
            System.exit(2);
        }
        String host = args[0];
        String rscpPassword = args[1];
        String user = args[2];
        String password = args[3];
        Path tagFile = Path.of(args.length > 4 ? args[4] : "tools/rscp/rscp_tags.tsv");
        int port = args.length > 5 ? Integer.parseInt(args[5]) : RscpClient.DEFAULT_PORT;

        loadTagIndex(tagFile);

        List<String> requests = new ArrayList<>(CONTAINER_TAGS);
        requests.addAll(DIRECT_TAGS);
        if (fullSweep) {
            requests.addAll(sweepRequestTags());
        }

        Map<String, String> summary = new LinkedHashMap<>();
        System.out.println("probing " + host + ":" + port + " with " + requests.size() + " tags"
                + (fullSweep ? " (full sweep)" : ""));

        RscpClient client = new RscpClient(host, port, user, password, rscpPassword, 10000);
        try {
            client.connect();
            System.out.println("authentication succeeded, the encryption matches the device");
            System.out.println();

            for (String name : requests) {
                int tag = tagFor(name);
                if (tag == 0) {
                    summary.put(name, "NOT_IN_CATALOGUE");
                    continue;
                }
                try {
                    RscpValue response = client.requestTag(tag);
                    System.out.println("=== " + name + String.format(" (0x%08X)", tag) + " ===");
                    print(response, "    ");
                    System.out.println();
                    summary.put(name, classify(response));
                } catch (RscpException e) {
                    // a lost connection must not end the run, otherwise a long sweep never completes
                    System.out.println("=== " + name + " === reconnecting after: " + e.getMessage());
                    summary.put(name, "IO_ERROR(" + e.getMessage() + ")");
                    client.close();
                    try {
                        client.connect();
                    } catch (RscpException reconnect) {
                        System.out.println("reconnect failed: " + reconnect.getMessage() + ", stopping");
                        break;
                    }
                }
            }
            Files.write(Path.of("probe_summary.tsv"),
                    summary.entrySet().stream().map(e -> e.getKey() + "\t" + e.getValue()).toList());
        } finally {
            client.close();
        }

        report(summary);
    }

    /**
     * Every request tag of the namespaces the binding cares about, taken from the catalogue. This can
     * only contain tags that exist, which is the point.
     */
    private static List<String> sweepRequestTags() {
        List<String> tags = new ArrayList<>();
        for (String name : TAG_INDEX.keySet()) {
            if (!name.contains("_REQ_")) {
                continue;
            }
            for (String namespace : NAMESPACES) {
                if (name.startsWith(namespace + "_REQ_")) {
                    tags.add(name);
                    break;
                }
            }
        }
        tags.sort(String::compareTo);
        return tags;
    }

    private static void report(Map<String, String> summary) {
        int ok = 0;
        int errors = 0;
        System.out.println();
        System.out.println("########## CAPABILITY SUMMARY ##########");
        for (Map.Entry<String, String> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
            if (entry.getValue().startsWith("OK")) {
                ok++;
            } else if (entry.getValue().startsWith("ERROR")) {
                errors++;
            }
        }
        System.out.println();
        System.out.printf("%d tags: %d answered, %d rejected, %d other%n", summary.size(), ok, errors,
                summary.size() - ok - errors);
        System.out.println("summary written to probe_summary.tsv");
    }

    /**
     * Resolves a request tag name through the index. A name without a request form falls back to the
     * plain tag, which is what a device answers a data point with.
     */
    private static int tagFor(String name) {
        String bare = name.startsWith("TAG_") ? name.substring(4) : name;
        Integer tag = TAG_INDEX.get(bare);
        if (tag != null) {
            return tag;
        }
        int underscore = bare.indexOf('_');
        if (underscore > 0 && !bare.contains("_REQ_")) {
            Integer request = TAG_INDEX.get(bare.substring(0, underscore) + "_REQ_" + bare.substring(underscore + 1));
            if (request != null) {
                return request;
            }
        }
        return 0;
    }

    /** Tag name to id, loaded from the extracted catalogue so the probe stays in step with it. */
    private static final Map<String, Integer> TAG_INDEX = new LinkedHashMap<>();

    private static void loadTagIndex(Path tagFile) throws Exception {
        for (String line : Files.readAllLines(tagFile)) {
            String[] parts = line.split("\t");
            if (parts.length != 2 || !parts[0].startsWith("TAG_")) {
                continue; // the file starts with a header row and may hold blank lines
            }
            String hex = parts[1].trim();
            if (hex.startsWith("0x") || hex.startsWith("0X")) {
                hex = hex.substring(2);
            }
            TAG_INDEX.put(parts[0].substring(4), (int) Long.parseLong(hex, 16));
        }
        System.out.println("loaded " + TAG_INDEX.size() + " tag names from " + tagFile);
    }

    private static void print(RscpValue value, String indent) {
        System.out.println(indent + value);
        for (RscpValue child : value.getChildren()) {
            print(child, indent + "    ");
        }
    }

    private static String classify(RscpValue response) {
        RscpError error = response.findError();
        if (error != null && !"UNKNOWN".equals(error.name())) {
            return "ERROR(" + error + ")";
        }
        int values = countValues(response);
        return values == 0 ? "EMPTY" : "OK(" + values + " values)";
    }

    private static int countValues(RscpValue value) {
        int count = value.getValue() != null ? 1 : 0;
        for (RscpValue child : value.getChildren()) {
            count += countValues(child);
        }
        return count;
    }

    private Probe() {
    }
}
