# `Leavesly`

In Bedrock Edition, there is an exclusive feature that turns some of the leaves white when it is snowing. Leavesly brings this feature to Java Edition with some enhancements, such as:
- Snow will affect all leaves, vines and some specific plants, such as short grass, fern, tall grass and large fern.
- There are different levels of coverage depending on the exposure of the leaves to the snow.
- Addon (flowers and fruits) from the leaves are not affected by the snow.
- Jungle leaves now have cocoa pods ([Podded Jungle Leaves](https://github.com/ishikyoo/podded-jungle-leaves)).
- Fully customisable, snow coverage, leaves colors and more can be tweaked.

### Codebase: `Seabreak`
This branch support `Minecraft` versions from `1.21.2` to `1.21.3`.

## Installation
You can install `Leavesly` by downloading it from the following:

[![GitHub Downloads](https://img.shields.io/github/downloads/ishikyoo/leavesly/total?style=for-the-badge&logo=github&color=F5F5F5)](https://github.com/ishikyoo/leavesly)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/leavesly?style=for-the-badge&logo=modrinth&color=00AD5B)](https://modrinth.com/mod/leavesly)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1109585?style=for-the-badge&logo=curseforge&color=F16436&)](https://www.curseforge.com/minecraft/mc-mods/leavesly)

Simply place the mod file `leavesly-1.2.0+seabreak.jar` in your `.minecraft\mods` folder, and load Minecraft using [Fabric](https://fabricmc.net/use/installer) or [Quilt](https://quiltmc.org/en/install).
You will need Fabric API or Quilted Fabric API.

You can also install it using [Prism Launcher](https://prismlauncher.org), or any other mod manager you like.

## Compiling from Source

Prerequisites: JDK 21

1. Clone this repository by running `git clone https://github.com/ishikyoo/leavesly.git` or `gh repo clone ishikyoo/leavesly`
2. Build the project by running `./gradlew build`
3. The mod jar file can be found at `build/libs/`