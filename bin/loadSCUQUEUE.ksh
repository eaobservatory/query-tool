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

ditscmd SCUQUEUE LOADQ $1

exit $?
