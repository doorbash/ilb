@echo off

@REM rd /s /q ..\app\src\main\jniLibs\arm64-v8a\
@REM rd /s /q ..\app\src\main\jniLibs\armeabi-v7a\
@REM rd /s /q ..\app\src\main\jniLibs\x86_64\
@REM rd /s /q ..\app\src\main\jniLibs\x86\

@REM mkdir ..\app\src\main\jniLibs\arm64-v8a\
@REM mkdir ..\app\src\main\jniLibs\armeabi-v7a\
@REM mkdir ..\app\src\main\jniLibs\x86_64\
@REM mkdir ..\app\src\main\jniLibs\x86\

set GOOS=android
set CGO_ENABLED=1

CALL :build_module ilb

cd src\tun2socks
go get -d ./...
gomobile bind -a -ldflags "-s -w" -target=android -o ..\..\..\app\libs\tun2socks.aar
cd ..\..

set NDK_PROJECT_PATH=src\busybox
cmd /c ndk-build clean
cmd /c ndk-build

del ..\app\src\main\jniLibs\arm64-v8a\libbusybox.so > nul 2> nul
move src\busybox\libs\arm64-v8a\busybox ..\app\src\main\jniLibs\arm64-v8a\libbusybox.so > nul
del ..\app\src\main\jniLibs\armeabi-v7a\libbusybox.so > nul 2> nul
move src\busybox\libs\armeabi-v7a\busybox ..\app\src\main\jniLibs\armeabi-v7a\libbusybox.so > nul
del ..\app\src\main\jniLibs\x86_64\libbusybox.so > nul 2> nul
move src\busybox\libs\x86_64\busybox ..\app\src\main\jniLibs\x86_64\libbusybox.so > nul
del ..\app\src\main\jniLibs\x86\libbusybox.so > nul 2> nul
move src\busybox\libs\x86\busybox ..\app\src\main\jniLibs\x86\libbusybox.so > nul

goto :eof

:build_module
set module=%~1
CALL :build arm64-v8a , arm64 , aarch64-linux-android30-clang
CALL :build armeabi-v7a , arm , armv7a-linux-androideabi30-clang
CALL :build x86_64 , amd64 , x86_64-linux-android30-clang
CALL :build x86 , 386 , i686-linux-android30-clang
EXIT /B 0

:build
set dir=%~1
set GOARCH=%~2
set arch=%~3

set CC=%ANDROID_NDK_HOME%\toolchains\llvm\prebuilt\windows-x86_64\bin\%arch%
del ..\app\src\main\jniLibs\%dir%\lib%module%.so > nul 2> nul
cd src\%module%
go get -d ./...
go build -o ..\..\..\app\src\main\jniLibs\%dir%\lib%module%.so -ldflags "-s -w" %module%.go
cd ..\..

set GOARCH=
set CC=
EXIT /B 0