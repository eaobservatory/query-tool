ifdef TOP
DEV_ROOT = ${TOP}/.
else
DEV_ROOT = .
endif

JAVAPACKAGES = \
	edu/jach/qt/utils \
	edu/jach/qt/gui \
	edu/jach/qt/djava \
	edu/jach/qt/app

JAVAMAIN = edu.jach.qt/app/QT

JAVALIB = qt.jar
JAVASRCLIB = qt-src.jar

JAVALIBRARIES = \
	activation.jar \
	diva.jar \
	jfcunit.jar \
	jsky.jar \
	junit.jar \
	soap.jar \
	mail.jar \
	optional.jar \
	xercesImpl.jar \
	xmlParserAPIs.jar \
	calpahtml.jar

EXTERNALCLASSES = \
	/local/java/jdk/jre/lib/ext/log4j-1.2rc1.jar \
	/jac_sw/orac3/GEMINI/install/classes \
	/jac_sw/orac3/ORAC/install/classes \
	/jac_sw/drama/CurrentRelease/javalib \
	/jac_sw/orac3/OT/install/classes \
	/jac_sw/itsroot/install/dcHub/javalib/dcHub.jar

ifdef SOURCES
include ../../../../../../make.tail
else
include make.tail
endif