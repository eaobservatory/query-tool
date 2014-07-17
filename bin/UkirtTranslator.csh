#!/bin/csh
set OT_INSTALL_DIR = /jac_sw/orac3
set OMP_DIR = /jac_sw/omp/QT

set CLASSPATH = ${OT_INSTALL_DIR}/output/lib/xercesImpl.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/output/lib/xmlParserAPIs.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/output/lib/gemini.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/output/lib/orac.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/output/lib/ot.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/output/lib/omp.jar
set CLASSPATH = ${CLASSPATH}:${OMP_DIR}/lib/jsky.jar
set CLASSPATH = ${CLASSPATH}:${OMP_DIR}/lib/qt.jar

set telescope = ukirt
set instConfigDir = ${OT_INSTALL_DIR}/output/cfg/ot/ukirt

java -ms10m -mx500m -oss10m -DOMP -Dtelescope=${telescope} \
    -Dot.cfgdir=${instConfigDir}/ \
    -Dot.resource.cfgdir=${instConfigDir}/ \
    -DqtConfig=${OMP_DIR}/config/qtSystem.conf.${telescope} \
    -cp ${CLASSPATH}:${instConfigDir} \
    edu.jach.qt.utils.UkirtTranslator \
    $argv[1-]

