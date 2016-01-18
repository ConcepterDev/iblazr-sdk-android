## iblazrSDK
Android iblazr SDK for communicating with the iblazr 2 via Bluetooth http://concepter.co
## Overview
This SDK provides a Java classes and interfaces for access to iblazr 2 via CoreBluetooth. All the following code compatible on phones with Android 4.3 or higher and Bluetooth 4.0.

> NOTE: not working in simulator

> P.S.: current version was taken from our [Shotlight App](https://play.google.com/store/apps/details?id=com.concepterllc.shotlight&hl=ru) as is, if you don’t understand anything, don’t hesitate to contact us (see at [feedback](#feedback) section)

## How To Get Started
* [Download iblazrSDK](https://github.com/ConcepterDev/iblazr-sdk-android/archive/master.zip) and try out the included  example iblazr2Demo app
* Read the "[Getting Started](#installation)" guide

### Installation
In your Android Studio select File -> New -> Import Module and choose our iblazrbluetoothlib. Then add this Module Dependency ;)<br>

```
### Device parameters
Check `STDevice.h`,`STDeviceIblazr2.h` and `STDeviceIblazrOriginal.h` for get available parametrs.<br>
`float lightPower` - value can be in range 0-16<br>
`float lightTemperature;` - value can be in range 0-125<br>
> NOTE:  `lightTemperature` in current version you can set 25 as maximum value. In `STDeviceManager.m` line:290 it be multiply with 5.


### Start using
Simple setup
```java
BLEManager bleManager = BLEManager.getInstance(bluetoothManager.getAdapter(), this, new OnIblazrDeviceDiscoverCallback());
bleManager.findDeviceFromConnectedDevices();
bleManager.scanLeDevice(true);
```

Make flash
```java
// Set custom temperature, you can set values from 0 to 0x7D and check it. You can take BLEIblazrDevice in onDeviceDiscovered(final BLEIblazrDevice 
// device) of OnIblazrDeviceDiscoverCallback
BLEIblazrDevice bleIblazrDevice;
bleIblazrDevice.setTemperature(0x7D);
```

Send check notification to iblazr flash. You can manualy setup this effect. (Works with iblazr 2. Read additional documentation)
```java
// set custom brightness, you can set value from 0 to 0x3F
device.setBrightness(0x3F);
```

And Stop
```java
// It sends zero time to iblazr2
bleIblazrDevice.stop();
```

## Additional Resources
You can find additional protocol information here:
* iblazr 2 documentation at [*LINK*](https://github.com/ConcepterDev/iblazr-2-protocol)
* iblazr original documentation at [*LINK*](https://github.com/ConcepterDev/iblazr-original-protocol)

## License
iblazrSDK is licensed under the MIT License. See LICENSE for details.

## Feedback
Contact the developer support team by sending an email to support@concepter.co

