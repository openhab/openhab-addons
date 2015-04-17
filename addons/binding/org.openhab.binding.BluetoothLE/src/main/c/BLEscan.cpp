/**
 *
 *  Copyright (c) 2014-2014, openHAB.org and others.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * ----------------------------------------------------------------------------
 *
 *  This application scans for Bluetooth low energy data and send valid frames
 *  to configurable IP/port address by UDP packets.
 *
 *  frame format (see le_advertising_info):
 *  +----------+-------------+---------+-----+---------+----------+---------+-----+
 *  | evt_type | bdaddr_type | bdaddr0 | ... | bdaddr5 | data_len | data[0] | ... |
 *  +----------+-------------+---------+-----+---------+----------+---------+-----+
 *
 *  Author: Patrick Ammann
 *
 *  03.12.2014	v0.01	Initial version
 *
 */

#include <stdio.h>
#include <errno.h>
#include <ctype.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>

#include <string.h>
#include <string>

#include <time.h>

#include <getopt.h>
#include <signal.h>
#include <arpa/inet.h>
#include <sys/param.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>


static volatile int signal_received = 0;
static volatile int verbose = 0;

static void sigint_handler(int sig) {
  if (verbose) printf("\nExit...caught signal %d\n", sig);
	signal_received = sig;
}

static int setnonblocking(int fd) {
    int flags;
    int err = flags = fcntl(fd, F_GETFL, 0);
    if (err < 0) {
      perror("Can not get flags");
      return err;
    }
    return fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

static time_t getMonotonicTime() {
  struct timespec tp;
  clock_gettime(CLOCK_MONOTONIC, &tp);
  return tp.tv_sec;
}

class BluetoothLE
{
private:
  int m_devID;
  int m_fd = -1;

public:
  BluetoothLE(int devID) {
    this->m_devID = devID;
  }

  int open() {
    int err;

	  uint8_t scan_type = 0x01;
	  uint16_t interval = htobs(0x0010);
	  uint16_t window = htobs(0x0010);
	  uint8_t own_type = 0x00;
	  uint8_t filter_policy = 0x00;

  	if (m_devID < 0) {
  		m_devID = hci_get_route(NULL);
    }
    if (m_devID < 0) {
      return -1;
    }

    if (verbose) printf("Open HCI device: /dev/hci%d\n", m_devID);

    m_fd = hci_open_dev(m_devID);
	  if (m_fd < 0) {
		  if (verbose) printf("Error: Could not open device\n");
      return -1;
	  }

    err = setnonblocking(m_fd);
	  if (err < 0) {
		  perror("Can not set non blocking");
      close();
      return err;
	  }

    err = hci_le_set_scan_parameters(m_fd, scan_type, interval, window, own_type, filter_policy, 10000);
	  if (err < 0) {
		  perror("Set scan parameters failed");
      close();
		  return err;
	  }

    return 0;
  }

  bool isOpen() {
    return m_fd > 0;
  }

  int startScan() {
	  int err;

	  struct hci_filter nf, of;
	  socklen_t olen;

    if (verbose) { printf("startScan...\n"); fflush(0); }

	  err = hci_le_set_scan_enable(m_fd, 0x01, 0 /*duplicates need*/, 10000);
	  if (err < 0) {
		  perror("Enable scan failed");
		  exit(1);
	  }

	  olen = sizeof(of);
	  if (getsockopt(m_fd, SOL_HCI, HCI_FILTER, &of, &olen) < 0) {
		  perror("Error: Could not get socket options");
		  exit(1);
	  }

	  hci_filter_clear(&nf);
	  hci_filter_set_ptype(HCI_EVENT_PKT, &nf);
	  hci_filter_set_event(EVT_LE_META_EVENT, &nf);
	  if (setsockopt(m_fd, SOL_HCI, HCI_FILTER, &nf, sizeof(nf)) < 0) {
		  perror("Error: Could not set socket options");
		  exit(1);
	  }

    return 0;
  }

  void processFrames(int udp_fd, struct sockaddr_in* dest) {
    unsigned char buffer[HCI_MAX_EVENT_SIZE], *ptr;
    ssize_t len = 0;
		evt_le_meta_event *meta;
		le_advertising_info *info;

    while ((len = read(m_fd, buffer, sizeof(buffer))) > 0) {
      ptr = buffer + (1 + HCI_EVENT_HDR_SIZE);
      meta = (evt_le_meta_event *)ptr;

      if (meta->subevent != EVT_LE_ADVERTISING_REPORT) {
        fprintf(stderr, "Not EVT_LE_ADVERTISING_REPORT recveived\n");
        break;
      }
      info = (le_advertising_info *)(meta->data + 1);

      //printf("info->evt_type=%d\n", info->evt_type);
      //fflush(0);

      /* only pass "Non connectable undirected advertising" for now */
      if (info->evt_type != 3)
        continue;
      
      if (verbose) {
        char addr[18];
        unsigned int i;
        ba2str(&info->bdaddr, addr);
        printf("evt_type=%u bdaddr_type=%u bdaddr=%s data_len=%u data:\n",
          (unsigned int)info->evt_type, (unsigned int)info->bdaddr_type, addr, (unsigned int)info->length);
        for (i = 0; i < info->length; i++) {
          printf("%02X ", info->data[i]);
        }
        printf("\n");
        fflush(0);
      }

      const size_t offset = HCI_EVENT_HDR_SIZE + 3;

      if (verbose == 4) {
        unsigned int i;
        for (i = 0; i < len - offset; i++) {
          printf("%02X ", (buffer+offset)[i]);
        }
        printf("\n");
      }

      if (verbose) printf("send UDP packet len=%u\n", (unsigned int)(len - offset));
      if (sendto(udp_fd, buffer + offset, len - offset, 0 , (struct sockaddr*)dest, sizeof(*dest)) == -1) {
        fprintf(stderr, "Failed to send UDP packet: %s\n", strerror(errno));
      }

    } // while

    if (len < 0 && errno == EAGAIN) {
      // If this condition passes, there is no data to be read
      //if (verbose) printf("No data, would block if not in non blocking state\n");
    } else if (len >= 0) {
      // Otherwise, you're good to go and buffer should contain "count" bytes.
      if (verbose) printf("Read return 0\n");
    } else {
        // Some other error occurred during read.
        fprintf(stderr, "Read failed: %s\n", strerror(errno));
        close();
    }
  }

  int stopScan() {
    if (verbose) { printf("stopScan...\n"); fflush(0); }
    int err = hci_le_set_scan_enable(m_fd, 0x00, 1, 10000);
	  if (err < 0) {
		  perror("Error: Disable scan failed");
	  }
    return 0;
  }

  void close() {
	  if (m_fd > 0) {
      hci_close_dev(m_fd);
    }
    m_fd = -1;
  }
};

static struct option lescan_options[] = {
	{ "help",	        no_argument,       0, 'h' },
  { "verbose",      no_argument,       0, 'v' },
  { "device",       required_argument, 0, 'd' },
  { "port",	        required_argument, 0, 'p' },
  { "scanInterval",	required_argument, 0, 'i' },
	{ 0, 0, 0, 0 }
};

static const char *lescan_help =
	"Usage:\n"
  "\t--help               Show help\n"
  "\t--verbose            Verbose\n"
  "\t--port <port>        UDP port (default: 9998)\n"
  "\t--device <id>        Set device by id (default: 0 -> /dev/hci0)\n"
  "\t--scanInterval <id>  Set scan interval time in seconds (default: 60s)\n"
;

int main(int argc, char **argv)
{
	int opt;

  struct sockaddr_in dest;
  const std::string address = "127.0.0.1";
  int port = 9998;
  int dev_id = -1;

/*
  According to the Bluetooth 4.0 core specification the time period of 'advertising events' are as shown below for the different types of 
  advertising packets:
  ADV_IND:         General connectable and scannable advertisement packets' time period ranges from 20 ms to 10.24s in steps of 0.625ms.
  ADV_DIRECT_IND:  Directed advertisement packets' time period is less than or equal to 3.75 ms. This kind of advertisement events can be
                   happen consecutively only for 1.28s. This is for a establishing a quick connection (if there is a device listening).
  ADV_NONCONN_IND: Non-connectable and non-scannable advertisement packets' time period ranges from 100 ms to 10.24s in steps of 0.625ms.
  ADV_SCAN_IND:    Scannable advertisement packets' time period ranges from 100 ms to 10.24s in steps of 0.625ms.
  -> scanWindow must be maximal 11s
*/
  const time_t scanWindow = 11;  // in seconds
  time_t scanInterval = 1 * 60;  // in seconds

  int udp_fd = -1;

  printf("BLEscan v0.2\n");

  while ((opt=getopt_long(argc, argv, "+", lescan_options, NULL)) != -1) {
    switch (opt) {
        case 'v':
          verbose++;
          break;
        case 'd':
          dev_id = atoi(optarg);
          break;
        case 'p':
          port = atoi(optarg);
          break;
        case 't':
          scanInterval = atoi(optarg);
          break;
		  default:
			  printf("%s", lescan_help);
			  exit(1);
		  }
	}

  if (scanInterval < scanWindow) {
    fprintf(stderr, "Invalid scan interval\n");
    exit(1);
  }

  // print info
  printf("HCI device   : /dev/hci%i\n", dev_id);
  printf("UDP dest     : adress=%s port=%u\n", address.c_str(), port);
  printf("Scan window  : %us\n", (unsigned int)scanWindow);
  printf("Scan interval: %us\n", (unsigned int)scanInterval);

  BluetoothLE* ble = new BluetoothLE(dev_id);

  // install signal handler
  struct sigaction sa;
  memset(&sa, 0, sizeof(sa));
  sa.sa_flags = SA_NOCLDSTOP;
  sa.sa_handler = sigint_handler;
  sigaction(SIGINT, &sa, NULL);

  // initialize destination address
  dest.sin_family = AF_INET;
  dest.sin_addr.s_addr = inet_addr(address.c_str());
  dest.sin_port = htons(port);


  enum {MODE_SCAN, MODE_WAIT} mode = MODE_WAIT;
  time_t scanStartTime = getMonotonicTime() - (scanInterval + 1); // force SCAN mode first

  for (;;) {
    /*
     * error tolerant solution:
     * Try to open hci device and udp socket on every round, if open has failed.
     */
    if (!ble->isOpen()) {
      ble->open();
    }
    if (udp_fd < 0) {
        if (verbose) printf("Open UDP socket\n");
        udp_fd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
        if (udp_fd < 0) {
            fprintf(stderr, "Failed to open UDP socket: %s\n", strerror(errno));
        }
    }

    if (ble->isOpen() && udp_fd > 0) {
      time_t currTime = getMonotonicTime();
      if (mode == MODE_WAIT && currTime > scanStartTime + scanInterval) {
        mode = MODE_SCAN;
        scanStartTime = currTime;
        ble->startScan();
      } else
      if (mode == MODE_SCAN && currTime > scanStartTime + scanWindow) {
        mode = MODE_WAIT;
        ble->stopScan();
      }

      ble->processFrames(udp_fd, &dest);
    }

    if (signal_received) {
      if (verbose) printf("signal_received=%d\n", signal_received);
      break;
    }

    sleep(1); // 1s
  } // for

  if (mode == MODE_SCAN) ble->stopScan();
  ble->close();

  return 0;
}

