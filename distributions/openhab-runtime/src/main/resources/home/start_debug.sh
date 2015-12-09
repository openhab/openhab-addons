#!/bin/sh

DIRNAME=`dirname "$0"`
exec "${DIRNAME}/start.sh" debug "${@}"