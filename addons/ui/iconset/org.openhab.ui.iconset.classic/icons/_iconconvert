#!/usr/bin/env bash

set -e

for file in "${1}"*.svg; do
  if [[ "${file}" != 'none'* ]]; then
    png="${file%.svg}.png"

    # Clean up SVG file.
    svgcleaner --remove-declarations=false --indent=2 "${file}" "${file}"

    # Convert SVG file to PNG.
    if [ ! -f "${png}" ]; then
      rsvg-convert "${file}" -o "${png}"
    fi

    # Losslessly minify PNG file.
    while pngout "${png}" -kbKGD; do
      : # Minify until maximum compression is reached.
    done
  fi
done
