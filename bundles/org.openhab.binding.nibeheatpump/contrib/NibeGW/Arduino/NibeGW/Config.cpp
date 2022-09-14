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

#include "Config.h"

#if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)
  Config config;
#else
  Config config = {
    BOARD_NAME,
    
    {
      BOARD_MAC,
      BOARD_IP,
      DNS_SERVER,
      GATEWAY_IP,
      NETWORK_MASK,
      ETH_INIT_DELAY
    },
  
    {
      TARGET_IP,
      TARGET_PORT,
      INCOMING_PORT_READCMDS,
      INCOMING_PORT_WRITECMDS,

      {
        SEND_ACK,
        ACK_MODBUS40,
        ACK_SMS40,
        ACK_RMU40,
      },
    },
  
    
    #ifdef ENABLE_DEBUG
    {
      VERBOSE_LEVEL
    }
    #endif
  
  };
#endif

