package net.runner.r_aul

import android.content.Context

class ReminderManager {
    companion object {
        fun initReminder(context: Context,reminders:Int,githubVersion:String) {
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(githubVersion,reminders.toString())
            editor.apply()
        }
        fun reminderExists(context: Context,githubVersion:String): Boolean {
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            return sharedPreferences.contains(githubVersion)
        }
        fun removeReminder(context: Context,githubVersion:String) {
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val current = sharedPreferences.getString(githubVersion,"")
            val new = current?.toInt()?.minus(1)
            if (new != null) {
                val editor = sharedPreferences.edit()
                if(new> -1){
                    editor.putString(githubVersion,new.toString())
                    editor.apply()
                }
            }
        }
        fun verifyReminder(context: Context,githubVersion:String): Boolean {
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val current = sharedPreferences.getString(githubVersion,"")?.toInt()
            if (current != null) {
                return current!=0
            }
            return false
        }
    }

}