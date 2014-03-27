#!/bin/csh
set OT_INSTALL_DIR = /jac_sw/orac3
set OMP_DIR = /jac_sw/omp/QT

set CLASSPATH = ${OT_INSTALL_DIR}/install/tools/xercesImpl.jar:${OT_INSTALL_DIR}/install/tools/xmlParserAPIs.jar:${OT_INSTALL_DIR}/install/lib/gemini.jar:${OT_INSTALL_DIR}/install/lib/orac.jar:${OT_INSTALL_DIR}/install/lib/ot.jar:${OT_INSTALL_DIR}/install/lib/omp.jar:${OMP_DIR}/lib/jsky.jar:${OMP_DIR}/lib/qt.jar

set telescope = ukirt
set instConfigDir = ${OT_INSTALL_DIR}/install/cfg/ot/ukirt

java -ms10m -mx500m -oss10m -DOMP -Dtelescope=${telescope} \
    -Dot.cfgdir=${instConfigDir}/ \
    -Dot.resource.cfgdir=${instConfigDir}/ \
    -DqtConfig=${OMP_DIR}/config/qtSystem.conf.${telescope} \
    -cp ${CLASSPATH}:${instConfigDir} \
    edu.jach.qt.utils.UkirtTranslator \
    $argv[1-]

