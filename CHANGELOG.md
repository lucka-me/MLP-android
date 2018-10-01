<div align=center><a href="https://github.com/lucka-me/MLP-android"><img src="./Resource/Banner.svg" alt="Banner"></a></div>

<h1 align=center>Changelog</h1>

```markdown
## [0.2.14] - 2018-10-01
- 0.2.13(311) -> 0.2.14(327)
- Notification modified and bugs fixed

### Changed
- Display current target in foreground service directly

### Fixed
- App crashes when start service with no target, it will check and warn user now
- Icon in service fab will disappear when tapped after "re-showing" and appear
  after next "re-showing" for unknown reason (probably caused by Material
  Library), it's fixed temporarily by hiding and showing it once.
```

```markdown
## [0.2.13] - 2018-10-01
- 0.2.12(276) -> 0.2.13(311)
- Upgrade Enviroment

### Changed
- Upgrade Android Studio and Gradle to 3.2
- Upgrade Kotlin to 1.2.71
- Refactored to AndroidX
- Remove the unnecessary Android-Support-Preference-V7-Fix dependency
- Preference summary text modified
```

```markdown
## [0.2.12] - 2018-10-01
- 0.2.11(272) -> 0.2.12(276)
- New function: Export to GPX

### Added
- Export to GPX works now
- Cancel notifications when server stopped
```

```markdown
## [0.2.11] - 2018-09-11
- 0.2.10(261) -> 0.2.11(272)
- New function & Bug fixed

### Added
- Notification to show current target

### Fixed
- Read values that not belong to targets when import from GPX
```

```markdown
## [0.2.10] - 2018-09-10
- 0.2.9(254) -> 0.2.10(261)
- New function: Automatic interval when import

### Added
- Clear mock targets
- Calculate interval from time automatically when import
```

```markdown
## [0.2.9] - 2018-09-08
- 0.2.8(241) -> 0.2.9(254)
- New function: Update interval

### Added
- Support mock target update interval
```

```markdown
## [0.2.8] - 2018-09-06
- 0.2.7(231) -> 0.2.8(241)
- New function: Import from GPX

### Added
- Import GPX, parse with SAX, support wpt, trkpt, ele and desc.
```

```markdown
## [0.2.7] - 2018-09-05
- 0.2.6(210) -> 0.2.7(231)
- New function: Customizing providers

### Added
- Option: Mock Customized Provider
- Screen: Customized Provider, to manage the customized providers

### Changed
- Add license to select_dialog_item_material.xml, which is from android
- Methods of DialogKit minor modified
```

```markdown
## [0.2.6] - 2018-09-05
- 0.2.5(202) -> 0.2.6(210)
- New option: Confirm to Delete

### Added
- Confirm to Delete option, a snack bar with UNDO action will show up after
  deleting if the option is off
- Edit and remove icon under cards
```

```markdown
## [0.2.5] - 2018-09-04
- 0.2.4(200) -> 0.2.5(202)
- Bugs fixed

### Fixed
- Export Enabled Targets Only option works oppositely
- Imported targets displayed in wrong cards
```

```markdown
## [0.2.4] - 2018-09-04
- 0.2.3(188) -> 0.2.4(200)
- New feature: Import / export

### Added
- Import / export mock targets from / to JSON file using SAF
- Export Enabled Targets Only option in Preference
```

```markdown
## [0.2.3] - 2018-09-04
- 0.2.2(172) -> 0.2.3(188)
- New feature: Swipe to delete or edit

### Added
- Swipe left to delete
- Long press or swipe right to edit with Edit Mock Target Dialog

### Changed
- In Add Mock Target Dialog, check value immediately after input and the add
  button will enabled only when both values are valid and not being edited
```

```markdown
## [0.2.2] - 2018-09-03
- 0.2.1(156) -> 0.2.2(172)
- New feature: AddMockTargetDialog with tabs

### Added
- Tabs in AddMockTargetDialog: basic and advanced
- About screen

### Fixed
- FabAddMockTarget doesn't hide when scroll down
```

```markdown
## [0.2.1] - 2018-09-03
- 0.2(144) -> 0.2.1(156)
- Fixed: Incomplete location object

### Changed
- Swap the fabService and fabAddMockTarget
- FabService has two new icons

### Fixed
- Incomplete location object when sending mock location, accuracy is non-
  nullable and 5.0 for default now
```

```markdown
## [0.2] - 2018-09-02
- 0.1.4(115) -> 0.2(144)
- New data structure

### Added
- New attributes for MockTarget:
  - Title (UI and methods updated)
  - Inteval, accuracy and altitude (UI and methods not updated yet)
- Save / load data with the new DataKit and Gson

### Changed
- MockTarget is a data class now, the old data could NOT be migrated
```

```markdown
## [0.1.4] - 2018-09-02
- 0.1.2(62) -> 0.1.4(115)
- New feature: Preference, enable/disable providers

### Added
- Preference screen
- Enable and disable providers from receiving mock locations

### Changed
- App icon improved. Since the Assest Studio seems to not support some features
  of SVG, the icon is generated manually. Looking for a better way.
- Switch compileSdkVersion and targetSdkVersion to 27 temporarily for some
  unresolved issues
```

```markdown
## [0.1.2] - 2018-09-02
- 0.1.1(54) -> 0.1.2(62)
- New function: Detect Enable mock location option and alert

### Added
- Detect if the Enable mock location option turned on before starting service
- Alert if the option is off

### Changed
- Code documented
```

```markdown
## [0.1.1] - 2018-08-31
- 0.1(33) -> 0.1.1(54)
- Bug fixed

### Added
- Push notification when exception encountered
- Get service state by a better but not best way, with a method that deprecated
  but still working

### Changed
- All string resources are in English now

### Fixed
- Service won't update mock location if the last target is disabled
```

```markdown
## [0.1] - 2018-08-30
- 0.1(33)
- Initial version
```
