package com.example.creativecompetitionaggregator.ui

fun openUrlInBrowser(context: android.content.Context, url: String) {
    try {
        val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context,android.net.Uri.parse(url))
    } catch (e: Exception) {
        try {
            context.startActivity(
                android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
            )
        } catch (ex: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(
                context,
                "Не удалось открыть ссылку.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}