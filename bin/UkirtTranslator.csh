#!/bin/csh
set OT_INSTALL_DIR = /jac_sw/ot_michelle
set OMP_DIR = /jac_sw/qt_michelle

set CLASSPATH = ${OT_INSTALL_DIR}/ORAC/tools/xercesImpl.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/ORAC/tools/xmlParserAPIs.jar
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/OT/install/classes
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/GEMINI/install/classes
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/ORAC/install/classes
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/OMP/install/classes
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/ODB/install/classes
set CLASSPATH = ${CLASSPATH}:${OT_INSTALL_DIR}/OT/install/classes
set CLASSPATH = ${CLASSPATH}:${OMP_DIR}/install/lib/jsky.jar
set CLASSPATH = ${CLASSPATH}:${OMP_DIR}/install/lib/qt.jar

set telescope = ukirt
set instConfigDir = ${OT_INSTALL_DIR}/OT/install/cfg/ukirt

# -Dot.resource.cfgdir=${instConfigDir}/ \

java -ms10m -mx500m -oss10m -DOMP -Dtelescope=${telescope} \
    -Dot.cfgdir=${instConfigDir}/ \
    -Dot.resource.cfgdir=./ \
    -DqtConfig=${OMP_DIR}/config/qtSystem.conf.${telescope} \
    -DOMP \
    -cp ${CLASSPATH}:${instConfigDir} \
    edu.jach.qt.utils.UkirtTranslator \
    $argv[1-]

