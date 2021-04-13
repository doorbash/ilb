# ILB (Internet Load Balancer)
Use both WiFi and Mobile data on your Android Phone at the same time to connect internet! **(Requires root access)**

## How it works?
ILB simply tries to find out what packet marks will end up getting routed to different network connections, then runs a local proxy server that marks outgoing packets (this is the part where root access is needed) to balance traffic between interfaces.
**It doesn't do anything with iptables or routing tables.**

## Building and Development
### Windows
* Prerequisites: Android NDK, Go, gomobile
* Set environment variable `ANDROID_NDK_HOME` to your Android ndk root.
* Replace files in [https://github.com/topjohnwu/Magisk/tree/master/tools/ndk-bins/21](https://github.com/topjohnwu/Magisk/tree/master/tools/ndk-bins/21) with their corresponding files in `%ANDROID_NDK_HOME%\toolchains\llvm\prebuilt\windows-x86_64\sysroot\usr\lib` (Backup first)
* Run `native\build.bat`
* To start development, open the project with Android Studio.

## Todo
* Icon
* Settings
* Logs
* Per-app VPN

## License
GPL