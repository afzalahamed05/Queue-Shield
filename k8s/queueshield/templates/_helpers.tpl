{{- define "queueshield.labels" -}}
app.kubernetes.io/part-of: queueshield
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
