#!/bin/csh
set OT_INSTALL_DIR = /jac_sw/orac3
set OMP_DIR = /jac_sw/omp/QT

set CLASSPATH = ${OT_INSTALL_DIR}/install/tools/xercesImpl.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/install/tools/xmlParserAPIs.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/install/lib/gemini.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/install/lib/orac.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/install/lib/ot.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/install/lib/omp.jar
set CLASSPATH = ${CLASSPATH}:${OMP_DIR}/lib/jsky.jar
set CLASSPATH = ${CLASSPATH}:${OMP_DIR}/lib/qt.jar

set telescope = ukirt
set instConfigDir = ${OT_INSTALL_DIR}/install/cfg/ot/ukirt

java -ms10m -mx500m -oss10m -DOMP -Dtelescope=${telescope} \
    -Dot.cfgdir=${instConfigDir}/ \
    -Dot.resource.cfgdir=${instConfigDir}/ \
    -DqtConfig=${OMP_DIR}/config/qtSystem.conf.${telescope} \
    -cp ${CLASSPATH}:${instConfigDir} \
    edu.jach.qt.utils.UkirtTranslator \
    $argv[1-]

