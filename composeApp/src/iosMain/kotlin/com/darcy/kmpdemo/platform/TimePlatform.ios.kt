package com.darcy.kmpdemo.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual object TimePlatform {
    actual fun getCurrentTimeStamp(): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
        return formatter.stringFromDate(NSDate())
    }
}