package com.darcy.kmpdemo.platform

private fun getLocalTimestamp(): String = js("""(() => {
    const d = new Date();
    const pad2 = n => String(n).padStart(2, '0');
    const pad3 = n => String(n).padStart(3, '0');
    return d.getFullYear() + '-' +
        pad2(d.getMonth() + 1) + '-' +
        pad2(d.getDate()) + ' ' +
        pad2(d.getHours()) + ':' +s
        pad2(d.getMinutes()) + ':' +
        pad2(d.getSeconds()) + '.' +
        pad3(d.getMilliseconds());
})()""")

actual object TimePlatform {
    actual fun getCurrentTimeStamp(): String {
        return getLocalTimestamp()
    }
}