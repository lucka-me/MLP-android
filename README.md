<h1 align=center><a href="#"><img src="./Resource/Banner.svg" alt="Banner"></a></h1>
<p align=center>
    <a href="./CHANGELOG.md"><img alt="Version 0.2.2" src="https://img.shields.io/badge/version-0.2.2-orange.svg"/></a>
    <a href="https://www.android.com/versions/lollipop-5-0/"><img alt="API Level 21" src="https://img.shields.io/badge/API_Level-21-A4C639.svg"/></a>
</p>

MLP is used to provide mock locations in Android. It works with `LocationManager` and the ***Enable mock location*** in Developer options.

The aim of MLP is to test and demonstrate the LBS applications of mine,  [RoundO](https://github.com/lucka-me/RoundO-android "GitHub") and [Patroute](https://github.com/lucka-me/Patroute-android "GitHub"), without going outside. I've tried some other similar applications but no one fits my need.

It took only several hours to develop it, it fits my need, but still buggy and hardly exception-handled, so it **may** work fine only when you follow the usage guidline below carefully.

## Usage
Please follow the steps carefully, otherwise MLP may crash.

1. Install MLP.
2. Open the ***Developer Options***, turn on the ***Enable mock location***, and select MLP in the ***Select mock location app*** if the option exists.
3. Launch MLP, tap the add fab beside the title, enter the longitude and latitude in degree, then add Title, altitude and accuracy if you want.
4. Tap the card to enable or disable the target
5. Long press the card to delete the target
6. Tap the start fab in the right bottom to start MLP foreground service
7. Now, the foreground service will send the enabled target(s) one by one and circularly, with interval of 5 seconds. The location will be sent to `GPS_PROVODER` and / or `NETWORK_PROVODER` (Could be configured in Preference screen).
8. Tap the stop fab to stop the service.

### Notice
For usage in mainland China: The coordinates are in WGS-84, not GCJ-02. You can look up the coordinate in Google Maps Satellite Layout.

## To-Do
I'm not sure if I'll develop it in the future, but since there are so many known issues, I'd like to fix and improve MLP when I'm free and glad to do so.

- [ ] Detect running service in a better way
- [x] `0.1.2` Detect if the mock location options are ready
- [ ] More data for mock target:
  - [x] `0.2` Title / Description
  - [x] `0.2` Update interval
  - [ ] Support interval in service
  - [x] `0.2` Accuracy
  - [x] `0.2` Altitude
  - [x] `0.2.2` Update interface of dialog
- [ ] Import target list from:
  - [ ] GPX file
  - [ ] Clipboard
- [ ] Support preferences:
  - [x] `0.1.4` Enable GPS_PROVODER
  - [x] `0.1.4` Enable NETWORK_PROVODER
  - [ ] Enable a custom provider
- [ ] UI improvement
  - [ ] Swipe to delete card
  - [ ] Re-order the cards by drag and drop
  - [ ] Long press to check details / edit
- [ ] Service Noification:
  - [ ] Current target
- [x] `0.1.2` Document the code

## License
This project is licensed under [MIT License](./LICENSE).
