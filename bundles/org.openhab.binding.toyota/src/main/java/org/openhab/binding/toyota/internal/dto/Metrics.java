package org.openhab.binding.toyota.internal.dto;

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

public class Metrics {
    public static Type LIST_CLASS = new TypeToken<List<Metrics>>() {
    }.getType();

    public String type;
    public double value;
    public @Nullable String unit;
}
