# Use plugins

Streamy has a rich collection of source, flow, transformer and sink plugins.
Plugins are available as self-contained packages called components.

You can install, remove and upgrade plugins by doing a drop-in-replace in the plugin folder.

* [[Syslog](/plugin/use/Syslog.md)] : Read or write packet in Syslog format (RFC3164 / RFC5424)
* [[Graphite](/plugin/use/Graphite.md)] : Read packet in graphite format
* [[Json](/plugin/use/Json.md)] : Unmarshal a field containing valid Json or marshal a Json field
* [[Fingerprint](/plugin/use/Fingerprint.md)] : Create a fingerprint of any Json field