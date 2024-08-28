{{/* vim: set filetype=mustache: */}}

{{/*
Expand the internal URI of the microservice.
*/}}
{{- define "eric-esoa-key-management-agent.serviceUri" }}
    {{- $serviceName := (include "eric-esoa-so-library-chart.name" . ) -}}
    {{- printf "%s://%s:%v" "http" $serviceName .Values.service.port.http }}
{{- end }}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-esoa-key-management-agent.version" }}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Expand the name of the chart.
*/}}
{{- define "eric-esoa-key-management-agent.fullname" -}}
{{- if .Values.fullnameOverride -}}
  {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
  {{- $name := default .Chart.Name .Values.nameOverride -}}
  {{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Create image pull secrets for global (outside of scope)
*/}}
{{- define "eric-esoa-key-management-agent.pullSecret.global" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
  {{- if .Values.global.pullSecret -}}
    {{- $pullSecret = .Values.global.pullSecret -}}
  {{- end -}}
  {{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence
*/}}
{{- define "eric-esoa-key-management-agent.pullSecret" -}}
{{- $pullSecret := (include "eric-esoa-key-management-agent.pullSecret.global" . ) -}}
{{- if .Values.imageCredentials -}}
  {{- if .Values.imageCredentials.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
  {{- end -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Reads eric-product-info.yaml and constructs an image reference
from the "registry", "repoPath", "name", and "tag" set in the
values under images.eric-esoa-key-management-agent
*/}}
{{- define "eric-esoa-key-management-agent.mainImagePath" -}}
    {{- template "eric-esoa-key-management-agent.imagePath" (dict "imageId" "eric-esoa-key-management-agent" "values" .Values "files" .Files) -}}
{{- end -}}

{{/*
Reads eric-product-info.yaml and constructs an image reference
from the "registry", "repoPath", "name", and "tag" set in the
values under images.common-base.
(using common_base_os_release, which is already used in the Dockerfile)
*/}}
{{- define "eric-esoa-key-management-agent.kmsAccessSetupPath" -}}
    {{- template "eric-esoa-key-management-agent.imagePath" (dict "imageId" "eric-esoa-key-management-agent" "values" .Values "files" .Files) -}}
{{- end -}}

{{/*
Any image path (DR-D1121-067)
*/}}
{{- define "eric-esoa-key-management-agent.imagePath" }}
    {{- $imageId := index . "imageId" -}}
    {{- $values := index . "values" -}}
    {{- $files := index . "files" -}}
    {{- $productInfo := fromYaml ($files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := index $productInfo "images" $imageId "registry" -}}
    {{- $repoPath := index $productInfo "images" $imageId "repoPath" -}}
    {{- $name := index $productInfo "images" $imageId "name" -}}
    {{- $tag :=  index $productInfo "images" $imageId "tag" -}}
    {{- if $values.global -}}
        {{- if $values.global.registry -}}
            {{- $registryUrl = default $registryUrl $values.global.registry.url -}}
            {{- if not (kindIs "invalid" $values.global.registry.repoPath) -}}
                {{- $repoPath = $values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if $values.imageCredentials -}}
        {{- if $values.imageCredentials.registry -}}
            {{- $registryUrl = default $registryUrl $values.imageCredentials.registry.url -}}
        {{- end -}}
        {{- if not (kindIs "invalid" $values.imageCredentials.repoPath) -}}
            {{- $repoPath = $values.imageCredentials.repoPath -}}
        {{- end -}}
        {{- $image := index $values.imageCredentials $imageId -}}
        {{- if $image -}}
            {{- if $image.registry -}}
                {{- $registryUrl = default $registryUrl $image.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" $image.repoPath) -}}
                {{- $repoPath = $image.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Timezone variable
*/}}
{{- define "eric-esoa-key-management-agent.timezone" }}
  {{- $timezone := "UTC" }}
  {{- if .Values.global }}
    {{- if .Values.global.timezone }}
      {{- $timezone = .Values.global.timezone }}
    {{- end }}
  {{- end }}
  {{- print $timezone | quote }}
{{- end -}}

{{/*
Return the fsgroup set via global parameter if it's set, otherwise 10000
*/}}
{{- define "eric-esoa-key-management-agent.fsGroup.coordinated" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.fsGroup -}}
      {{- if .Values.global.fsGroup.manual -}}
        {{ .Values.global.fsGroup.manual }}
      {{- else -}}
        {{- if eq .Values.global.fsGroup.namespace true -}}
          # The 'default' defined in the Security Policy will be used.
        {{- else -}}
          10000
      {{- end -}}
    {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "eric-esoa-key-management-agent.selectorLabels" -}}
app.kubernetes.io/name: {{ include "eric-esoa-so-library-chart.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "eric-esoa-key-management-agent.serviceAccountName" -}}
  {{- if .Values.serviceAccount.create }}
    {{- default (include "eric-esoa-key-management-agent.fullname" .) .Values.serviceAccount.name }}
  {{- else }}
    {{- default "default" .Values.serviceAccount.name }}
  {{- end }}
{{- end }}
{{/*
Expand the name of the Kubernetes secret associated with the service account.
Information from the secret is used when accessing the ADP KMS
microservice - this template is a single point of definition to
help ensure the secret can be predictably mounted to pods.
*/}}
{{- define "eric-esoa-key-management-agent.serviceAccountSecretName" -}}
{{- template "eric-esoa-key-management-agent.serviceAccountName" . -}}-token
{{- end -}}

{{/*
KMS CA certificate file for TLS communication to ADP KMS.
*/}}
{{- define "eric-esoa-key-management-agent.kmsCaCertFile" -}}
"/run/secrets/kms-ca-cert/{{ .Values.certificate.cacert.filePath }}"
{{- end -}}

{{/*
Create container level annotations
*/}}
{{- define "eric-esoa-key-management-agent.container-annotations" }}
{{- $appArmorValue := .Values.appArmorProfile.type -}}
    {{- if .Values.appArmorProfile -}}
        {{- if .Values.appArmorProfile.type -}}
            {{- if eq .Values.appArmorProfile.type "localhost" -}}
                {{- $appArmorValue = printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile }}
            {{- end}}
container.apparmor.security.beta.kubernetes.io/eric-esoa-key-management-agent: {{ $appArmorValue | quote }}
        {{- end}}
    {{- end}}
{{- end}}

{{/*
Seccomp profile section (DR-1123-128)
*/}}
{{- define "eric-esoa-key-management-agent.seccomp-profile" }}
    {{- if .Values.seccompProfile }}
      {{- if .Values.seccompProfile.type }}
          {{- if eq .Values.seccompProfile.type "Localhost" }}
              {{- if .Values.seccompProfile.localhostProfile }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
  localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
            {{- end }}
          {{- else }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
          {{- end }}
        {{- end }}
      {{- end }}
{{- end }}

{{/*
Create a map from global values with defaults if not in the values file.
*/}}
{{ define "eric-schema-registry-sr.globalMap" }}
  {{- $defaults := dict "security" (dict "policyBinding" (dict "create" false)) -}}
  {{- $defaults := merge $defaults (dict "security" (dict "policyReferenceMap" (dict "default-restricted-security-policy" "default-restricted-security-policy"))) -}}
  {{ if .Values.defaults }}
    {{- mergeOverwrite $defaults .Values.defaults | toJson -}}
  {{ else }}
    {{- $defaults | toJson -}}
  {{ end }}
{{ end }}

{{/*

{{/*
Define tolerations to comply to DR-D1120-060
*/}}
{{- define "eric-esoa-key-management-agent.tolerations" -}}
{{- if .Values.tolerations -}}
  {{- toYaml .Values.tolerations -}}
{{- end -}}
{{- end -}}

{{/*
Create a merged set of nodeSelectors from global and service level.
*/}}
{{- define "eric-esoa-key-management-agent.nodeSelector" -}}
{{- $globalValue := (dict) -}}
{{- if .Values.global -}}
    {{- if .Values.global.nodeSelector -}}
      {{- $globalValue = .Values.global.nodeSelector -}}
    {{- end -}}
{{- end -}}
{{- if .Values.nodeSelector -}}
  {{- range $key, $localValue := .Values.nodeSelector -}}
    {{- if hasKey $globalValue $key -}}
         {{- $Value := index $globalValue $key -}}
         {{- if ne $Value $localValue -}}
           {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
         {{- end -}}
     {{- end -}}
    {{- end -}}
    nodeSelector: {{- toYaml (merge $globalValue .Values.nodeSelector) | trim | nindent 2 -}}
{{- else -}}
  {{- if not ( empty $globalValue ) -}}
    nodeSelector: {{- toYaml $globalValue | trim | nindent 2 -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-esoa-key-management-agent.mainImagePullPolicy" -}}
    {{- template "eric-esoa-key-management-agent.registryImagePullPolicy" (dict "imageId" "eric-esoa-key-management-agent" "values" .Values) -}}
{{- end -}}
{{- define "eric-esoa-key-management-agent.kmsAccessPullPolicy" -}}
    {{- template "eric-esoa-key-management-agent.registryImagePullPolicy" (dict "imageId" "eric-esoa-key-management-agent" "values" .Values) -}}
{{- end -}}

{{/*
    Define Image Pull Policy
*/}}
{{- define "eric-esoa-key-management-agent.registryImagePullPolicy" -}}
    {{- $imageId := index . "imageId" -}}
    {{- $values := index . "values" -}}
    {{- $registryPullPolicy := "IfNotPresent" -}}
    {{- if $values.global -}}
        {{- if $values.global.registry -}}
            {{- if $values.global.registry.imagePullPolicy -}}
                {{- $registryPullPolicy = $values.global.registry.imagePullPolicy -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if $values.imageCredentials -}}
        {{- if (index $values.imageCredentials $imageId) -}}
            {{- if (index $values.imageCredentials $imageId "imagePullPolicy") -}}
                {{- $registryPullPolicy = index $values.imageCredentials $imageId "imagePullPolicy" -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- print $registryPullPolicy -}}
{{- end -}}

{/*
Define JVM heap size (DR-D1126-010 | DR-D1126-011)
*/}}
{{- define "eric-esoa-key-management-agent.jvmHeapSettings" -}}
    {{- $initRAM := "" -}}
    {{- $maxRAM := "" -}}
    {{/*
       ramLimit is set by default to 1.0, this is if the service is set to use anything less than M/Mi
       Rather than trying to cover each type of notation,
       if a user is using anything less than M/Mi then the assumption is its less than the cutoff of 1.3GB
       */}}
    {{- $ramLimit := 1.0 -}}
    {{- $ramComparison := 1.3 -}}

    {{- if not (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory") -}}
        {{- fail "memory limit for eric-esoa-key-management-agent is not specified" -}}
    {{- end -}}

    {{- if (hasSuffix "Gi" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory")) -}}
        {{- $ramLimit = trimSuffix "Gi" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory") | float64 -}}
    {{- else if (hasSuffix "G" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory")) -}}
        {{- $ramLimit = trimSuffix "G" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory") | float64 -}}
    {{- else if (hasSuffix "Mi" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory")) -}}
        {{- $ramLimit = (div (trimSuffix "Mi" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory") | float64) 1000) | float64  -}}
    {{- else if (hasSuffix "M" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory")) -}}
        {{- $ramLimit = (div (trimSuffix "M" (index .Values "resources" "eric-esoa-key-management-agent" "limits" "memory")| float64) 1000) | float64  -}}
    {{- end -}}


    {{- if (index .Values "resources" "eric-esoa-key-management-agent" "jvm") -}}
        {{- if (index .Values "resources" "eric-esoa-key-management-agent" "jvm" "initialMemoryAllocationPercentage") -}}
            {{- $initRAM = (index .Values "resources" "eric-esoa-key-management-agent" "jvm" "initialMemoryAllocationPercentage") | float64 -}}
            {{- $initRAM = printf "-XX:InitialRAMPercentage=%f" $initRAM -}}
        {{- else -}}
            {{- fail "initialMemoryAllocationPercentage not set" -}}
        {{- end -}}
        {{- if and (index .Values "resources" "eric-esoa-key-management-agent" "jvm" "smallMemoryAllocationMaxPercentage") (index .Values "resources" "eric-esoa-key-management-agent" "jvm" "largeMemoryAllocationMaxPercentage") -}}
            {{- if lt $ramLimit $ramComparison -}}
                {{- $maxRAM = (index .Values "resources" "eric-esoa-key-management-agent" "jvm" "smallMemoryAllocationMaxPercentage") | float64 -}}
                {{- $maxRAM = printf "-XX:MaxRAMPercentage=%f" $maxRAM -}}
            {{- else -}}
                {{- $maxRAM = (index .Values "resources" "eric-esoa-key-management-agent" "jvm" "largeMemoryAllocationMaxPercentage") | float64 -}}
                {{- $maxRAM = printf "-XX:MaxRAMPercentage=%f" $maxRAM -}}
            {{- end -}}
        {{- else -}}
            {{- fail "smallMemoryAllocationMaxPercentage | largeMemoryAllocationMaxPercentage not set" -}}
        {{- end -}}
    {{- else -}}
        {{- fail "jvm heap percentages are not set" -}}
    {{- end -}}
{{- printf "%s %s" $initRAM $maxRAM -}}
{{- end -}}
