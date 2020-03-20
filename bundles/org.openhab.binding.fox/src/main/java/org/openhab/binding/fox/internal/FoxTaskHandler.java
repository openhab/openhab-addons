package org.openhab.binding.fox.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.CommandOption;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.fox.internal.core.Fox;
import org.openhab.binding.fox.internal.core.FoxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoxTaskHandler {

    private final Logger logger = LoggerFactory.getLogger(FoxTaskHandler.class);
    private List<CommandOption> commandOptions;

    public FoxTaskHandler() {
        commandOptions = new ArrayList<CommandOption>();
    }

    public void setCommands(Map<String, String> options) {
        commandOptions.clear();
        for (String key : options.keySet()) {
            commandOptions.add(new CommandOption(key, options.get(key)));
        }
    }

    public List<StateOption> listStates() {
        ArrayList<StateOption> stateOptions = new ArrayList<StateOption>();
        for (CommandOption cmd : commandOptions) {
            stateOptions.add(new StateOption(cmd.getCommand(), cmd.getLabel()));
        }
        logger.debug(stateOptions.toString());
        return stateOptions;
    }

    public List<CommandOption> listCommands() {
        logger.debug(commandOptions.toString());
        return new ArrayList<CommandOption>(commandOptions);
    }

    private boolean hasCommandValue(String commandValue) {
        for (CommandOption commandOption : commandOptions) {
            if (commandValue.equals(commandOption.getCommand())) {
                return true;
            }
        }
        return false;
    }

    private String findCommandValue(String commandLabel) {
        for (CommandOption commandOption : commandOptions) {
            if (commandLabel.equals(commandOption.getLabel())) {
                return commandOption.getCommand();
            }
        }
        return null;
    }

    private String findCommandLabel(String commandValue) {
        for (CommandOption commandOption : commandOptions) {
            if (commandValue.equals(commandOption.getCommand())) {
                return commandOption.getLabel();
            }
        }
        return null;
    }

    private int tryParseCommandValue(String commandValue) {
        try {
            return Integer.parseInt(commandValue.replaceFirst("T", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int findTaskId(String commandValue) {
        return hasCommandValue(commandValue) ? tryParseCommandValue(commandValue) : 0;
    }

    private int findTaskIdByLabel(String commandLabel) {
        String commandValue = findCommandValue(commandLabel);
        return commandValue != null ? tryParseCommandValue(commandValue) : 0;
    }

    private void tryDoTask(Fox fox, int taskId) throws FoxException {
        fox.doTask(taskId);
    }

    public void request(Fox fox, String commandValue) throws FoxException {
        int taskId = findTaskId(commandValue);
        if (taskId > 0) {
            tryDoTask(fox, taskId);
        }
    }

    public void requestByLabel(Fox fox, String commandLabel) throws FoxException {
        int taskId = findTaskIdByLabel(commandLabel);
        if (taskId > 0) {
            tryDoTask(fox, taskId);
        }
    }

    public String findTask(String task) {
        return hasCommandValue(task) ? task : null;
    }

    public String findTaskLabel(String task) {
        return findCommandLabel(task);
    }
}
