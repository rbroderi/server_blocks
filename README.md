# Server Blocks 0.4.0

Independent Java 25 library for Paper/Purpur 26.3. Maven coordinates:
`io.github.rbroderi:server-blocks:0.4.0`. MIT licensed. Run `mvn install` here
to use it from another project; no public artifact repository is configured yet.
The independently written API source is MIT licensed (LICENSE-MIT). The repository's
original GPLv3 LICENSE is preserved; no legacy GPL gameplay source is included here.

This is an installable API plugin named ServerBlocks. Put server-blocks-0.4.0.jar
in plugins/ alongside each consumer. Consumers declare depend: [ServerBlocks],
compile with scope provided, and do not shade or relocate the API. Purpur loads
the provider first and prevents consumers from enabling if it is absent.

## Build and install

Requires Java 25 and Purpur 26.3. Run `mvn verify` to compile, test and package.
Copy `target/server-blocks-0.4.0.jar` to your server's `plugins/` folder, install
compatible consumer plugins such as ZBlocks 2.3.0 or Written Ages alpha47, and
restart normally. Earlier 0.3.0 developer-library JARs cannot be installed as plugins.
Keep only one ServerBlocks version. Consumers retain their own saved data and packs.

GitHub Actions builds the plugin and source JAR as downloadable artifacts on each
push or pull request. No resource pack, Nexo or additional runtime library is required
by the provider itself. Custom models in consumer plugins still require their packs.

To target the API, install it locally with `mvn install`, use Maven coordinates
above with `<scope>provided</scope>`, and add `depend: [ServerBlocks]` to your
plugin.yml. Do not shade or relocate `dev.serverblocks`; the server supplies it.
Create managers using your own JavaPlugin instance and close them during disable.

The current API is compiled against Purpur 26.3.build.2643-experimental. Its offline
suite contains 21 tests. Live server/client collision and anti-cheat compatibility
still need testing. The happy-ghast backend uses a narrow version-specific native
adapter; consumers must restore native backing blocks if it is unavailable.

| API | Responsibility |
| --- | --- |
| Position | Immutable world UUID + x/y/z, encoding and asymmetric range bounds |
| BlockIndex | Consumer-owned identity lookup, snapshots and validated moves |
| LoadedBlocks | Loaded-chunk-only lookup and living-entity occupancy checks |
| AtomicYaml | Strict reads; temp-file and atomic-replacement saves |
| TaggedItems | Item identity with the consumer's existing PDC key |
| DisplayBoxes | Tagged, non-persistent block-display geometry |
| BlockProtection | Optional protection of solid/air-backed registered cells from vanilla mutations |
| ResetPlanner | Prevalidate whole reset move sets; supports swaps/cycles |
| BlockMutationEvent | Optional veto before a consumer changes world blocks |
| ModelKey / ItemModels | Validated namespace:path keys and modern item_model components |
| ModelDisplays | Model-backed ItemDisplays using NONE transform |
| PackReadiness | Independent acknowledgements for viewers and stacked packs |
| PackBlockRenderer | Managed display lifecycle, per-viewer collision skins, pack-status fallback, chunk refresh and custom sound fallback |
| HitboxGeometry | Validated cuboids, square interaction coverage and bounded overlapping collision cubes |
| EntityHitboxes | Consumer-owned click/attack routing, collision entities, chunk teardown, repair and failure callbacks |

World/registry access and event dispatch require the main server thread; not Folia.
After `BlockIndex.move`, update your entry's coordinate. Iterate snapshots during
mutation. `LoadedBlocks.at` returns null for missing worlds, invalid heights and
unloaded chunks; never force-load to advance a simulation. Saves serialize on the
caller thread and must be bounded. Corrupt reads throw: preserve the save and stop
the affected subsystem instead of replacing it with an empty registry.

DisplayBoxes/ModelDisplays only create geometry: the consumer owns collision,
teardown and chunk lifecycle. PackBlockRenderer instead manages its own entities,
cleanup and chunk/status refresh; close it during plugin disable. The library
never uploads or sends packs. Registries and save schemas
are per consumer; it does not coordinate overlapping registrations across plugins.
Reserve separate locations. Events now have one shared class identity; protection
integrations compile against this API JAR. There is no WorldGuard adapter.

ZBlocks uses the index, coordinates, storage, protection, tags, occupancy, reset
validation and PackBlockRenderer. Written Ages alpha47 uses loaded lookup, tags,
atomic saves, named item models and Stargate display geometry. Its native
registries/schemas and existing model identifiers are retained for compatibility.

PackBlockRenderer accepts the consumer's item model, item material and client
collision BlockData independently. upsert/remove take stable consumer-owned IDs;
all operations stay on the main thread. No persistent entity IDs are saved.
Pass a pack UUID when acknowledgement must correspond to one specific manual
server pack. With no UUID, success for any server pack enables visuals; the
operator must ensure that pack contains the model assets. Client-local packs
cannot be detected by server status events. Reconnect after plugin reload; use
normal server restarts instead of /reload. Held model items require their assets
even if passed to a player whose pack is absent; the block fallback does not
rewrite inventory items per viewer.

Nexo is an ideas/capability reference only. No Nexo code, assets, decompiled
implementation or dependency is included. Useful patterns: separate block lookup
and placement from gameplay mechanics; separate collision and rendering; account
for vanilla behavior displaced by note-block/chorus/tripwire visual backends.
Future pack-based models should keep a usable vanilla fallback.

EntityHitboxes takes stable IDs, a Position, clickable boxes and solid boxes.
Pass a click callback and a failure callback; restore your native backing blocks
if the collision backend fails. Replace solid backing blocks with a noncolliding
protected marker yourself. Maintain your own gameplay saves. Always close the
manager at disable. It removes entities on chunk unload and recreates them only
when their chunks are already loaded. Occupancy checks ignore tagged helper
entities. Interaction boxes are square in X/Z but can have independent height;
rectangles use overlapping squares. Collision cubes have a minimum size of .25;
shorter dimensions expand symmetrically. Both planners reject more than 64 shapes
per object rather than allocating unbounded entity sets.

Physical collision requires a narrow 26.3 native reflection adapter because
Bukkit's HappyGhast API does not expose the stay-still state. The adapter invokes
the native metadata-synchronizing setter, renewing the timeout every ten ticks.
It fails explicitly on unsupported mappings. No Nexo implementation is included.
Call target(player,range) for admin ray targeting. Clicks validate reach, occlusion,
main hand and spectator state; the consumer must dispatch appropriate protection
events, enforce permissions/game modes, and own placement/breaking and drops.

References: https://docs.nexomc.com/community-guides/api and
https://docs.nexomc.com/mechanics/custom-block-mechanics.
