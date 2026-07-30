#!/bin/bash
echo "手动安装apk-debug 开始..."
adb install -t composeApp/build/intermediates/apk/debug/composeApp-debug.apk
echo "手动安装apk-debug 结束..."
