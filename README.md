# Reliquified Twilight Forest new relics fix

Compatibility mod for Minecraft 1.21.1 / NeoForge. It rewrites the Relics
0.10 API references left in Reliquified Twilight Forest 0.5.3 and bridges the
relic definitions and runtime calls to Relics 0.12.8.

Build 1.0.3 registers the reference rewrite through `META-INF/coremods.json`,
so NeoForge applies it before reading Reliquified Twilight Forest's own
`ChainBlockMixin`. It also redirects legacy `RelicItem` super calls (including
Curios hooks, active ability casting, and slot modifiers) to the scoped bridge.
The Java compatibility mixins then provide the current Relics definitions and
runtime behavior. The legacy interface bridge is scoped to the add-on's 18
relic classes and does not change the global `RelicItem`.

Version 1.0.4 restores Deer Antler's `ride_along` client activation. Relics
0.12 sends ability input only to the server, while the original Deer Antler
callback accepts only the client-side `END` stage, where it selects the target
and sends `CastRideAlongAbilityPacket`. A client-only mixin now invokes the
validated Relics activation path for that item and ability on release. Other
items keep their existing activation path, including Goblin Nose's toggle.
