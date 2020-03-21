package org.openhab.binding.fox.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.fox.internal.core.Fox;
import org.openhab.binding.fox.internal.core.FoxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoxResultHandler {

    private final Logger logger = LoggerFactory.getLogger(FoxResultHandler.class);
    private List<StateOption> stateOptions;

    public FoxResultHandler() {
        stateOptions = new ArrayList<StateOption>();
    }

    public void setStates(Map<String, String> options) {
        stateOptions.clear();
        for (String key : options.keySet()) {
            stateOptions.add(new StateOption(key, options.get(key)));
        }
    }

    public List<StateOption> listStates() {
        logger.debug(stateOptions.toString());
        return new ArrayList<StateOption>(stateOptions);
    }

    private boolean hasStateValue(String stateValue) {
        for (StateOption stateOption : stateOptions) {
            if (stateValue.equals(stateOption.getValue())) {
                return true;
            }
        }
        return false;
    }

    private String findStateLabel(String stateValue) {
        for (StateOption stateOption : stateOptions) {
            if (stateValue.equals(stateOption.getValue())) {
                return stateOption.getLabel();
            }
        }
        return null;
    }

    private String tryNoticeResult(Fox fox) throws FoxException {
        return fox.noticeResult();
    }

    public String acquire(Fox fox) throws FoxException {
        return tryNoticeResult(fox);
    }

    public String findResult(String result) {
        return hasStateValue(result) ? result : null;
    }

    public String findResultLabel(String result) {
        return findStateLabel(result);
    }
}
