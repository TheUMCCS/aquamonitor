#!/bin/sh
java -Djava.ext.dirs=/usr/local/tomcat/webapps/goma/WEB-INF/lib:/usr/local/tomcat/lib -classpath ".:/usr/local/tomcat/lib/postgresql-9.1-901.jdbc4.jar:/usr/local/tomcat/webapps/goma/WEB-INF/classes" edu.miami.ccs.goma.ApprovalReminder
