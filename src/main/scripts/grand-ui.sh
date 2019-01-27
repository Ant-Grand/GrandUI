#!/bin/sh

## resolve links - $0 may be a link
PRG="$0"

# need this for relative symlinks
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done

BASEDIR=`dirname "$PRG"`

ARCH=x86_64
if [ `java -version 2>&1 | grep -c 64` -eq 0 ]; then
    ARCH=x86
fi

if [ `java -version 2>&1 | sed -n 's/.* version "\([^.]*\)\..*".*/\1/p'` -eq 1 ]; then
    java -Djava.ext.dirs=$BASEDIR/lib:$BASEDIR/lib/gtk/$ARCH -jar $BASEDIR/lib/grand-ui.jar
else
    java -p $BASEDIR/lib:$BASEDIR/lib/gtk/$ARCH -m grand.ui
fi
