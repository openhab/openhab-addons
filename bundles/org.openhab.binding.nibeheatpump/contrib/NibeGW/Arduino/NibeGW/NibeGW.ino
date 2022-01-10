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
 *  14.3.2021   v3.01   Fix Prodino build + fixed UDP issue + debug improvements.
 */

// ######### CONFIGURATION #######################

#define VERSION                 "3.01"

// Enable if you use ProDiNo board
// Have been tested with KMPDinoEthernet v1.6.1 (https://github.com/kmpelectronics/Arduino/tree/master/KMPDinoEthernet/Releases)
//#define PRODINO_BOARD

// Enable if ENC28J60 LAN module is used
//#define TRANSPORT_ETH_ENC28J60

// Enable if you use STM32 NUCLEO-F429ZI
//#define STM32_F429ZI_BOARD


// Enable debug printouts
//#define ENABLE_DEBUG

// Enable UDP debug printouts, listen printouts e.g. via netcat (nc -l -u 50000)
//#define ENABLE_UDP_DEBUG

#define VERBOSE_LEVEL           1

#define BOARD_NAME              "Arduino NibeGW"
#define BOARD_MAC               { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED }
#define BOARD_IP                { 192, 168, 1, 50 }
#define GATEWAY_IP              { 192, 168, 1, 1 }
#define NETWORK_MASK            { 255, 255, 255, 0 }
#define INCOMING_PORT_READCMDS  9999
#define INCOMING_PORT_WRITECMDS 10000

#define TARGET_IP               192, 168, 1, 19
#define TARGET_PORT             9999
#define TARGET_DEBUG_PORT       50000

// Delay before initialize ethernet on startup in seconds
#define ETH_INIT_DELAY          5

// Used serial port and direction change pin for RS-485 port
// Note! Select if Serial is SW or HW serial port in NibeGw.h
#ifdef PRODINO_BOARD
 #define RS485_PORT              Serial1
 #define RS485_DIRECTION_PIN     3
#elif defined STM32_F429ZI_BOARD
 #include <HardwareSerial.h>
 HardwareSerial Serial1(PG9,PG14);
 #define RS485_PORT              Serial1
 #define RS485_DIRECTION_PIN     PF15
#else
 #define RS485_PORT              Serial
 #define RS485_DIRECTION_PIN     2
#endif

#define ACK_MODBUS40            true
#define ACK_SMS40               false
#define ACK_RMU40               false
#define SEND_ACK                true

#define DEBUG_BUFFER_SIZE       80

// ######### INCLUDES #######################

#ifdef TRANSPORT_ETH_ENC28J60
 #include <UIPEthernet.h>
#elif defined STM32_F429ZI_BOARD
 #include <LwIP.h>
 #include <STM32Ethernet.h>
 #include <EthernetUdp.h>
#elif defined PRODINO_BOARD
 #include <SPI.h>
 #include "KmpDinoEthernet.h"
 #include "KMPCommon.h"
 #include "Ethernet/utility/w5100.h"
#else
 #include <SPI.h>
 #include <Ethernet.h>
 #include <EthernetUdp.h>
#endif

#ifdef STM32_F429ZI_BOARD
 #include <IWatchdog.h>
#else
 #include <avr/wdt.h>
#endif

#include "NibeGw.h"

// ######### VARIABLES #######################

// The media access control (ethernet hardware) address for the shield
byte mac[] = BOARD_MAC;

//The IP address for the shield
byte ip[] = BOARD_IP;

//The IP address of the gateway
byte gw[] = GATEWAY_IP;

//The network mask
byte mask[] = NETWORK_MASK;

boolean ethernetInitialized = false;

// Target IP address and port where Nibe UDP packets are send
IPAddress targetIp(TARGET_IP);
EthernetUDP udp;
EthernetUDP udp4writeCmnds;

NibeGw nibegw(&RS485_PORT, RS485_DIRECTION_PIN);

// ######### DEBUG #######################

#define DEBUG_BUFFER_SIZE       80

#ifdef ENABLE_DEBUG
 #define DEBUG_PRINT(level, message) if (verbose >= level) { debugPrint(message); }
 #define DEBUG_PRINTDATA(level, message, data) if (verbose >= level) { sprintf(debugBuf, message, data); debugPrint(debugBuf); }
 #define DEBUG_PRINTARRAY(level, data, len) if (verbose >= level) { for (int i = 0; i < len; i++) { sprintf(debugBuf, "%02X", data[i]); debugPrint(debugBuf); }}
#else
 #define DEBUG_PRINT(level, message)
 #define DEBUG_PRINTDATA(level, message, data)
 #define DEBUG_PRINTARRAY(level, data, len)
#endif

#ifdef ENABLE_DEBUG
char verbose = VERBOSE_LEVEL;
char debugBuf[DEBUG_BUFFER_SIZE];

void debugPrint(char* data)
{
#ifdef ENABLE_UDP_DEBUG
  if (ethernetInitialized)
  {
    udp.beginPacket(targetIp, TARGET_DEBUG_PORT);
    udp.write(data);
    udp.endPacket();
  }
#endif

#ifdef PRODINO_BOARD
  Serial.print(data);
#endif
}
#endif

// ######### FUNCTION DEFINITION ######################

void nibeCallbackMsgReceived(const byte* const data, int len);
int nibeCallbackTokenReceived(eTokenType token, byte* data);
void sendUdpPacket(const byte * const data, int len);
void initializeEthernet();


// ######### SETUP #######################

void setup()
{
  // Start watchdog
#ifdef STM32_F429ZI_BOARD
  IWatchdog.begin(2000000); // 2 sec
#else
  wdt_enable (WDTO_2S);
#endif

  nibegw.setCallback(nibeCallbackMsgReceived, nibeCallbackTokenReceived);
  nibegw.setAckModbus40Address(ACK_MODBUS40);
  nibegw.setAckSms40Address(ACK_SMS40);
  nibegw.setAckRmu40Address(ACK_RMU40);
  nibegw.setSendAcknowledge(SEND_ACK);

#ifdef ENABLE_NIBE_DEBUG
  nibegw.setDebugCallback(nibeDebugCallback);
  nibegw.setVerboseLevel(VERBOSE_LEVEL);
#endif

#ifdef PRODINO_BOARD
  DinoInit();
  Serial.begin(115200, SERIAL_8N1);
#endif

  DEBUG_PRINTDATA(0, "%s ", BOARD_NAME);
  DEBUG_PRINTDATA(0, "version %s\n", VERSION);
  DEBUG_PRINT(0, "Started\n");
}

// ######### MAIN LOOP #######################

void loop()
{
#ifdef STM32_F429ZI_BOARD
  IWatchdog.reload();
#else
  wdt_reset();
#endif

  long now = millis() / 1000;

  if (!nibegw.connected())
  {
    nibegw.connect();
  }
  else
  {
    do
    {
      nibegw.loop();
#ifdef TRANSPORT_ETH_ENC28J60
      Ethernet.maintain();
#endif
    } while (nibegw.messageStillOnProgress());
  }

  if (!ethernetInitialized && now >= ETH_INIT_DELAY)
  {
    initializeEthernet();
  }
}

// ######### FUNCTIONS #######################

void initializeEthernet()
{
  DEBUG_PRINT(1, "Initializing Ethernet\n");
  Ethernet.begin(mac, ip, gw, mask);

#ifdef PRODINO_BOARD
  W5100.setRetransmissionCount(1);
#endif

  ethernetInitialized = true;
  udp.begin(INCOMING_PORT_READCMDS); 
  udp4writeCmnds.begin(INCOMING_PORT_WRITECMDS);

#ifdef ENABLE_DEBUG
  DEBUG_PRINTDATA(0, "%s ", BOARD_NAME);
  DEBUG_PRINTDATA(0, "version %s\n", VERSION);
  sprintf(debugBuf, "MAC=%02X:%02X:%02X:%02X:%02X:%02X\n", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
  DEBUG_PRINT(0, debugBuf);
  sprintf(debugBuf, "IP=%d.%d.%d.%d\n", ip[0], ip[1], ip[2], ip[3]);
  DEBUG_PRINT(0, debugBuf);
  sprintf(debugBuf, "GW=%d.%d.%d.%d\n", gw[0], gw[1], gw[2], gw[3]);
  DEBUG_PRINT(0, debugBuf);
  sprintf(debugBuf, "TARGET IP=%d.%d.%d.%d\n", TARGET_IP);
  DEBUG_PRINT(0, debugBuf);
  DEBUG_PRINTDATA(0, "TARGET PORT=%d\n", TARGET_PORT);
  DEBUG_PRINTDATA(0, "INCOMING_PORT_READCMDS=%d\n", INCOMING_PORT_READCMDS);
  DEBUG_PRINTDATA(0, "INCOMING_PORT_WRITECMDS=%d\n", INCOMING_PORT_WRITECMDS);
  DEBUG_PRINTDATA(0, "TARGET PORT=%d\n", TARGET_PORT);
  DEBUG_PRINTDATA(0, "ACK_MODBUS40=%s\n", ACK_MODBUS40 ? "true" : "false");
  DEBUG_PRINTDATA(0, "ACK_SMS40=%s\n", ACK_SMS40 ? "true" : "false");
  DEBUG_PRINTDATA(0, "ACK_RMU40=%s\n", ACK_RMU40 ? "true" : "false");
  DEBUG_PRINTDATA(0, "SEND_ACK=%s\n", SEND_ACK ? "true" : "false");
  DEBUG_PRINTDATA(0, "ETH_INIT_DELAY=%d\n", ETH_INIT_DELAY);
  DEBUG_PRINTDATA(0, "RS485_DIRECTION_PIN=%d\n", RS485_DIRECTION_PIN);
#endif
}

void nibeCallbackMsgReceived(const byte* const data, int len)
{
  if (ethernetInitialized)
  {
    sendUdpPacket(data, len);
  }
}


int nibeCallbackTokenReceived(eTokenType token, byte* data)
{
  int len = 0;
  if (ethernetInitialized)
  {
    if (token == READ_TOKEN)
    {
      DEBUG_PRINT(3, "Read token received from nibe\n");
      int packetSize = udp.parsePacket();
      if (packetSize) {
        len = udp.read(data, packetSize);
        DEBUG_PRINTDATA(2, "Send read command to nibe, len=%d, ", len);
        DEBUG_PRINT(1, " data in: ");
        DEBUG_PRINTARRAY(1, data, len)
        DEBUG_PRINT(1, "\n");
#ifdef TRANSPORT_ETH_ENC28J60
        udp.flush();
        udp.stop();
        udp.begin(INCOMING_PORT_READCMDS);
#endif
      }
    }
    else if (token == WRITE_TOKEN)
    {
      DEBUG_PRINT(3, "Write token received from nibe\n");
      int packetSize = udp4writeCmnds.parsePacket();
      if (packetSize) {
        len = udp4writeCmnds.read(data, packetSize);
        DEBUG_PRINTDATA(2, "Send write command to nibe, len=%d, ", len);
        DEBUG_PRINT(1, " data in: ");
        DEBUG_PRINTARRAY(1, data, len)
        DEBUG_PRINT(1, "\n");
#ifdef TRANSPORT_ETH_ENC28J60
        udp4writeCmnds.flush();
        udp4writeCmnds.stop();
        udp4writeCmnds.begin(INCOMING_PORT_WRITECMDS);
#endif
      }
    }
  }
  return len;
}

void nibeDebugCallback(byte verbose, char* data)
{
  DEBUG_PRINT(verbose, data);
}

void sendUdpPacket(const byte * const data, int len)
{
#ifdef ENABLE_DEBUG
  sprintf(debugBuf, "Sending UDP packet to %d.%d.%d.%d:", TARGET_IP);
  DEBUG_PRINT(2, debugBuf);
  DEBUG_PRINTDATA(2, "%d", TARGET_PORT);
  DEBUG_PRINTDATA(2, ", len=%d, ", len);
  DEBUG_PRINT(1, "data out: ");
  DEBUG_PRINTARRAY(1, data, len)
  DEBUG_PRINT(1, "\n");
#endif

  udp.beginPacket(targetIp, TARGET_PORT);
  udp.write(data, len);
  int retval = udp.endPacket();
  DEBUG_PRINTDATA(3, "UDP packet sent %s\n", retval == 0 ? "failed" : "succeed");
}
