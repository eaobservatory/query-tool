#!/bin/csh
set OT_INSTALL_DIR = /jac_sw/orac3
set OMP_DIR = /jac_sw/omp/QT

set CLASSPATH = ${OT_INSTALL_DIR}/tools/xercesImpl.jar:${OT_INSTALL_DIR}/tools/xmlParserAPIs.jar:${OT_INSTALL_DIR}/GEMINI/install/classes:${OT_INSTALL_DIR}/ORAC/install/classes:${OT_INSTALL_DIR}/OT/install/classes/:${OT_INSTALL_DIR}/OMP/install/classes/:${OT_INSTALL_DIR}/ODB/install/classes/:${OT_INSTALL_DIR}/OT/tools/jsky.jar:.:${OT_INSTALL_DIR}/OT/install/cfg/ukirt:${OMP_DIR}/lib/qt.jar

set telescope = ukirt
set instConfigDir = ${OT_INSTALL_DIR}/OT/install/cfg/ukirt

java -ms10m -mx500m -oss10m -DOMP -Dtelescope=${telescope} -Dot.cfgdir=${instConfigDir}/ -Dot.resource.cfgdir=./ -cp ${CLASSPATH}:${instConfigDir} edu.jach.qt.utils.UkirtTranslator $argv[1-]

