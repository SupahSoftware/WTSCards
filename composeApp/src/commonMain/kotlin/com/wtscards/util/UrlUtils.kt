package com.wtscards.util

import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder

object UrlUtils {
    fun getEbaySoldListingsUrl(searchTerm: String): String {
        val encodedTerm = URLEncoder.encode(searchTerm, "UTF-8")
        return "https://www.ebay.com/sch/i.html?_nkw=$encodedTerm&LH_Sold=1"
    }

    fun openInBrowser(url: String) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openEbaySoldListings(searchTerm: String) {
        openInBrowser(getEbaySoldListingsUrl(searchTerm))
    }

    fun getSportsCardProUrl(searchTerm: String): String {
        val encodedTerm = URLEncoder.encode(searchTerm, "UTF-8")
        return "https://www.sportscardspro.com/search-products?type=prices&q=$encodedTerm&go=Go"
    }

    fun openSportsCardPro(searchTerm: String) {
        openInBrowser(getSportsCardProUrl(searchTerm))
    }
}
