/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

//LoRa Receiver
Shelly.addEventHandler(function (event) {
  if (
    !event ||
    event.name !== 'lora' ||
    event.id !== 100 ||
    !event.info ||
    !event.info.data
  ) {
    return;
  }

  console.log(
    'LoRa Event:', JSON.stringify({info: event.info, }));

  let message = atob(event.info.data);
  console.log('Received message:', message);

  Shelly.emitEvent("oh-lora.data", event);
});
