#!/bin/sh
case $1 in
   *.xml)
       ditscmd OCSQUEUE INSERTQ $1;;
   * )
       ditscmd JCMT_INST LOAD $1;;
esac
exit $?
