name := name.value + "-plugin-syslog"

// Jmh settings
sourceDirectory in Jmh := new File((sourceDirectory in Test).value.getParentFile, "bench")
classDirectory in Jmh := (classDirectory in Test).value
dependencyClasspath in Jmh := (dependencyClasspath in Test).value

// Enable some plugins
enablePlugins(JavaAppPackaging, JmhPlugin)