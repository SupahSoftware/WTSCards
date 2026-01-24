package com.wtscards.util

object VersionInfo {
    private val versionValue: String by lazy {
        loadProperty("app.version") ?: "1.0.0"
    }
    
    private val gitHashValue: String by lazy {
        loadProperty("app.gitHash") ?: "unknown"
    }
    
    fun getVersion(): String = versionValue
    
    fun getGitHash(): String = gitHashValue
    
    fun getFullVersion(): String = "$versionValue-$gitHashValue"
    
    private fun loadProperty(key: String): String? {
        return try {
            val inputStream = VersionInfo::class.java.classLoader
                .getResourceAsStream("version.properties")
                ?: return null
            
            inputStream.use { stream ->
                val properties = java.util.Properties()
                properties.load(stream)
                properties.getProperty(key)
            }
        } catch (e: Exception) {
            null
        }
    }
}
