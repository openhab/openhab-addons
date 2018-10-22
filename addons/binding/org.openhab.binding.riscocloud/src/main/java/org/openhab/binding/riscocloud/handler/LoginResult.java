package org.openhab.binding.riscocloud.handler;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.riscocloud.json.ServerDatasHandler;

public class LoginResult {

    String errorDetail;
    String error;
    String statusDescr;
    Map<Integer, String> siteList = new HashMap<Integer, String>();
    ServerDatasHandler serverDatasHandler;

}
