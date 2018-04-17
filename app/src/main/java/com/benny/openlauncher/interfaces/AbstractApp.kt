package com.benny.openlauncher.interfaces

import com.benny.openlauncher.util.BaseIconProvider

interface AbstractApp {
    var label: String
    var packageName: String
    var className: String
    var iconProvider: BaseIconProvider
}
