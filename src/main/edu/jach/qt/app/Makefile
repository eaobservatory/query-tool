BUILD     = ../../../../../../..
include	    $(BUILD)/makeInclude

PACKAGE	    = $(QT_PACKAGE)/app

#### Targets settings

SOURCES = \
	Subject.java \
	Observer.java \
	Querytool.java \
	QT.java\
	NoSuchParameterException.java

include $(BUILD)/make.tail

# Phony Targets
.PHONY: clean build help

#### Help
help:
	@echo "Usage: make [targets...]"
	@echo ""
	@echo "where targets include:"
	@echo ""
	@echo "  help           display this help"
	@echo "  all            compile all (default)"
	@echo "  clean          remove all class files"
	@echo "  build          rebuild all inconditionnally"
	@echo "  <class file>   compile the given file"
	@echo "  <subdir>.make  compile the given subdir"
	@echo "  <subdir>.clean clean the given subdir"

