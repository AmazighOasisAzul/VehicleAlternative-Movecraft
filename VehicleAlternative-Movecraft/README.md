# VehicleAlternative

A highly configurable Minecraft Paper plugin for vehicle speed modifications and enhanced boat mechanics. Designed as a vanilla-friendly alternative to Movecraft that preserves Minecraft's feel while adding exciting customization options.

## Features

### 🐴 Vehicle Speed Modifications
- **Horses, Donkeys, Mules**: Configurable speed and jump multipliers
- **Boats**: Different speed settings for water, ice, and land
- **Minecarts**: Adjustable speed with powered rail boost multipliers
- **Pigs**: Modified speed when riding with carrot on stick
- **Striders**: Separate lava and land speed settings
- **Camels**: Speed and dash cooldown customization

### ⛵ Boat Climbing Mechanics
- Boats can climb up blocks outside of water
- Boats can ascend waterfalls and water blocks
- Configurable max climb height
- Adjustable climb speed
- Optional stamina system (coming soon)

### ⚙️ Highly Configurable
- Every vehicle type can be enabled/disabled independently
- Per-world override support for different world settings
- Permission-based access control for each vehicle type
- Particle and sound effects (optional)
- Performance tuning options

## Installation

1. Download the latest release JAR file
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Edit `plugins/VehicleAlternative/config.yml` to customize settings
5. Run `/va reload` to apply changes

## Requirements

- **Server**: Paper 1.20.1 or newer
- **Java**: Java 17 or newer

## Configuration

The plugin comes with a comprehensive configuration file. Here's a quick overview:

```yaml
# Enable/disable the entire plugin
global:
  enabled: true
  debug: false

# Horse settings - modify speed and jump
horses:
  enabled: true
  speed-multiplier: 1.5
  jump-multiplier: 1.2
  min-speed-multiplier: 1.2
  max-speed-multiplier: 2.5

# Boat settings - speed and climbing
boats:
  enabled: true
  speed:
    water-multiplier: 1.8
    ice-multiplier: 2.5
    land-multiplier: 0.8
  climbing:
    enabled: true
    allow-on-land: true
    allow-in-water: true
    max-climb-height: 2
    climb-speed: 0.5

# ... and much more!
```

See the full `config.yml` for all available options.

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `vehiclealternative.admin` | Access to admin commands | op |
| `vehiclealternative.use` | General vehicle usage | true |
| `vehiclealternative.use.horse` | Use modified horses | true |
| `vehiclealternative.use.boat` | Use modified boats | true |
| `vehiclealternative.use.minecart` | Use modified minecarts | true |

## Commands

| Command | Aliases | Description |
|---------|---------|-------------|
| `/vehiclealternative` | `/va`, `/vehicle` | Main command |
| `/va help` | - | Show help menu |
| `/va info` | `/va version` | Show plugin information |
| `/va reload` | - | Reload configuration |

## Building from Source

```bash
# Clone the repository
git clone https://github.com/AmazighOasisAzul/VehicleAlternative-Movecraft.git
cd VehicleAlternative-Movecraft

# Build with Maven
mvn clean package

# The compiled JAR will be in target/VehicleAlternative-1.0.0-SNAPSHOT.jar
```

## Per-World Configuration

You can override settings for specific worlds:

```yaml
global:
  per-world-overrides:
    world_nether:
      horses:
        speed-multiplier: 2.0
      boats:
        enabled: false
    mining_world:
      minecarts:
        speed-multiplier: 3.0
```

## Examples

### Fast Horses Configuration
```yaml
horses:
  enabled: true
  speed-multiplier: 2.0
  jump-multiplier: 1.5
  apply-to-donkeys: true
  apply-to-mules: true
```

### Boat Mountain Climbing
```yaml
boats:
  climbing:
    enabled: true
    allow-on-land: true
    max-climb-height: 3
    climb-speed: 0.8
```

### Speed Minecart Racing
```yaml
minecarts:
  enabled: true
  speed-multiplier: 2.5
  max-speed-multiplier: 5.0
  powered-rail-boost: 3.0
```

## Support & Contributing

- **Issues**: [GitHub Issues](https://github.com/AmazighOasisAzul/VehicleAlternative-Movecraft/issues)
- **Discussions**: [GitHub Discussions](https://github.com/AmazighOasisAzul/VehicleAlternative-Movecraft/discussions)

Contributions are welcome! Feel free to open a pull request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

Developed by Ryane

Special thanks to the Paper team for their excellent server software and API.
