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
 * Frame format:
 * +----+------+------+-----+-----+----+----+-----+
 * | 5C | ADDR | ADDR | CMD | LEN |  DATA   | CHK |
 * +----+------+------+-----+-----+----+----+-----+
 *
 *      |------------ CHK ------------------|
 *
 *  Address: 
 *    0x0016 = SMS40
 *    0x0019 = RMU40
 *    0x0020 = MODBUS40
 * 
 *  Checksum: XOR
 *
 * When valid data is received (checksum ok),
 *  ACK (0x06) should be sent to the heat pump.
 * When checksum mismatch,
 *  NAK (0x15) should be sent to the heat pump.
 *
 * Author: pauli.anttila@gmail.com
 *
 */
#ifndef NibeGw_h
#define NibeGw_h

#include <Arduino.h>

#define HARDWARE_SERIAL_WITH_PINS
//#define HARDWARE_SERIAL

//#define ENABLE_NIBE_DEBUG

// state machine states
enum eState
{
  STATE_WAIT_START,
  STATE_WAIT_DATA,
  STATE_OK_MESSAGE_RECEIVED,
  STATE_CRC_FAILURE,
};

enum eTokenType
{
  READ_TOKEN,
  WRITE_TOKEN
};
    
// message buffer for RS-485 communication. Max message length is 80 bytes + 6 bytes header
#define MAX_DATA_LEN 128

#define NIBE_CALLBACK_MSG_RECEIVED void (*callback_msg_received)(const byte* const data, int len)
#define NIBE_CALLBACK_MSG_RECEIVED_TOKEN int (*callback_msg_token_received)(eTokenType token, byte* data)

#ifdef ENABLE_NIBE_DEBUG
#define NIBE_CALLBACK_MSG_RECEIVED_DEBUG void (*debug)(byte verbose, char* data)
#endif

#define SMS40     0x16
#define RMU40     0x19
#define MODBUS40  0x20

class NibeGw
{
  private:
    eState state;
    boolean connectionState;
    byte directionPin;
    byte buffer[MAX_DATA_LEN];
    byte index;
    #if defined(HARDWARE_SERIAL_WITH_PINS)
      HardwareSerial* RS485;
      int RS485RxPin;
      int RS485TxPin;
    #elif defined(HARDWARE_SERIAL)
      HardwareSerial* RS485;
    #else
      Serial_* RS485;
    #endif
    NIBE_CALLBACK_MSG_RECEIVED;
    NIBE_CALLBACK_MSG_RECEIVED_TOKEN;
    byte verbose;
    boolean ackModbus40;
    boolean ackSms40;
    boolean ackRmu40;
    boolean sendAcknowledge;

    int checkNibeMessage(const byte* const data, byte len);
    void sendData(const byte* const data, byte len);
    void sendAck();
    void sendNak();
    boolean shouldAckNakSend(byte address);

    #ifdef ENABLE_NIBE_DEBUG
    NIBE_CALLBACK_MSG_RECEIVED_DEBUG;
    char debug_buf[100];
    #endif

  public:
    #if defined(HARDWARE_SERIAL_WITH_PINS)
      NibeGw(HardwareSerial* serial, int RS485DirectionPin, int RS485RxPin, int RS485TxPin);
    #elif defined(HARDWARE_SERIAL)
      NibeGw(HardwareSerial* serial, int RS485DirectionPin);
    #else
      NibeGw(Serial_* serial, int RS485DirectionPin);
    #endif
    NibeGw& setCallback(NIBE_CALLBACK_MSG_RECEIVED, NIBE_CALLBACK_MSG_RECEIVED_TOKEN);

    #ifdef ENABLE_NIBE_DEBUG
    NibeGw& setDebugCallback(NIBE_CALLBACK_MSG_RECEIVED_DEBUG);
    #endif

    void connect();
    void disconnect();
    boolean connected();
    void setVerboseLevel(byte level);
    boolean messageStillOnProgress();
    void loop();

    void setAckModbus40Address(boolean val);
    void setAckSms40Address(boolean val);
    void setAckRmu40Address(boolean val);
    void setSendAcknowledge(boolean val);
};

#endif
