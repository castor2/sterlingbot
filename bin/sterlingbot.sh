#!/bin/bash

export GC_OPT="0server -Xms2g -Xmx2g -XX:NewSize=768m -XX:MaxNewSize=768m -XX:+DisableExplicitGC -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m"
export JAVA_OPTS="$GC_OPT -Dnetworkaddress.cache.ttl=0 -Dnetworkaddress.cache.negative.ttl=0 -Dsun.net.inetaddr.ttl=0"

printUsage()
{
    echo "Usage: $0 [dev|stg|prod] [start|stop]"
    exit 0
}

if [ $# -lt 1 ]
then
    printUsage $*
fi

case $1 in
    dev)
        APP_HOME=/home/castor/apps/sterlingbot
	JVM_OPTS="-Xms1g -Xmx1g"
	APP_CONFIG=$APP_HOME/conf/application_dev.properties
	;;
    stg)
	;;
    prod)
	;;
    *)
        echo env parameter error
	;;
esac

case "$2" in
    start)
        nohup java $JAVA_OPTS -jar $APP_HOME/sterlingbot.jar --spring.config.location=$APP_CONFIG 1> /dev/null 2>&1 & 
	echo -n $! > $APP_HOME/bin/sterlingbot.pid
	;;
    stop)
        echo ['cat $APP_HOME/bin/sterlingbot.pid'] stop 
	kill -9 'cat $APP_HOME/bin/sterlingbot.pid'
        ;; 
    *)
        printUsage $*
esac
