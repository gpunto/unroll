package com.gianmarcodavid.unroll

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var unrolledLink = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        share_btn.setOnClickListener {
            if (unrolledLink.isNotEmpty()) share(unrolledLink)
        }

        intent.process()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.process()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LINK, unrolledLink)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        unrolledLink = savedInstanceState.getString(LINK).orEmpty()
    }

    private fun Intent.process() {
        PATTERN_TWEET_LINK
            .takeIf { action == Intent.ACTION_SEND && type == "text/plain" }
            ?.matchEntire(getStringExtra(Intent.EXTRA_TEXT).orEmpty())
            ?.groupValues
            ?.getOrNull(1)
            ?.let { tweetId -> SHARE_URL.format(tweetId) }
            ?.also(::share)
            .orEmpty()
            .let(::updateUI)
    }

    private fun updateUI(unrolledLink: String) {
        this.unrolledLink = unrolledLink
        url.text = unrolledLink.takeIf(String::isNotEmpty) ?: getString(R.string.no_link)
        share_btn.isVisible = unrolledLink.isNotEmpty()
    }

    private fun share(unrolledLink: String) {
        ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setChooserTitle("Share unrolled thread")
            .setText(unrolledLink)
            .startChooser()
    }

    companion object {
        private val PATTERN_TWEET_LINK = "http[s]://twitter.com/.+/status/(.+)".toRegex()
        private const val SHARE_URL = "https://threadreaderapp.com/thread/%s.html"
        private const val LINK = "LINK"
    }
}

