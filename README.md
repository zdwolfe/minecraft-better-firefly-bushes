# Better Firefly Bushes

A Fabric mod for Minecraft 1.21+ that adds time-based control to firefly bush animations.

## Features

- Firefly bushes only animate during configurable time windows (default: night time)
- Configurable frozen frame when not animating

## Demo

https://github.com/user-attachments/assets/9e864ef8-0033-4f30-a9bc-a5d8abc763cc

## Installation

1. Install [Fabric Loader](https://fabricmc.net/)
2. Place Better Firefly Bushes JAR in `.minecraft/mods` folder
3. Launch Minecraft

## Configuration

Configuration file: `.minecraft/config/better-firefly-bushes.properties`

- `enableTimeBasedControl` (default: true) - Enable/disable time-based animation control
- `animationStartTime` (default: 12000) - Minecraft time when animations start (0-24000)
- `animationEndTime` (default: 23000) - Minecraft time when animations end (0-24000)
- `frozenFrame` (default: 0) - Frame to display when not animating (0-9)

## Building

See [docs/BUILDING.md](docs/BUILDING.md) for details.

## License

See [LICENSE](./LICENSE)
