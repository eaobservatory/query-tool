#!/bin/ksh

#ditsgetinfo -running SCUQUEUE
#if [[ $? != 0 ]]; then
#    set cwd = $PWD;
#    cd /export/data/dewitt/jcmt/ocs/queue2;
#    echo $PWD;
#    $(perl -Mblib scuqueue &);
#    echo "Queue running";
#    cd $cwd;
#    /export/data/dewitt/jcmt/ocs/queue2/scuqueue
#fi
function domozilla {
    mozilla -remote "ping()"
    if [[ $? != 0 ]]; then
	echo Launching mozilla
	mozilla $1 &
    else
	mozilla -remote "openFILE($1)"
    fi
    sleep 2
    mozilla -remote "ping()"
#    exit $?
}


echo IMP_KEY $IMP_KEY >/tmp/foo

case $1 in
    *.html) domozilla $1;;
    * ) ditscmd SCUQUEUE ADDBACK $1;;
esac

exit $?
