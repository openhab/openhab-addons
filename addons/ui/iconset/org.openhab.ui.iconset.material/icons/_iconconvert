#!/bin/sh

for file in "${1}"*.svg; do

  if [[ "${file}" != 'none'* ]]; then

    png=`echo "${file}" | sed s/.svg/.png/`

    # format SVG file
    if type xmllint &>/dev/null; then
      xmllint "${file}" -o "${file}" --pretty 1
    else
      echo "error: xmllint not installed" 2>1
      exit 1
    fi

    # convert SVG file to PNG
    if [ ! -f "${png}" ]; then

      if type rsvg-convert &>/dev/null; then

        # dependency: librsvg
        echo "rsvg-convert: converting ${file}"
        rsvg-convert "${file}" -o "${png}"

      elif type svgexport &>/dev/null; then

        # dependency: svgexport (via npm)
        echo "svgexport: converting ${file}"
        svgexport "${file}" "${png}"

      else
        echo "error: no SVG converter installed" 2>1
        exit 1
      fi

    fi

  fi
done
