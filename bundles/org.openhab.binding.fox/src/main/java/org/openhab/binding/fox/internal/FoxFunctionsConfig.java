package org.openhab.binding.fox.internal;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class FoxFunctionsConfigFunctions {
    Map<String, String> tasks = new HashMap<String, String>();
    Map<String, String> results = new HashMap<String, String>();
}

class FoxFunctionsConfigRoot {
    FoxFunctionsConfigFunctions API = new FoxFunctionsConfigFunctions();
}

public class FoxFunctionsConfig {
    FoxFunctionsConfigRoot root;

    FoxFunctionsConfig(String json) {
        if (json != null && !json.isEmpty()) {
            try {
                root = new Gson().fromJson(json, FoxFunctionsConfigRoot.class);
            } catch (JsonSyntaxException e) {
                root = new FoxFunctionsConfigRoot();
            }
        } else {
            root = new FoxFunctionsConfigRoot();
        }
    }

    Map<String, String> getTasks() {
        return root.API.tasks;
    }

    Map<String, String> getResults() {
        return root.API.results;
    }
}
