#!/local/bin/tcsh

# /********************************************************************************
#  * CruiseControl, a Continuous Integration Toolkit                              *
#  * Copyright (C) 2001  ThoughtWorks, Inc.                                       *
#  * 651 W Washington Ave. Suite 500                                              *
#  * Chicago, IL 60661 USA                                                        *
#  *                                                                              *
#  * This program is free software; you can redistribute it and/or                *
#  * modify it under the terms of the GNU General Public License                  *
#  * as published by the Free Software Foundation; either version 2               *
#  * of the License, or (at your option) any later version.                       *
#  *                                                                              *
#  * This program is distributed in the hope that it will be useful,              *
#  * but WITHOUT ANY WARRANTY; without even the implied warranty of               *
#  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                *
#  * GNU General Public License for more details.                                 *
#  *                                                                              *
#  * You should have received a copy of the GNU General Public License            *
#  * along with this program; if not, write to the Free Software                  *
#  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.  *
#  ********************************************************************************/

set ccdir=/home/mrippa/local/cc
set libdir=$ccdir/lib
set qtdir=$ccdir/QT

#set cruise_path=${JAVA_HOME}/lib/tools.jar:$ccdir/dist/cruisecontrol.jar:$libdir/ant.jar:$libdir/xerces.jar:$libdir/mail.jar:$libdir/optional.jar:$libdir/junit.jar:$libdir/activation.jar:$qtdir:.

set cruise_path=${CLASSPATH}

echo ${JAVA_HOME}

set xcruise="java -cp $cruise_path net.sourceforge.cruisecontrol.MasterBuild $*"
echo $xcruise

$xcruise >>&! /home/mrippa/tmp/logs/cc &

