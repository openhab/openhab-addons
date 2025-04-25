#!/usr/bin/env python3
"""
Downloads current Argo Ulisse firmware binary files from manufacturer's servers
"""

__license__ = """This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0

SPDX-License-Identifier: EPL-2.0
"""

import hashlib
import secrets
import urllib.request
from enum import Enum
from itertools import cycle


# Randomized values. Do not seem to impact the downloaded file
USERNAME = secrets.token_hex(4)
PASSWORD_MD5 = hashlib.md5(secrets.token_hex(4).encode('ASCII')).hexdigest()
CPU_ID = secrets.token_hex(8)


class FwType(str, Enum):
    UNIT = 'OU_FW'
    WIFI = 'UI_FW'


def get_uri(fw_type: FwType, page: int):
    return f'http://31.14.128.210/UI/UI.php?CM={fw_type}&PK={page}&USN={USERNAME}&PSW={PASSWORD_MD5}&CPU_ID={CPU_ID}'


def get_api_response(fw_type: FwType, page: int):
    with urllib.request.urlopen(get_uri(fw_type, page)) as response:
        data: str = response.read().decode().rstrip()
        if not data.endswith('|||'):
            raise RuntimeError(f"Invalid upstream response {data}")
        return {e.split('=')[0]: str.join("=", e.split('=')[1:]) for e in data[:-3].split('|')}


def download_fw_from_remote_server(fw_type: FwType, split_into_multiple_files=False):
    print(f'> {get_uri(fw_type, -1)}...')
    ver_response = get_api_response(fw_type, -1)
    try:
        size = int(ver_response['SIZE'])
        chunk_count = int(ver_response['NUM_PACK'])
        checksum = int(ver_response['CKS'])  # CRC-16?
        base_offset = int(ver_response['OFFSET'])
        print(f'FW Version: {ver_response}\n\tRelease: {ver_response["RELEASE"]}\n\tSize: {size}'
              f'\n\t#chunks: {chunk_count}\n\tchecksum: {checksum}')

        total_received_size = 0
        data = ""
        current_offset = base_offset
        for i in range(0, chunk_count):
            chunk_response = get_api_response(fw_type, i)
            current_chunk_size_bytes = int(chunk_response['SIZE'])
            print(f'{fw_type} chunk [{i+1}/{chunk_count}] - Response: {chunk_response}')

            response_offset = int(chunk_response['OFFSET'])
            if response_offset != current_offset:
                if not split_into_multiple_files:
                    difference = response_offset - current_offset
                    print(f"Current offset is {current_offset}, but the response wants to write to {response_offset}."
                          f" Padding with 0xDEADBEEF")
                    fillers = cycle(['DE', 'AD', 'BE', 'EF'])
                    for x in range(0, difference):
                        data += next(fillers)
                    current_offset += difference
                else:
                    save_to_file(base_offset, data, fw_type, total_received_size, ver_response["RELEASE"])
                    total_received_size = 0
                    data = ""
                    current_offset = response_offset
                    base_offset = response_offset
            total_received_size += current_chunk_size_bytes
            current_offset += current_chunk_size_bytes
            data += chunk_response['DATA'][:current_chunk_size_bytes*2]

        save_to_file(base_offset, data, fw_type, total_received_size, ver_response["RELEASE"])

    finally:
        finish_response = get_api_response(fw_type, 256)
        print(finish_response)


def save_to_file(base_offset, data, fw_type, total_received_size, version):
    print()
    print('-' * 50)
    print(f'Received {total_received_size} bytes. Total binary size: {len(data) / 2:.0f}[b]')
    print(f'Data (base16):\n\t{data}\n')
    fw_binary = bytes.fromhex(data)
    filename = f'Argo_firmware_{fw_type}_v{version}__offset_0x{base_offset:X}.bin'
    with open(filename, "wb") as output_file:
        output_file.write(fw_binary)
    print(f'Firmware written to {filename}')


if __name__ == '__main__':
    print(f'Username={USERNAME}, Password={PASSWORD_MD5}, CPU_ID={CPU_ID}')
    download_fw_from_remote_server(fw_type=FwType.UNIT, split_into_multiple_files=False)
    download_fw_from_remote_server(fw_type=FwType.WIFI, split_into_multiple_files=False)
