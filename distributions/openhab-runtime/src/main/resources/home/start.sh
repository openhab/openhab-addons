#!/bin/sh

DIRNAME=`dirname "$0"`
exec "${DIRNAME}/runtime/karaf/bin/karaf" "${@}"
