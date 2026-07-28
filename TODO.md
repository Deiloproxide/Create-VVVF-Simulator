<div align="center">

![icon](icon/icon.svg)
# TODO
Current development status and known issues.
</div>

## Completed
- Build the realtime generated audio pipeline.
- Test the realtime audio pipeline with a 440 Hz sine wave.
- Port the VVVF-Simulator algorithm layer and line-voltage waveform processing.
- Map VVVF-Simulator train properties to Create train properties.
- Temporarily hard-code a modulation strategy for the minimum audible prototype.
- Port more train running-sound filters, gear sounds, and harmonic sounds.
- Fix speed continuity and audio issues for remote and cross-dimension trains.
- Add high-speed airflow wind sounds for trains.
- Split server-side and client-side versions.
- Add mod compatibility handling and feature fallbacks.
- Migrate the project to Forge 1.20.1.
- Build an in-game GUI for parameter configuration.
- Port the external YAML config parser and support configurable VVVF strategies.
- Port more configurable properties from VVVF-Simulator and keep configs interoperable.
- Handle mod dependency and compatibility issues.
- Improve train event handling.
- Add the optional audio reload command.
- Add optional integration with Sound Physics related mods.
## Planned
- Add rail and contact-wire friction sounds for trains.
## Optional
- Configure a separate strategy for each carriage in the train station's
  `Assemble Train` screen.
- Change train control to power-based control.
- Modify the driving logic of other entities for GoA3/GoA4 operation.
## Known Issues
- Use this mod carefully on low-performance devices or servers with a very large
  number of trains. It may cause audio stutter or loud audio glitches. See
  [this post](https://www.bilibili.com/opus/1223450262128033811) for the reason.
  Although mobile platforms are now supported, using this mod with the FCL
  launcher on Android is still not recommended.
- Because of a motor pole-pair bug in the original
  [VVVF-Simulator](https://github.com/VvvfGeeks/VVVF-Simulator)
  project, `tn27.yaml` sounds slower during acceleration. An
  [issue](https://github.com/VvvfGeeks/VVVF-Simulator/issues/32)
  has already been submitted.
- When used together with
  [Create: Electro Energetics](https://github.com/george8188625/Create-Electro-Energetics),
  trains may have a `duet` effect, where train sounds are duplicated or layered unexpectedly.
## Feedback
If you find other issues or have better ideas, feedback is welcome in
[GitHub issues](https://github.com/Deiloproxide/Create-VVVF-Simulator/issues).