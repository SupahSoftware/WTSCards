package com.wtscards.util

object VersionInfo {
    private val versionValue: String by lazy {
        loadProperty("app.version") ?: "1.0.0"
    }
    
    private val gitHashValue: String by lazy {
        loadProperty("app.gitHash") ?: "unknown"
    }
    
    /**
     * Returns the version string (e.g., "1.0.1")
     */
    fun getVersion(): String = versionValue
    
    /**
     * Returns the git hash (e.g., "6c654c3")
     */
    fun getGitHash(): String = gitHashValue
    
    /**
     * Returns the full version string with git hash (e.g., "1.0.1-6c654c3")
     */
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
