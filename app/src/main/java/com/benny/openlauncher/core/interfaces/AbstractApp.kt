package com.benny.openlauncher.core.interfaces

import com.benny.openlauncher.core.util.BaseIconProvider

interface AbstractApp {
    var label: String
    var packageName: String
    var className: String
    var iconProvider: BaseIconProvider
}
