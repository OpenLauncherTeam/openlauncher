package com.benny.openlauncher.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ContentUris
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.ContactsContract
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.text.format.Formatter
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.benny.openlauncher.BuildConfig
import com.benny.openlauncher.R
import com.benny.openlauncher.core.activity.CoreHome
import com.benny.openlauncher.core.util.Tool
import java.io.*
import java.util.*

fun Tool.split(string: String, delim: String): Array<String> {
    val list = ArrayList<String>()
    val charArr = string.toCharArray()
    val delimArr = delim.toCharArray()
    var counter = 0
    var i = 0
    while (i < charArr.size) {
        var k = 0
        for (j in delimArr.indices) {
            if (charArr[i + j] == delimArr[j]) {
                k++
            } else {
                break
            }
        }
        if (k == delimArr.size) {
            var s = ""
            while (counter < i) {
                s += charArr[counter]
                counter++
            }
            i = i + k
            counter = i
            list.add(s)
        }
        i++
    }
    var s = ""
    if (counter < charArr.size) {
        while (counter < charArr.size) {
            s += charArr[counter]
            counter++
        }
        list.add(s)
    }
    return list.toTypedArray()
}

fun Tool.getContactIDFromNumber(context: Context, contactNumber: String): Long {
    val UriContactNumber = Uri.encode(contactNumber)
    var phoneContactID = Random().nextInt().toLong()
    val contactLookupCursor = context.contentResolver.query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, UriContactNumber),
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID), null, null, null)
    while (contactLookupCursor!!.moveToNext()) {
        phoneContactID = contactLookupCursor.getLong(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
    }
    contactLookupCursor.close()
    return phoneContactID
}

fun Tool.factorColorBrightness(color: Int, brightnessFactorPercent: Int): Int {
    var color = color
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] *= (brightnessFactorPercent / 100.0).toFloat()
    hsv[2] = if (hsv[2] > 255) 255f else hsv[2]
    color = Color.HSVToColor(hsv)
    return color
}

fun Tool.fetchThumbnailId(context: Context, phoneNumber: String): Int? {
    val uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
    val cursor = context.contentResolver.query(uri, arrayOf(ContactsContract.Contacts.PHOTO_ID), null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC")

    try {
        var thumbnailId: Int? = null
        if (cursor!!.moveToFirst()) {
            thumbnailId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID))
        }
        return thumbnailId
    } finally {
        cursor!!.close()
    }
}

fun Tool.fetchThumbnail(context: Context, phoneNumber: String): Bitmap? {
    print(phoneNumber)
    val thumbnailId = fetchThumbnailId(context, phoneNumber) ?: return null
    val uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId.toLong())
    val cursor = context.contentResolver.query(uri, arrayOf(ContactsContract.CommonDataKinds.Photo.PHOTO), null, null, null)

    try {
        var thumbnail: Bitmap? = null
        if (cursor!!.moveToFirst()) {
            val thumbnailBytes = cursor.getBlob(0)
            if (thumbnailBytes != null) {
                thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)
            }
        }
        return thumbnail
    } finally {
        cursor!!.close()
    }
}

fun Tool.openPhoto(context: Context, number: String): Bitmap? {
    print(number)
    val contactId = Tool.getContactIDFromNumber(context, number)
    val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
    val photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)

    val cursor = context.contentResolver.query(photoUri, arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null) ?: return null
    try {
        if (cursor.moveToFirst()) {
            val data = cursor.getBlob(0)
            if (data != null) {
                return BitmapFactory.decodeStream(ByteArrayInputStream(data))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        cursor.close()
    }
    return null
}

fun Tool.isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        return true
    } catch (e: PackageManager.NameNotFoundException) {
        return false
    }

}

fun Tool.startApp(context: Context, app: AppManager.App) {
    if (app.packageName == "com.benny.openlauncher") {
        LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context)
        CoreHome.consumeNextResume = true
    } else {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setClassName(app.packageName, app.className)
            context.startActivity(intent)

            CoreHome.consumeNextResume = true
        } catch (e: Exception) {
            toast(context, R.string.toast_app_uninstalled)
        }

    }
}

fun Tool.startApp(context: Context, intent: Intent) {
    if (intent.component!!.packageName == "com.benny.openlauncher") {
        LauncherAction.RunAction(LauncherAction.Action.LauncherSettings, context)
        CoreHome.consumeNextResume = true
    } else {
        try {
            context.startActivity(intent)
            CoreHome.consumeNextResume = true
        } catch (e: Exception) {
            toast(context, R.string.toast_app_uninstalled)
        }

    }
}

fun Tool.getStartAppIntent(app: AppManager.App): Intent {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.setClassName(app.packageName, app.className)
    return intent
}

val Tool.btnColorMaskController: View.OnTouchListener
    get() = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().scaleY(1.1f).scaleX(1.1f).duration = 50
                (v as TextView).setTextColor(Color.rgb(200, 200, 200))
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                v.animate().scaleY(1f).scaleX(1f).duration = 50
                (v as TextView).setTextColor(Color.WHITE)
                return@OnTouchListener false
            }
        }
        false
    }

fun Tool.writeToFile(name: String, data: String, context: Context) {
    try {
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput(name, Context.MODE_PRIVATE))
        outputStreamWriter.write(data)
        outputStreamWriter.close()
    } catch (ignore: IOException) {
        // do nothing
    }

}

@Throws(Exception::class)
private fun Tool.convertStreamToString(`is`: InputStream): String {
    val reader = BufferedReader(InputStreamReader(`is`))
    val sb = StringBuilder()
    var line: String? = reader.readLine()
    while (line != null) {
        sb.append(line).append("\n")
        line = reader.readLine()
    }
    reader.close()
    return sb.toString()
}

fun Tool.getStringFromFile(name: String, context: Context): String? {
    try {
        val fin = context.openFileInput(name)
        val ret = convertStreamToString(fin)
        fin.close()
        return ret
    } catch (e: Exception) {
        return null
    }
}

fun Tool.wrapColorTag(str: String, @ColorInt color: Int): String {
    return "<font color='" + String.format("#%06X", 0xFFFFFF and color) + "'>" + str + "</font>"
}

@SuppressLint("DefaultLocale")
fun Tool.getRAM_Info(context: Context): String {
    val actManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)

    return String.format("<big><big><b>%s</b></big></big><br\\>%s / %s",
            context.getString(R.string.memory),
            Formatter.formatFileSize(context, memInfo.availMem),
            Formatter.formatFileSize(context, memInfo.totalMem)
    )
}

@SuppressLint("DefaultLocale")
fun Tool.getStorage_Info(context: Context): String {
    val externalFilesDir = Environment.getExternalStorageDirectory() ?: return "?"
    val stat = StatFs(externalFilesDir.path)
    val blockSize = stat.blockSize.toLong()
    return String.format("<big><big><b>%s</b></big></big><br\\>%s / %s",
            context.getString(R.string.storage),
            Formatter.formatFileSize(context, blockSize * stat.availableBlocks),
            Formatter.formatFileSize(context, blockSize * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) stat.blockCountLong else stat.blockCount.toLong())
    )
}

val Tool.oL_LauncherIcon: Int
    @DrawableRes
    get() = if (BuildConfig.IS_TEST_BUILD) R.drawable.ic_launcher_nightly else R.drawable.ic_launcher

fun Tool.copy(context: Context, stringIn: String, stringOut: String) {
    try {
        val desktopData = File(stringOut)
        desktopData.delete()
        val dockData = File(stringOut)
        dockData.delete()
        val generalSettings = File(stringOut)
        generalSettings.delete()
        print("deleted")

        val inputStream = FileInputStream(stringIn)
        val out = FileOutputStream(stringOut)

        val buffer = ByteArray(1024)
        var read: Int = inputStream.read(buffer)
        while (read != -1) {
            out.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
        inputStream.close()

        // write the output file
        out.flush()
        out.close()
        print("copied")

    } catch (e: Exception) {
        Toast.makeText(context, R.string.dialog__backup_app_settings__error, Toast.LENGTH_SHORT).show()
    }
}

class ToolExtension {}


