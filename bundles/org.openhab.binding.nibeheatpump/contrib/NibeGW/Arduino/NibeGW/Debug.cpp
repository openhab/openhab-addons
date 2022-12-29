/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Author: lassi.niemisto@gmail.com, pauli.anttila@gmail.com
 *
 */

#include "Debug.h"

#ifdef ENABLE_DEBUG

char debugBuf[DEBUG_BUFFER_SIZE];

#ifdef ENABLE_REMOTE_DEBUG

  #if (CONN_MODE == CONN_MODE_ETH)
    #if defined(PRODINO_BOARD)
      #include "KmpDinoEthernet.h"
    #else
      #include "KMPProDinoESP32.h"
    #endif
    EthernetServer telnet(23);
    EthernetClient client;
  #elif (CONN_MODE == CONN_MODE_WIFI)
    #include <WiFi.h>
    WiFiServer telnet(23);
    WiFiClient client;
  #endif 
#endif
  
void debugPrint(char* data) {
  #ifdef ENABLE_SERIAL_DEBUG
  Serial.print(data);
  #endif

  #ifdef ENABLE_REMOTE_DEBUG
  if (client) {
    client.write(data);
  }
  #endif
}

void initializeDebug()
{
  #ifdef ENABLE_REMOTE_DEBUG
    telnet.begin();
  #endif
}

bool getDebugInput(char* pChar)
{
#ifdef ENABLE_REMOTE_DEBUG
  client = telnet.available();

  if (client && client.connected() && client.available()) {
    *pChar = client.read();
    return true;
  }
#endif
  return false;
}

void exitDebugSession()
{
#ifdef ENABLE_REMOTE_DEBUG
  if (client) {
    client.flush();
    client.stop();
  }
#endif 
}

#endif
