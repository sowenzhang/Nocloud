package com.nocloudchat

import java.io.File

expect fun openFileInExplorer(path: String)
expect fun getDownloadDirectory(): File
expect fun detectSsidPlatform(): String?
expect fun getPreferencesDirectory(): File
expect fun pickFile(): File?
