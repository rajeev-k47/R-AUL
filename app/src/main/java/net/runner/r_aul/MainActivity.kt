package net.runner.r_aul

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException


object Raul{
    private lateinit var Url: String

    fun init(githubUserName:String,repoName:String){
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
                        val appVersion ="v${getAppVersion(context).first}.${getAppVersion(context).second}"
                        if(latestTag!=appVersion){
                            CoroutineScope(Dispatchers.Main).launch {
                                showUpdateDialog(context, Url)
                            }
                        }
                    }
                } else {

                }
            }
        })
    }

    private fun DownloadLatest(){

    }
    private fun getAppVersion(context: Context): Pair<String, Int> {
        val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        return Pair(packageInfo.versionName, packageInfo.versionCode)
    }

    private fun showUpdateDialog(context: Context, updateUrl: String) {
        AlertDialog.Builder(context)
            .setTitle("Update Available")
            .setMessage("A new version is available. Please update.")
            .setPositiveButton("Update") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                context.startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show()
    }

}