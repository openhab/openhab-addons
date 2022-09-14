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
#ifndef Config_h
#define Config_h

#include <Arduino.h>

// ######### BOARD SELECTION #######################

// Possible target base architectures
#define BASE_ARCH_ARDUINO  1
#define BASE_ARCH_ESP32    2

// Selected base architecture
#define BASE_ARCH   BASE_ARCH_ESP32

// Possible outgoing connectivity modes
#define CONN_MODE_ETH  1
#define CONN_MODE_WIFI 2

// Selected outgoing connectivity mode
#define CONN_MODE   CONN_MODE_ETH

// Enable if you use ProDiNo NetBoard V2.1 board
//#define PRODINO_BOARD

// Enable if you use PRODINo ESP32 Ethernet v1 (Enable also HARDWARE_SERIAL_WITH_PINS in NibeGW.h)
#define PRODINO_BOARD_ESP32

// Enable if you use a generic ESP32 board (Enable also HARDWARE_SERIAL_WITH_PINS in NibeGW.h)
//#define GENERIC_BOARD_ESP32

// Enable if ENC28J60 LAN module is used
//#define TRANSPORT_ETH_ENC28J60

// ######### CONFIGURATION #######################

// Enable dynamic configuration mode via WiFi connection (supported only by the PRODINO_BOARD_ESP32 board)
// Dynamic configuration mode is loaded if input 0 is ON during boot
// When dynamic configuration mode is activated, login to the 'Bleeper' WiFi Access point:
//  1. Configuration page is available on IP 192.168.4.1 port 80 (http://192.168.4.1).
//  2. OTA update page is available on IP 192.168.4.1 port 8080 (http://192.168.4.1:8080/update).
// Install following libraries via Arduino IDE library manager:
//  1. Bleeper (tested with version 1.1.0)
//  2. ElegantOTA (tested with version 2.2.9)
#define ENABLE_DYNAMIC_CONFIG

// Enable debug printouts.
#define ENABLE_DEBUG
#define VERBOSE_LEVEL           1
#define ENABLE_SERIAL_DEBUG
#define ENABLE_REMOTE_DEBUG     // Remote debug is available in telnet port 23 

#define BOARD_NAME              "Arduino NibeGW"

// Ethernet configuration
#define BOARD_MAC               "DE:AD:BE:EF:FE:ED" // Not used in wifi mode, as ESP32 has a default MAC
#define BOARD_IP                "192.168.1.100"
#define DNS_SERVER              "192.168.1.1"
#define GATEWAY_IP              "192.168.1.1"
#define NETWORK_MASK            "255.255.255.0"

// Wifi configuration
#define WIFI_SSID               ""
#define WIFI_PASS               ""

// UDP ports for incoming messages
#define INCOMING_PORT_READCMDS  9999
#define INCOMING_PORT_WRITECMDS 10000

// Target IP address and port where Nibe UDP packets are send
#define TARGET_IP               "192.168.1.101"
#define TARGET_PORT             9999

// Delay before initialize ethernet on startup in seconds
#define ETH_INIT_DELAY          5

// Send acknowledge PDU's to Nibe
#define SEND_ACK                true

// Ack following periperial messages
#define ACK_MODBUS40            true
#define ACK_SMS40               false
#define ACK_RMU40               false



// Used serial port and direction change pin for RS-485 port
#if defined(PRODINO_BOARD)
  #define RS485_PORT            Serial1
  #define RS485_DIRECTION_PIN   3
#elif defined(PRODINO_BOARD_ESP32)
  #define WDT_TIMEOUT           2
  #define RS485_RX_PIN          4
  #define RS485_TX_PIN          16
  #define RS485_DIRECTION_PIN   2
#elif defined(GENERIC_BOARD_ESP32)
  #define WDT_TIMEOUT           2
  #define RS485_RX_PIN          16
  #define RS485_TX_PIN          17
  #define RS485_DIRECTION_PIN   19
#else
  #define RS485_PORT            Serial
  #define RS485_DIRECTION_PIN   2
#endif


// ######### VARIABLES #######################

#if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)

  #include "Bleeper.h"    // https://github.com/workilabs/Bleeper
  #include "ElegantOTA.h" // https://github.com/ayushsharma82/ElegantOTA

  class EthConfig: public Configuration {
    public:
      persistentStringVar(mac, BOARD_MAC);
      persistentStringVar(ip, BOARD_IP);
      persistentStringVar(dns, DNS_SERVER);
      persistentStringVar(gateway, GATEWAY_IP);
      persistentStringVar(mask, NETWORK_MASK);
      persistentIntVar(initDelay, ETH_INIT_DELAY);
  };

  class NibeAckConfig: public Configuration {
    public:
      persistentIntVar(sendAck, SEND_ACK);
      persistentIntVar(modbus40, ACK_MODBUS40);
      persistentIntVar(sms40, ACK_SMS40);
      persistentIntVar(rmu40, ACK_RMU40);
  };
  
  class NibeConfig: public Configuration {
    public:
      persistentStringVar(targetIp, TARGET_IP);
      persistentIntVar(targetPort, TARGET_PORT);
      persistentIntVar(readCmdsPort, INCOMING_PORT_READCMDS);
      persistentIntVar(writeCmdsPort, INCOMING_PORT_WRITECMDS);
      subconfig(NibeAckConfig, ack);
  };

  #ifdef ENABLE_DEBUG
  class DebugConfig: public Configuration {
    public:
      persistentIntVar(verboseLevel, VERBOSE_LEVEL);
  };
  #endif
  
  class Config: public RootConfiguration {
    public:
      persistentStringVar(boardName, BOARD_NAME);
      subconfig(EthConfig, eth);
      subconfig(NibeConfig, nibe);
      #ifdef ENABLE_DEBUG
      subconfig(DebugConfig, debug);
      #endif
  };
    
#else

  typedef struct Config {
    String    boardName;
    struct {
      String    mac;
      String    ip;
      String    dns;
      String    gateway;
      String    mask;
      uint16_t  initDelay;
    } eth;
    
    struct {
      String    targetIp;
      uint16_t  targetPort;
      uint16_t  readCmdsPort;
      uint16_t  writeCmdsPort;
      struct {
        uint8_t sendAck;
        uint8_t modbus40;
        uint8_t sms40;
        uint8_t rmu40;
      } ack;
    } nibe;
  
    
    #ifdef ENABLE_DEBUG
    struct {
      uint8_t   verboseLevel;
    } debug;
    #endif
  
  };
#endif

extern Config config;

#endif
