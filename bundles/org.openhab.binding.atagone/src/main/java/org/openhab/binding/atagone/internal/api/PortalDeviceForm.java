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
package org.openhab.binding.atagone.internal.api;

import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.atagone.internal.dto.DeviceConfigDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;

/**
 * The portal's device settings form, read as the browser would see it and changed field by field. The form also
 * holds the owner's address, so every field not being changed is sent back exactly as read.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
final class PortalDeviceForm {

    private static final Pattern FORM = Pattern.compile("<form[^>]*action=\"/Device/EditDevice\"[^>]*>(.*?)</form>",
            Pattern.DOTALL);
    private static final Pattern CONTROL = Pattern.compile("(<input\\b[^>]*>)|(<select\\b[^>]*>.*?</select>)",
            Pattern.DOTALL);
    private static final Pattern OPTION = Pattern.compile("<option\\b([^>]*)>", Pattern.DOTALL);
    private static final Set<String> FLAGS = Set.of("summer_eco_mode", "dhw_legion_enabled");
    // The form offers "automatic" pre-heat as 240, the device reports it as 1440 minutes.
    private static final int MAX_PREHEAT_AUTOMATIC = 1440;
    private static final String MAX_PREHEAT_AUTOMATIC_FORM_VALUE = "240";

    private final List<FormField> fields = new ArrayList<>();
    private final Map<String, List<String>> options = new HashMap<>();

    private record FormField(String name, String value) {
    }

    private PortalDeviceForm() {
    }

    static PortalDeviceForm parse(String html) throws AtagOneCommunicationException {
        Matcher form = FORM.matcher(html);
        if (!form.find()) {
            throw new AtagOneCommunicationException("Portal device form not found, page layout changed?");
        }
        PortalDeviceForm result = new PortalDeviceForm();
        Matcher control = CONTROL.matcher(form.group(1));
        while (control.find()) {
            if (control.group(1) != null) {
                result.addInput(control.group(1));
            } else {
                result.addSelect(control.group(2));
            }
        }
        if (result.value("deviceId") == null || result.value("__RequestVerificationToken") == null) {
            throw new AtagOneCommunicationException("Portal device form is incomplete");
        }
        return result;
    }

    private void addInput(String tag) {
        String name = attribute(tag, "name");
        String type = attribute(tag, "type");
        if (name == null) {
            return;
        }
        String kind = type == null ? "text" : type.toLowerCase(Locale.ROOT);
        if (Set.of("submit", "button", "image", "reset", "file").contains(kind)) {
            return;
        }
        if (("checkbox".equals(kind) || "radio".equals(kind)) && !tag.matches("(?s).*\\bchecked\\b.*")) {
            return;
        }
        String value = attribute(tag, "value");
        fields.add(new FormField(name, value == null ? ("checkbox".equals(kind) ? "on" : "") : value));
    }

    private void addSelect(String markup) {
        String name = attribute(markup, "name");
        if (name == null) {
            return;
        }
        List<String> values = new ArrayList<>();
        String selected = null;
        Matcher option = OPTION.matcher(markup);
        while (option.find()) {
            String value = attribute(option.group(1), "value");
            if (value != null) {
                values.add(value);
                if (selected == null && option.group(1).matches("(?s).*\\bselected\\b.*")) {
                    selected = value;
                }
            }
        }
        options.put(name, values);
        if (selected == null && !values.isEmpty()) {
            selected = values.get(0);
        }
        if (selected != null) {
            fields.add(new FormField(name, selected));
        }
    }

    private static @Nullable String attribute(String tag, String name) {
        // Browsers use the first of duplicate attributes; the address fields carry Name="Location.x" before name="x".
        Matcher m = Pattern.compile("(?<![\\w-])" + name + "=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE).matcher(tag);
        return m.find() ? unescape(m.group(1)) : null;
    }

    private static String unescape(String text) {
        return text.replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    @Nullable
    String value(String name) {
        for (FormField field : fields) {
            if (field.name().equals(name)) {
                return field.value();
            }
        }
        return null;
    }

    /** The fields as the browser posts them, in document order. */
    Fields toFields() {
        Fields result = new Fields();
        for (FormField field : fields) {
            result.add(field.name(), field.value());
        }
        result.add("X-Requested-With", "XMLHttpRequest");
        return result;
    }

    List<String> names() {
        List<String> names = new ArrayList<>();
        for (Fields.Field field : toFields()) {
            names.add(field.getName());
        }
        return names;
    }

    private void set(String name, String value) throws AtagOneCommunicationException {
        List<String> allowed = options.get(name);
        if (allowed != null && !allowed.contains(value)) {
            throw new AtagOneCommunicationException("Portal does not offer '" + value + "' for " + name);
        }
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).name().equals(name)) {
                fields.set(i, new FormField(name, value));
                return;
            }
        }
        throw new AtagOneCommunicationException("Portal form has no field " + name);
    }

    private void setFlag(String name, boolean on) throws AtagOneCommunicationException {
        int index = -1;
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).name().equals(name)) {
                index = i;
                break;
            }
        }
        if (index < 0 || !FLAGS.contains(name)) {
            throw new AtagOneCommunicationException("Portal form has no switch " + name);
        }
        fields.removeIf(field -> field.name().equals(name));
        if (on) {
            fields.add(index, new FormField(name, "true"));
            fields.add(index + 1, new FormField(name, "false"));
        } else {
            fields.add(index, new FormField(name, "false"));
        }
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String named(Map<Integer, String> names, int id, String what) throws AtagOneCommunicationException {
        String name = names.get(id);
        if (name == null) {
            throw new AtagOneCommunicationException("Unknown " + what + " " + id);
        }
        return name;
    }

    private static String wholeUnits(long seconds, long unit, String what) throws AtagOneCommunicationException {
        if (seconds < unit || seconds % unit != 0) {
            throw new AtagOneCommunicationException(
                    what + " must be a whole number of " + (unit == 86400 ? "days" : "hours"));
        }
        return Long.toString(seconds / unit);
    }

    /**
     * Applies what differs from the device's current configuration. Fields the form does not have are refused when
     * they would change.
     *
     * @return whether any form field changed
     */
    boolean apply(@Nullable Integer controlMode, DeviceConfigUpdateDTO update, @Nullable DeviceConfigDTO current)
            throws AtagOneCommunicationException {
        String before = snapshot();
        DeviceConfigDTO base = current == null ? new DeviceConfigDTO() : current;
        boolean known = current != null;

        refuseIfChanged("privacy_mode", update.privacy_mode, base.privacy_mode, known);

        if (controlMode != null) {
            set("control_mode", controlMode == CH_CONTROL_MODE_WEATHER ? "wdr" : "room_control");
        }
        Integer heatingType = update.ch_heating_type;
        if (heatingType != null && differs(heatingType, base.ch_heating_type, known)) {
            String name = named(HEATING_TYPE_NAMES, heatingType, "heating type").replace('-', '_');
            // The portal's own spelling of this option is "radiator_undefloor".
            set("ch_heating_type", "radiator_underfloor".equals(name) ? "radiator_undefloor" : name);
        }
        Integer insulation = update.ch_isolation;
        if (insulation != null && differs(insulation, base.ch_isolation, known)) {
            set("isolation_level", named(INSULATION_NAMES, insulation, "insulation"));
        }
        Integer size = update.ch_building_size;
        if (size != null && differs(size, base.ch_building_size, known)) {
            set("ch_building_size", named(BUILDING_SIZE_NAMES, size, "building size"));
        }
        Integer influence = update.wdr_temps_influence;
        if (influence != null && differs(influence, base.wdr_temps_influence, known)) {
            String name = named(WDR_TEMPERATURE_INFLUENCE_NAMES, influence, "WDR influence");
            set("wdr_temps_influence", "room-control".equals(name) ? "room" : name);
        }
        Double climateZone = update.climate_zone;
        if (climateZone != null && differs(climateZone, base.climate_zone, known)) {
            set("climate_zone", decimal(climateZone));
        }
        Double offset = update.wd_temp_offs;
        if (offset != null && differs(offset, base.wd_temp_offs, known)) {
            set("wdr_temps_offset", decimal(offset));
        }
        Integer summerEco = update.summer_eco_mode;
        if (summerEco != null && differs(summerEco, base.summer_eco_mode, known)) {
            setFlag("summer_eco_mode", summerEco != 0);
        }
        Double summerEcoTemp = update.summer_eco_temp;
        if (summerEcoTemp != null && differs(summerEcoTemp, base.summer_eco_temp, known)) {
            set("summer_eco_temp", decimal(summerEcoTemp));
        }
        Integer frost = update.frost_prot_enabled;
        if (frost != null && differs(frost, base.frost_prot_enabled, known)) {
            String name = named(FROST_PROTECTION_NAMES, frost, "frost protection");
            set("frost_protection", "inside".equals(name) ? "room" : "both".equals(name) ? "outside, room" : name);
        }
        Double frostRoom = update.frost_prot_temp_room;
        if (frostRoom != null && differs(frostRoom, base.frost_prot_temp_room, known)) {
            set("frost_protection_temp_inside", decimal(frostRoom));
        }
        Double frostOutside = update.frost_prot_temp_outs;
        if (frostOutside != null && differs(frostOutside, base.frost_prot_temp_outs, known)) {
            set("frost_protection_temp_outside", decimal(frostOutside));
        }
        Integer preheat = update.max_preheat;
        if (preheat != null && differs(preheat, base.max_preheat, known)) {
            set("max_preheat",
                    preheat == MAX_PREHEAT_AUTOMATIC ? MAX_PREHEAT_AUTOMATIC_FORM_VALUE : preheat.toString());
        }
        Double vacationTemp = update.ch_vacation_temp;
        if (vacationTemp != null && differs(vacationTemp, base.ch_vacation_temp, known)) {
            set("vacation_mode_temp", decimal(vacationTemp));
        }
        Long vacationLength = update.ch_mode_vacation;
        if (vacationLength != null && differs(vacationLength, base.ch_mode_vacation, known)) {
            set("ch_mode_vacation", wholeUnits(vacationLength, 86400, "Vacation duration"));
        }
        Long extendLength = update.ch_mode_extend;
        if (extendLength != null && differs(extendLength, base.ch_mode_extend, known)) {
            set("ch_mode_extend", wholeUnits(extendLength, 3600, "Extend duration"));
        }
        Integer timeZone = update.time_zone;
        if (timeZone != null && differs(timeZone, base.time_zone, known)) {
            String zone = named(TIME_ZONE_NAMES, timeZone, "time zone");
            set("time_zone", zone.substring(0, 1).toUpperCase(Locale.ROOT) + zone.substring(1));
        }
        Integer legionEnabled = update.dhw_legion_enabled;
        if (legionEnabled != null && differs(legionEnabled, base.dhw_legion_enabled, known)) {
            setFlag("dhw_legion_enabled", legionEnabled != 0);
        }
        Integer legionDay = update.dhw_legion_day;
        if (legionDay != null && differs(legionDay, base.dhw_legion_day, known)) {
            set("dhw_legion_day", legionDay.toString());
        }
        Integer legionTime = update.dhw_legion_time;
        if (legionTime != null && differs(legionTime, base.dhw_legion_time, known)) {
            set("dhw_legion_time", String.format(Locale.ROOT, "%02d:%02d", legionTime / 60, legionTime % 60));
        }
        return !before.equals(snapshot());
    }

    private String snapshot() {
        StringBuilder text = new StringBuilder();
        for (FormField field : fields) {
            text.append(field.name()).append('=').append(field.value()).append('\n');
        }
        return text.toString();
    }

    private static boolean differs(Number wanted, Number current, boolean known) {
        return !known || wanted.doubleValue() != current.doubleValue();
    }

    private static void refuseIfChanged(String name, @Nullable Number wanted, Number current, boolean known)
            throws AtagOneCommunicationException {
        if (wanted != null && differs(wanted, current, known)) {
            throw new AtagOneCommunicationException(name + " cannot be changed through the portal");
        }
    }
}
