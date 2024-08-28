#!/bin/bash
#
# COPYRIGHT Ericsson 2023-2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#


CUR_DIR=$(pwd)

adp_log() {
  local msg="$(echo "$@" | sed 's|"|\\"|g' | tr -d '\n')"
  printf '{"timestamp":"%s", "version":"1.0.0", "severity":"debug", "service_id":"%s", "message":"%s"}\n' \
    "$(date --iso-8601=seconds)" "$SERVICE_ID" "$msg"
}

## Add KMS CA CERT if it exist into existing keyStore
if [[ -f  $KMS_CACERT_FILE ]];
then
  mkdir /tmp/individualCerts && cd $_
  FILE_COUNT=$(csplit -f individual- $KMS_CACERT_FILE  '/-----BEGIN CERTIFICATE-----/' '{*}' --elide-empty-files | wc -l)
  echo "Number of certs for KMS in ${KMS_CACERT_FILE} bundle is ${FILE_COUNT}"
  for CAFILE in $(ls)
  do
    adp_log "Adding siptlsca-${CAFILE} to java keystore ${DEFAULT_JAVA_CACERTS}"
    OUTPUT="$(keytool -storepass $JAVA_KEYSTORE_PW -noprompt -trustcacerts -importcert \
                      -file $CAFILE -alias "siptlsca-${CAFILE}" -keystore $DEFAULT_JAVA_CACERTS 2>&1)" || {
      adp_log "keytool error: $OUTPUT"
      exit 1
    }
    adp_log "keytool: $OUTPUT"
  done
  cd $CUR_DIR && rm -rf /tmp/individualCerts
fi

java $JAVA_OPTS \
     -Djava.security.egd=file:/dev/./urandom \
     -Dspring.config.additional-location=optional:$SPRING_CONFIG_CUSTOM_LOCATIONS \
     -jar /eric-esoa-key-management-agent.jar