#!/bin/sh
set -e

if [ "$(id -u)" = "0" ]; then
  # /app/uploads is bind-mounted from the host in production. A fresh or
  # restored host directory won't carry the ownership baked into the image
  # at build time, so fix the mount point here before dropping privileges.
  # Only the mount point itself needs this - files and directories the app
  # creates from here on inherit correct ownership automatically, since it
  # runs as the app user from this point onward.
  [ -d /app/uploads ] && chown app:app /app/uploads
  exec setpriv --reuid=app --regid=app --init-groups java $JAVA_OPTS -jar /app/app.jar
else
  exec java $JAVA_OPTS -jar /app/app.jar
fi
