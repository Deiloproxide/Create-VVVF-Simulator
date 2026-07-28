<div align="center">

![icon](icon/icon.svg)
# Documentation
How to use this mod.
</div>

## Environments
- Client: optional
- Server: optional

| Behaviour          | Server available | Server unavailable    |
|--------------------|------------------|-----------------------|
| Client available   | Full functions   | Client-side functions |
| Client unavailable | Compatible       | -                     |
## Configs
Config values can change train sounds.
### Create configs
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
### This mod configs
You can access this mod's configs in the following ways:
- Use the NeoForge built-in config screen.
- Use `Access configs of other mods` in the Create config screen.
- Edit the config file directly on disk.
## Events extension
When bad things happen to a train, such as a crash or derailment, this extension
notifies all online players instead of only the train owner.<br>
This extension is compatible with Create: Aeronautics and can show the position
correctly. Create itself may show an x/z position around 2048xxxx in faraway places.
## Make a resource pack of train sounds
This mod can import custom train sounds from a resource pack.
1. Prepare a resource pack sample.
2. Put YAML configs from VVVF-Simulator version 1.10+ into
   `assets/createvvvfsim/strategy/`.
3. Put impulse responses (IRs) into `assets/createvvvfsim/irsound/`.
4. Pack it and import it into your game.
### Built-in resources
#### Some YAML configs are built in and do not require any resource pack
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
- `default.yaml`: same as `siemens.yaml`, used as the import error fallback.
#### Some IRs are built in and do not require any resource pack
- `default.ir`: built-in IR from VVVF-Simulator, used as the import error fallback.
- `alt1.ir`
- `alt2.ir`
- `alt3.ir`

Alternative IRs are all from [Echo Thief](https://www.echothief.com), a website
for IRs from natural environments such as forests, churches, bridges, and
underpasses.<br>
These IRs are used to simulate the reverb of underground metro spaces and stations.
### Notes
This mod only supports `.ir` audio and uncompressed `.wav` audio. Other audio
formats are not supported directly.<br>
When you need to import compressed `.wav` files or other formats, use
[tools](#conversion) to convert them to supported formats.
## Commands
This mod provides client commands to load or reload configs.
### Load YAML config
Type `/vvvf loadyaml <your yaml config name>` to load your YAML config.
In version 1.1.x, this command is `/vvvf load <your yaml config name>`.<br>
The mod searches by the following steps:
1. If the typed name contains a `.yaml` or `.yml` extension, it tries to search it.
2. If the typed name does not contain one of the extensions above, it tries to add
   the extension and then search.
3. If the config is not found or an import error occurs, it falls back to the
   default YAML config.
### Load IR sound
Type `/vvvf loadir <your ir sound name>` to load your IR sound.<br>
The mod searches by the following steps:
1. If the typed name contains a `.ir` or `.wav` extension, it tries to search it.
2. If the typed name contains an unsupported extension such as `.mp3`, `.ogg`, or
   `.flac`, it shows an unsupported-format notification.
3. If the typed name does not contain one of the supported extensions above, it
   tries to add the extension and then search.
4. If the IR is not found or an import error occurs, it falls back to the
   default IR sound.
### Reload
Type `/vvvf reload` to reload audio when:
- [Create train configs](#create-configs) have been modified.
- [This mod configs](#this-mod-configs) have been modified.
- Sound suddenly disappears.
- VVVF sound does not match the train speed.
## Tools
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