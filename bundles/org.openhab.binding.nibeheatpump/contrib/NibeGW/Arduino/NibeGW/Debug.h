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
 * Author: pauli.anttila@gmail.com
 *
 */
#ifndef Debug_h
#define Debug_h

#ifdef ENABLE_DEBUG
  #define DEBUG_PRINT_MSG(level, message) if (config.debug.verboseLevel >= level) { debugPrint(message); }
  #define DEBUG_PRINT_VARS(level, message, ...) if (config.debug.verboseLevel >= level) { sprintf(debugBuf, message, __VA_ARGS__); debugPrint(debugBuf); }
  #define DEBUG_PRINT_ARRAY(level, data, len) if (config.debug.verboseLevel >= level) { for (int i = 0; i < len; i++) { sprintf(debugBuf, "%02X", data[i]); debugPrint(debugBuf); }}

  #define DEBUG_BUFFER_SIZE   80
  char debugBuf[DEBUG_BUFFER_SIZE];

  #ifdef ENABLE_REMOTE_DEBUG
    EthernetServer telnet(23);
  #endif
  
  void debugPrint(char* data) {
    #ifdef ENABLE_SERIAL_DEBUG
      Serial.print(data);
    #endif
  
    #ifdef ENABLE_REMOTE_DEBUG
      telnet.print(data);
    #endif
  }

#else
  #define DEBUG_PRINT_MSG(level, message)
  #define DEBUG_PRINT_VARS(level, message, ...)
  #define DEBUG_PRINT_ARRAY(level, data, len)
#endif

#endif
