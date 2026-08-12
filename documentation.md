<div align="center">

![icon](icon/icon.svg)
# Documentation
How to use Create: VVVF-Simulator in Minecraft.
</div>

## Overview
Create: VVVF-Simulator is a Create addon that simulates VVVF train audio in
game. It works with Forge and NeoForge, supports both built-in and imported
sound resources, and can keep train sound behavior synchronized across client
and server.<br>
[VVVF Simulator Library](https://github.com/Deiloproxide/VVVF-Simulator-Library)
provides the core VVVF simulator, YAML loading helpers,
and shared audio/data structures used by this mod.
## Contents
- [For Users](#for-users)
- [For Developers](#for-developers)
- [Mod Usage](#mod-usage)
- [Tools Usage](#tools-usage)
## For Users
### Dependencies
This mod requires Create and VVVF Simulator Library.

| Dependencies           | Version                       |
|------------------------|-------------------------------|
| Create                 | \>=0.5.1.j                    |
| VVVF Simulator Library | 1.0.x (for this mod \>=1.2.1) |
### Environments
- Client: optional
- Server: optional

| Behaviour          | Server available | Server unavailable    |
|--------------------|------------------|-----------------------|
| Client available   | Full functions   | Client-side functions |
| Client unavailable | Compatible       | -                     |

Install the mod on both sides for full synchronization. Client-only installation
keeps local sound features available, while server-only installation keeps the
server compatible with clients that not have the mod.

| Loader   | Minecraft | Runtime Java |
|----------|-----------|--------------|
| Forge    | 1.20.1    | 17           |
| NeoForge | 1.21.1    | 21           |
### Installation
#### Prebuilt Artifacts
You can get this mod from
[Modrinth](https://modrinth.com/mod/create-vvvf-simulator),
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-vvvf-simulator), and
[GitHub Release](https://github.com/Deiloproxide/Create-VVVF-Simulator/releases).
#### Build From Source
The repository uses Java 21 to run Gradle. Platform jars are compiled for the
Java version shown in the table.
- Build all platforms.
```bash
./gradlew build
```
- Build one platform.
```bash
./gradlew :[platform]:build --configure-on-demand
```
The build reads loader versions from `versions.json` and writes platform
artifacts to `[platform]/build/libs` directory.
## For Developers
### Dependencies
Add the matching platform artifact as a compile dependency and, when needed, as a
runtime mod dependency. The exact Maven coordinates depend on where you publish
or consume the artifact.
- Groovy DSL
```groovy
repositories{
    maven{url="[repository]"}
}
dependencies{
    implementation("[dependency]")
}
```
- Kotlin DSL
```kotlin
repositories{
    maven(url=uri("[repository]"))
}
dependencies{
    implementation("[dependency]")
}
```
| Provider   | Repository                     | Dependency                                                         |
|------------|--------------------------------|--------------------------------------------------------------------|
| JitPack    | https://jitpack.io             | com.github.Deiloproxide.Create-VVVF-Simulator:[platform]:[version] |
| Modrinth   | https://api.modrinth.com/maven | maven.modrinth:create-vvvf-simulator:[platform]-[version]          |
| CurseMaven | https://cursemaven.com         | curse.maven:create-vvvf-simulator-1580570:[file_id]                |
### Loader Metadata
Declare a dependency on this mod in your own loader metadata if your integration
requires it. This gives users a clear loader error when the required mod is missing.
#### Forge
```toml
[[dependencies."[mod_id]"]]
modId="create_vvvf_simulator"
mandatory=true
versionRange="[mod_version_range]"
ordering="NONE"
side="CLIENT"
```
#### NeoForge
```toml
[[dependencies."[mod_id]"]]
modId="create_vvvf_simulator"
type="required"
versionRange="[mod_version_range]"
ordering="NONE"
side="CLIENT"
```
### Notes
The distributed platform jars include and relocate these direct
audio-processing libraries:
- `com.github.wendykierp:JTransforms:3.2`
- `org.visnow:JLargeArrays:1.7`
- `org.apache.commons:commons-math3:3.6.1`

They are relocated under this mod's shadow namespace. Treat relocated packages as
internal implementation details; do not import them from another mod.
## Mod Usage
### Configs
Config values can change train sounds.
#### Create configs
The following Create configs are recommended instead of the default ones.
The default configs accelerate too fast and require manual speed control.
These values refer to real metro operation, with about 30 seconds to leave a
station and a maximum speed around 80-120 km/h.
```toml
[trains]
manualTrainSpeedModifier=1
[trains.trainStats]
trainTopSpeed=32
trainTurningTopSpeed=28
trainAcceleration=1
[trains.poweredTrainStats]
poweredTrainTopSpeed=32
poweredTrainTurningTopSpeed=28
poweredTrainAcceleration=1
```
#### This mod configs
You can access this mod's configs in the following ways:
- Use the NeoForge built-in config screen.
- Use `Access configs of other mods` in the Create config screen.
- Edit the config file directly on disk.
### Events extension
When a train runs into a serious problem, this extension broadcasts the event to
all online players instead of only notifying the train owner.
#### Features
- Broadcast train events: crashes, derailments, high-stress failures, double
  portals, and end-of-track events are sent to all compatible online players.
- Precise positions: notifications include a traceback position. This improves
  Create 0.5.1.j behavior; Create 6.x already has built-in position reporting.
- Create Aeronautics compatibility: positions are converted for
  physics-simulated structures, avoiding the faraway x/z coordinates that Create
  may show around 2048xxxx.
- Localized dimensions: custom dimensions are displayed through
  `dimension.<mod_name>.<dimension_name>` when that translation exists. If no
  translation is available, the internal dimension id is converted to readable text.
### Make a resource pack of train sounds
This mod can import custom train sounds from a resource pack.
1. Prepare a resource pack sample.
2. Put YAML configs from VVVF-Simulator version 1.10+ into
   `assets/createvvvfsim/strategy/`.
3. Put impulse responses (IRs) into `assets/createvvvfsim/irsound/`.
4. Pack it and import it into your game.
#### Built-in resources
Some YAML configs are built in and do not require a resource pack:
- `siemens.yaml`: Siemens alarm, typical metro examples:
  - Shanghai Metro Line 2 LuDengXia.
  - Zhengzhou Metro Line 1.
  - Hangzhou Metro Line 1 Period 2.
  - Ningbo Metro Line 1 Period 1.
- `alstom.yaml`: Alstom OptOnix, typical metro examples:
  - Shanghai Metro Line 3, 4, 5.
  - Beijing Metro Line 6.
  - Nanjing Metro Line S1, S3, 4.
- `tn27.yaml`: Shidai PMSM drive, typical metro examples:
  - Foshan Metro Line 2, 4.
  - Ningbo Metro Line 5, 6, 7, 8.
  - Guangzhou Metro Line 3, 8, 11.
- `toyodenki.yaml`: Toyo Denki, typical metro examples:
  - Beijing Metro Line 1, 10.
  - Chengdu Metro Line 1.
- `default.yaml`: same as `siemens.yaml`, used as the initial setup.

Some IRs are built in and do not require a resource pack:
- `default.ir`: built-in IR from VVVF-Simulator, used as the initial setup.
- `alt1.ir`
- `alt2.ir`
- `alt3.ir`

Alternative IRs are all from [Echo Thief](https://www.echothief.com), a website
for IRs from natural environments such as forests, churches, bridges, and
underpasses.<br>
These IRs are used to simulate the reverb of underground metro spaces and stations.
#### Notes
This mod only supports `.ir` audio and uncompressed `.wav` audio. Other audio
formats are not supported directly.<br>
When you need to import compressed `.wav` files or other formats, use
[tools](#conversion) to convert them to supported formats.
### Commands
This mod provides client commands to load or reload configs.
#### Load YAML config
Type `/vvvf loadyaml <your yaml config name>` to load your YAML config.
In version 1.1.x, this command is `/vvvf load <your yaml config name>`.<br>
The mod searches by the following steps:
1. If the typed name contains a `.yaml` or `.yml` extension, it tries to search it.
2. If the typed name does not contain one of the extensions above, it tries to add
   the extension and then search.
3. If the config is not found or an import error occurs, it reports an error.
#### Load IR sound
Type `/vvvf loadir <your ir sound name>` to load your IR sound.<br>
The mod searches by the following steps:
1. If the typed name contains a `.ir` or `.wav` extension, it tries to search it.
2. If the typed name contains an unsupported extension such as `.mp3`, `.ogg`, or
   `.flac`, it shows an unsupported-format notification.
3. If the typed name does not contain one of the supported extensions above, it
   tries to add the extension and then search.
4. If the IR is not found or an import error occurs, it reports an error.
#### Reload
Type `/vvvf reload` to reload audio when:
- [Create train configs](#create-configs) have been modified.
- [This mod configs](#this-mod-configs) have been modified.
- Sound suddenly disappears.
- VVVF sound does not match the train speed.
## Tools Usage
To pair with in-game functions, this mod provides some [tools](tools).
These tools are redistributable, for example when someone makes a GUI for them.
### Requirements
No site-packages are required. All packages are from the Python Standard Library.<br>
[FFmpeg](https://ffmpeg.org/download.html) is required when converting audio to `.ir`.<br>
[VVVF-Simulator](https://github.com/intel713/VVVF-Simulator) is required when
editing YAML configs.
### Translation sample
Generate a language sample JSON. It updates when language resources update.
#### Usage
```bash
python VVVFTools.py generate -o <output path>
```
### Update YAML configs
A forked VVVF-Simulator was used in this mod since version 1.2.0, instead of the
main VVVF-Simulator used in version 1.1.x.<br>
A deserialize key was changed from `Saw` to `Triangle` after an update of the
fork, which means YAML configs from the main version cannot be used in the fork
version directly.<br>
Now the in-game function is fully compatible with this change, but the external
VVVF-Simulator still cannot use those configs directly.<br>
If you want to edit YAML configs from the main version in the forked version,
use this tool to replace `Saw` with `Triangle`.
#### Usage
```bash
python VVVFTools.py update -i <input path> -o <output path> -s <read buffer size (default 32768)>
```
### Conversion
Because audio formats are complicated, this mod provides a unified lightweight
format to reduce audio decode complexity.
#### Audio to .ir
Convert another audio format, or even an audio stream in a video, to `.ir` format.<br>
This requires FFmpeg.
#### .ir to .wav
Convert `.ir` format to uncompressed `.wav` format, which is also supported by this mod.
#### Usage
```bash
python VVVFTools.py convert -d [ir, wav] -i <input path> -o <output path>
```
### Helps
Run `python VVVFTools.py --help` to get usage.