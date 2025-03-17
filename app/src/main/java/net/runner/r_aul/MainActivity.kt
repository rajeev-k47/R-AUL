package net.runner.r_aul

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException


object Raul{
    private lateinit var Url: String
    private var gituname = ""
    private var repo = ""
    private var updateUrl = ""

    fun init(githubUserName:String,repoName:String){
        gituname=githubUserName
        repo=repoName
        Url = "https://api.github.com/repos/$githubUserName/$repoName/tags"
    }

    fun listen(context: Context){
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(Url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(response.body?.string())
                    var latestTag =""
                    if (jsonArray.length() > 0) latestTag=jsonArray.getJSONObject(0).getString("name") else latestTag=""
                    if(latestTag.isNotEmpty()){

                        val client1 = OkHttpClient()
                        val request1 = Request.Builder()
                            .url("https://api.github.com/repos/$gituname/$repo/releases/tags/$latestTag")
                            .build()

                        val respons = client1.newCall(request1).execute()
                        val jsonObject = JSONObject(respons.body?.string())
                        val assets = jsonObject.getJSONArray("assets")
                        if(assets.length()>0){
                            updateUrl = assets.getJSONObject(assets.length()-1).getString("browser_download_url")
                        }

                        val appVersion ="v${getAppVersion(context).first}.${getAppVersion(context).second}"
                        if(latestTag!=appVersion){
                            CoroutineScope(Dispatchers.Main).launch {
                                showUpdateDialog(context, updateUrl, latestTag)
                            }
                        }
                    }
                } else {

                }
            }
        })
    }

    private fun DownloadLatest(updateUrl: String,context: Context,latestTag: String,onComplete: (File?) -> Unit){
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(updateUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("R-AUL","Response successful")
                    val contentLength = response.body?.contentLength() ?: -1
                    val file = File(context.cacheDir, "${latestTag}.apk")
                    file.outputStream().use { output ->
                        val inputStream = response.body?.byteStream()
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead: Long = 0
                        while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                            totalBytesRead += bytesRead
                            output.write(buffer, 0, bytesRead)
                            val progress = if (contentLength > 0) {
                                ((totalBytesRead * 100) / contentLength).toInt()
                            } else {
                                -1
                            }
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Downloading Update", Toast.LENGTH_SHORT).show()
                        }
                    }
                    onComplete(file)
                } else {
                    Log.d("R-AUL","Response unsuccessful")
                    onComplete(null)
                }
            }
        })

    }
    private fun installApk(context: Context, apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val packageManager = context.packageManager
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:${context.packageName}")
                startActivity(context,intent,null)
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }
    private fun getAppVersion(context: Context): Pair<String, Int> {
        val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        return Pair(packageInfo.versionName!!, packageInfo.versionCode)
    }

    private fun showUpdateDialog(context: Context, updateUrl: String,latestTag:String) {
        AlertDialog.Builder(context)
            .setTitle("Update Available")
            .setMessage("A new version is available. Please update.")
            .setPositiveButton("Update") { _, _ ->
                DownloadLatest(updateUrl, context,latestTag) { file ->
                    file?.let {
                        installApk(context, it)
                    }
                }
            }
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show()
    }

}