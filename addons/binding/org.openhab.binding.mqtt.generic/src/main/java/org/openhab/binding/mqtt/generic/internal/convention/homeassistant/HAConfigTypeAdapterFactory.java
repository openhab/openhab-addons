package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class HAConfigTypeAdapterFactory implements TypeAdapterFactory {

    public final static HAConfigTypeAdapterFactory INSTANCE = new HAConfigTypeAdapterFactory();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!HAConfiguration.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {

            @Override
            public T read(JsonReader in) throws IOException {
                T result = delegate.read(new MappingJsonReader(in));
                expandTidleInTopics(HAConfiguration.class.cast(result));
                return result;
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }
        };
    }

    private void expandTidleInTopics(HAConfiguration config) {
        if (config != null) {
            Class<?> type = config.getClass();

            String tilde = config.tilde;

            while (type != Object.class) {
                Field[] fields = type.getDeclaredFields();

                for (Field field : fields) {
                    if (String.class.isAssignableFrom(field.getType()) && field.getName().endsWith("_topic")) {
                        field.setAccessible(true);

                        try {
                            String oldValue = (String) field.get(config);
                            final String newValue = StringUtils.replace(oldValue, "~", tilde);

                            field.set(config, newValue);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                type = type.getSuperclass();
            }
        }
    }
}

class MappingJsonReader extends JsonReaderProxy {

    private static final Map<String, String> REPLACEMENTS = new HashMap<>();

    static {
        REPLACEMENTS.put("aux_cmd_t", "aux_command_topic");
        REPLACEMENTS.put("aux_stat_tpl", "aux_state_template");
        REPLACEMENTS.put("aux_stat_t", "aux_state_topic");
        REPLACEMENTS.put("avty_t", "availability_topic");
        REPLACEMENTS.put("away_mode_cmd_t", "away_mode_command_topic");
        REPLACEMENTS.put("away_mode_stat_tpl", "away_mode_state_template");
        REPLACEMENTS.put("away_mode_stat_t", "away_mode_state_topic");
        REPLACEMENTS.put("bri_cmd_t", "brightness_command_topic");
        REPLACEMENTS.put("bri_scl", "brightness_scale");
        REPLACEMENTS.put("bri_stat_t", "brightness_state_topic");
        REPLACEMENTS.put("bri_val_tpl", "brightness_value_template");
        REPLACEMENTS.put("bat_lev_t", "battery_level_topic");
        REPLACEMENTS.put("bat_lev_tpl", "battery_level_template");
        REPLACEMENTS.put("chrg_t", "charging_topic");
        REPLACEMENTS.put("chrg_tpl", "charging_template");
        REPLACEMENTS.put("clr_temp_cmd_t", "color_temp_command_topic");
        REPLACEMENTS.put("clr_temp_stat_t", "color_temp_state_topic");
        REPLACEMENTS.put("clr_temp_val_tpl", "color_temp_value_template");
        REPLACEMENTS.put("cln_t", "cleaning_topic");
        REPLACEMENTS.put("cln_tpl", "cleaning_template");
        REPLACEMENTS.put("cmd_t", "command_topic");
        REPLACEMENTS.put("curr_temp_t", "current_temperature_topic");
        REPLACEMENTS.put("dev_cla", "device_class");
        REPLACEMENTS.put("dock_t", "docked_topic");
        REPLACEMENTS.put("dock_tpl", "docked_template");
        REPLACEMENTS.put("err_t", "error_topic");
        REPLACEMENTS.put("err_tpl", "error_template");
        REPLACEMENTS.put("fanspd_t", "fan_speed_topic");
        REPLACEMENTS.put("fanspd_tpl", "fan_speed_template");
        REPLACEMENTS.put("fanspd_lst", "fan_speed_list");
        REPLACEMENTS.put("fx_cmd_t", "effect_command_topic");
        REPLACEMENTS.put("fx_list", "effect_list");
        REPLACEMENTS.put("fx_stat_t", "effect_state_topic");
        REPLACEMENTS.put("fx_val_tpl", "effect_value_template");
        REPLACEMENTS.put("exp_aft", "expire_after");
        REPLACEMENTS.put("fan_mode_cmd_t", "fan_mode_command_topic");
        REPLACEMENTS.put("fan_mode_stat_tpl", "fan_mode_state_template");
        REPLACEMENTS.put("fan_mode_stat_t", "fan_mode_state_topic");
        REPLACEMENTS.put("frc_upd", "force_update");
        REPLACEMENTS.put("hold_cmd_t", "hold_command_topic");
        REPLACEMENTS.put("hold_stat_tpl", "hold_state_template");
        REPLACEMENTS.put("hold_stat_t", "hold_state_topic");
        REPLACEMENTS.put("ic", "icon");
        REPLACEMENTS.put("init", "initial");
        REPLACEMENTS.put("json_attr", "json_attributes");
        REPLACEMENTS.put("max_temp", "max_temp");
        REPLACEMENTS.put("min_temp", "min_temp");
        REPLACEMENTS.put("mode_cmd_t", "mode_command_topic");
        REPLACEMENTS.put("mode_stat_tpl", "mode_state_template");
        REPLACEMENTS.put("mode_stat_t", "mode_state_topic");
        REPLACEMENTS.put("name", "name");
        REPLACEMENTS.put("on_cmd_type", "on_command_type");
        REPLACEMENTS.put("opt", "optimistic");
        REPLACEMENTS.put("osc_cmd_t", "oscillation_command_topic");
        REPLACEMENTS.put("osc_stat_t", "oscillation_state_topic");
        REPLACEMENTS.put("osc_val_tpl", "oscillation_value_template");
        REPLACEMENTS.put("pl_arm_away", "payload_arm_away");
        REPLACEMENTS.put("pl_arm_home", "payload_arm_home");
        REPLACEMENTS.put("pl_avail", "payload_available");
        REPLACEMENTS.put("pl_cls", "payload_close");
        REPLACEMENTS.put("pl_disarm", "payload_disarm");
        REPLACEMENTS.put("pl_hi_spd", "payload_high_speed");
        REPLACEMENTS.put("pl_lock", "payload_lock");
        REPLACEMENTS.put("pl_lo_spd", "payload_low_speed");
        REPLACEMENTS.put("pl_med_spd", "payload_medium_speed");
        REPLACEMENTS.put("pl_not_avail", "payload_not_available");
        REPLACEMENTS.put("pl_off", "payload_off");
        REPLACEMENTS.put("pl_on", "payload_on");
        REPLACEMENTS.put("pl_open", "payload_open");
        REPLACEMENTS.put("pl_osc_off", "payload_oscillation_off");
        REPLACEMENTS.put("pl_osc_on", "payload_oscillation_on");
        REPLACEMENTS.put("pl_stop", "payload_stop");
        REPLACEMENTS.put("pl_unlk", "payload_unlock");
        REPLACEMENTS.put("pow_cmd_t", "power_command_topic");
        REPLACEMENTS.put("ret", "retain");
        REPLACEMENTS.put("rgb_cmd_tpl", "rgb_command_template");
        REPLACEMENTS.put("rgb_cmd_t", "rgb_command_topic");
        REPLACEMENTS.put("rgb_stat_t", "rgb_state_topic");
        REPLACEMENTS.put("rgb_val_tpl", "rgb_value_template");
        REPLACEMENTS.put("send_cmd_t", "send_command_topic");
        REPLACEMENTS.put("send_if_off", "send_if_off");
        REPLACEMENTS.put("set_pos_tpl", "set_position_template");
        REPLACEMENTS.put("set_pos_t", "set_position_topic");
        REPLACEMENTS.put("spd_cmd_t", "speed_command_topic");
        REPLACEMENTS.put("spd_stat_t", "speed_state_topic");
        REPLACEMENTS.put("spd_val_tpl", "speed_value_template");
        REPLACEMENTS.put("spds", "speeds");
        REPLACEMENTS.put("stat_clsd", "state_closed");
        REPLACEMENTS.put("stat_off", "state_off");
        REPLACEMENTS.put("stat_on", "state_on");
        REPLACEMENTS.put("stat_open", "state_open");
        REPLACEMENTS.put("stat_t", "state_topic");
        REPLACEMENTS.put("stat_val_tpl", "state_value_template");
        REPLACEMENTS.put("sup_feat", "supported_features");
        REPLACEMENTS.put("swing_mode_cmd_t", "swing_mode_command_topic");
        REPLACEMENTS.put("swing_mode_stat_tpl", "swing_mode_state_template");
        REPLACEMENTS.put("swing_mode_stat_t", "swing_mode_state_topic");
        REPLACEMENTS.put("temp_cmd_t", "temperature_command_topic");
        REPLACEMENTS.put("temp_stat_tpl", "temperature_state_template");
        REPLACEMENTS.put("temp_stat_t", "temperature_state_topic");
        REPLACEMENTS.put("tilt_clsd_val", "tilt_closed_value");
        REPLACEMENTS.put("tilt_cmd_t", "tilt_command_topic");
        REPLACEMENTS.put("tilt_inv_stat", "tilt_invert_state");
        REPLACEMENTS.put("tilt_max", "tilt_max");
        REPLACEMENTS.put("tilt_min", "tilt_min");
        REPLACEMENTS.put("tilt_opnd_val", "tilt_opened_value");
        REPLACEMENTS.put("tilt_status_opt", "tilt_status_optimistic");
        REPLACEMENTS.put("tilt_status_t", "tilt_status_topic");
        REPLACEMENTS.put("t", "topic");
        REPLACEMENTS.put("uniq_id", "unique_id");
        REPLACEMENTS.put("unit_of_meas", "unit_of_measurement");
        REPLACEMENTS.put("val_tpl", "value_template");
        REPLACEMENTS.put("whit_val_cmd_t", "white_value_command_topic");
        REPLACEMENTS.put("whit_val_stat_t", "white_value_state_topic");
        REPLACEMENTS.put("whit_val_tpl", "white_value_template");
        REPLACEMENTS.put("xy_cmd_t", "xy_command_topic");
        REPLACEMENTS.put("xy_stat_t", "xy_state_topic");
        REPLACEMENTS.put("xy_val_tpl", "xy_value_template");
    }

    public MappingJsonReader(JsonReader delegate) {
        super(delegate);
    }

    @Override
    public String nextName() throws IOException {
        String name = super.nextName();
        return REPLACEMENTS.getOrDefault(name, name);
    }

}

class JsonReaderProxy extends JsonReader {
    private final JsonReader delegate;

    public JsonReaderProxy(JsonReader delegate) {
        super(new StringReader(""));
        this.delegate = delegate;
    }

    @Override
    public void beginArray() throws IOException {
        delegate.beginArray();
    }

    @Override
    public void beginObject() throws IOException {
        delegate.beginObject();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void endArray() throws IOException {
        delegate.endArray();
    }

    @Override
    public void endObject() throws IOException {
        delegate.endObject();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public boolean hasNext() throws IOException {
        return delegate.hasNext();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean nextBoolean() throws IOException {
        return delegate.nextBoolean();
    }

    @Override
    public double nextDouble() throws IOException {
        return delegate.nextDouble();
    }

    @Override
    public int nextInt() throws IOException {
        return delegate.nextInt();
    }

    @Override
    public long nextLong() throws IOException {
        return delegate.nextLong();
    }

    @Override
    public String nextName() throws IOException {
        return delegate.nextName();
    }

    @Override
    public void nextNull() throws IOException {
        delegate.nextNull();
    }

    @Override
    public String nextString() throws IOException {
        return delegate.nextString();
    }

    @Override
    public JsonToken peek() throws IOException {
        return delegate.peek();
    }

    @Override
    public void skipValue() throws IOException {
        delegate.skipValue();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}