/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * ----------------------------------------------------------------------------
 *
 *  Author: pauli.anttila@gmail.com
 *
 *
 *  2.11.2013   v1.00   Initial version.
 *  3.11.2013   v1.01
 *  27.6.2014   v1.02   Fixed compile error and added Ethernet initialization delay.
 *  29.6.2015   v2.00   Bidirectional support.
 *  18.2.2017   v3.00   Redesigned.
 *  14.3.2021   v3.01   Fix Prodino build + fixed UDP issue + debug improvements
 *  3.7.2022    v4.00   Send messages to IP address received from the UDP messages
 *  13.7.2022   v4.01   Fixed target IP address issue
 *  29.7.2022   v5.00   New configuration model and PRODINo ESP32 Ethernet v1 support with OTA update
 *  19.11.2022  v5.10   Support 16-bit addressing.
 */

#define VERSION   "5.00"

// ######### INCLUDES #######################

#include "Config.h"

#if defined(PRODINO_BOARD)
  #include "KmpDinoEthernet.h"
  #include "KMPCommon.h"
  #include "Ethernet/utility/w5100.h"
#elif defined(PRODINO_BOARD_ESP32)
  #include <esp_task_wdt.h>
  #include "KMPProDinoESP32.h"
  #include "KMPCommon.h"
#elif defined(TRANSPORT_ETH_ENC28J60)
 #include <UIPEthernet.h>
#else
 #include <SPI.h>
 #include <Ethernet.h>
 #include <EthernetUdp.h>
#endif
 
#if !defined(PRODINO_BOARD_ESP32)
  #include <avr/wdt.h>
#endif

#include "NibeGw.h"
#include "Debug.h"

// ######### VARIABLES #######################

boolean ethInitialized = false;

IPAddress targetIp;
EthernetUDP udp;
EthernetUDP udp4writeCmnds;

#if defined(PRODINO_BOARD_ESP32)
  HardwareSerial RS485_PORT(1);
  NibeGw nibegw(&RS485_PORT, RS485_DIRECTION_PIN, RS485_RX_PIN, RS485_TX_PIN);
#else
  NibeGw nibegw(&RS485_PORT, RS485_DIRECTION_PIN);
#endif

#if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)
  boolean dynamicConfigStarted = false;

  class ConfigObserver: public ConfigurationObserver {
  public:
    void onConfigurationChanged(const ConfigurationPropertyChange value) {
      DEBUG_PRINT_VARS(0, "Configuration parameter '%s' changed from '%s' to '%s'\n", 
        String(value.key).c_str(), 
        String(value.oldValue).c_str(),
        String(value.newValue).c_str());
    }
  };

#endif

// ######### SETUP #######################

void setup() {
  #if defined(PRODINO_BOARD_ESP32)
    KMPProDinoESP32.begin(ProDino_ESP32_Ethernet);
    KMPProDinoESP32.setStatusLed(red);
  #endif
  
  #if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)
    if (isDynamicConfigModeActivated()) {
      setupDynamicConfigMode();
    } else {
      setupStaticConfigMode();
    }
  #else
    setupStaticConfigMode();
  #endif
    
}

void setupStaticConfigMode() {
  #if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)
    // Use temporarily longer wathdog time as possible flash formating might take a while
    esp_task_wdt_init(60, true);
    esp_task_wdt_add(NULL);
    KMPProDinoESP32.setStatusLed(white);
    Bleeper
      .configuration
        .set(&config)
        .done()
      .storage
        .set(new SPIFFSStorage())
        .done()
      .init();
    esp_task_wdt_reset();
    KMPProDinoESP32.setStatusLed(red);
  #endif

  #if defined(PRODINO_BOARD_ESP32)
    Serial.begin(115200, SERIAL_8N1);
  #elif defined(PRODINO_BOARD)
    Serial.begin(115200, SERIAL_8N1);
    DinoInit();
  #endif
   
  // Start watchdog
  #if defined(PRODINO_BOARD_ESP32)
    esp_task_wdt_init(WDT_TIMEOUT, true);
    esp_task_wdt_add(NULL);
  #else
    wdt_enable(WDTO_2S);
  #endif

  nibegw.setCallback(nibeCallbackMsgReceived, nibeCallbackTokenReceived);
  nibegw.setSendAcknowledge(config.nibe.ack.sendAck);
  nibegw.setAckModbus40Address(config.nibe.ack.modbus40);
  nibegw.setAckSms40Address(config.nibe.ack.sms40);
  nibegw.setAckRmu40Address(config.nibe.ack.rmu40);

  #ifdef ENABLE_NIBE_DEBUG
    nibegw.setDebugCallback(nibeDebugCallback);
    nibegw.setVerboseLevel(config.debug.verboseLevel);
  #endif

  targetIp.fromString(config.nibe.targetIp);

  DEBUG_PRINT_VARS(0, "%s version %s Started\n", config.boardName.c_str(), VERSION);
}

#if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)

boolean isDynamicConfigModeActivated() {
  if (KMPProDinoESP32.getOptoInState(0)) {
      delay(50);
      if (KMPProDinoESP32.getOptoInState(0)) {
        return true;
      }
  }
  return false; 
}

void setupDynamicConfigMode() {
  KMPProDinoESP32.setStatusLed(white);
  Bleeper
    .verbose(115200)
    .configuration
      .set(&config)
      .addObserver(new ConfigObserver(), {})
      .done()
    .configurationInterface
      .addDefaultWebServer()
      .done()
    .connection
      .setSingleConnectionFromPriorityList({
          new AP()
      })
      .done()
    .storage
      .set(new SPIFFSStorage())
      .done()
    .init(); 

    ElegantOTA.begin(&otaServer);
    otaServer.begin();

    dynamicConfigStarted = true;
    KMPProDinoESP32.setStatusLed(blue);
}
#endif

// ######### MAIN LOOP #######################

void loop() {
  #if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)
    if (dynamicConfigStarted) {
      loopDynamicConfigMode();
    } else {
      loopNormalMode();
    }
  #else
    loopNormalMode();
  #endif
}

void loopNormalMode() {
  #if defined(PRODINO_BOARD_ESP32)
    esp_task_wdt_reset();
  #else
    wdt_reset();
  #endif
  
  long now = millis() / 1000;

  if (!nibegw.connected()) {
    nibegw.connect();
  } else {
    do {
      nibegw.loop();
      #ifdef TRANSPORT_ETH_ENC28J60
        Ethernet.maintain();
      #endif
    } while (nibegw.messageStillOnProgress());
  }

  if (!ethInitialized && now >= config.eth.initDelay) {
    initializeEthernet();
    #ifdef ENABLE_DEBUG
      telnet.begin();
    #endif
  }

  #if defined(ENABLE_DEBUG) && defined(ENABLE_REMOTE_DEBUG)
    if (ethInitialized) {
        handleTelnet();
    }
  #endif
}

#if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)
void loopDynamicConfigMode() {
  Bleeper.handle();
  otaServer.handleClient();
}
#endif

// ######### FUNCTIONS #######################

void initializeEthernet() {
  DEBUG_PRINT_MSG(1, "Initializing Ethernet\n");

  uint8_t   mac[6];
  sscanf(config.eth.mac.c_str(), "%x:%x:%x:%x:%x:%x", mac, mac+1, mac+2, mac+3, mac+4, mac+5);
  
  IPAddress ip;
  IPAddress dns;
  IPAddress gw;
  IPAddress mask;
  
  ip.fromString(config.eth.ip);
  dns.fromString(config.eth.dns);
  gw.fromString(config.eth.gateway);
  mask.fromString(config.eth.mask);
  
  Ethernet.begin(mac, ip, dns, gw, mask);

  #if defined(PRODINO_BOARD_ESP32)
    Ethernet.setRetransmissionCount(1);
    Ethernet.setRetransmissionTimeout(50);
  #elif defined(PRODINO_BOARD)
    W5100.setRetransmissionCount(1);
    W5100.setRetransmissionTime(50);
  #endif
  
  ethInitialized = true;
  udp.begin(config.nibe.readCmdsPort); 
  udp4writeCmnds.begin(config.nibe.writeCmdsPort);

  printInfo();
  
  #if defined(PRODINO_BOARD_ESP32)
    KMPProDinoESP32.offStatusLed();
  #endif
}

void nibeCallbackMsgReceived(const byte* const data, int len) {
  #if defined(PRODINO_BOARD_ESP32)
    KMPProDinoESP32.setStatusLed(green);
  #endif
    
  if (ethInitialized) {
    sendUdpPacket(data, len);
  }

  #if defined(PRODINO_BOARD_ESP32)
    KMPProDinoESP32.offStatusLed();
  #endif
}

int nibeCallbackTokenReceived(eTokenType token, byte* data) {
  int len = 0;
  if (ethInitialized) {
    if (token == READ_TOKEN) {
      DEBUG_PRINT_MSG(3, "Read token received from nibe\n");
      int packetSize = udp.parsePacket();
      if (packetSize) {
        #if defined(PRODINO_BOARD_ESP32)
          KMPProDinoESP32.setStatusLed(white);
        #endif
        targetIp = udp.remoteIP();
        len = udp.read(data, packetSize);
        DEBUG_PRINT_VARS(2, "Send read command to nibe, len=%d, ", len);
        DEBUG_PRINT_MSG(1, " data in: ");
        DEBUG_PRINT_ARRAY(1, data, len)
        DEBUG_PRINT_MSG(1, "\n");
        
        #if defined(TRANSPORT_ETH_ENC28J60)
          udp4readCmnds.flush();
          udp4readCmnds.stop();
          udp4readCmnds.begin(config.nibe.readCmdsPort);
        #endif
      }
    } else if (token == WRITE_TOKEN) {
      DEBUG_PRINT_MSG(3, "Write token received from nibe\n");
      int packetSize = udp4writeCmnds.parsePacket();
      if (packetSize) {
        #if defined(PRODINO_BOARD_ESP32)
          KMPProDinoESP32.setStatusLed(orange);
        #endif
        targetIp = udp.remoteIP();
        len = udp4writeCmnds.read(data, packetSize);
        DEBUG_PRINT_VARS(2, "Send write command to nibe, len=%d, ", len);
        DEBUG_PRINT_MSG(1, " data in: ");
        DEBUG_PRINT_ARRAY(1, data, len)
        DEBUG_PRINT_MSG(1, "\n");
        
        #if defined(TRANSPORT_ETH_ENC28J60)
          udp4writeCmnds.flush();
          udp4writeCmnds.stop();
          udp4writeCmnds.begin(config.nibe.writeCmdsPort);
        #endif
      }
    }

    #if defined(PRODINO_BOARD_ESP32)
      KMPProDinoESP32.offStatusLed();
    #endif
  }
  return len;
}

void nibeDebugCallback(byte level, char* data) {
  DEBUG_PRINT_MSG(level, data);
}

void sendUdpPacket(const byte* const data, int len) {
  #ifdef ENABLE_DEBUG
    DEBUG_PRINT_VARS(2, "Sending UDP packet to %s:%d, len=%d", IPtoString(targetIp).c_str(), config.nibe.targetPort, len);
    DEBUG_PRINT_MSG(1, " data out: ");
    DEBUG_PRINT_ARRAY(1, data, len)
    DEBUG_PRINT_MSG(1, "\n");
  #endif

  #if defined(PRODINO_BOARD_ESP32)
    EthernetLinkStatus linkStatus = Ethernet.linkStatus();
    if (linkStatus != LinkON) {
      DEBUG_PRINT_VARS(0, "Ethernet link is down, link status = %d\n", linkStatus);
      return;
    }
  #endif
    
  udp.beginPacket(targetIp, config.nibe.targetPort);
  
  udp.write(data, len);
  int retval = udp.endPacket();
  if (retval) {
    DEBUG_PRINT_MSG(3, "UDP packet sent succeed\n");
  } else {
    DEBUG_PRINT_MSG(1, "UDP packet sent failed\n");
  }
}

String IPtoString(const IPAddress& address) {
  return String() + address[0] + "." + address[1] + "." + address[2] + "." + address[3];
}

void printInfo() {
  #ifdef ENABLE_DEBUG
  DEBUG_PRINT_VARS(0, "%s version %s\nUsing configuration:\n", config.boardName.c_str(), VERSION);
  DEBUG_PRINT_VARS(0, "MAC=%s\n", config.eth.mac.c_str());
  DEBUG_PRINT_VARS(0, "IP=%s\n", config.eth.ip.c_str());
  DEBUG_PRINT_VARS(0, "DNS=%s\n", config.eth.dns.c_str());
  DEBUG_PRINT_VARS(0, "MASK=%s\n", config.eth.mask.c_str());
  DEBUG_PRINT_VARS(0, "GATEWAY=%s\n", config.eth.gateway.c_str());
  DEBUG_PRINT_VARS(0, "ETH_INIT_DELAY=%d\n", config.eth.initDelay);
  DEBUG_PRINT_VARS(0, "TARGET_IP=%s\n", IPtoString(targetIp).c_str());
  DEBUG_PRINT_VARS(0, "TARGET_PORT=%d\n", config.nibe.targetPort);
  DEBUG_PRINT_VARS(0, "INCOMING_PORT_READCMDS=%d\n", config.nibe.readCmdsPort);
  DEBUG_PRINT_VARS(0, "INCOMING_PORT_WRITECMDS=%d\n", config.nibe.writeCmdsPort);
  DEBUG_PRINT_VARS(0, "SEND_ACK=%s\n", config.nibe.ack.sendAck ? "true" : "false");
  if (config.nibe.ack.sendAck) {
    DEBUG_PRINT_VARS(0, "ACK_MODBUS40=%s\n", config.nibe.ack.modbus40 ? "true" : "false");
    DEBUG_PRINT_VARS(0, "ACK_SMS40=%s\n", config.nibe.ack.sms40 ? "true" : "false");
    DEBUG_PRINT_VARS(0, "ACK_RMU40=%s\n", config.nibe.ack.rmu40 ? "true" : "false");
  }
  #endif
  DEBUG_PRINT_VARS(0, "VERBOSE_LEVEL=%d\n", config.debug.verboseLevel);
  
  #if defined(ENABLE_DEBUG) && defined(ENABLE_REMOTE_DEBUG)
    DEBUG_PRINT_MSG(0, "REMOTE_DEBUG_ENABLED=true\n");
  #else
    DEBUG_PRINT_MSG(0, "REMOTE_DEBUG_ENABLED=false\n");
  #endif
  
  #if defined(PRODINO_BOARD_ESP32) && defined(ENABLE_DYNAMIC_CONFIG)
    DEBUG_PRINT_MSG(0, "DYNAMIC_CONFIG_ENABLED=true\n");
  #else
    DEBUG_PRINT_MSG(0, "DYNAMIC_CONFIG_ENABLED=false\n");
  #endif
}

#if defined(ENABLE_DEBUG) && defined(ENABLE_REMOTE_DEBUG)
void handleTelnet() {
  EthernetClient client = telnet.available();
  
  if (client) {
    char c = client.read();

    switch (c) {

      case '?':
      case 'h':
        client.println(config.boardName.c_str());
        client.println("Commands:");
        client.println(" E -> exit");
        client.println(" i -> info");
        #ifdef ENABLE_DEBUG
        client.println(" 1 -> set verbose level to 1");
        client.println(" 2 -> set verbose level to 2");
        client.println(" 3 -> set verbose level to 3");
        client.println(" 4 -> set verbose level to 4");
        client.println(" 5 -> set verbose level to 5");
        #endif
        break;
        
      case 'i':
        printInfo();
        break;

      case 'E':
        client.println("Connection closed");
        client.flush();
        client.stop();
        break;

      #ifdef ENABLE_DEBUG
      case '1':
      case '2':
      case '3':
      case '4':
      case '5': 
        client.print("Setting verbose level to ");
        client.println(c);
        config.debug.verboseLevel = c - 0x30;
        break;
      #endif

      case '\n':
      case '\r':
        break;

      default:
        client.print("Unknown command ");
        client.println(c);
    }
  }
}
#endif
