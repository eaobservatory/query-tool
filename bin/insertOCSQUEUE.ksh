#!/bin/sh
case $1 in
   *.xml)
       ditscmd OCSQUEUE ADDBACK $1;;
   * )
       ditscmd UKIRT_INST LOAD $1;;
esac
exit $?
