# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

VehicleAlternative is a Paper Minecraft plugin (Java 17+, Paper 1.20.1+) that provides configurable vehicle speed modifications and boat climbing mechanics. It's designed as a vanilla-friendly alternative to Movecraft.

**Core Features:**
- Speed multipliers for horses, boats, minecarts, pigs, striders, and camels
- Boat climbing mechanics (climb blocks on land and in water)
- Per-world configuration overrides
- Permission-based access control
- Optional particle and sound effects

## Build & Development Commands

### Build
```bash
mvn clean package
```
Output: `target/VehicleAlternative-1.0.0-SNAPSHOT.jar`

### Clean Build
```bash
mvn clean
```

### Compile Only
```bash
mvn compile
```

### Install to Local Maven Repository
```bash
mvn clean install
```

## Testing the Plugin

This plugin is tested by running it on a Paper server. There is no automated test framework currently in place.

**Manual Testing Setup:**
1. Build the plugin: `mvn clean package`
2. Copy JAR to Paper server's `plugins/` folder
3. Start/restart the Paper server
4. Test in-game with different vehicles
5. Use `/va reload` to test configuration changes without restarting

**In-Game Commands:**
- `/va help` - Show help menu
- `/va info` or `/va version` - Show plugin information
- `/va reload` - Reload configuration (requires `vehiclealternative.admin` permission)

## Architecture

### Plugin Lifecycle
The plugin follows the standard Bukkit/Paper plugin lifecycle:
1. **onEnable()** - Initializes ConfigManager, registers event listeners, and registers commands
2. **onDisable()** - Cleanup (currently minimal)

Main class: `VehicleAlternative.java` (singleton pattern via `getInstance()`)

### Core Components

**Configuration System (`config/ConfigManager.java`)**
- Loads and caches all configuration values from `config.yml`
- Provides getters for all settings
- No direct file I/O after initial load (uses Bukkit's FileConfiguration)
- Reload support via `loadConfig()`

**Event Listeners**
1. `listeners/VehicleSpeedListener.java` - Handles speed modifications for all vehicle types
   - `VehicleEnterEvent` - Modifies entity attributes when player mounts vehicle (horses, pigs, striders, camels)
   - `VehicleMoveEvent` - Applies velocity multipliers during movement (boats, minecarts)
   
2. `listeners/BoatClimbListener.java` - Handles boat climbing mechanics
   - `VehicleMoveEvent` - Detects when boat should climb and applies upward velocity
   - Checks terrain ahead of boat to determine climb height
   - Differentiates between water climbing and land climbing

**Command System (`commands/MainCommand.java`)**
- Single command handler for `/vehiclealternative` (aliases: `/va`, `/vehicle`)
- Implements both `CommandExecutor` and `TabCompleter`
- Subcommands: help, info/version, reload

### Key Design Patterns

**Attribute Modification vs Velocity Manipulation**
- **Horses/Pigs/Striders/Camels**: Modifies Bukkit entity attributes (`GENERIC_MOVEMENT_SPEED`, `HORSE_JUMP_STRENGTH`) when player enters vehicle
- **Boats/Minecarts**: Applies velocity multipliers on each move event (more granular control needed)

**Permission Checking**
- Every vehicle type has an optional permission requirement (`require-permission` config)
- Permission format: `vehiclealternative.use.<vehicle_type>`
- Permission checking happens in listeners, not in config

**Per-World Overrides**
- Config supports per-world settings via `global.per-world-overrides` map
- Currently defined in config.yml but implementation is not present in ConfigManager
- If implementing this, you'll need to add world-specific config loading logic

### Important Implementation Details

**Boat Climbing Logic**
- Uses `getBlockAhead()` to detect obstacles in boat's direction of travel
- Calculates climb height by counting solid blocks upward from obstacle
- Applies upward velocity (`setY()`) while maintaining forward momentum
- Respects `max-climb-height` configuration limit
- Separate toggles for land vs water climbing

**Speed Multiplier Application**
- Minecarts: Normalizes velocity vector then multiplies by speed, capped at `max-speed-multiplier`
- Boats: Multiplies velocity by terrain-appropriate multiplier (water/ice/land)
- Horses: Directly modifies base attribute values with min/max clamping
- Powered rails: Additional boost multiplier applied when minecart is on powered rail

**Debug Mode**
- Enable via `global.debug: true` in config
- Logs additional information to console (horse modifications, boat climbs, etc.)
- Check `config.isDebug()` before logging

## Configuration

Primary config: `src/main/resources/config.yml` (copied to `plugins/VehicleAlternative/config.yml` at runtime)

**Important Config Sections:**
- `global` - Plugin-wide settings and per-world overrides (note: per-world not implemented yet)
- `horses`, `boats`, `minecarts`, `pigs`, `striders`, `camels` - Vehicle-specific settings
- `boats.climbing` - Boat climbing mechanics configuration
- `effects` - Particle and sound effects (defined in config but not implemented)
- `performance` - Update intervals and tick limits (defined but not implemented)
- `messages` - Customizable plugin messages with color code support

## When Making Changes

**Adding a New Vehicle Type:**
1. Add config section in `config.yml`
2. Add fields and getters to `ConfigManager.java`
3. Add event handler in `VehicleSpeedListener.java` or create new listener
4. Add permission to `plugin.yml`
5. Test permission checking logic

**Modifying Speed Behavior:**
- For entity-based vehicles (horses, pigs): Modify attribute values in `VehicleEnterEvent` handler
- For vehicle entities (boats, minecarts): Modify velocity in `VehicleMoveEvent` handler
- Always check permission and enabled status before applying modifications

**Configuration Changes:**
- Update `config.yml` with new settings
- Add corresponding fields to `ConfigManager.java`
- Load values in `ConfigManager.loadConfig()` with sensible defaults
- Add getter methods following existing naming convention

**Important Considerations:**
- All speed multipliers are relative to vanilla speeds (1.0 = vanilla, 2.0 = double speed)
- Permission checks should respect both global permission and vehicle-specific permission requirements
- The plugin uses Paper API, not pure Bukkit - Paper-specific features are available
- Resource filtering is enabled in Maven - `${project.version}` in YAML files is replaced at build time
