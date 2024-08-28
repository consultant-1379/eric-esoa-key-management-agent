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

FROM armdocker.rnd.ericsson.se/proj-esoa-so/so-base-openjdk17:1.3.2-1

ARG USER_ID=40514
RUN echo "$USER_ID:!::0:::::" >>/etc/shadow

ARG USER_NAME="eric-esoa-key-management-agent"
RUN echo "$USER_ID:x:$USER_ID:0:An Identity for $USER_NAME:/nonexistent:/bin/false" >>/etc/passwd

ADD target/eric-esoa-key-management-agent.jar eric-esoa-key-management-agent.jar
COPY entrypoint.sh /entrypoint.sh

ENV DEFAULT_JAVA_CACERTS="/usr/lib64/jvm/java-17-openjdk-17/lib/security/cacerts"
ENV JAVA_KEYSTORE_PW="changeit"

RUN chown $USER_ID "/var/lib/ca-certificates/java-cacerts" \
    && chmod +w "/var/lib/ca-certificates/java-cacerts" \
    && chmod +x /entrypoint.sh

USER $USER_ID

LABEL \
    org.opencontainers.image.title=eric-esoa-key-management-agent \
    org.opencontainers.image.vendor=Ericsson \
    com.ericsson.product-revision="R1A" \
    com.ericsson.product-number="unknown"

ENTRYPOINT ["sh", "-c","/entrypoint.sh"]
