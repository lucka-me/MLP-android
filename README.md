<h1 align=center><a href="#"><img src="./Resource/Banner.svg" alt="Banner"></a></h1>
<p align=center>
    <a href="./CHANGELOG.md"><img alt="Version 0.2.15" src="https://img.shields.io/badge/version-0.2.15-orange.svg"/></a>
    <a href="https://www.android.com/versions/lollipop-5-0/"><img alt="Minmum SDK 21" src="https://img.shields.io/badge/min_SDK-21-A4C639.svg"/></a>
</p>

MLP is used to provide mock locations in Android. It works with `LocationManager` and the ***Enable mock location*** in Developer options.

The aim of MLP is to test and demonstrate the LBS applications of mine,  [RoundO](https://github.com/lucka-me/RoundO-android "GitHub") and [Patroute](https://github.com/lucka-me/Patroute-android "GitHub"), without going outside. I've tried some other similar applications but no one fits my need.

~~It took only several hours to develop it, it fits my need, but still buggy and hardly exception-handled, so it **may** work fine only when you follow the usage guideline below carefully.~~

After development during several days, the application is more flexible, but following the guideline is still highly recommended.

## Usage
Please follow the steps carefully, otherwise MLP may crash.

1. Install MLP.
2. Open the ***Developer Options***, turn on the ***Enable mock location***, and select MLP in the ***Select mock location app*** if the option exists.
3. Launch MLP, tap the add fab beside the title, enter the longitude and latitude in degree, then add title, altitude and accuracy if you want.
4. Tap the card to enable or disable the target.
5. Swipe the card to left to delete the target, long press or swipe to right to edit.
6. Tap the start fab in the right bottom to start MLP foreground service.
7. Now, the foreground service will send the enabled target(s) one by one and circularly, with interval of 5 seconds. The location will be sent to `GPS_PROVODER` and / or `NETWORK_PROVODER` (Could be configured in Preference screen).
8. Tap the stop fab to stop the service.

### Import / Export
MLP could **import / export** JSON in formatt of itself since `0.2.4`， **import** GPX since `0.2.8` and **export** GPX since `0.2.12`. However, the function has not been tested yet, it may be buggy or changed in the future.

### Notice
For usage in mainland China: The coordinates are in WGS-84, not GCJ-02. You can look up the coordinate in OpenStreetMap or Google Maps Satellite Layout.

## To-Do
I'm not sure if I'll develop it in the future, but since there are so many known issues, I'd like to fix and improve MLP when I'm free and glad to do so.

- [ ] Detect running service in a better way
- [x] `0.1.2` Detect if the mock location options are ready
- [x] More data for mock target:
  - [x] `0.2` Title / Description
  - [x] `0.2` Update interval
  - [x] `0.2.9` Support interval in service
  - [x] `0.2` Accuracy
  - [x] `0.2` Altitude
  - [x] `0.2.2` Update interface of dialog
- [ ] Import / export target list from / to:
  - [x] `0.2.4` JSON file
  - [x] GPX file
    - [x] `0.2.8` Import
    - [x] `0.2.12` Export
    - [x] `0.2.10` Convert time to interval
  - [ ] Clipboard
  - [ ] CSV
- [ ] Support preferences:
  - [x] `0.1.4` Enable GPS_PROVODER
  - [x] `0.1.4` Enable NETWORK_PROVODER
  - [x] `0.2.7` Enable a custom provider
- [ ] UI improvement
  - [x] `0.2.3` Swipe left to delete card
  - [ ] Re-order the cards by drag and drop
  - [x] `0.2.3` Long press / swipe right to check details / edit
  - [x] `0.2.6` Option to enable / disable Deletion Confirmation
- [x] Service Noification:
  - [x] `0.2.11` Current target
- [x] `0.1.2` Document the code
- [ ] Learn to release the application

## License
This application is licensed under [MIT License](./LICENSE).
