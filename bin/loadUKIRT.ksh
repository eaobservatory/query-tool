#!/bin/sh
#
case $1 in
    *.xml)
# We are using the queue functionality
        echo "Sending file to queue";
        ditscmd OCSQUEUE ADDBACK $1;;
    * )
# We are talking directly to the instrument task
        echo "Sending file to inst task";
        ditscmd UKIRT_INST LOAD $1;;
esac
exit $?
