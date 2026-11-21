package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

object AdMobConfig {
    // Official Google AdMob Test Ad Unit IDs (used during development/debug)
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_REWARDED_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_REWARDED_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
    const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // User's Production StudyWin India AdMob Ad Unit IDs
    const val PROD_BANNER_UNIT_ID = "ca-app-pub-6545218519929279/1965994726"
    const val PROD_REWARDED_UNIT_ID = "ca-app-pub-6545218519929279/9899043803"
}

private data class PendingRewardedShow(
    val activityRef: WeakReference<Activity>,
    val userId: String,
    val placement: String,
    val onRewardEarned: (rewardToken: String, unitId: String) -> Unit,
    val onAdSkippedOrClosed: () -> Unit,
    val onAdUnavailable: (String) -> Unit
)

class AdMobManager private constructor() {
    private val initStarted = AtomicBoolean(false)
    private val sdkReady = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var appContext: Context? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var pendingShow: PendingRewardedShow? = null
    private var preloadRetryCount = 0
    private var pendingTimeoutRunnable: Runnable? = null

    private val _useTestAds = MutableStateFlow(true)
    val useTestAds: StateFlow<Boolean> = _useTestAds.asStateFlow()

    private val _bannerUnitId = MutableStateFlow(AdMobConfig.PROD_BANNER_UNIT_ID)
    private val _rewardedUnitId = MutableStateFlow(AdMobConfig.PROD_REWARDED_UNIT_ID)
    private val _interstitialUnitId = MutableStateFlow(AdMobConfig.TEST_INTERSTITIAL_UNIT_ID)

    private val _isRewardedLoaded = MutableStateFlow(false)
    val isRewardedLoaded: StateFlow<Boolean> = _isRewardedLoaded.asStateFlow()

    private val _isRewardedLoading = MutableStateFlow(false)
    val isRewardedLoading: StateFlow<Boolean> = _isRewardedLoading.asStateFlow()

    private val _lastAdStatus = MutableStateFlow("Initializing AdMob SDK...")
    val lastAdStatus: StateFlow<String> = _lastAdStatus.asStateFlow()

    private val _noFillEncountered = MutableStateFlow(false)
    val noFillEncountered: StateFlow<Boolean> = _noFillEncountered.asStateFlow()

    fun initializeOnce(context: Context) {
        val app = context.applicationContext
        appContext = app
        if (initStarted.compareAndSet(false, true)) {
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfig)
            try {
                MobileAds.initialize(app) {
                    sdkReady.set(true)
                    Log.d(TAG, "Google Mobile Ads SDK initialized")
                    preloadRewardedAd(app)
                    preloadInterstitial(app)
                }
            } catch (e: Exception) {
                sdkReady.set(true)
                _lastAdStatus.value = "AdMob init error: ${e.localizedMessage}"
                Log.e(TAG, "MobileAds.initialize failed", e)
            }
            mainHandler.postDelayed({
                if (sdkReady.compareAndSet(false, true)) {
                    preloadRewardedAd(app)
                }
            }, 3500L)
        }
    }

    fun updateAdConfig(
        useTestAds: Boolean,
        bannerId: String,
        rewardedId: String,
        interstitialId: String
    ) {
        val prevTest = _useTestAds.value
        val prevRewarded = _rewardedUnitId.value
        _useTestAds.value = useTestAds
        if (bannerId.isNotBlank()) _bannerUnitId.value = bannerId
        if (rewardedId.isNotBlank()) _rewardedUnitId.value = rewardedId
        if (interstitialId.isNotBlank()) _interstitialUnitId.value = interstitialId

        if (prevTest != useTestAds || (rewardedId.isNotBlank() && prevRewarded != rewardedId)) {
            rewardedAd = null
            rewardedInterstitialAd = null
            _isRewardedLoaded.value = false
            _isRewardedLoading.value = false
            preloadRetryCount = 0
            appContext?.let { preloadRewardedAd(it) }
        }
    }

    fun activeBannerUnitId(): String =
        if (_useTestAds.value) AdMobConfig.TEST_BANNER_UNIT_ID else _bannerUnitId.value

    fun activeRewardedUnitId(): String =
        if (_useTestAds.value) AdMobConfig.TEST_REWARDED_UNIT_ID else _rewardedUnitId.value

    fun activeInterstitialUnitId(): String =
        if (_useTestAds.value) AdMobConfig.TEST_INTERSTITIAL_UNIT_ID else _interstitialUnitId.value

    fun preloadRewardedAd(context: Context, userId: String = "", placement: String = "general") {
        appContext = context.applicationContext
        if (!initStarted.get()) {
            initializeOnce(context.applicationContext)
            return
        }
        loadRewardedInternal(context, userId, placement)
    }

    private fun cancelPendingTimeout() {
        pendingTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingTimeoutRunnable = null
    }

    private fun loadRewardedInternal(loadContext: Context, userId: String, placement: String) {
        if (_isRewardedLoading.value || rewardedAd != null || rewardedInterstitialAd != null) return
        _isRewardedLoading.value = true
        val unitId = activeRewardedUnitId()
        _lastAdStatus.value = "Loading rewarded ad ($unitId)..."
        val adRequest = AdRequest.Builder().build()

        mainHandler.post {
            RewardedAd.load(
                loadContext,
                unitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        cancelPendingTimeout()
                        _noFillEncountered.value = false
                        if (userId.isNotBlank()) {
                            val ssv = ServerSideVerificationOptions.Builder()
                                .setUserId(userId)
                                .setCustomData(placement)
                                .build()
                            ad.setServerSideVerificationOptions(ssv)
                        }
                        rewardedAd = ad
                        _isRewardedLoading.value = false
                        _isRewardedLoaded.value = true
                        preloadRetryCount = 0
                        _lastAdStatus.value = "Rewarded Ad ready"

                        val pending = pendingShow
                        pendingShow = null
                        val pendingActivity = pending?.activityRef?.get()
                        if (pending != null && pendingActivity != null && !pendingActivity.isFinishing && !pendingActivity.isDestroyed) {
                            presentRewardedAd(ad, pendingActivity, pending)
                        }
                    }

                    override fun onAdFailedToLoad(loadError: LoadAdError) {
                        rewardedAd = null
                        val detail = "Ad error (${loadError.code}): ${loadError.message}"
                        Log.w(TAG, "Rewarded ad failed to load [$unitId]: $detail")

                        // Fallback to RewardedInterstitial Test Ad if RewardedAd returns No Fill (code 3) in Test Mode
                        if (_useTestAds.value && loadError.code == AdRequest.ERROR_CODE_NO_FILL) {
                            loadRewardedInterstitialFallback(loadContext, userId, placement, loadError)
                            return
                        }

                        finishLoadFailure(loadError, unitId, userId, placement, loadContext)
                    }
                }
            )
        }
    }

    private fun loadRewardedInterstitialFallback(
        loadContext: Context,
        userId: String,
        placement: String,
        primaryError: LoadAdError
    ) {
        val fallbackUnit = AdMobConfig.TEST_REWARDED_INTERSTITIAL_UNIT_ID
        RewardedInterstitialAd.load(
            loadContext,
            fallbackUnit,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    cancelPendingTimeout()
                    _noFillEncountered.value = false
                    if (userId.isNotBlank()) {
                        ad.setServerSideVerificationOptions(
                            ServerSideVerificationOptions.Builder()
                                .setUserId(userId)
                                .setCustomData(placement)
                                .build()
                        )
                    }
                    rewardedInterstitialAd = ad
                    _isRewardedLoading.value = false
                    _isRewardedLoaded.value = true
                    preloadRetryCount = 0
                    _lastAdStatus.value = "Rewarded Interstitial Ad ready"

                    val pending = pendingShow
                    pendingShow = null
                    val pendingActivity = pending?.activityRef?.get()
                    if (pending != null && pendingActivity != null && !pendingActivity.isFinishing && !pendingActivity.isDestroyed) {
                        presentRewardedInterstitialAd(ad, pendingActivity, pending, fallbackUnit)
                    }
                }

                override fun onAdFailedToLoad(fallbackError: LoadAdError) {
                    rewardedInterstitialAd = null
                    finishLoadFailure(primaryError, activeRewardedUnitId(), userId, placement, loadContext)
                }
            }
        )
    }

    private fun finishLoadFailure(
        loadError: LoadAdError,
        unitId: String,
        userId: String,
        placement: String,
        loadContext: Context
    ) {
        cancelPendingTimeout()
        _isRewardedLoading.value = false
        _isRewardedLoaded.value = false
        _noFillEncountered.value = (loadError.code == AdRequest.ERROR_CODE_NO_FILL)
        _lastAdStatus.value = "Ad error (${loadError.code}): ${loadError.message}"

        val pending = pendingShow
        pendingShow = null
        if (pending != null) {
            val hint = when {
                !_useTestAds.value && loadError.code in listOf(0, 1, 3) ->
                    "Live Ad Unit $unitId returned '${loadError.message}' (code ${loadError.code}). Turn Test Ads ON in Admin Panel for development, or configure your ~ AdMob App ID in AndroidManifest & publish on Play Store."
                loadError.code == AdRequest.ERROR_CODE_NO_FILL ->
                    "Google AdMob returned No Fill (code 3) on this emulator/network. Test on a real Android phone with updated Play Services and no Ad-Blocker/Private DNS."
                else ->
                    "Rewarded ad unavailable (${loadError.message}, code ${loadError.code}). Ensure Play Services & internet are active."
            }
            pending.onAdUnavailable(hint)
        } else if (preloadRetryCount < 3) {
            preloadRetryCount += 1
            val delayMs = 2500L * preloadRetryCount
            mainHandler.postDelayed({
                loadRewardedInternal(loadContext.applicationContext, userId, placement)
            }, delayMs)
        }
    }

    /**
     * Strictly enforces AdMob Rewarded Ad completion before triggering [onRewardEarned].
     * If an ad is not yet cached when tapped, loads on-demand using the Activity context and auto-displays.
     */
    fun showRewardedAd(
        activity: Activity?,
        userId: String,
        placement: String,
        onRewardEarned: (rewardToken: String, unitId: String) -> Unit,
        onAdSkippedOrClosed: () -> Unit,
        onAdUnavailable: (String) -> Unit,
        onAdLoading: () -> Unit = {}
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onAdUnavailable("Activity unavailable for displaying ad")
            return
        }
        initializeOnce(activity)

        val request = PendingRewardedShow(
            activityRef = WeakReference(activity),
            userId = userId,
            placement = placement,
            onRewardEarned = onRewardEarned,
            onAdSkippedOrClosed = onAdSkippedOrClosed,
            onAdUnavailable = onAdUnavailable
        )

        val ad = rewardedAd
        if (ad != null) {
            presentRewardedAd(ad, activity, request)
            return
        }
        val fallbackAd = rewardedInterstitialAd
        if (fallbackAd != null) {
            presentRewardedInterstitialAd(
                fallbackAd,
                activity,
                request,
                AdMobConfig.TEST_REWARDED_INTERSTITIAL_UNIT_ID
            )
            return
        }

        pendingShow = request
        onAdLoading()

        cancelPendingTimeout()
        val timeout = Runnable {
            val timedOutPending = pendingShow
            if (timedOutPending != null) {
                pendingShow = null
                _isRewardedLoading.value = false
                timedOutPending.onAdUnavailable(
                    "Ad request timed out (${_lastAdStatus.value}). Check device internet / Play Services."
                )
            }
        }
        pendingTimeoutRunnable = timeout
        mainHandler.postDelayed(timeout, 12_000L)

        loadRewardedInternal(activity, userId, placement)
    }

    private fun presentRewardedAd(
        ad: RewardedAd,
        activity: Activity,
        request: PendingRewardedShow
    ) {
        rewardedAd = null
        _isRewardedLoaded.value = false
        val unitId = activeRewardedUnitId()
        if (request.userId.isNotBlank()) {
            ad.setServerSideVerificationOptions(
                ServerSideVerificationOptions.Builder()
                    .setUserId(request.userId)
                    .setCustomData(request.placement)
                    .build()
            )
        }

        val rewardEarned = AtomicBoolean(false)
        val rewardToken = UUID.randomUUID().toString()

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewardedAd(activity, request.userId, request.placement)
                if (rewardEarned.get()) {
                    request.onRewardEarned(rewardToken, unitId)
                } else {
                    request.onAdSkippedOrClosed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                preloadRewardedAd(activity, request.userId, request.placement)
                request.onAdUnavailable("Ad failed to display: ${adError.message}")
            }
        }

        mainHandler.post {
            ad.show(activity) {
                rewardEarned.compareAndSet(false, true)
            }
        }
    }

    private fun presentRewardedInterstitialAd(
        ad: RewardedInterstitialAd,
        activity: Activity,
        request: PendingRewardedShow,
        unitId: String
    ) {
        rewardedInterstitialAd = null
        _isRewardedLoaded.value = false
        if (request.userId.isNotBlank()) {
            ad.setServerSideVerificationOptions(
                ServerSideVerificationOptions.Builder()
                    .setUserId(request.userId)
                    .setCustomData(request.placement)
                    .build()
            )
        }

        val rewardEarned = AtomicBoolean(false)
        val rewardToken = UUID.randomUUID().toString()

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewardedAd(activity, request.userId, request.placement)
                if (rewardEarned.get()) {
                    request.onRewardEarned(rewardToken, unitId)
                } else {
                    request.onAdSkippedOrClosed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                preloadRewardedAd(activity, request.userId, request.placement)
                request.onAdUnavailable("Ad failed to display: ${adError.message}")
            }
        }

        mainHandler.post {
            ad.show(activity) {
                rewardEarned.compareAndSet(false, true)
            }
        }
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || !sdkReady.get()) return
        val unitId = activeInterstitialUnitId()
        mainHandler.post {
            InterstitialAd.load(
                context.applicationContext,
                unitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        }
    }

    fun showInterstitialIfReady(activity: Activity?, onDone: () -> Unit) {
        val ad = interstitialAd
        if (activity == null || ad == null) {
            onDone()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitial(activity)
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onDone()
            }
        }
        ad.show(activity)
    }

    companion object {
        private const val TAG = "AdMobManager"

        @Volatile
        private var INSTANCE: AdMobManager? = null

        fun getInstance(): AdMobManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdMobManager().also { INSTANCE = it }
            }
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Reusable AdMob Banner composable that handles lifecycle pause/resume/destroy cleanly
 * and collapses without errors if an ad fails to load.
 */
@Composable
fun AdBannerFooter(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    var adFailed by remember(adUnitId) { mutableStateOf(false) }
    if (adFailed) return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        adFailed = true
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            adView.resume()
        },
        onRelease = { adView ->
            adView.pause()
            adView.destroy()
        }
    )
}
