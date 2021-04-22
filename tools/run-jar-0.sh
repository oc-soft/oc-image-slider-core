# !/bin/env bash
# vi: se ts=2 sw=2 et:

SOURCE=${BASH_SOURCE[0]}

while [ -h ${SOURCE} ]; do
  TARGET=$(readlink ${SOURCE})
  if [[ $TARGET == /* ]]; then
    SOURCE=${TARGET}
  else
    DIR=$(dirname $SOURCE)
    SOURCE=${DIR}/${TARGET}
  fi
done
JAR_ROOT_DIR=$(cd $(dirname ${SOURCE}) >/dev/null 2>&1 && pwd)

