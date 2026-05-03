package com.cam.scanner.scantopdf.android.activities

//import com.cam.scanner.scantopdf.android.pixelnetica.MainIdentity
//import com.cam.scanner.scantopdf.android.pixelnetica.camera.CameraActivity
//import com.pixelnetica.imagesdk.ImageSdkLibrary
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.cam.scanner.scantopdf.android.AppController
import com.cam.scanner.scantopdf.android.BuildConfig
import com.cam.scanner.scantopdf.android.R
import com.cam.scanner.scantopdf.android.SingleTon.PdfSettings
import com.cam.scanner.scantopdf.android.adapters.FavoriteDocumentsAdapter
import com.cam.scanner.scantopdf.android.adapters.FileModelAdapter
import com.cam.scanner.scantopdf.android.adapters.PageSizesAdapter
import com.cam.scanner.scantopdf.android.ads.AdClosed
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask
import com.cam.scanner.scantopdf.android.asynctasks.CreatePdfTask
import com.cam.scanner.scantopdf.android.asynctasks.DeleteFolderOrFileTask
import com.cam.scanner.scantopdf.android.asynctasks.GetFilesTask
import com.cam.scanner.scantopdf.android.asynctasks.GetOcrDocuments
import com.cam.scanner.scantopdf.android.asynctasks.GetTempCompressBitmapFolders
import com.cam.scanner.scantopdf.android.asynctasks.GetTempCompressedBitmapPath
import com.cam.scanner.scantopdf.android.asynctasks.GetValidUrlOrNot
import com.cam.scanner.scantopdf.android.barcodereader.BarcodeReaderActivity
import com.cam.scanner.scantopdf.android.barcodereader.model.ResultBarCode
import com.cam.scanner.scantopdf.android.databinding.ActivityDashboardBinding
import com.cam.scanner.scantopdf.android.db.AndroidDatabaseManager
import com.cam.scanner.scantopdf.android.db.DBHandler
import com.cam.scanner.scantopdf.android.db.SharedPrefsActivity
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener
import com.cam.scanner.scantopdf.android.interfaces.CreateMultipleTempBitmapListener
import com.cam.scanner.scantopdf.android.interfaces.CreateTempBitmapListener
import com.cam.scanner.scantopdf.android.interfaces.FetchOcrDocumentsListener
import com.cam.scanner.scantopdf.android.interfaces.FileOperationListener
import com.cam.scanner.scantopdf.android.interfaces.FileOrFolderDeleteListener
import com.cam.scanner.scantopdf.android.interfaces.GoogleDriveDataDownloadListener
import com.cam.scanner.scantopdf.android.interfaces.GoogleDriveDataUploadListener
import com.cam.scanner.scantopdf.android.interfaces.OnFetchingCompleted
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener
import com.cam.scanner.scantopdf.android.interfaces.OnOfferUrlChecked
import com.cam.scanner.scantopdf.android.interfaces.PDFCreationCallback
import com.cam.scanner.scantopdf.android.models.FileModel
import com.cam.scanner.scantopdf.android.models.ImageToPdfOptions
import com.cam.scanner.scantopdf.android.models.PageSize
import com.cam.scanner.scantopdf.android.models.PdfModel
import com.cam.scanner.scantopdf.android.models.enums.DocumentTypeEnum
import com.cam.scanner.scantopdf.android.pdf.PdfEditorActivity
import com.cam.scanner.scantopdf.android.rest.RegisterToApi
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult
import com.cam.scanner.scantopdf.android.util.Constants
import com.cam.scanner.scantopdf.android.util.FlashScanUtil
import com.cam.scanner.scantopdf.android.util.PrefManager
import com.cam.scanner.scantopdf.android.util.ScanConstants
import com.cam.scanner.scantopdf.android.util.SubscribeToTopic
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.itl.commonres.permissions.PermissionPreference
import com.itl.commonres.permissions.PermissionStatus
import com.itl.commonres.permissions.PermissionUtils
import com.itl.commonres.permissions.PermissionsListSealedClass
import com.itl.commonres.utils.AdsPlacementsEnum
import com.itl.commonres.utils.CommonMethods
import com.itl.commonres.utils.InterstitialAdCappingEnum
import com.itl.commonres.utils.PermissionInterface
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import java.io.File


class HomeActivity : BaseActivity(), View.OnClickListener, OnFetchingCompleted,
    OnItemSelectListener, PDFCreationCallback, FileOrFolderDeleteListener, FileOperationListener,
    OnOfferUrlChecked, PurchasesUpdatedListener, AdClosed,
    PermissionUtils.RequestPermissionsInterface, PermissionInterface, FetchOcrDocumentsListener {
    //    private var mIdentity: MainIdentity? = null
    private lateinit var binding: ActivityDashboardBinding
    private var screenName = "HomeActivity";

    private var flashScanUtil: FlashScanUtil? = null
    private var fetchedFolderNamesList: MutableList<FileModel>? = ArrayList()
    private var fileModelAdapter: FileModelAdapter? = null
    private var favoriteDocumentsAdapter: FavoriteDocumentsAdapter? = null
    private var prefManager: PrefManager? = null
    private var isPdfCreationForSharing = false
    private var dbHandler: DBHandler? = null
    private var billingClient: BillingClient? = null
    private var selectedFileModel: FileModel? = null
    private var mFileModelForSaveToDrive: FileModel? = null
    var fileModelForWaterMark: FileModel? = null
    private var pdfModel: PdfModel? = null
    private var permissionUtils: PermissionUtils? = null

    private var lastClickedTime: Long = 0
    private var deviceHeight = 0
    private var positionForSaveToDrive = -1
    private var navFrom = 0

    private var isNativeAdAlreadyLoaded = false
    private var isMultiplePdfCreationWithCompression = false
    private var sharePdfDirectWithoutOpen = false
    private var isGetAllDataFromDrive = false
    private var isComingFromNotiOffer = false
    private var isPermissionGranted = false

    private var selectedPageSize: String? = null
    private var deviceIdOfInstallTime: String? = null
    private var planIdStr: String? = null
    private var offerUrl: String? = null
    private val permissionList: MutableList<String>? = null

    private var ocrDirectoryPath = "";

    private var isDocumentsPresent = false;
    val finalFileModelList: MutableList<FileModel> = ArrayList()
    private val REQUEST_CODE_FETCH_OCR_DOCUMENTS: Int = 301
    private var isCheckPermissionResultOnly = false
    private var click: View? = null

    var fileModelListForWaterMark: MutableList<FileModel>? = ArrayList()
        get() {
            if (field == null) {
                field = ArrayList()
            }
            return field
        }
        set(fileModelListForWaterMark) {
            if (!field!!.isEmpty()) {
                field!!.clear()
            }
            field!!.addAll(fileModelListForWaterMark!!)/*this.fileModelListForWaterMark = fileModelListForWaterMark;*/
        }

    var pdfFileNameForMultipleDocs: String? = null
        get() {
            if (TextUtils.isEmpty(field)) {
                field = flashScanUtil?.fileDateFormatName + getString(R.string.suffix_app_name)
            }
            return field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "onCreate")
        setLayoutDimensions()
        getIntents()
        initObjects()
        clickListeners()
        setupRecyclerViewLayoutManager()
        initVars()

        if (Constants.IS_OWN_API_IMPLEMENT && deviceIdOfInstallTime == null) {
            registerToApi()
        }

        /*isPermissionGranted = checkAndGrantPermissions()
        if (isPermissionGranted) {
            fetchFiles()
        }*/
        //loadExitNativeAd();
//        topicSubscription()

        //        firebaseRemoteConfig();
        if (!prefManager!!.isAppAdFree && flashScanUtil!!.isConnectingToInternet) {
            callExitNativeAd()
        }

        dbVisibility()
        planPaidSuccess()

        /*offerUrl = "https://astroproducts.s3.amazonaws.com/hor/appads/andr/html/TarotLife_Offer_Ask_Tarot_1_en.html";

        boolean urlValid = util.urlValidOrNot(offerUrl);*/

        //For testing only
//        openOfferActivity();
        if (isComingFromNotiOffer) {
            if (!TextUtils.isEmpty(offerUrl) && (offerUrl?.length ?: 0) > 10) {
                GetValidUrlOrNot(this, this).execute(offerUrl)
            } else {
                openPlanScreenWithoutOfferUrl()
            }
        }
    }

    private fun initVars() {
        deviceIdOfInstallTime = prefManager!!.deviceIdOfInstallTime
        offerUrl = prefManager!!.offerUrlServer

        Log.i(TAG, "offerUrl: $offerUrl")
    }

    private fun openPlanScreenWithoutOfferUrl() {
        var defaultIntent: Intent? = null
        var planId = 0
        if (planIdStr != null) {
            try {
                planId = planIdStr!!.toInt()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        if (planId == Constants.PLAN_PEMIUM_YEARLY) {
            if (prefManager!!.isPremiumYearly /* || prefManager.isPremiumQuarterly()*/) {
                defaultIntent = Intent(this, PremiumActivity::class.java)
            }/* else{
                defaultIntent = new Intent(this, CurrentPlanActivity.class);
            }*/
        } else if (planId == Constants.PLAN_OCR_MONTHLY) {
            defaultIntent = Intent(this, OcrPlanDialog::class.java)
        }

        if (defaultIntent != null) {
            startActivity(defaultIntent)
        }
    }

    private fun openOfferActivity() {
        val intent = Intent(this, OfferActivity::class.java)
        intent.putExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF, planIdStr)
        startActivity(intent)
    }

    private fun topicSubscription() {
        val subscribeToTopic = SubscribeToTopic(this)

        /*if (BuildConfig.DEBUG) {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.DEBUG_HOME)
        }*/
        subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.HOME)

        if (!prefManager!!.isUnsubscribedFromFree) {
            subscribeToTopic.doSubscribeToTopic(Constants.SubscribeToTopic.FREE)
            subscribeToTopic.doSubscribeToTestTopic(Constants.SubscribeToTopic.FREE_TEST)
            Log.i(TAG, "Subscribed to Free and free_test")
        }
    }

    private fun planPaidSuccess() {
        var successMsg: String? = null
        if (navFrom == Constants.PLAN_PEMIUM_YEARLY) {
            successMsg =
                getString(R.string.premium_yearly_success_msg, getString(R.string.app_name))
        } else if (navFrom == Constants.PLAN_OCR_MONTHLY) {
            successMsg = getString(R.string.ocr_monthly_success_msg, getString(R.string.app_name))
        }
        if (successMsg != null) {
            flashScanUtil!!.showSnackBar(findViewById(android.R.id.content), successMsg)
        }
    }

    private fun getIntents() {
        intent?.let {
            navFrom = intent.getIntExtra(Constants.EXTRA_PLAN_PAID_SUCCESS, 0)
            if (it.hasExtra(Constants.EXTRA_BACKSTACKOFFER)) {
                isComingFromNotiOffer =
                    intent.getBooleanExtra(Constants.EXTRA_BACKSTACKOFFER, false)
                planIdStr = intent.getStringExtra(Constants.EXTRA_PLAN_ID_IN_NOTIF)

                Log.i(TAG, "isComingFromNotiOffer: $isComingFromNotiOffer, planIdStr$planIdStr")
            }
        }
    }

    private fun registerToApi() {
        if (flashScanUtil?.isConnectingToInternet == true) {
            val registerToApi = RegisterToApi(this, object : OnApiResult {
                override fun onApiResponse() {
                    Log.i(TAG, "onApiResponse")
                }

                override fun onApiFailure() {
                    Log.i(TAG, "onApiFailure")
                }
            })
            registerToApi.doRegister()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")

//        showPremium()
//        showRecentsRv(fetchedFileList)
//        plansVisibilityInNav()

        if (isStoragePermissionGranted()) {
            fetchFiles()
            processForPermissions(true)
        }
        /*val firebaseRemoteConfigOperations = FirebaseRemoteConfigOperations(this, this)
        firebaseRemoteConfigOperations.firebaseRemoteConfig()*/
        val isSplashAdShowStatus = prefManager!!.showAppOpenAd()
        Log.e(TAG, "isSplashAdShowStatus$isSplashAdShowStatus")
    }

    private fun showPremium() {
        if (prefManager!!.isAppAdFree) {
            binding.toolbar.ivPremiumCrown.visibility = View.VISIBLE
        } else {
            binding.toolbar.ivPremiumCrown.visibility = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent")
    }

    private fun dbVisibility() {
        val selfAndroidId = Settings.Secure.getString(
            baseContext.contentResolver, Settings.Secure.ANDROID_ID
        )

        Log.i(TAG, "self android_id: $selfAndroidId")

        if (dbHandler!!.existDevicesAllowed(selfAndroidId)) {
            binding.toolbar.ivNavDb.visibility = View.VISIBLE
            binding.toolbar.ivNavPrefs.visibility = View.VISIBLE
        } else {
            binding.toolbar.ivNavDb.visibility = View.INVISIBLE
            binding.toolbar.ivNavPrefs.visibility = View.INVISIBLE
        }
    }

    private fun setLayoutDimensions() {
        val displayMetrics = DisplayMetrics()
        val windowmanager: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowmanager.defaultDisplay?.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels

        deviceHeight = displayMetrics.heightPixels
    }

    private fun fetchFiles() {
        // get files and set adapter here
        /*new GetFilesTask(this, "", this, Constants.RECENT_DOCS_COUNT_LIMIT,
                prefManager.getAppSortingOrder()).execute();
*/
        // to Show last three recent files or documents in Recent Documents 'modificationTimeDescending' is explicitly set here as sorting
        GetFilesTask(
            this,
            "",
            this,
            Constants.RECENT_DOCS_COUNT_LIMIT,
            Constants.SORT_BY.modificationTimeDescending
        ).execute()
//        fetchOcrFiles()
    }

    private fun initObjects() {
        flashScanUtil = FlashScanUtil(this)
        prefManager = PrefManager(this)
        prefManager?.isSplashDone = true

        dbHandler = AppController.getINSTANCE().dbHandler

        pdfModel = PdfModel()

        /*mIdentity = ViewModelProvider(this)[MainIdentity::class.java]
        mIdentity!!.loadSettings()*/

        /*val isLoad = ImageSdkLibrary.load(application)

        Log.i(TAG, "Load lib: $isLoad")*/
        permissionUtils = PermissionUtils(this, this, this@HomeActivity)
    }

    private fun clickListeners() {
        binding.apply {
            homeLayout.llBottomBar.tvSaveAsPdf.setOnClickListener(this@HomeActivity)
            homeLayout.llBottomBar.tvShare.setOnClickListener(this@HomeActivity)
            homeLayout.llBottomBar.tvDelete.setOnClickListener(this@HomeActivity)

            homeLayout.llDocScanner.setOnClickListener(this@HomeActivity)
            homeLayout.viewAllDoc.setOnClickListener(this@HomeActivity)
            homeLayout.flOcr.setOnClickListener(this@HomeActivity)
            homeLayout.flBarCode.setOnClickListener(this@HomeActivity)
            homeLayout.fabCamera.setOnClickListener(this@HomeActivity)
            homeLayout.fabMedia.setOnClickListener(this@HomeActivity)
            homeLayout.ivMedia.setOnClickListener(this@HomeActivity)
            homeLayout.ivCamera.setOnClickListener(this@HomeActivity)
            homeLayout.progressLay.root.setOnClickListener(this@HomeActivity)
            homeLayout.llNoDocument.flMedia.setOnClickListener(this@HomeActivity)
            homeLayout.llNoDocument.flCamera.setOnClickListener(this@HomeActivity)

            toolbar.ivNavMenu.setOnClickListener(this@HomeActivity)
            toolbar.ivBack.setOnClickListener(this@HomeActivity)
            toolbar.ivNavDb.setOnClickListener(this@HomeActivity)
            toolbar.ivNavPrefs.setOnClickListener(this@HomeActivity)
            toolbar.ivPremiumCrown.setOnClickListener(this@HomeActivity)
            toolbar.ivSettings.setOnClickListener(this@HomeActivity)
            toolbar.ivSettings.visibility = View.VISIBLE
            toolbar.ivDrive.setOnClickListener(this@HomeActivity)
            toolbar.ivDrive.visibility = View.VISIBLE

        }
    }

    private fun OpenDocScanner() {
        if (!isPermissionGranted) {
            processForPermissions()
            return
        }
        if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
            return
        }
        lastClickedTime = SystemClock.elapsedRealtime()

        if (binding.homeLayout.llBottomBar.root.visibility == View.VISIBLE) {
            hideCheckBoxAndRemoveBottomBar()
        }
        openDocScanner()
    }

    override fun onClick(v: View) {
        click = v
        when (v.id) {
            R.id.viewAllDoc -> {
                OpenDocScanner()
            }
            R.id.ll_doc_scanner -> {
                OpenDocScanner()
            }

            R.id.tv_save_as_pdf -> {
                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                CommonMethods.logCustomFireBaseEvents(
                    "HomeActivity" + "_" + getString(R.string.module_name),
                    com.itl.commonres.utils.Constants.CLICK_PDF_ICON
                )
                var selectedFileModelList: List<FileModel>? = null
                if (fileModelAdapter != null) {
                    selectedFileModelList = fileModelAdapter!!.selectedFileModelList
                }
                if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                    if (selectedFileModelList.size == 1) {
                        selectedFileModel = selectedFileModelList.get(0);
                        createPDF()
                        hideCheckBoxAndRemoveBottomBar()
                    } else {
                        if (Constants.IS_CREATE_PDF_DIRECT) {
                            pdfFileNameForMultipleDocs =
                                flashScanUtil!!.fileDateFormatName + getString(R.string.suffix_app_name)
                            handleMultipleDocPdfCreation(selectedFileModelList, PDF_BY_DIRECT)
                            hideCheckBoxAndRemoveBottomBar()
                        } else {
                            showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_BY_DIRECT)
                        }
                    }
                } else {
                    flashScanUtil!!.showSnackBar(
                        findViewById(android.R.id.content),
                        getString(R.string.please_select_files)
                    )
                }
            }

            R.id.tv_share -> {
                var fileModelList: List<FileModel?>? = null
                if (fileModelAdapter != null) {
                    fileModelList = fileModelAdapter!!.selectedFileModelList
                }
                CommonMethods.logCustomFireBaseEvents(
                    screenName + "_" + getString(R.string.module_name),
                    com.itl.commonres.utils.Constants.CLICK_SHARE_ICON
                )
                if (fileModelList != null && !fileModelList.isEmpty()) {
                    showShareDialog()
                } else {
                    flashScanUtil!!.showSnackBar(
                        findViewById(android.R.id.content), getString(R.string.please_select_files)
                    )
                }
            }

            R.id.tv_delete -> {
                var selectedFileModelList1: List<FileModel?>? = null
                if (fileModelAdapter != null) {
                    selectedFileModelList1 = fileModelAdapter!!.selectedFileModelList
                }
                CommonMethods.logCustomFireBaseEvents(
                    screenName + "_" + getString(R.string.module_name),
                    com.itl.commonres.utils.Constants.CLICK_DELETE_ICON
                )
                if (selectedFileModelList1 != null && !selectedFileModelList1.isEmpty()) {
                    showDeleteDialog(selectedFileModelList1)
                } else {
                    flashScanUtil!!.showSnackBar(
                        findViewById(android.R.id.content), getString(R.string.please_select_files)
                    )
                }
            }

            R.id.fl_ocr -> {
                if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                    return
                }
                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                if (binding.homeLayout.llBottomBar.root.visibility == View.VISIBLE) {
                    hideCheckBoxAndRemoveBottomBar()
                }
                lastClickedTime = SystemClock.elapsedRealtime()
//                flashScanUtil!!.logHomeScreenClickEvent(Constants.FirebaseClickEvents.HOME_SCREEN_OCR)
                openOcrActivity()
            }

            R.id.fl_media, R.id.fab_media, R.id.iv_media -> {
                if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                    return
                }
                lastClickedTime = SystemClock.elapsedRealtime()

                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                if (FlashScanUtil.isOsLessThanR()) {
                    try {
                        Matisse.from(this)
                            .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF), false)
                            .countable(true)
                            .showSingleMediaType(true)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            .thumbnailScale(0.9f).maxSelectable(1000).imageEngine(GlideEngine())
                            .forResult(REQUEST_GET_IMAGES_USING_LIBRARY)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    startActivityForResult(intent, REQUEST_GET_IMAGES_USING_LIBRARY)
                }

            }

            R.id.fl_camera, R.id.fab_camera, R.id.iv_camera -> {
                if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                    return
                }
                lastClickedTime = SystemClock.elapsedRealtime()
                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }/*if (Constants.IS_SHOWING_CAMERA_IN_OWN_APP) {
                    startActivity(new Intent(this, CameraActivity.class));
                } else {
                    startScan(ScanConstants.OPEN_CAMERA);
                }*/
                val fileSink = externalCacheDir

                if (fileSink != null) {
                    if (fileSink.exists() || fileSink.mkdirs()) {
                        val captureIntent = Intent(this, CaptureImagesActivity::class.java)
                        startActivityForResult(captureIntent, TAKE_PHOTO)
                        /*val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, TAKE_PHOTO)*//*val intent = CameraActivity.newIntent(
                            this,
                            mIdentity!!.SdkFactory,
                            fileSink.absolutePath,
                            "camera-prefs",
                            false
                        )
                        startActivityForResult(intent, TAKE_PHOTO)*/
                    }
                }
            }

            R.id.fl_bar_code -> {
                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                if (SystemClock.elapsedRealtime() - lastClickedTime < 1000) {
                    return
                }
                if (binding.homeLayout.llBottomBar.root.visibility == View.VISIBLE) {
                    hideCheckBoxAndRemoveBottomBar()
                }
                lastClickedTime = SystemClock.elapsedRealtime()
//                flashScanUtil!!.logHomeScreenClickEvent(Constants.FirebaseClickEvents.HOME_SCREEN_QR_BARCODE)
                openBarCodeActivity()
            }


            R.id.tv_privacy_policy -> {
                Handler().postDelayed(
                    { openWebViewActivity(Constants.URLs.PRIVACY_POLICY) },
                    Constants.NAV_DRAWER_CLOSE_TIME
                )
            }

            R.id.tv_product_tour -> {
                Handler().postDelayed(
                    { this@HomeActivity.openProductTourActivity() }, Constants.NAV_DRAWER_CLOSE_TIME
                )
            }

            R.id.tv_about_app -> {
                Handler().postDelayed(
                    { this@HomeActivity.openAboutAppActivity() }, Constants.NAV_DRAWER_CLOSE_TIME
                )
            }

            R.id.tv_rate_app -> {
                Handler().postDelayed({ flashScanUtil!!.rateUs() }, Constants.NAV_DRAWER_CLOSE_TIME)
            }

            R.id.tv_share_app -> {
                flashScanUtil!!.shareApp()
            }

            R.id.btn_progress_lay -> {}
            R.id.tv_settings -> {
                Handler().postDelayed({ openSettingsActivity() }, Constants.NAV_DRAWER_CLOSE_TIME)
            }

            R.id.iv_nav_db -> openDbScreen()
            R.id.iv_nav_prefs -> openPrefsScreen()
            R.id.nav_tv_premium_plan -> {
                askToBePremium()
            }

            R.id.nav_tv_ocr_monthly -> {
                askToGetOcrMonthly()
            }

            R.id.tv_g_drive, R.id.iv_drive -> {
                if (flashScanUtil!!.isConnectingToInternet) {
                    allAppUserDataFromGoogleDrive
                } else {
                    Toast.makeText(
                        this,
                        this.resources.getString(R.string.connect_to_internet),
                        Toast.LENGTH_LONG
                    ).show()
                }
//                closeDrawer()
            }

            R.id.iv_back -> {
                onBackPressed()
            }

            R.id.iv_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    private fun askToGetOcrMonthly() {
        val existingEmailInDb = dbHandler!!.getEmail(deviceIdOfInstallTime)
        if (Constants.IS_OWN_API_IMPLEMENT && (existingEmailInDb == null || existingEmailInDb.isEmpty())) {
            openAskEmailActivity(Constants.PLAN_ACTIVITY_OCR_MONTHLY)
        } else {
            openOcrMonthlyDialogActivity()
        }
    }

    private fun openOcrMonthlyDialogActivity() {
        val intent = Intent(this@HomeActivity, OcrPlanDialog::class.java)
        startActivityForResult(intent, Constants.REQUEST_CODE_OCR_MONTHLY)
        //        new OcrPlanDialog(HomeActivity.this).openDialog();
    }

    private fun askToBePremium() {
        val existingEmailInDb = dbHandler!!.getEmail(deviceIdOfInstallTime)
        if (Constants.IS_OWN_API_IMPLEMENT && (existingEmailInDb == null || existingEmailInDb.isEmpty())) {
//            askEmail();
            openAskEmailActivity(Constants.PLAN_ACTIVITY_PREMIUM)
        } else {
            openPremiumActivity()
        }
    }

    private fun connectBillingService() {
        //loadPriceProgress();
        /*billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()

        clearGooglePlayStoreBillingCacheIfPossible()

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                // hidePriceProgress();
                *//*if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    val purchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)
                    if (purchasesResult.purchasesList != null) {
                        val purchases = purchasesResult.purchasesList
                        if (purchases!!.size > 0) {
                            var isPremiumFound = false
                            var isOcrMonthlyFound = false

                            for (purchase in purchases) {
                                if (Constants.PRODUCT_ID_PREMIUM == purchase.sku) {
                                    Log.i(TAG, "premium in restore")
                                    isPremiumFound = true
                                    isYearlyPlanExpired = false
                                } else {
                                    isYearlyPlanExpired = true
                                }

                                if (Constants.PRODUCT_ID_OCR_MONTH == purchase.sku) {
                                    Log.i(TAG, "ocr monthly in restore")
                                    isOcrMonthlyFound = true
                                    isMonthlyOcrExpired = false
                                } else {
                                    isMonthlyOcrExpired = true
                                }
                            }

                            //After loop end
                            prefManager!!.isPremiumYearly = isPremiumFound
                            prefManager!!.isOcrMonthly = isOcrMonthlyFound

                            *//**//*if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                                //openCurrentPlanActivity();
                            }*//**//*
                        } else {
                            prefManager!!.isPremiumYearly = false
                            prefManager!!.isOcrMonthly = false
                            isYearlyPlanExpired = true
                            isMonthlyOcrExpired = true
                            //case when subscription is expired but app not refreshed or killed
                            if (binding.navDrawer.navTvPremiumPlan.text.toString().equals(
                                    resources.getString(R.string.current_plan), ignoreCase = true
                                )
                            ) {
                                flashScanUtil!!.showSnackBar(
                                    findViewById(android.R.id.content),
                                    getString(R.string.subscription_expired)
                                )
                            }
                            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                                openPremiumActivity()
                            }
                        }
                    } else {
//                        isYearlyPlanExpired = true;
//                        isMonthlyOcrExpired = true;
                        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            openPremiumActivity()
                        }
                    }
                }*//*
                plansVisibilityInNav()
            }

            override fun onBillingServiceDisconnected() {
                // hidePriceProgress();
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })*/
    }

    private fun clearGooglePlayStoreBillingCacheIfPossible() {
        /*billingClient!!.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { billingResult, list ->
            Log.i(
                TAG, "onPurchaseHistoryResponse"
            )
        }*/
    }

    private fun openAskEmailActivity(whichPlanActivity: Int) {
        val intent = Intent(this@HomeActivity, AskEmailActivity::class.java)
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity)
        startActivity(intent)
    }

    private fun openPremiumActivity() {
        val intent = Intent(this@HomeActivity, PremiumActivity::class.java)
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY)
    }

    private fun openCurrentPlanActivity() {
        val intent = Intent(this@HomeActivity, CurrentPlanActivity::class.java)
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY)
    }

    private fun openDbScreen() {
        val intent = Intent(this@HomeActivity, AndroidDatabaseManager::class.java)
        startActivity(intent)
    }

    private fun openPrefsScreen() {
        val intent = Intent(this@HomeActivity, SharedPrefsActivity::class.java)
        startActivity(intent)
    }

    private fun showAskPdfNameDialogForMultiDoc(
        selectedFileModelList: List<FileModel>, pdfVia: Int
    ) {
        val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_ask_pdf_name)

        val et_pdf_name = dialog.findViewById<EditText>(R.id.et_pdf_name)
        val btn_done = dialog.findViewById<Button>(R.id.btn_done)
        et_pdf_name.setText(flashScanUtil!!.fileDateFormatName + getString(R.string.suffix_app_name))

        val rbOriginal = dialog.findViewById<RadioButton>(R.id.rb_original)
        val rbCompressed = dialog.findViewById<RadioButton>(R.id.rb_compressed)

        val spinner = dialog.findViewById<Spinner>(R.id.spinner)


        val pageSizeList = flashScanUtil!!.pageSizeList
        if (pageSizeList != null && !pageSizeList.isEmpty()) {
            val pageSizesAdapter = PageSizesAdapter(this, pageSizeList)
            spinner.adapter = pageSizesAdapter
        }
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                val pageSize: PageSize = parent.getItemAtPosition(position) as PageSize
                if (pageSize != null) {
                    selectedPageSize = pageSize.sizeValue
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        btn_done.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (TextUtils.isEmpty(et_pdf_name.text.toString().trim { it <= ' ' })) {
                    Toast.makeText(
                        this@HomeActivity,
                        "" + getString(R.string.please_name_the_pdf),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                if (TextUtils.isEmpty(selectedPageSize)) {
                    Toast.makeText(
                        this@HomeActivity,
                        "" + getString(R.string.please_select_page_size),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                PdfSettings.getInstance().selectedPdfPageSize = selectedPageSize
                pdfFileNameForMultipleDocs = et_pdf_name.text.toString().trim { it <= ' ' }

                if (rbOriginal.isChecked) {
                    isMultiplePdfCreationWithCompression = false
                } else if (rbCompressed.isChecked) {
                    isMultiplePdfCreationWithCompression = true
                }
                handleMultipleDocPdfCreation(selectedFileModelList, pdfVia)
                hideCheckBoxAndRemoveBottomBar()
                dialog.dismiss()
            }
        })

        dialog.show()
    }

    /*private void askEmail() {
        Dialog dialog = new Dialog(this);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_ask_email);

        EditText etEmail = dialog.findViewById(R.id.et_email);
        Button btSubmit = dialog.findViewById(R.id.btn_submit);

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputEmail = etEmail.getText().toString().trim();

                if (TextUtils.isEmpty(inputEmail)) {
                    Toast.makeText(this, "" + getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!checkEmail(inputEmail)) {
                    Toast.makeText(this, "" + getString(R.string.enter_valid_email_address), Toast.LENGTH_SHORT).show();
                    return;
                }

                openPremiumActivity();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static boolean checkEmail(String email) {
        return !(email == null || TextUtils.isEmpty(email)) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }*/
    private fun handleMultipleDocPdfCreation(selectedFileModelList: List<FileModel>, pdfVia: Int) {
        when (pdfVia) {
            PDF_BY_DIRECT -> if (!prefManager!!.isAppWatermarkFree && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                fileModelListForWaterMark = selectedFileModelList as ArrayList
                goToWaterMarkRemoveActivityForMultipleDocuments()
            } else {
                if (prefManager!!.isAppWatermarkFree || prefManager!!.isPremiumYearly /*|| prefManager.isPremiumQuarterly()*/) {
                    saveAsPdfSelectedDocuments(selectedFileModelList, false)
                } else {
                    saveAsPdfSelectedDocuments(selectedFileModelList, true)
                }
            }

            PDF_VIA_SHARE -> if (!prefManager!!.isAppWatermarkFree && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                fileModelListForWaterMark = selectedFileModelList as ArrayList
                goToWaterMarkRemoveActivityForShareMultipleDocuments()
            } else {
                if (prefManager!!.isAppWatermarkFree/* || prefManager!!.isPremiumYearly  || prefManager.isPremiumQuarterly()*/) {
                    createPdfForShareSelectedDocuments(selectedFileModelList, false)
                } else {
                    createPdfForShareSelectedDocuments(selectedFileModelList, true)
                }
            }
        }
    }

    private fun goToWaterMarkRemoveActivityForMultipleDocuments() {
        val intent = Intent(this, WaterMarkRemoveActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FOR_MULTIPLE_DOCUMENT)
    }

    /*private void showRewardAdDialogForSelectedDocuments(List<FileModel> selectedFileModelList) {
        Dialog dialog = new Dialog(this);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        saveAsPdfSelectedDocuments(selectedFileModelList, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*/ /*
                        saveAsPdfSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        saveAsPdfSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(HomeActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/
    private fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openRateAppActivity() {
        val intent = Intent(this, RateAppActivity::class.java)
        startActivity(intent)
    }

    private fun openAboutAppActivity() {
        val intent = Intent(this, AboutAppActivity::class.java)
        startActivity(intent)
    }

    private fun openProductTourActivity() {
        val intent = Intent(this, ProductTourActivity::class.java)
        startActivity(intent)
    }

    private fun openWebViewActivity(url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(Constants.PutExtraConstants.URL, url)
        startActivity(intent)
    }


    private fun openBarCodeActivity() {/*Intent intent = new Intent(this, BarCodeScanActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);*/

        /*Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(this, true, false);*/
        CommonMethods.logCustomFireBaseEvents(
            screenName, com.itl.commonres.utils.Constants.OPEN_QR_CODE_SCANNER
        )
        val intent = Intent(this, BarcodeReaderActivity::class.java)
        intent.putExtra(BarcodeReaderActivity.KEY_AUTO_FOCUS, true)
        intent.putExtra(BarcodeReaderActivity.KEY_USE_FLASH, false)
        startActivityForResult(intent, BARCODE_READER_ACTIVITY_REQUEST)
    }

    private fun openOcrActivity() {
        CommonMethods.logCustomFireBaseEvents(
            screenName, com.itl.commonres.utils.Constants.OPEN_OCR_SCANNER
        )
        val intent = Intent(this, OcrActivity::class.java)
        intent.putExtra(Constants.PutExtraConstants.IS_COMING_FROM_HOME_DASHBOARD, true)
        startActivity(intent)
//        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    private fun openFavoriteDocumentsActivity() {
        CommonMethods.logCustomFireBaseEvents(
            screenName, com.itl.commonres.utils.Constants.OPEN_MY_FAVORITES
        )
        val intent = Intent(this, FavoriteDocumentsActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FETCH_FAVORITE_DOCUMENTS)
//        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    /*private void showPopUpMoreMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        Field[] fields = popupMenu.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("mPopup".equals(field.getName())) {
                field.setAccessible(true);
                try {
                    Object menuPopupHelper = field.get(popupMenu);
                    if (menuPopupHelper != null) {
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceShowIcon.invoke(menuPopupHelper, true);
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        popupMenu.getMenuInflater().inflate(R.menu.more_popup_menu, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.menu_rename).setVisible(false);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_save_as_pdf:
                        saveAsPdfSelectedDocuments();
                        break;
                    case R.id.menu_share:
                        showShareDialog();
                        break;
                    case R.id.menu_delete:
                        showDeleteDialog();
                        break;
                }
                return true;
            }
        });

        popupMenu.show();
    }*/
    private fun saveAsPdfSelectedDocuments(
        selectedFileModelList: List<FileModel>?, isWaterMarkToBeShown: Boolean
    ) {
        if (isMultiplePdfCreationWithCompression) {
            val selectedFoldersPathList = ArrayList<String>()
            for (fileModel in selectedFileModelList!!) {
                selectedFoldersPathList.add(fileModel.path)
            }
            if (!selectedFoldersPathList.isEmpty()) {
                GetTempCompressBitmapFolders(
                    this,
                    selectedFoldersPathList,
                    object : CreateMultipleTempBitmapListener {
                        override fun onCompressBitmapStart() {
                            binding.homeLayout.progressLay.root.visibility = View.VISIBLE
                        }

                        override fun onCompressBitmapComplete(foldersList: ArrayList<String>) {
                            binding.homeLayout.progressLay.root.visibility = View.GONE
                            if (foldersList != null && !foldersList.isEmpty()) {
                                val filePathList: MutableList<String> = ArrayList()
                                for (folderPath in foldersList) {
                                    val fileOrDirectory = File(folderPath)

                                    if (fileOrDirectory.isDirectory) {
                                        val files = fileOrDirectory.listFiles()
                                        if (files != null && files.size > 0) {
                                            flashScanUtil!!.sortFilesByNameAtoZ(files)
                                            /*val appSortingOrder = prefManager!!.appSortingOrder
                                            when (appSortingOrder) {
                                                Constants.SORT_BY.defaultOrder, Constants.SORT_BY.modificationTimeDescending -> flashScanUtil!!.sortFilesByDescendingLastModified(
                                                    files
                                                )

                                                Constants.SORT_BY.modificationTimeAscending -> flashScanUtil!!.sortFilesByAscendingLastModified(
                                                    files
                                                )

                                                Constants.SORT_BY.nameAtoZ -> flashScanUtil!!.sortFilesByNameAtoZ(
                                                    files
                                                )

                                                Constants.SORT_BY.nameZtoA -> flashScanUtil!!.sortFilesByNameZtoA(
                                                    files
                                                )
                                            }*/
                                            for (file in files) {
                                                filePathList.add(file.path)
                                            }
                                        }
                                    } else {
                                        filePathList.add(fileOrDirectory.path)
                                    }
                                }
                                if (!filePathList.isEmpty()) {
                                    isPdfCreationForSharing = false
                                    // currently not in use
                                    createPdf(
                                        filePathList,
                                        pdfFileNameForMultipleDocs,
                                        isWaterMarkToBeShown
                                    )
                                }
                            }
                        }
                    }).execute()
            }
        } else {
            val filePathList: MutableList<String> = ArrayList()
            for (fileModel in selectedFileModelList!!) {
                if (fileModel != null) {
                    val fileOrDirectory = File(fileModel.path)

                    if (fileOrDirectory.isDirectory) {
                        val files = fileOrDirectory.listFiles()
                        if (files != null && files.size > 0) {
                            flashScanUtil!!.sortFilesByNameAtoZ(files)
                            /*val appSortingOrder = prefManager!!.appSortingOrder
                            when (appSortingOrder) {
                                Constants.SORT_BY.defaultOrder, Constants.SORT_BY.modificationTimeDescending -> flashScanUtil!!.sortFilesByDescendingLastModified(
                                    files
                                )

                                Constants.SORT_BY.modificationTimeAscending -> flashScanUtil!!.sortFilesByAscendingLastModified(
                                    files
                                )

                                Constants.SORT_BY.nameAtoZ -> flashScanUtil!!.sortFilesByNameAtoZ(
                                    files
                                )

                                Constants.SORT_BY.nameZtoA -> flashScanUtil!!.sortFilesByNameZtoA(
                                    files
                                )
                            }*/
                            for (file in files) {
                                if (file.isFile && file.exists()) {
                                    if (file.name.equals(
                                            Constants.JSON_FILE_NAME, ignoreCase = true
                                        )
                                    ) {
                                        continue
                                    }
                                    filePathList.add(file.path)
                                }
                            }
                        }
                    } else {
                        if (fileOrDirectory != null) {
                            if (fileOrDirectory.isFile && fileOrDirectory.exists()) {
                                filePathList.add(fileOrDirectory.path)
                            }
                        }
                    }
                }
            }
            if (!filePathList.isEmpty()) {
                isPdfCreationForSharing = false
                createPdf(filePathList, pdfFileNameForMultipleDocs, isWaterMarkToBeShown)
            } else {
                // show warning dialog
                showNoFilesInDocumentDialog()
            }
        }
    }

    private fun showNoFilesInDocumentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.no_files_in_document_warning_txt).setCancelable(false)
            .setPositiveButton(android.R.string.yes) { dialog, which -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun createPdf(
        imagesUriList: List<String>, pdfFileName: String?, isWaterMarkToBeShown: Boolean
    ) {
        val imageToPdfOptions = ImageToPdfOptions()
        imageToPdfOptions.pageSize = Constants.PdfConstants.DEFAULT_PDF_PAGE_SIZE
        imageToPdfOptions.pageColor = Constants.PdfConstants.DEFAULT_PDF_PAGE_COLOR
        imageToPdfOptions.setMargins(0, 0, 0, 0)
        imageToPdfOptions.pdfQuality = Constants.PdfConstants.DEFAULT_PDF_QUALITY
        imageToPdfOptions.borderWidth = Constants.PdfConstants.DEFAULT_BORDER_WIDTH
        imageToPdfOptions.isWaterMarkAdded = isWaterMarkToBeShown
        imageToPdfOptions.waterMark = flashScanUtil!!.waterMark
        CreatePdfTask(this, pdfFileName, imageToPdfOptions, imagesUriList, this, true).execute()
    }

    private fun openDocScanner() {
        CommonMethods.logCustomFireBaseEvents(
            screenName, com.itl.commonres.utils.Constants.OPEN_DOC_SCANNER
        )
        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        intent.putExtra(Constants.PutExtraConstants.IS_COMING_FROM_HOME_DASHBOARD, true)
        startActivityForResult(intent, REQUEST_CODE_FETCH_ALL_DOCUMENTS)
    }

    override fun onFetchingComplete(fileModelList: List<FileModel>) {
        binding.homeLayout.progressLay.root.visibility = View.GONE

        if (isDocumentsPresent) {
            finalFileModelList.clear()
            if (fileModelAdapter != null) {
                fileModelAdapter?.notifyDataSetChanged()
            }
            fetchedFileList.clear()
            if (favoriteDocumentsAdapter != null) {
                favoriteDocumentsAdapter?.notifyDataSetChanged()
            }
            isDocumentsPresent = false
        }

        if (fileModelList != null && !fileModelList.isEmpty()) {

            if (!fetchedFileList.isEmpty()) {
                fetchedFileList.clear()
            }
            isDocumentsPresent = true
            fetchedFileList.addAll(fileModelList)
            showRecentsRv(fileModelList)
            // temporary for favorites
            showFavoriteRv()
        } else {
            hideRecyclerView()
        }
    }

    override fun onFetchingStartOcr() {
        binding.homeLayout.progressLay.root.visibility = View.VISIBLE
    }

    override fun onFetchingStart() {
        binding.homeLayout.progressLay.root.visibility = View.VISIBLE
    }

    override fun onFetchingCompleted(fileModelList: MutableList<FileModel>?) {
        binding.homeLayout.progressLay.root.visibility = View.GONE
        if (isDocumentsPresent) {
            if (fileModelList != null && !fileModelList.isEmpty()) {
                finalFileModelList.addAll(fileModelList)
                fileModelAdapter?.notifyDataSetChanged()
            }
        } else {
            if (fileModelList != null && !fileModelList.isEmpty()) {

                if (!finalFileModelList.isEmpty()) {
                    finalFileModelList.clear()
                }
                isDocumentsPresent = true
                finalFileModelList.addAll(fileModelList)
                showRecentsRv(fileModelList)
                // temporary for favorites
                showFavoriteRv()
            } else {
                hideRecyclerView()
            }
        }
    }

    /*override fun onFetchingStart() {
        binding.homeLayout.progressLay.root.visibility = View.VISIBLE
    }*/

    private fun showFavoriteRv() {/*if (!favoritesDocsList.isEmpty())
            favoritesDocsList.clear();*/


        val favoritesDocsList: MutableList<FileModel> = ArrayList()
        for (fileModel in fetchedFileList) {
            if (fileModel.isStarred) {
                favoritesDocsList.add(fileModel)
            }
        }

        /* int favDocsCount = favoritesDocsList.size();
        if (favDocsCount <= 0) {
            ll_no_favorite_document.setVisibility(View.VISIBLE);
            rv_favorites.setVisibility(View.GONE);
            return;
        } else {
            ll_no_favorite_document.setVisibility(View.GONE);
            rv_favorites.setVisibility(View.VISIBLE);
        }*/
        if (!favoritesDocsList.isEmpty()) {
            binding.homeLayout.llNoFavoriteDocument.visibility = View.GONE
            binding.homeLayout.rvFavorites.visibility = View.VISIBLE

            val finalFavoritesDocsList: MutableList<FileModel> = ArrayList()
            if (favoritesDocsList.size > Constants.FAVORITE_DOCS_COUNT_LIMIT) {
                for (i in 0 until Constants.FAVORITE_DOCS_COUNT_LIMIT) {
                    finalFavoritesDocsList.add(favoritesDocsList[i])
                }
                finalFavoritesDocsList.add(
                    FileModel(
                        getString(R.string.view_more), ""
                    )
                ) // for view more functionality
            } else {
                finalFavoritesDocsList.addAll(favoritesDocsList)
            }

            //Optimized
            /*favoritesDocsList.addAll(fileModelList);
        if (fileModelList.size() > Constants.FAVORITE_DOCS_COUNT_LIMIT) {
            favoritesDocsList.add(new FileModel(getString(R.string.view_more), ""));  // for view more functionality
        }*/
            ////

            //Rishav code
            /*if (fileModelList.size() > Constants.FAVORITE_DOCS_COUNT_LIMIT) {
            for (int i = 0; i < Constants.FAVORITE_DOCS_COUNT_LIMIT; i++) {
                favoritesDocsList.add(fileModelList.get(i));
            }
            favoritesDocsList.add(new FileModel(getString(R.string.view_more), ""));  // for view more functionality
        } else {
            favoritesDocsList.addAll(fileModelList);
        }*/
            ////
            favoriteDocumentsAdapter = FavoriteDocumentsAdapter(this, finalFavoritesDocsList, this,this)
            binding.homeLayout.rvFavorites.adapter = favoriteDocumentsAdapter

        } else {
            binding.homeLayout.llNoFavoriteDocument.visibility = View.VISIBLE
            binding.homeLayout.rvFavorites.visibility = View.GONE
        }
    }

    val fetchedFileList: MutableList<FileModel>
        get() {
            if (fetchedFolderNamesList == null) {
                fetchedFolderNamesList = ArrayList()
            }
            return fetchedFolderNamesList!!
        }

    private fun hideRecyclerView() {
        binding.homeLayout.tvHeadingRecentDocs.visibility = View.GONE
        binding.homeLayout.rvScannerFiles.visibility = View.GONE
        binding.homeLayout.llNoDocument.root.visibility = View.VISIBLE
        binding.homeLayout.viewAllDoc.visibility = View.GONE
        binding.homeLayout.llFloating.visibility = View.GONE
        binding.homeLayout.rvFavorites.visibility = View.GONE
        binding.homeLayout.llNoFavoriteDocument.visibility = View.VISIBLE

        if (!prefManager!!.isAppAdFree && flashScanUtil!!.isConnectingToInternet && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(
                AdsPlacementsEnum.SH_HOME_EMPTY_LIST.value
            )
        ) {
            binding.homeLayout.adViewBannerContainer.visibility = View.VISIBLE
            callNativeAd(binding.homeLayout.nativeSmallAdNoDoc)
        } else {
            Log.e("Mobibuz : ", "Ad Not Showing")
            binding.homeLayout.adViewBannerContainer.visibility = View.GONE
        }


        /*loadLargeBannerAd();
        if (!isNativeAdAlreadyLoaded) {
            Log.e(TAG, "loadBottomNativeAd called");
            //loadBottomNativeAd();
        }*/
    }

    private fun manageFavRecentDocsViewOnNoDocPresent() {
        /*val ll_favorites_params =
            binding.homeLayout.flFavorites.layoutParams as LinearLayout.LayoutParams*/
        //ll_favorites_params.height = deviceHeight * 10 / 100
        //binding.homeLayout.flFavorites.layoutParams = ll_favorites_params
        binding.homeLayout.tvHeadingRecentDocs.visibility = View.GONE
        //binding.homeLayout.tvHeadingFavorites.visibility = View.GONE
    }

    /*private void loadBottomNativeAd() {

        boolean showNative = dbHandler.showNative();

//        if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_HOME_ACTIVITY) {
        if (!prefManager.isAppAdFree() && showNative) {
            if (flashScanUtil.isConnectingToInternet()) {
               // ll_native_ad_view.setVisibility(View.VISIBLE);
                //loadNativeAd();
            } else {
                //ll_native_ad_view.setVisibility(View.GONE);
            }
        } else {
            //ll_native_ad_view.setVisibility(View.GONE);
        }
    }*//* private void loadNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(this, BuildConfig.NATIVE_AD_ID)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                        if (isDestroyed()) {
                            unifiedNativeAd.destroy();
                            return;
                        }
                        if (nativeAd != null) {
                            nativeAd.destroy();
                        }
                        nativeAd = unifiedNativeAd;
                        *//*new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT);*/ /*
                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.item_view_native_ad, null, false);
                        if (unifiedNativeAdView != null) {
                            mapUnifiedNativeAdToLayout(unifiedNativeAd, unifiedNativeAdView);
                            fl_native_ad_view.removeAllViews();
                            fl_native_ad_view.addView(unifiedNativeAdView);
                        }

                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }*//* private void mapUnifiedNativeAdToLayout(UnifiedNativeAd adFromGoogle, UnifiedNativeAdView myAdView) {
        MediaView mediaView = myAdView.findViewById(R.id.ad_media);
        myAdView.setMediaView(mediaView);
        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_headline));
        myAdView.setBodyView(myAdView.findViewById(R.id.ad_body));
        myAdView.setCallToActionView(myAdView.findViewById(R.id.ad_call_to_action));
        CardView cardView = myAdView.findViewById(R.id.cv_app_icon);
        myAdView.setIconView(myAdView.findViewById(R.id.ad_app_icon));
        myAdView.setPriceView(myAdView.findViewById(R.id.ad_price));
        myAdView.setStarRatingView(myAdView.findViewById(R.id.ad_stars));
        myAdView.setStoreView(myAdView.findViewById(R.id.ad_store));
        myAdView.setAdvertiserView(myAdView.findViewById(R.id.ad_advertiser));

        *//*myAdView.setImageView();
        myAdView.setClickConfirmingView();
        myAdView.setAdChoicesView();*//*

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        myAdView.getMediaView().setMediaContent(adFromGoogle.getMediaContent());
        Log.e(TAG, "ad headline :" + adFromGoogle.getHeadline());
        ((TextView) myAdView.getHeadlineView()).setText(adFromGoogle.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        Log.e(TAG, "ad body :" + adFromGoogle.getBody());
        if (adFromGoogle.getBody() == null) {
            myAdView.getBodyView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getBodyView()).setText(adFromGoogle.getBody());
        }

        Log.e(TAG, "ad call to action :" + adFromGoogle.getCallToAction());
        if (adFromGoogle.getCallToAction() == null) {
            myAdView.getCallToActionView().setVisibility(View.GONE);
        } else {
            ((Button) myAdView.getCallToActionView()).setText(adFromGoogle.getCallToAction());
        }

        if (adFromGoogle.getIcon() == null) {
            myAdView.getIconView().setVisibility(View.GONE);
            cardView.setVisibility(View.GONE);
        } else {
            cardView.setVisibility(View.VISIBLE);
            myAdView.getIconView().setVisibility(View.VISIBLE);
            ((ImageView) myAdView.getIconView()).setImageDrawable(adFromGoogle.getIcon().getDrawable());
        }

        Log.e(TAG, "ad price :" + adFromGoogle.getPrice());
        if (adFromGoogle.getPrice() == null) {
            myAdView.getPriceView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getPriceView()).setText(adFromGoogle.getPrice());
        }

        if (adFromGoogle.getStarRating() == null) {
            myAdView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) myAdView.getStarRatingView()).setRating(adFromGoogle.getStarRating().floatValue());
        }

        Log.e(TAG, "ad store :" + adFromGoogle.getStore());
        if (adFromGoogle.getStore() == null) {
            myAdView.getStoreView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getStoreView()).setText(adFromGoogle.getStore());
        }

        Log.e(TAG, "ad advertiser :" + adFromGoogle.getAdvertiser());
        if (adFromGoogle.getAdvertiser() == null) {
            myAdView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getAdvertiserView()).setText(adFromGoogle.getAdvertiser());
        }

        myAdView.setNativeAd(adFromGoogle);
    }*//*private void loadLargeBannerAd() {
        if (!prefManager.isAppAdFree() && Constants.SHOW_MEDIUM_BANNER_ADS.FOR_HOME_ACTIVITY) {
            if (flashScanUtil.isConnectingToInternet()) {
                ll_adView.setVisibility(View.VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            } else {
                ll_adView.setVisibility(View.GONE);
            }
        }
    }*/
    private fun showRecentsRv(fileModelList: List<FileModel>) {
        binding.homeLayout.tvHeadingRecentDocs.visibility = View.VISIBLE

        binding.homeLayout.llNoDocument.root.visibility = View.GONE
        binding.homeLayout.adViewBannerContainer.visibility = View.GONE
        //ll_adView.setVisibility(View.GONE);
        //ll_native_ad_view.setVisibility(View.GONE);
        binding.homeLayout.rvScannerFiles.visibility = View.VISIBLE
        binding.homeLayout.llFloating.visibility = View.VISIBLE

        val recentDocumentsList: MutableList<FileModel> = ArrayList()
        if (fileModelList.size > Constants.RECENT_DOCS_COUNT_LIMIT) {
            for (i in 0 until Constants.RECENT_DOCS_COUNT_LIMIT) {
                recentDocumentsList.add(fileModelList[i])
            }
            // for showing ad
            val showNative = dbHandler!!.showNative()/*if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
                    && Constants.SHOW_NATIVE_ADS.FOR_HOME_ACTIVITY) {*/
            if (!prefManager!!.isAppAdFree && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST && showNative) {
                if (recentDocumentsList.size <= Constants.AD_PER_ITEM_RECENT) {
                    finalFileModelList.addAll(recentDocumentsList)
                    val fileModel = FileModel()
                    fileModel.isAdView = true
                    finalFileModelList.add(fileModel)
                } else {
                    for (i in recentDocumentsList.indices) {
                        if (Constants.AdAfterItems.FOR_MULTIPLE_ITEMS) {
                            if (i != 0 && i % Constants.AD_PER_ITEM_RECENT == 0) {
                                val fileModel = FileModel()
                                fileModel.isAdView = true
                                finalFileModelList.add(fileModel)
                            }
                            finalFileModelList.add(recentDocumentsList[i])
                        } else if (Constants.AdAfterItems.FOR_SINGLE_ITEM) {
                            if (i == Constants.AD_PER_ITEM_RECENT) {
                                val fileModel = FileModel()
                                fileModel.isAdView = true
                                finalFileModelList.add(fileModel)
                            }
                            finalFileModelList.add(fileModelList[i])
                        }
                    }
                }
            } else {
                finalFileModelList.addAll(recentDocumentsList)
            }
            binding.homeLayout.viewAllDoc.visibility = View.VISIBLE
            //==============
           /* finalFileModelList.add(
                FileModel(
                    getString(R.string.view_all), ""
                )
            )*/ // for view all functionality
        } else {
            recentDocumentsList.addAll(fileModelList)

            // for showing ad
            val showNative = dbHandler!!.showNative()

            /*if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST
                    && Constants.SHOW_NATIVE_ADS.FOR_HOME_ACTIVITY) {*/
            if (!prefManager!!.isAppAdFree && Constants.SHOW_NATIVE_ADS.FOR_RECYCLERVIEW_LIST && showNative) {
                if (recentDocumentsList.size <= Constants.AD_PER_ITEM_RECENT) {
                    finalFileModelList.addAll(recentDocumentsList)
                    val fileModel = FileModel()
                    fileModel.isAdView = true
                    finalFileModelList.add(fileModel)
                } else {
                    for (i in recentDocumentsList.indices) {
                        if (Constants.AdAfterItems.FOR_MULTIPLE_ITEMS) {
                            if (i != 0 && i % Constants.AD_PER_ITEM_RECENT == 0) {
                                val fileModel = FileModel()
                                fileModel.isAdView = true
                                finalFileModelList.add(fileModel)
                            }
                            finalFileModelList.add(recentDocumentsList[i])
                        } else if (Constants.AdAfterItems.FOR_SINGLE_ITEM) {
                            if (i == Constants.AD_PER_ITEM_RECENT) {
                                val fileModel = FileModel()
                                fileModel.isAdView = true
                                finalFileModelList.add(fileModel)
                            }
                            finalFileModelList.add(fileModelList[i])
                        }
                    }
                }
            } else {
                finalFileModelList.addAll(recentDocumentsList)
            }
            //==============
            /*finalFileModelList.add(new FileModel(getString(R.string.show_camera_media__floating_view), ""));*/
        }

        /*if (!recentDocumentsList.isEmpty()) {
            if (recentDocumentsList.size() < Constants.RECENT_DOCS_COUNT_LIMIT) {
                ll_floating.setVisibility(View.GONE);
            } else {
                ll_floating.setVisibility(View.GONE);
            }
        }*/
        fileModelAdapter = FileModelAdapter(this, finalFileModelList, this, this)
        binding.homeLayout.rvScannerFiles.adapter = fileModelAdapter
    }

    private fun manageFavRecentDocsViewWhenDocIsAlreadyPresent() {
        /*val ll_favorites_params =
            binding.homeLayout.flFavorites.layoutParams as LinearLayout.LayoutParams
        ll_favorites_params.height = deviceHeight * 22 / 100
        binding.homeLayout.flFavorites.layoutParams = ll_favorites_params*/
        binding.homeLayout.tvHeadingRecentDocs.visibility = View.VISIBLE
        //binding.homeLayout.tvHeadingFavorites.visibility = View.VISIBLE
    }

    var fileModel: FileModel? = null

    override fun onItemSelect(o: Any) {
        if (fileModelAdapter != null && fileModelAdapter!!.isVisibleAllCheckbox) {
            return
        }
        if (o == null) return
        //var fileModel: FileModel? = null
        if (o is FileModel) {
            fileModel = o
        }
        if (!isPermissionGranted) {
            processForPermissions()
            return
        }

        fileModel?.let { moreFeatureSelected(it) }
    }

    private fun moreFeatureSelected(fileModel: FileModel) {
        if (fileModel != null) {

            if (fileModel.type == DocumentTypeEnum.PDF.value) {

                val file = File(fileModel.path)
                var isOcrResultFileAlreadySaved = false
                var ocrResultSavedFilePath: String? = null
                if (file.isDirectory && file.exists()) {
                    val files = file.listFiles()
                    if (files != null && files.size > 0) {
                        for (eachFile in files) {
                            if (eachFile != null && eachFile.isFile && eachFile.exists()) {
                                if (eachFile.name.contains("_" + getString(R.string.suffix_app_name)) &&
                                    flashScanUtil!!.getExtensionFromFileName(eachFile.name)
                                        .equals(
                                            Constants.TXT_FILE_EXTENSION_WITHOUT_DOT,
                                            ignoreCase = true
                                        )
                                ) {
                                    isOcrResultFileAlreadySaved = true
                                    ocrResultSavedFilePath = eachFile.path
                                    break
                                }
                            }
                        }
                    }
                }


                val intent = Intent(this, OcrResultActivity::class.java)
                intent.putExtra(
                    Constants.PutExtraConstants.OCR_RESULT_FROM_SCREEN,
                    Constants.OcrResultScreenFrom.FROM_DOCUMENT
                )
                intent.putExtra(
                    Constants.PutExtraConstants.FILE_PATH,
                    FlashScanUtil.getOcrProcessingPath(this).absolutePath + "/" + fileModel.name
                )
                if (isOcrResultFileAlreadySaved && !TextUtils.isEmpty(ocrResultSavedFilePath)) {
                    intent.putExtra(
                        Constants.PutExtraConstants.OCR_SAVED_FILE_PATH,
                        ocrResultSavedFilePath
                    )
                }
                startActivityForResult(intent, REQUEST_CODE_FETCH_OCR_DOCUMENTS)
            }

            if (!TextUtils.isEmpty(fileModel.name) && fileModel.name.equals(
                    getString(R.string.view_all), ignoreCase = true
                ) && TextUtils.isEmpty(fileModel.thumbnailPath)
            ) {
                openDocScanner()
                return
            }

            if (!TextUtils.isEmpty(fileModel.name) && fileModel.name.equals(
                    getString(R.string.view_more), ignoreCase = true
                ) && TextUtils.isEmpty(fileModel.thumbnailPath)
            ) {
                openFavoriteDocumentsActivity()
            } else {
                val intent = Intent(this, ScanResultActivity::class.java)
                intent.putExtra(
                    ScanConstants.PutExtraConstants.FROM_SCREEN,
                    ScanConstants.ScreenConstants.FROM_HOME_SCREEN
                )
                intent.putExtra(ScanConstants.PutExtraConstants.FOLDER_NAME, fileModel.name)
                intent.putExtra(ScanConstants.PutExtraConstants.DATE_TAKEN, fileModel.dateTaken)
                intent.putExtra(
                    ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE,
                    fileModel.isSavedOnGoogleDrive
                )
                intent.putExtra(
                    ScanConstants.PutExtraConstants.GOOGLE_DRIVE_FOLDER_ID,
                    fileModel.googleDriveFolderId
                )
                startActivityForResult(intent, REQUEST_CODE_FETCH_ALL_DOCUMENTS)
//                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            }
        }
    }

    override fun onItemLongPress(o: Any) {
        if (fileModelAdapter != null && fileModelAdapter!!.isVisibleAllCheckbox) {
            binding.homeLayout.llBottomBar.root.visibility = View.VISIBLE
            binding.homeLayout.llFloating.visibility = View.GONE/*fab_camera.setVisibility(View.GONE);
            fab_media.setVisibility(View.GONE);*/
        }
    }

    override fun onItemAction(o: Any, view: View) {/*      FileModel fileModel = null;
        if (o == null && view == null)
            return;
        if (o instanceof FileModel) {
            fileModel = (FileModel) o;
        }
        if (fileModel != null) {
            showPopUpMenu(fileModel, view);
        }*/
    }

    /*private void showPopUpMenu(FileModel fileModel, View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        Field[] fields = popupMenu.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("mPopup".equals(field.getName())) {
                field.setAccessible(true);
                try {
                    Object menuPopupHelper = field.get(popupMenu);
                    if (menuPopupHelper != null) {
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceShowIcon.invoke(menuPopupHelper, true);
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        popupMenu.getMenuInflater().inflate(R.menu.more_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_save_as_pdf:
                    if (fileModel != null) {
                        *//*fetchFilesForPdfConversion(fileModel.getName());*/ /*
                        File fileOrDirectory = new File(fileModel.getPath());
                        List<String> filePathList = new ArrayList<>();
                        if (fileOrDirectory.isDirectory()) {
                            File[] files = fileOrDirectory.listFiles();
                            if (files != null && files.length > 0) {
                                for (File file : files) {
                                    filePathList.add(file.getPath());
                                }
                                if (!filePathList.isEmpty()) {
                                    isPdfCreationForSharing = false;
                                    createPdf(filePathList, fileModel.getName());
                                }
                            }
                        } else {
                            filePathList.add(fileOrDirectory.getPath());
                            if (!filePathList.isEmpty()) {
                                isPdfCreationForSharing = false;
                                createPdf(filePathList, fileModel.getName());
                            }
                        }
                    }
                    break;
                case R.id.menu_save_to_gallery:
                    Toast.makeText(this, "save to gallery called", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_share:
                    *//*Toast.makeText(this, "share called", Toast.LENGTH_SHORT).show();*/ /*
                    showShareDialog(fileModel);
                    break;
                case R.id.menu_rename:
                    showCommonDialog(fileModel, Constants.FileOperations.ACTION_RENAME);
                    break;
                case R.id.menu_delete:
                    showCommonDialog(fileModel, Constants.FileOperations.ACTION_DELETE);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }*/
    private fun showCommonDialog(fileModel: FileModel, action: Int) {
        val dialog = Dialog((this))
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.common_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val msgHeading = dialog.findViewById<TextView>(R.id.msg_heading)
        val btn_cancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        val btn_ok = dialog.findViewById<TextView>(R.id.btn_ok)
        val et_pdf_name = dialog.findViewById<EditText>(R.id.et_pdf_name)

        when (action) {
            Constants.FileOperations.ACTION_RENAME -> {
                dialogTitle.text = getString(R.string.rename_file)
                msgHeading.text = getString(R.string.rename_msg)
                msgHeading.text = ""
                et_pdf_name.setText(fileModel.name)
                et_pdf_name.setSelection(et_pdf_name.text.length)
            }

            Constants.FileOperations.ACTION_DELETE -> {
                dialogTitle.text = getString(R.string.delete)
                msgHeading.text = getString(R.string.delete_msg)
                btn_cancel.setText(R.string.keep_it)
                btn_ok.setText(R.string.yes_btn_dialog)
                et_pdf_name.visibility = View.GONE
                if (fileModel.isSavedOnGoogleDrive) {
                    dialog.findViewById<View>(R.id.rdo_grp_delete_options).visibility = View.VISIBLE
                }
            }
        }
        btn_cancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()/*clearSelectedFiles();*/
        })


        btn_ok.setOnClickListener { v: View? ->
            when (action) {
                Constants.FileOperations.ACTION_RENAME -> {
                    val folderName: String = et_pdf_name.getText().toString().trim { it <= ' ' }
                    if (TextUtils.isEmpty(folderName)) {
                        Toast.makeText(
                            this@HomeActivity,
                            getString(R.string.please_name_file),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else if (folderName.equals(fileModel.name, ignoreCase = true)) {
                        Toast.makeText(
                            this@HomeActivity,
                            getString(R.string.file_name_same_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    renameFolder(folderName, fileModel)
                    //renameFromGoogleDriveById(fileModel, folderName);
                    dialog.dismiss()
                }

                Constants.FileOperations.ACTION_DELETE -> {/*File dir = new File(fileModel.getPath());*//*deleteRecursive(dir);*/
                    DeleteFolderOrFileTask(this@HomeActivity, fileModel.path, this).execute()
                    AppController.getINSTANCE().dbHandler.deleteApplyFilterFolder(fileModel.name)
                    dialog.dismiss()
                    fileModelAdapter!!.notifyDataSetChanged()
                    if ((dialog.findViewById<View>(R.id.rd_delete_from_both) as RadioButton).isChecked) {
                        deleteFromGoogleDrive(fileModel.name)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun renameFolder(newFolderName: String, fileModel: FileModel) {
        val oldFolder = File(fileModel.folder, fileModel.name)
        val newFolder = File(fileModel.folder, newFolderName)
        val isRenamed = oldFolder.renameTo(newFolder)
        if (isRenamed) {
            AppController.getINSTANCE().dbHandler.updateFolderName(fileModel.name, newFolderName)
            AppController.getINSTANCE().dbHandler.updateApplyFilterFolder(
                newFolderName, fileModel.name
            )

            val originalName = fileModel.name

            val dstOriginalFolderName = File(FlashScanUtil.getDocOriginalPath(this), originalName)
            val tempOriginal = File(FlashScanUtil.getDocOriginalPath(this), newFolderName)
            dstOriginalFolderName.renameTo(tempOriginal)

            val dstOriginalFolderName3 = File(
                Environment.getExternalStorageDirectory().absolutePath + File.separator + Constants.ITL_PDF_DIRECTORY,
                "$originalName.pdf"
            )
            val tempOriginal3 = File(
                Environment.getExternalStorageDirectory().absolutePath + File.separator + Constants.ITL_PDF_DIRECTORY,
                "$newFolderName.pdf"
            )
            dstOriginalFolderName3.renameTo(tempOriginal3)

            flashScanUtil!!.showSnackBar(
                findViewById(android.R.id.content), getString(R.string.rename_success_msg)
            )


            if (fileModel.isSavedOnGoogleDrive) {
                val strFileId = fileModel.googleDriveFolderId
                flashScanUtil!!.deleteFolderByIdFromGoogleDrive(
                    this, strFileId, resources.getString(R.string.updating_files_to_google_drive)
                ) {
                    prefManager!!.deleteFolderFromGoogleDriveDataList(strFileId)
                    fileModel.name = newFolderName
                    fileModel.path = newFolder.path
                    flashScanUtil!!.saveFileInGoogleDrive(
                        this,
                        Constants.ROOT_FOLDER_NAME,
                        fileModel,
                        false,
                        resources.getString(R.string.updating_file_metadata),
                        GoogleDriveDataUploadListener { folderId: String? ->
                            fetchFiles()
                        })
                }
            } else {
                fetchFiles()
            }
        } else {
            flashScanUtil!!.showSnackBar(
                findViewById(android.R.id.content), getString(R.string.same_folder_already_exist)
            )
        }
    }

    private fun showShareDialog(fileModel: FileModel) {
        val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.save_as_dailog)
        dialog.setCancelable(true)

        val ll_preview_pdf = dialog.findViewById<LinearLayout>(R.id.ll_preview_pdf)
        if (prefManager!!.isAppWatermarkFree || prefManager!!.isPremiumYearly /* || prefManager.isPremiumQuarterly()*/) {  // means user puchase product
            ll_preview_pdf.visibility = View.GONE
        } else {
            ll_preview_pdf.visibility = View.VISIBLE
        }

        val ll_share_as_pdf = dialog.findViewById<LinearLayout>(R.id.ll_share_as_pdf)
        val ll_share_as_image = dialog.findViewById<LinearLayout>(R.id.ll_share_as_image)
        val tv_preview = dialog.findViewById<TextView>(R.id.tv_preview)
        val tv_pdf_watermark = dialog.findViewById<TextView>(R.id.tv_pdf_watermark)
        tv_pdf_watermark.text = getString(R.string.pdf_preview_txt, getString(R.string.app_name))

        tv_preview.setOnClickListener {
            dialog.dismiss()
            if (Constants.IS_CREATE_PDF_DIRECT) {
                sharePdfDirectWithoutOpen = false
                fileModel.pdfFileName = fileModel.name
                handlePdfCreation(fileModel, PDF_VIA_SHARE)
            } else {
                showAskPdfNameDialog(fileModel, PDF_VIA_SHARE)
            }
        }

        ll_share_as_pdf.setOnClickListener {
            dialog.dismiss()
            if (Constants.IS_CREATE_PDF_DIRECT) {
                sharePdfDirectWithoutOpen = true
                fileModel.pdfFileName = fileModel.name
                handlePdfCreation(fileModel, PDF_VIA_SHARE)
            } else {
                showAskPdfNameDialog(fileModel, PDF_VIA_SHARE)
            }
        }

        ll_share_as_image.setOnClickListener { v: View? ->
            dialog.dismiss()
            val fileOrDirectory: File = File(fileModel.path)
            val uriList: ArrayList<Uri> = ArrayList()
            if (fileOrDirectory.isDirectory()) {
                val files: Array<File>? = fileOrDirectory.listFiles()
                if (files != null && files.size > 0) {
                    for (file: File in files) {
                        if (file.isFile() && file.exists()) {
                            if (!TextUtils.isEmpty(file.getName()) && file.getName()
                                    .equals(Constants.JSON_FILE_NAME, ignoreCase = true)
                            ) {
                                continue
                            }
                        }
                        val uriForFile: Uri? = FileProvider.getUriForFile(
                            this, BuildConfig.APPLICATION_ID + ".fileprovider", file
                        )
                        if (uriForFile != null) uriList.add(uriForFile)
                    }
                    if (!uriList.isEmpty()) {
                        shareMultiple(uriList)
                    } else {
                        showNoFileToShareDialog()
                    }
                } else {
                    showNoFileToShareDialog()
                }
            } else {
                val uriForFile: Uri? = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".fileprovider", fileOrDirectory
                )
                if (uriForFile != null) uriList.add(uriForFile)
                if (!uriList.isEmpty()) {
                    shareMultiple(uriList)
                } else {
                    showNoFileToShareDialog()
                }
            }
        }
        dialog.show()
    }

    private fun showNoFileToShareDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.no_files_in_document_to_share_warning).setCancelable(false)
            .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun goToWaterMarkRemoveActivityForShareSingleDocument() {
        val intent = Intent(this, WaterMarkRemoveActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT)
    }

    /*private void showRewardAdDialogForShareSingleDocument(FileModel fileModel) {
        Dialog dialog = new Dialog(this);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        createPdfForShareSingleDocument(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*/ /*
                        createPdfForShareSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForShareSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(HomeActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/
    override fun onPdfCreationStarted() {
        binding.homeLayout.progressLay.root.visibility = View.VISIBLE
    }

    private fun showPdfPathDialog(savedPdfPath: String) {
        val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.saved_pdf_dialog)
        val tv_pdf_path = dialog.findViewById<TextView>(R.id.tv_pdf_path)
        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btn_open = dialog.findViewById<Button>(R.id.btn_open)

        tv_pdf_path.text = savedPdfPath
        btn_cancel.setOnClickListener { v: View? -> dialog.dismiss() }
        btn_open.setOnClickListener {
            openFile(savedPdfPath)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openFile(savedPdfPath: String) {
        val file = File(savedPdfPath)
        if (file.isFile) {
            flashScanUtil!!.openFile(this, file)
        }
    }

    private fun shareMultiple(uriList: ArrayList<Uri>) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND_MULTIPLE)
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.here_are_some_files, getString(R.string.app_name))
        )
        intent.setType("*/*")
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)

        var shareMessage = this.getString(R.string.app_share_msg)
        shareMessage =
            shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n"
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val resInfoList: List<ResolveInfo> =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            for (uri in uriList) {
                grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    override fun onPdfCreated(savedPdfPath: String) {
        binding.homeLayout.progressLay.root.visibility = View.GONE
        if (Constants.IS_SHOWING_CREATED_PDF_IN_OWN_APP) {
            if (!sharePdfDirectWithoutOpen) {
                val intent = Intent(this, PdfEditorActivity::class.java)
                intent.putExtra(Constants.PutExtraConstants.SAVED_PDF_PATH, savedPdfPath)
                startActivity(intent)
            } else {
                val file = File(savedPdfPath)
                val uris = ArrayList<Uri>()
                if (file.isFile) {
                    val uriForFile = FileProvider.getUriForFile(
                        this, BuildConfig.APPLICATION_ID + ".fileprovider", file
                    )
                    if (uriForFile != null) uris.add(uriForFile)
                }
                if (!uris.isEmpty()) {
                    shareMultiple(uris)
                }
            }
            sharePdfDirectWithoutOpen = false
        } else {
            if (!isPdfCreationForSharing) {
                if (!isFinishing || !isDestroyed) {
                    showPdfPathDialog(savedPdfPath)
                }
            } else {
                val file = File(savedPdfPath)
                val uris = ArrayList<Uri>()
                if (file.isFile) {
                    val uriForFile = FileProvider.getUriForFile(
                        this, BuildConfig.APPLICATION_ID + ".fileprovider", file
                    )
                    if (uriForFile != null) uris.add(uriForFile)
                }
                if (!uris.isEmpty()) {
                    shareMultiple(uris)
                }
            }
        }
    }

    private fun showShareDialog() {
        val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.save_as_dailog)
        dialog.setCancelable(true)

        val ll_preview_pdf = dialog.findViewById<LinearLayout>(R.id.ll_preview_pdf)
        if (prefManager!!.isAppWatermarkFree/* || prefManager!!.isPremiumYearly || prefManager.isPremiumQuarterly()*/) {  // means user puchase product
            ll_preview_pdf.visibility = View.GONE
        } else {
            ll_preview_pdf.visibility = View.VISIBLE
        }

        val ll_share_as_pdf = dialog.findViewById<LinearLayout>(R.id.ll_share_as_pdf)
        val ll_share_as_image = dialog.findViewById<LinearLayout>(R.id.ll_share_as_image)
        val tv_preview = dialog.findViewById<TextView>(R.id.tv_preview)
        val tv_pdf_watermark = dialog.findViewById<TextView>(R.id.tv_pdf_watermark)
        tv_pdf_watermark.text = getString(R.string.pdf_preview_txt, getString(R.string.app_name))

        tv_preview.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                click = v
                dialog.dismiss()
                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                var selectedFileModelList: List<FileModel>? = null
                if (fileModelAdapter != null) {
                    selectedFileModelList = fileModelAdapter!!.selectedFileModelList
                }
                if (selectedFileModelList != null && !selectedFileModelList.isEmpty()) {
                    if (Constants.IS_CREATE_PDF_DIRECT) {
                        sharePdfDirectWithoutOpen = false
                        pdfFileNameForMultipleDocs =
                            flashScanUtil!!.fileDateFormatName + getString(R.string.suffix_app_name)
                        handleMultipleDocPdfCreation(selectedFileModelList, PDF_VIA_SHARE)
                        hideCheckBoxAndRemoveBottomBar()
                    } else {
                        showAskPdfNameDialogForMultiDoc(selectedFileModelList, PDF_VIA_SHARE)
                    }
                }
            }
        })

        ll_share_as_pdf.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                click = v
                dialog.dismiss()

                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                var selectedFileModelList: List<FileModel>? = null
                if (fileModelAdapter != null) {
                    selectedFileModelList = fileModelAdapter!!.selectedFileModelList
                }
                if (selectedFileModelList != null && !selectedFileModelList!!.isEmpty()) {
                    if (Constants.IS_CREATE_PDF_DIRECT) {
                        sharePdfDirectWithoutOpen = true
                        pdfFileNameForMultipleDocs =
                            flashScanUtil!!.fileDateFormatName + getString(R.string.suffix_app_name)
                        handleMultipleDocPdfCreation(selectedFileModelList!!, PDF_VIA_SHARE)
                        hideCheckBoxAndRemoveBottomBar()
                    } else {
                        showAskPdfNameDialogForMultiDoc(selectedFileModelList!!, PDF_VIA_SHARE)
                    }
                }
            }
        })

        ll_share_as_image.setOnClickListener { v: View? ->
            click = v
            dialog.dismiss()
            var selectedFileModelList: List<FileModel?>? = null
            if (fileModelAdapter != null) {
                selectedFileModelList = fileModelAdapter!!.getSelectedFileModelList()
            }
            if (selectedFileModelList != null && !selectedFileModelList!!.isEmpty()) {
                val uriList: ArrayList<Uri> = ArrayList()
                for (fileModel: FileModel? in selectedFileModelList!!) {
                    if (fileModel != null) {
                        val fileOrDirectory: File = File(fileModel.path)
                        if (fileOrDirectory.isDirectory()) {
                            val files: Array<File>? = fileOrDirectory.listFiles()
                            if (files != null && files.size > 0) {
                                for (file: File in files) {
                                    if (file.isFile() && file.exists()) {
                                        if (!TextUtils.isEmpty(file.getName()) && file.getName()
                                                .equals(
                                                    Constants.JSON_FILE_NAME, ignoreCase = true
                                                )
                                        ) {
                                            continue
                                        }
                                    }
                                    val uriForFile: Uri? = FileProvider.getUriForFile(
                                        this, BuildConfig.APPLICATION_ID + ".fileprovider", file
                                    )
                                    if (uriForFile != null) uriList.add(uriForFile)
                                }
                            }
                        } else {
                            val uriForFile: Uri? = FileProvider.getUriForFile(
                                this,
                                BuildConfig.APPLICATION_ID + ".fileprovider",
                                fileOrDirectory
                            )
                            if (uriForFile != null) uriList.add(uriForFile)
                        }
                    }
                }
                if (!uriList.isEmpty()) {
                    shareMultiple(uriList)
                } else {
                    showNoFileToShareDialog()
                }
            }
            hideCheckBoxAndRemoveBottomBar()
        }
        dialog.show()
    }

    private fun goToWaterMarkRemoveActivityForShareMultipleDocuments() {
        val intent = Intent(this, WaterMarkRemoveActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS)
    }

    /*private void showRewardAdDialogForShareSelectedDocuments(List<FileModel> selectedFileModelList) {
        Dialog dialog = new Dialog(this);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        createPdfForShareSelectedDocuments(selectedFileModelList, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*//*
                        createPdfForShareSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForShareSelectedDocuments(selectedFileModelList, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(HomeActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }*/
    private fun createPdfForShareSelectedDocuments(
        selectedFileModelList: List<FileModel>?, isWaterMarkToBeShown: Boolean
    ) {
        if (isMultiplePdfCreationWithCompression) {
            val selectedFoldersPathList = ArrayList<String>()
            for (fileModel in selectedFileModelList!!) {
                selectedFoldersPathList.add(fileModel.path)
            }
            if (!selectedFoldersPathList.isEmpty()) {
                GetTempCompressBitmapFolders(
                    this,
                    selectedFoldersPathList,
                    object : CreateMultipleTempBitmapListener {
                        override fun onCompressBitmapStart() {
                            binding.homeLayout.progressLay.root.visibility = View.VISIBLE
                        }

                        override fun onCompressBitmapComplete(foldersList: ArrayList<String>) {
                            binding.homeLayout.progressLay.root.visibility = View.GONE
                            if (foldersList != null && !foldersList.isEmpty()) {
                                val filePathList: MutableList<String> = ArrayList()
                                for (folderPath in foldersList) {
                                    val fileOrDirectory = File(folderPath)
                                    if (fileOrDirectory.isDirectory) {
                                        val files = fileOrDirectory.listFiles()
                                        if (files != null && files.size > 0) {
                                            flashScanUtil!!.sortFilesByNameAtoZ(files)
                                            /*val appSortingOrder = prefManager!!.appSortingOrder
                                            when (appSortingOrder) {
                                                Constants.SORT_BY.defaultOrder, Constants.SORT_BY.modificationTimeDescending -> flashScanUtil!!.sortFilesByDescendingLastModified(
                                                    files
                                                )

                                                Constants.SORT_BY.modificationTimeAscending -> flashScanUtil!!.sortFilesByAscendingLastModified(
                                                    files
                                                )

                                                Constants.SORT_BY.nameAtoZ -> flashScanUtil!!.sortFilesByNameAtoZ(
                                                    files
                                                )

                                                Constants.SORT_BY.nameZtoA -> flashScanUtil!!.sortFilesByNameZtoA(
                                                    files
                                                )
                                            }*/
                                            for (file in files) {
                                                filePathList.add(file.path)
                                            }
                                        }
                                    } else {
                                        filePathList.add(fileOrDirectory.path)
                                    }
                                }
                                if (!filePathList.isEmpty()) {
                                    isPdfCreationForSharing = true
                                    //  currently not in use
                                    createPdf(
                                        filePathList,
                                        pdfFileNameForMultipleDocs,
                                        isWaterMarkToBeShown
                                    )
                                }
                            }
                        }
                    }).execute()
            }
        } else {
            val filePathList: MutableList<String> = ArrayList()
            for (fileModel in selectedFileModelList!!) {
                if (fileModel != null) {
                    val fileOrDirectory = File(fileModel.path)
                    if (fileOrDirectory.isDirectory) {
                        val files = fileOrDirectory.listFiles()
                        if (files != null && files.size > 0) {
                            flashScanUtil!!.sortFilesByNameAtoZ(files)
                            /*val appSortingOrder = prefManager!!.appSortingOrder
                            when (appSortingOrder) {
                                Constants.SORT_BY.defaultOrder, Constants.SORT_BY.modificationTimeDescending -> flashScanUtil!!.sortFilesByDescendingLastModified(
                                    files
                                )

                                Constants.SORT_BY.modificationTimeAscending -> flashScanUtil!!.sortFilesByAscendingLastModified(
                                    files
                                )

                                Constants.SORT_BY.nameAtoZ -> flashScanUtil!!.sortFilesByNameAtoZ(
                                    files
                                )

                                Constants.SORT_BY.nameZtoA -> flashScanUtil!!.sortFilesByNameZtoA(
                                    files
                                )
                            }*/
                            for (file in files) {
                                if (file.isFile && file.exists()) {
                                    if (file.name.equals(
                                            Constants.JSON_FILE_NAME, ignoreCase = true
                                        )
                                    ) {
                                        continue
                                    }
                                    filePathList.add(file.path)
                                }
                            }
                        }
                    } else {
                        if (fileOrDirectory.isFile && fileOrDirectory.exists()) {
                            if (!fileOrDirectory.name.equals(
                                    Constants.JSON_FILE_NAME, ignoreCase = true
                                )
                            ) {
                                filePathList.add(fileOrDirectory.path)
                            }
                        }
                    }
                }
            }
            if (!filePathList.isEmpty()) {
                isPdfCreationForSharing =
                    true/*createPdf(filePathList, getString(R.string.prefix_document) + System.currentTimeMillis());*/
                createPdf(filePathList, pdfFileNameForMultipleDocs, isWaterMarkToBeShown)
            } else {
                // showWarning message
                showNoFilesInDocumentDialog()
            }
        }

        //---------
    }

    private fun showDeleteDialog(selectedFileModelList1: List<FileModel?>) {
        val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.common_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val msgHeading = dialog.findViewById<TextView>(R.id.msg_heading)
        val btn_cancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        val btn_ok = dialog.findViewById<TextView>(R.id.btn_ok)
        val et_pdf_name = dialog.findViewById<EditText>(R.id.et_pdf_name)
        et_pdf_name.visibility = View.GONE
        dialog.findViewById<View>(R.id.rdo_grp_delete_options).visibility = View.VISIBLE

        dialogTitle.text = getString(R.string.delete)
        msgHeading.text = getString(R.string.delete_msg)
        btn_cancel.setText(R.string.keep_it)
        btn_ok.setText(R.string.yes_btn_dialog)

        btn_cancel.setOnClickListener {
            dialog.dismiss()
            hideCheckBoxAndRemoveBottomBar()
        }

        btn_ok.setOnClickListener { v: View? ->
            dialog.dismiss()
            val filePathList: MutableList<String> = ArrayList()
            for (fileModel: FileModel? in selectedFileModelList1) {
                if (fileModel != null) {
                    filePathList.add(fileModel.path)
                    if ((dialog.findViewById<View>(R.id.rd_delete_from_both) as RadioButton).isChecked) {
                        deleteFromGoogleDrive(fileModel.name)
                    }
                }
            }
            if (!filePathList.isEmpty()) {
                DeleteFolderOrFileTask(this, filePathList, FileOrFolderDeleteListener {
                    fetchFiles()
                    flashScanUtil!!.showSnackBar(
                        findViewById(android.R.id.content), getString(R.string.delete_success_msg)
                    )
                }).execute()
            }
            hideCheckBoxAndRemoveBottomBar()
        }
        dialog.show()
    }

    private fun showDeleteDialogGoogleDrive(fileModel: FileModel) {
        val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.common_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val msgHeading = dialog.findViewById<TextView>(R.id.msg_heading)
        val btn_cancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        val btn_ok = dialog.findViewById<TextView>(R.id.btn_ok)
        val et_pdf_name = dialog.findViewById<EditText>(R.id.et_pdf_name)
        et_pdf_name.visibility = View.GONE

        dialogTitle.text = getString(R.string.delete)
        msgHeading.text = resources.getString(R.string.delete_from_drive)
        btn_cancel.setText(R.string.keep_it)
        btn_ok.setText(R.string.yes_btn_dialog)

        btn_cancel.setOnClickListener { v: View? -> dialog.dismiss() }

        btn_ok.setOnClickListener { v: View? ->
            dialog.dismiss()
            deleteFromGoogleDriveById(fileModel)
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_DRIVE_SIGN_IN -> {
                binding.homeLayout.progressLay.root.visibility = View.GONE
                Log.d("googlesignin", "$resultCode ${data?.data}")
                if (resultCode == RESULT_OK && data != null) {
                    Log.d(TAG, "onActivityResult: isGetAllDataFromDrive = $isGetAllDataFromDrive")
                    flashScanUtil!!.handleSignInResult(this, data)
                    if (isGetAllDataFromDrive) {
                        allAppUserDataFromGoogleDrive
                        isGetAllDataFromDrive = false
                    } else if (mFileModelForSaveToDrive != null) {
                        checkDriveSignIn(mFileModelForSaveToDrive!!, positionForSaveToDrive)
                    }
                }
            }

            PERMISSIONS_SETTING_REQUEST_CODE -> {
                processForPermissions()
                if (!isPermissionGranted) finish()
            }

            BARCODE_READER_ACTIVITY_REQUEST -> if (resultCode == RESULT_OK) {
                if (data != null) {
                    val barcode =
                        data.getParcelableExtra<ResultBarCode>(BarcodeReaderActivity.KEY_CAPTURED_BARCODE)
                    if (barcode != null) {
                        navigateToBarcodeResultActivity(barcode)
                    }
                }
            }

            REQUEST_CODE_AD_FREE -> {
                Log.i(TAG, "onActivityResult REQUEST_CODE_AD_FREE")
                if (resultCode == RESULT_OK) {
                    //Remove Ad Free from Nav Drawer
//                    handleAdFreeView();
                    flashScanUtil!!.showSnackBar(
                        findViewById(android.R.id.content), getString(
                            R.string.ad_free_success_msg,
                            getString(R.string.app_name)
                        )
                    )
                    try {
                        reCreate()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            REQUEST_CODE_FOR_SINGLE_DOCUMENT -> when (resultCode) {
                Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD -> if (fileModelForWaterMark != null) {
                    createPdfForSingleDocument(fileModelForWaterMark, false)
                }

                Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK -> if (fileModelForWaterMark != null) {
                    createPdfForSingleDocument(fileModelForWaterMark, false)
                    Toast.makeText(
                        this,
                        "" + getString(
                            R.string.water_mark_free_success_msg, getString(R.string.app_name)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }

                Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED, Constants.WaterMarkActivityResultCodes.RESULT_IGNORE -> if (fileModelForWaterMark != null) createPdfForSingleDocument(
                    fileModelForWaterMark, true
                )
            }

            REQUEST_CODE_FOR_MULTIPLE_DOCUMENT -> when (resultCode) {
                Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD -> if (fileModelListForWaterMark != null) {
                    saveAsPdfSelectedDocuments(fileModelListForWaterMark, false)
                }

                Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK -> if (fileModelListForWaterMark != null) {
                    saveAsPdfSelectedDocuments(fileModelListForWaterMark, false)
                    Toast.makeText(
                        this,
                        "" + getString(
                            R.string.water_mark_free_success_msg,
                            getString(R.string.app_name)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }

                Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED, Constants.WaterMarkActivityResultCodes.RESULT_IGNORE -> if (fileModelListForWaterMark != null) saveAsPdfSelectedDocuments(
                    fileModelListForWaterMark, true
                )
            }

            REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT -> when (resultCode) {
                Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD -> if (fileModelForWaterMark != null) {
                    createPdfForShareSingleDocument(fileModelForWaterMark, false)
                }

                Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK -> if (fileModelForWaterMark != null) {
                    createPdfForShareSingleDocument(fileModelForWaterMark, false)
                    Toast.makeText(
                        this,
                        "" + getString(
                            R.string.water_mark_free_success_msg,
                            getString(R.string.app_name)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }

                Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED, Constants.WaterMarkActivityResultCodes.RESULT_IGNORE -> if (fileModelForWaterMark != null) createPdfForShareSingleDocument(
                    fileModelForWaterMark, true
                )
            }

            REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS -> when (resultCode) {
                Constants.WaterMarkActivityResultCodes.RESULT_EARNED_REWARD -> if (fileModelListForWaterMark != null) {
                    createPdfForShareSelectedDocuments(fileModelListForWaterMark, false)
                }

                Constants.WaterMarkActivityResultCodes.RESULT_PURCHASE_WATERMARK -> if (fileModelListForWaterMark != null) {
                    createPdfForShareSelectedDocuments(fileModelListForWaterMark, false)
                    Toast.makeText(
                        this,
                        "" + getString(
                            R.string.water_mark_free_success_msg,
                            getString(R.string.app_name)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }

                Constants.WaterMarkActivityResultCodes.RESULT_AD_CANCELLED, Constants.WaterMarkActivityResultCodes.RESULT_IGNORE -> if (fileModelListForWaterMark != null) createPdfForShareSelectedDocuments(
                    fileModelListForWaterMark, true
                )
            }

            REQUEST_IMAGE_GET -> if (resultCode == RESULT_OK) {
                if (data != null) {
                    val imageUri = data.data
                    if (imageUri != null) {
                    }
                }
            }

            REQUEST_CODE_FETCH_FAVORITE_DOCUMENTS, REQUEST_CODE_FETCH_ALL_DOCUMENTS -> {
                isNativeAdAlreadyLoaded = true
                fetchFiles()
            }

            REQUEST_GET_IMAGES_USING_LIBRARY -> if (resultCode == RESULT_OK) {
                if (data != null) {
                    var corruptFileCount = 0

                    var selectedImagesPathList = if (FlashScanUtil.isOsLessThanR()) {
                        ArrayList(Matisse.obtainPathResult(data))
                    } else {
                        ArrayList(FlashScanUtil.getClipData(data, this))
                    }
                    val tempList = ArrayList<String?>()
                    try {
                        Log.e(TAG, "aa " + selectedImagesPathList.size)

                        var i = 0
                        while (i < selectedImagesPathList.size) {
                            val file = File(selectedImagesPathList[i])

                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            BitmapFactory.decodeFile(file.absolutePath, options)
                            val imageHeight = options.outHeight
                            val imageWidth = options.outWidth


                            if ((imageHeight == -1 && imageWidth == -1) || file.length() <= 0) {      // image height width of corrupt image is -1
                                corruptFileCount++
                            } else {
                                tempList.add(selectedImagesPathList[i])
                            }

                            i++
                        }
                        selectedImagesPathList = ArrayList(tempList)
                    } catch (e: Exception) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    if (corruptFileCount > 0) {
                        flashScanUtil!!.showSnackBar(
                            binding.llMain, String.format(
                                getString(R.string.corrupted_file_error) + "", corruptFileCount
                            )
                        )
                    }
                    if (selectedImagesPathList.isNotEmpty()) {
                        copyFiles(selectedImagesPathList)
                    }

                    /*Intent intent = new Intent(this, ImageCropActivity.class);
                        intent.putExtra(Constants.PutExtraConstants.FROM_SOURCE, Constants.FROM_MEDIA_FILES);
                        intent.putParcelableArrayListExtra(Constants.PutExtraConstants.SELECTED_IMAGES_LIST, selectedImagesPathList);
                        startActivity(intent);*/

                    //Todo commented by Harshit
                    /*if (selectedImagesPathList != null && !selectedImagesPathList.isEmpty()) {
                            if (selectedImagesPathList.size() == 1) {
                                intentToScanActivity(selectedImagesPathList);
                            } else {
                                ArrayList<String> selectedImagesList = new ArrayList<>(selectedImagesPathList);
                                intentToCropFilterActivity(selectedImagesList);
                            }
                        }*/
                }
            }

            TAKE_PHOTO -> if (RESULT_OK == resultCode) {
                /*if (data == null) return
                val fileSink = externalCacheDir

                if (fileSink != null) {
                    if (fileSink.exists() || fileSink.mkdirs()) {
                        val timeStamp =
                            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

                        // Build picture name from
                        val fileName = String.format("shot-%s.jpg", timeStamp)
                        val pictureFile = File(fileSink.absolutePath, fileName)
                        if (data.extras?.get("data") != null) {
                            val bitmap = data.extras?.get("data") as Bitmap?
                            val out = FileOutputStream(pictureFile)
                            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            if (out != null) {
                                out.flush()
                                out.close()
                            }
                            val capturedPaths = ArrayList<String?>()
                            capturedPaths.add(pictureFile.absolutePath)
                            copyFiles(capturedPaths)
                        }
                    }
                }*/
                if (data == null) return
                val capturedPaths = data.getSerializableExtra("cam_paths") as ArrayList<String>
                copyFiles(capturedPaths)
            }

            Constants.REQUEST_CODE_PREMIUM_YEALY -> {
                Log.i(TAG, "HomeActivity REQUEST_CODE_PREMIUM_YEALY")
                if (resultCode == RESULT_OK) {
                    //PREMIUM taken
                    handlePremium()
                    if (prefManager!!.purchasedPlanName == Constants.BUY_NOW_YEARLY) {
                        if (Constants.YEARLY_PLAN_RESTORED) {  // show msg when yearly plan restored in other device
                            flashScanUtil!!.showSnackBar(
                                binding.llMain,
                                getString(R.string.premium_yearly_success_restore_msg)
                            )
                            Constants.YEARLY_PLAN_RESTORED = false
                        } else {   // show msg when yearly plan purchased
                            flashScanUtil!!.showSnackBar(
                                binding.llMain,
                                getString(
                                    R.string.premium_yearly_success_msg,
                                    getString(R.string.app_name)
                                )
                            )
                        }
                    }/*else{
                        if(Constants.QUARTERLY_PLAN_RESTORED){  // show msg when quaterly plan restored in other device
                            flashScanUtil.showSnackBar(llMain, getString(R.string.premium_quarterly_success_restored_msg));
                            Constants.QUARTERLY_PLAN_RESTORED = false;
                        }
                        else {   // show msg when quaterly plan purchased
                            flashScanUtil.showSnackBar(llMain, getString(R.string.premium_quarterly_success_msg));
                        }
                    }*//*try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
            }

            Constants.REQUEST_CODE_OCR_MONTHLY -> {
                Log.i(TAG, "HomeActivity REQUEST_CODE_PREMIUM_YEALY")
                if (resultCode == RESULT_OK) {
                    //OCR monthly taken
                    handleOcrMonthly()
                    flashScanUtil!!.showSnackBar(
                        binding.llMain,
                        getString(R.string.ocr_monthly_success_msg, getString(R.string.app_name))
                    )/*try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
            }

            REQUEST_CODE_FETCH_OCR_DOCUMENTS -> {
                if (resultCode == RESULT_OK) {
                    fetchFiles()
                }
            }
        }
    }

    private fun handleOcrMonthly() {

    }

    private fun handlePremium() {
    }

    private fun copyFiles(selectedImagesPathList: ArrayList<String>) {
        val folderName = flashScanUtil!!.folderCurrentTime

        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            Constants.DOC_PROCESSING_PATH = new File(getApplicationthis().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            + File.separator + Constants.ROOT_FOLDER_NAME);

            Constants.DOC_ORIGINAL_PATH = new File(getApplicationthis().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + File.separator + Constants.ROOT_FOLDER_NAME + File.separator + Constants.originalFolderName);
        }*/
        //File dstFolderName = new File(flashScanUtil.getPath(this, Environment.DIRECTORY_PICTURES,Constants.ROOT_FOLDER_NAME), folderName);
        val dstFolderName = File(FlashScanUtil.getDocProcessingPath(this), folderName)
        val dstOriginalFolderName = File(FlashScanUtil.getDocOriginalPath(this), folderName)

        if (dstFolderName != null) {
            if (!dstFolderName.exists()) {
                dstFolderName.mkdirs()
            }
        }

        if (dstOriginalFolderName != null) {
            if (!dstOriginalFolderName.exists()) {
                dstOriginalFolderName.mkdirs()
            }
        }

        CopyFileTask(
            this,
            selectedImagesPathList,
            dstFolderName.absolutePath,
            dstOriginalFolderName.absolutePath,
            object : CopyOperationListener {
                override fun onCopyStart() {
                    binding.homeLayout.progressLay.root.visibility = View.VISIBLE
                }

                override fun onCopyComplete(fileOperation: Int) {
                    binding.homeLayout.progressLay.root.visibility = View.GONE

                    val intent = Intent(this@HomeActivity, ScanResultActivity::class.java)
                    intent.putExtra(
                        ScanConstants.PutExtraConstants.FROM_SCREEN,
                        ScanConstants.ScreenConstants.FROM_HOME_SCREEN
                    )
                    intent.putExtra(ScanConstants.PutExtraConstants.FOLDER_NAME, folderName)
                    startActivityForResult(intent, REQUEST_CODE_FETCH_ALL_DOCUMENTS)

                    if (selectedImagesPathList.size == 1) {
                        val pathLists = ArrayList<String>()
                        val file = File(selectedImagesPathList[0])
                        val processedPath = File(dstFolderName, "0_" + file.name)
                        pathLists.add(processedPath.absolutePath)

                        val intent1 = Intent(this@HomeActivity, ImageCropActivity::class.java)
                        intent1.putExtra("is_bmp", true)
                        intent1.putExtra("folder_name", folderName)
                        intent1.putStringArrayListExtra(
                            Constants.PutExtraConstants.SELECTED_IMAGES_LIST, pathLists
                        )
                        intent1.putExtra("pos", 0)

                        startActivity(intent1)
                    } /*else overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)*/
                }
            },
            true
        ).execute()
    }


    private fun reCreate() {
        finish()
        startActivity(intent)
    }

    private fun navigateToBarcodeResultActivity(barcode: ResultBarCode) {
        val intent = Intent(this, BarCodeResultActivity::class.java)
        intent.putExtra(Constants.PutExtraConstants.SCANNED_BARCODE, barcode)
        startActivity(intent)
//        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    override fun onFileOrFolderDeleted() {
        fetchFiles()
        flashScanUtil!!.showSnackBar(
            findViewById(android.R.id.content), getString(R.string.delete_success_msg)
        )
    }

    override fun onBackPressed() {
        if (fileModelAdapter != null && fileModelAdapter!!.isVisibleAllCheckbox) {
            hideCheckBoxAndRemoveBottomBar()
            //            ll_floating.setVisibility(View.VISIBLE);
            /*fab_camera.setVisibility(View.VISIBLE);
            fab_media.setVisibility(View.VISIBLE);*/
        } else {/*finishAffinity();*/
            val showIntersExit = dbHandler!!.showIntersExit()
            //            if (!prefManager.isAppAdFree() && Constants.SHOW_INTERSTITIAL_ADS.FOR_EXIT_APP_DIALOG) {
            if (!prefManager!!.isAppAdFree && showIntersExit && flashScanUtil?.isConnectingToInternet == true) {
                show(this)/*if (interstitialAd != null && interstitialAd.isLoaded()) {
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            showExitAppDialog();
                        }

                        @Override
                        public void onAdFailedToLoad(int i) {
                            super.onAdFailedToLoad(i);
                            showExitAppDialog();
                        }
                    });
                    interstitialAd.show();
                } *//*if (AdsManager.getINSTANCE().isAdLoadedForExitApp()) {
                    AdsManager.getINSTANCE().showInterstitialAdForExitApp(new AdManagerListener() {
                        @Override
                        public void onAdLoaded() {

                        }

                        @Override
                        public void onAdFailedToLoad() {
                            showExitAppDialog();
                        }

                        @Override
                        public void onAdClosed() {
                            showExitAppDialog();
                        }
                    });
                } else {
                    showExitAppDialog();
                }*/
            } else {
                showExitAppDialog()
            }
        }
    }

    private fun hideCheckBoxAndRemoveBottomBar() {
        fileModelAdapter!!.hideAllCheckBoxes()
        binding.homeLayout.llBottomBar.root.visibility = View.GONE
        binding.homeLayout.llFloating.visibility = View.VISIBLE
    }

    private fun showExitAppDialog() {

        finish()
        /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.exit_msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAffinity();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();*/


        /*val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_exit_app)
        dialog.setCancelable(false)


        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btn_ok = dialog.findViewById<Button>(R.id.btn_ok)
        val ad_view_banner_container =
            dialog.findViewById<LinearLayout>(R.id.ad_view_banner_container)
        val nativeSmallExitDialog = dialog.findViewById<FrameLayout>(R.id.nativeSmallExitDialog)


        //boolean showNative = dbHandler.showNative();

//        if (!prefManager.isAppAdFree() && Constants.SHOW_NATIVE_ADS.FOR_HOME_ACTIVITY) {
        if (!prefManager!!.isAppAdFree && flashScanUtil!!.isConnectingToInternet) {
            ad_view_banner_container.visibility = View.VISIBLE
            if (AppController.nativeAdExitAppDialog != null) {
                smallNativeAdSet(AppController.nativeAdExitAppDialog, nativeSmallExitDialog, false)
            }
        } else {
            ad_view_banner_container.visibility = View.GONE
        }


        btn_cancel.setOnClickListener { dialog.dismiss() }

        btn_ok.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
            System.exit(0)
        }

        dialog.show()*/
    }

    private fun callNativeAd(nativeSmallAdNoDoc: FrameLayout?) {
        if (AppController.nativeAdDoc == null) {
            val customEventNativeLoader = AdLoader.Builder(
                this, BuildConfig.AD_UNIT_ID_DOC_BLACK_SCREEN_NATIVE_AD
            ).forNativeAd { nativeAd: NativeAd? ->
                Log.e("HOME_NO_DOC_NATIVE_AD ", "onUnifiedNativeAdLoaded G `> " + "")
                AppController.nativeAdDoc = nativeAd
                smallDocNativeAdSet(nativeAd, nativeSmallAdNoDoc, false)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(
                        "HOME_NO_DOC_NATIVE_AD ", "onAdFailedToLoad G > " + loadAdError.message
                    )
                }
            }).withNativeAdOptions(
                NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()
            ).build()
            customEventNativeLoader.loadAd(AdRequest.Builder().build())
        } else {
            smallDocNativeAdSet(AppController.nativeAdDoc, nativeSmallAdNoDoc, false)
        }
    }

    private fun callExitNativeAd() {
        if (AppController.nativeAdExitAppDialog == null) {
            val customEventNativeLoader = AdLoader.Builder(
                this, BuildConfig.NATIVE_EXIT
            ).forNativeAd { nativeAd: NativeAd? ->
                Log.e("HOME_NATIVE_AD ", "onUnifiedNativeAdLoaded G `> " + "")
                AppController.nativeAdExitAppDialog = nativeAd
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("HOME_NATIVE_AD ", "onAdFailedToLoad G > " + loadAdError.message)
                }
            }).withNativeAdOptions(
                NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()
            ).build()
            customEventNativeLoader.loadAd(AdRequest.Builder().build())
        } else {
            //smallNativeAdSet(AppController.nativeAdExitAppDialog, nativeSmallExitDialog, false);
        }
    }

    /*  private void loadExitNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(this, BuildConfig.NATIVE_AD_ID)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        Log.i("----HomeActivity", "onUnifiedNativeAdLoaded > ");
                        if (isDestroyed()) {
                            unifiedNativeAd.destroy();
                            return;
                        }

                        *//*new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT);*/ /*
                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.item_view_native_ad, null, false);
                        if (unifiedNativeAdView != null) {
                            unifiedNativeAdViewExit = unifiedNativeAdView;
                            Log.i("----HomeActivity", "unifiedNativeAdView > ");
                            mapUnifiedNativeAdToLayout(unifiedNativeAd, unifiedNativeAdView);

                        }

                    }
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.i("----HomeActivity", "onAdFailedToLoad > ");

                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }*/
    override fun actionShare(o: Any) {
        var fileModel: FileModel? = null
        if (o != null) {
            if (o is FileModel) {
                fileModel = o
            }
        }
        if (fileModel == null) return
        CommonMethods.logCustomFireBaseEvents(
            screenName + "_" + getString(R.string.module_name),
            com.itl.commonres.utils.Constants.CLICK_SHARE_ICON
        )
        showShareDialog(fileModel)
    }

    override fun actionRename(o: Any) {
        var fileModel: FileModel? = null
        if (o != null) {
            if (o is FileModel) {
                fileModel = o
            }
        }
        if (fileModel == null) return
        showCommonDialog(fileModel, Constants.FileOperations.ACTION_RENAME)
    }

    override fun actionDelete(o: Any) {
        var fileModel: FileModel? = null
        if (o != null) {
            if (o is FileModel) {
                fileModel = o
            }
        }
        if (fileModel == null) return
        CommonMethods.logCustomFireBaseEvents(
            screenName + "_" + getString(R.string.module_name),
            com.itl.commonres.utils.Constants.CLICK_DELETE_ICON
        )
        showCommonDialog(fileModel, Constants.FileOperations.ACTION_DELETE)
    }

    override fun actionAddToDrive(o: Any, position: Int) {
        if (flashScanUtil?.isConnectingToInternet == false) {
            Toast.makeText(
                this, resources.getString(R.string.connect_to_internet), Toast.LENGTH_LONG
            ).show()
            return
        }

        var fileModel: FileModel? = null

        CommonMethods.logCustomFireBaseEvents(
            screenName + "_" + getString(R.string.module_name),
            com.itl.commonres.utils.Constants.CLICK_GOOGLE_DRIVE_ICON
        )
        if (o != null) {
            if (o is FileModel) {
                fileModel = o
            }
        }
        if (fileModel == null) return
        checkDriveSignIn(fileModel, position)
    }

    override fun actionSaveAsPdf(o: Any) {
        //var fileModel: FileModel? = null
        click = null
        if (o != null) {
            if (o is FileModel) {
                fileModel = o
                selectedFileModel = fileModel
            }
        }
        if (fileModel == null) return
        CommonMethods.logCustomFireBaseEvents(
            screenName + "_" + getString(R.string.module_name),
            com.itl.commonres.utils.Constants.CLICK_PDF_ICON
        )
        if (!prefManager!!.isAppAdFree && flashScanUtil!!.isConnectingToInternet && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(
                AdsPlacementsEnum.SH_HOME_PDF_ICON_CLICK.value
            ) && CommonMethods.isInterstitialCappingValid(InterstitialAdCappingEnum.SH_HOME_PDF_ICON_CLICK.value)
        ) {
            val value =
                com.itl.commonres.utils.Constants.AdInterstitialCappingArrayList[InterstitialAdCappingEnum.SH_HOME_PDF_ICON_CLICK.value] + 1
            com.itl.commonres.utils.Constants.AdInterstitialCappingArrayList[InterstitialAdCappingEnum.SH_HOME_PDF_ICON_CLICK.value] =
                value
            //show(this);
            // load interstitial Ad for PDF creation
            Constants.isAppInBackground = false
            loadInterstitialAd(this, BuildConfig.AD_UNIT_ID_PDF_ICON_INTERSTITIAL_AD, this)
        } else {
            Log.e("Mobibuz : ", "Ad Not Showing")
            if (!isPermissionGranted) {
                processForPermissions()
                return
            }
            createPDF()
        }
    }

    private fun showAskPdfNameDialog(fileModel: FileModel, pdfVia: Int) {
        val dialog = Dialog(this)
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_ask_pdf_name)

        val et_pdf_name = dialog.findViewById<EditText>(R.id.et_pdf_name)
        et_pdf_name.setText(fileModel.name)
        val btn_done = dialog.findViewById<Button>(R.id.btn_done)

        val rbOriginal = dialog.findViewById<RadioButton>(R.id.rb_original)
        val rbCompressed = dialog.findViewById<RadioButton>(R.id.rb_compressed)

        val spinner = dialog.findViewById<Spinner>(R.id.spinner)


        val pageSizeList = flashScanUtil!!.pageSizeList
        if (pageSizeList != null && !pageSizeList.isEmpty()) {
            val pageSizesAdapter = PageSizesAdapter(this, pageSizeList)
            spinner.adapter = pageSizesAdapter
        }
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                val pageSize: PageSize = parent.getItemAtPosition(position) as PageSize
                if (pageSize != null) {
                    selectedPageSize = pageSize.sizeValue
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        btn_done.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (TextUtils.isEmpty(et_pdf_name.text.toString().trim { it <= ' ' })) {
                    Toast.makeText(
                        this@HomeActivity,
                        "" + getString(R.string.please_name_the_pdf),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if (TextUtils.isEmpty(selectedPageSize)) {
                    Toast.makeText(
                        this@HomeActivity,
                        "" + getString(R.string.please_select_page_size),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                PdfSettings.getInstance().selectedPdfPageSize = selectedPageSize
                fileModel.pdfFileName = et_pdf_name.text.toString().trim { it <= ' ' }
                if (rbOriginal.isChecked) {
                    fileModel.isCompressedPdf = false
                    // working same as previous
                } else if (rbCompressed.isChecked) {
                    // new working for compressoion
                    fileModel.isCompressedPdf = true
                }
                handlePdfCreation(fileModel, pdfVia)
                dialog.dismiss()
            }
        })

        dialog.show()
    }

    private fun handlePdfCreation(fileModel: FileModel, pdfVia: Int) {
        when (pdfVia) {
            PDF_BY_DIRECT -> {
                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                if (!prefManager!!.isAppWatermarkFree && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    fileModelForWaterMark = fileModel
                    goToWaterMarkRemoveActivityForSingleDocument()
                } else {
                    if (prefManager!!.isAppWatermarkFree || prefManager!!.isPremiumYearly /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForSingleDocument(fileModel, false)
                    } else {
                        createPdfForSingleDocument(fileModel, true)
                    }
                }
            }

            PDF_VIA_SHARE -> {
                if (!isPermissionGranted) {
                    processForPermissions()
                    return
                }
                if (!prefManager!!.isAppWatermarkFree && Constants.SHOW_REWARDED_ADS.FOR_SAVE_AS_PDF) {
                    fileModelForWaterMark = fileModel
                    goToWaterMarkRemoveActivityForShareSingleDocument()
                } else {
                    if (prefManager!!.isAppWatermarkFree || prefManager!!.isPremiumYearly /*|| prefManager.isPremiumQuarterly()*/) {
                        createPdfForShareSingleDocument(fileModel, false)
                    } else {
                        createPdfForShareSingleDocument(fileModel, true)
                    }
                }
            }
        }
    }

    private fun goToWaterMarkRemoveActivityForSingleDocument() {
        val intent = Intent(this, WaterMarkRemoveActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FOR_SINGLE_DOCUMENT)
    }

    /*private void showRewardAdDialogForSingleDocument(FileModel fileModel) {
        Dialog dialog = new Dialog(this);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_rewarded_ad_pdf);

        Button btn_watch_video = dialog.findViewById(R.id.btn_watch_video);
        Button btn_purchase = dialog.findViewById(R.id.btn_purchase);

        btn_watch_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.i(TAG, "onUserEarnedReward called");
                        createPdfForSingleDocument(fileModel, false);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        Log.i(TAG, "onRewardedAdClosed called");
                        *//*loadRewardedAd();*/ /*
                        createPdfForSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        Log.i(TAG, "onRewardedAdFailedToShow called");
                        createPdfForSingleDocument(fileModel, true);
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        super.onRewardedAdOpened();
                        Log.i(TAG, "onRewardedAdOpened called");
                    }
                };
                rewardedAd.show(HomeActivity.this, rewardedAdCallback);
            }
        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/
    private fun createPdfForSingleDocument(fileModel: FileModel?, isWaterMarkToBeShown: Boolean) {
        if (fileModel!!.isCompressedPdf) {
            GetTempCompressedBitmapPath(this, fileModel.path, object : CreateTempBitmapListener {
                override fun onCompressingStart() {
                    binding.homeLayout.progressLay.root.visibility = View.VISIBLE
                }

                override fun onCompressingComplete(compressedFile: File) {
                    binding.homeLayout.progressLay.root.visibility = View.GONE
                    if (compressedFile != null) {
                        createPdfFromDir(compressedFile.path, fileModel, isWaterMarkToBeShown)
                    }
                }
            }).execute()
        } else {
            createPdfFromDir(fileModel.path, fileModel, isWaterMarkToBeShown)
        }
    }

    private fun createPdfForShareSingleDocument(
        fileModel: FileModel?, isWaterMarkToBeShown: Boolean
    ) {
        if (fileModel!!.isCompressedPdf) {
            GetTempCompressedBitmapPath(this, fileModel.path, object : CreateTempBitmapListener {
                override fun onCompressingStart() {
                    binding.homeLayout.progressLay.root.visibility = View.VISIBLE
                }

                override fun onCompressingComplete(compressedFile: File) {
                    binding.homeLayout.progressLay.root.visibility = View.GONE
                    if (compressedFile != null) {
                        createPdfForShareFromDir(
                            compressedFile.path, fileModel, isWaterMarkToBeShown
                        )
                    }
                }
            }).execute()
        } else {
            createPdfForShareFromDir(fileModel.path, fileModel, isWaterMarkToBeShown)
        }
    }

    private fun createPdfForShareFromDir(
        path: String, fileModel: FileModel?, isWaterMarkToBeShown: Boolean
    ) {
        val fileOrDirectory = File(path)
        if (fileOrDirectory.isDirectory) {
            val files = fileOrDirectory.listFiles()
            if (files != null && files.size > 0) {
                flashScanUtil!!.sortFilesByNameAtoZ(files)
                /*val appSortingOrder = prefManager!!.appSortingOrder
                when (appSortingOrder) {
                    Constants.SORT_BY.defaultOrder, Constants.SORT_BY.modificationTimeDescending -> flashScanUtil!!.sortFilesByDescendingLastModified(
                        files
                    )

                    Constants.SORT_BY.modificationTimeAscending -> flashScanUtil!!.sortFilesByAscendingLastModified(
                        files
                    )

                    Constants.SORT_BY.nameAtoZ -> flashScanUtil!!.sortFilesByNameAtoZ(files)
                    Constants.SORT_BY.nameZtoA -> flashScanUtil!!.sortFilesByNameZtoA(files)
                }*/
                val filePathList: MutableList<String> = ArrayList()
                for (file in files) {
                    if (file.isFile && file.exists()) {
                        if (file.name.equals(Constants.JSON_FILE_NAME, ignoreCase = true)) {
                            continue
                        }
                        filePathList.add(file.path)
                    }
                }
                if (!filePathList.isEmpty()) {
                    isPdfCreationForSharing = true
                    createPdf(filePathList, fileModel!!.pdfFileName, isWaterMarkToBeShown)
                } else {
                    // showWarning message
                    showNoFilesInDocumentDialog()
                }
            } else {
                showNoFilesInDocumentDialog()
            }
        }
    }

    private fun createPdfFromDir(
        path: String, fileModel: FileModel?, isWaterMarkToBeShown: Boolean
    ) {
        val fileOrDirectory = File(path)
        val filePathList: MutableList<String> = ArrayList()
        if (fileOrDirectory.isDirectory) {
            val files = fileOrDirectory.listFiles()
            if (files != null && files.size > 0) {
                flashScanUtil!!.sortFilesByNameAtoZ(files)
                /*val appSortingOrder = prefManager!!.appSortingOrder
                when (appSortingOrder) {
                    Constants.SORT_BY.defaultOrder, Constants.SORT_BY.modificationTimeDescending -> flashScanUtil!!.sortFilesByDescendingLastModified(
                        files
                    )

                    Constants.SORT_BY.modificationTimeAscending -> flashScanUtil!!.sortFilesByAscendingLastModified(
                        files
                    )

                    Constants.SORT_BY.nameAtoZ -> flashScanUtil!!.sortFilesByNameAtoZ(files)
                    Constants.SORT_BY.nameZtoA -> flashScanUtil!!.sortFilesByNameZtoA(files)
                }*/
                for (file in files) {
                    if (file.isFile && file.exists()) {
                        if (file.name.equals(Constants.JSON_FILE_NAME, ignoreCase = true)) {
                            continue
                        }
                        filePathList.add(file.path)
                    }
                }
                if (!filePathList.isEmpty()) {
                    isPdfCreationForSharing = false
                    createPdf(filePathList, fileModel!!.pdfFileName, isWaterMarkToBeShown)
                } else {
                    // show warning message
                    showNoFilesInDocumentDialog()
                }
            } else {
                showNoFilesInDocumentDialog()
            }
        }
    }

    override fun makeFavourite(o: Any) {
        var fileModel: FileModel? = null
        if (o == null) return
        if (o is FileModel) {
            fileModel = o
        }
        if (fileModel == null) return

        // use fileModel object here for functionality
        Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show()

        flashScanUtil!!.readUpdateCreateMetaDataJson(fileModel)
        showFavoriteRv()
    }

    override fun removeFavourite(o: Any) {
        var fileModel: FileModel? = null
        if (o == null) return
        if (o is FileModel) {
            fileModel = o
            finalFileModelList.find { it == fileModel }?.isStarred = false
            fileModelAdapter?.notifyItemChanged(finalFileModelList.indexOf(fileModel))
        }
        if (fileModel == null) return

        // use fileModel object here for functionality
        Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show()
        flashScanUtil!!.readUpdateCreateMetaDataJson(fileModel)
        showFavoriteRv()
    }

    override fun onDestroy() {/*if (nativeAd != null) {
            nativeAd.destroy();
        }*/
        if (fileModelAdapter != null) {
            fileModelAdapter!!.destroyAdapterNativeAd()
        }
        super.onDestroy()
    }

    override fun onSuccess() {
        Log.i(TAG, "open offer activity")
        openOfferActivity()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
    }

    /*private class GetValidUrlOrNot extends AsyncTask<Void, Void, Boolean> {
        String url = "";
        //testUrl = "https://cdn.horoscopelogy.com/hor/appads/andr/TarotLife_Offer_Ask_Tarot_5.html";

        @Override
        protected Boolean doInBackground(Void... voids) {
//            Log.e(TAG, "=======     doInBackground");

            url = prefManager.getOfferUrlServer();
            return urlValidOrNot(url);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                if (result) {
//                    Log.e(TAG, "=======     onPostExecute Result "+result);
                    if (util.isConnectingToInternet()) {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        offer_layout.setVisibility(View.VISIBLE);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.loadUrl(url);
                        webView.setWebViewClient(new MyWebViewClient(this));

                        // getHtmlFromWeb(url);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "RESULT 4 exception " + e.getMessage());
            }
        }
    }

    public boolean urlValidOrNot(String urls) {
        try {
            URL url = new URL(urls);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout(2000);
            isOnline = conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            isOnline = false;
        }
        return isOnline;
    }

    private class MyWebViewClient extends WebViewClient {
        this contx;

        MyWebViewClient(this ctx) {
            contx = ctx;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            Log.e(TAG, "=======     URL" + url);
            String TEL_PREFIX = "tel:";
            if (url.startsWith(TEL_PREFIX)) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            } else if (url.equalsIgnoreCase(Constants.WEBSITE_URL)) {
//                Log.e(TAG, "=======     main Activity  else if Offer Case");
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                webView.setVisibility(View.GONE);
                offer_layout.setVisibility(View.GONE);
                return true;
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                webView.setVisibility(View.GONE);
                offer_layout.setVisibility(View.GONE);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //cancelProgressDialog();
        }
    }*//*private void firebaseRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        //Set default values
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        //Fetch and activate remote config values
        fetchAndActivateRemoteConfigValues();
    }

    private void fetchAndActivateRemoteConfigValues() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            Log.i(TAG, "Fetch and activate succeeded");
                            Toast.makeText(HomeActivity.this, "Fetch and activate succeeded",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(HomeActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        operationOnRemoteConfigData();
                    }
                });
    }

    private void operationOnRemoteConfigData() {

        putInDbRules();

        String dataJsonStr = mFirebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_DATA);
        Log.i(TAG, "data: " + dataJsonStr);

        try {

            JSONObject dataJson = new JSONObject(dataJsonStr);

            Log.d("My App", dataJson.toString());

            JSONArray jsonArrDevices = dataJson.getJSONArray(Constants.JSON_NODE_DEVICES);

            putInDbDevices(jsonArrDevices);

            dbVisibility();
        } catch (JSONException je) {
            Log.e(TAG, "jsonexception;" + je.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "exception" + e.getMessage());
        }

        getValuesFromDb();
    }

    private void putInDbDevices(JSONArray jsonArrDevices) {
        if (dbHandler.existDevicesAllowed()) {
            dbHandler.deleteDeviceAllowed();
            Log.i(TAG, "previous devices deleted");
        }

        for (int i = 0; i < jsonArrDevices.length(); i++) {
            try {
                String androidId = (String) jsonArrDevices.get(i);
                Log.i(TAG, "android_id: " + androidId);

                if (!dbHandler.existDevicesAllowed(androidId)) {
                    dbHandler.insertDevicesAllowed(androidId);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void putInDbRules() {
        boolean showIntersSplash = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_INTERS_SPLASH);
        boolean showIntersExit = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_INTERS_EXIT);
        boolean showIntersCreation = mFirebaseRemoteConfig.getBoolean(Constants.REMOTE_CONFIG_SHOW_INTERS_CREATION);
        int intersSplashAfter = (int) mFirebaseRemoteConfig.getLong(Constants.REMOTE_CONFIG_INTERS_SPLASH_AFTER);
        int intersCreateFreqInSession = (int) mFirebaseRemoteConfig.getLong(Constants.REMOTE_CONFIG_INTERS_CREATE_FREQ_IN_SESSION);

        int showIntersSplashInt = showIntersSplash ? 1 : 0;
        int showIntersExitInt = showIntersExit ? 1 : 0;
        int showIntersCreationInt = showIntersCreation ? 1 : 0;

        Log.i(TAG, "show_inters_splash: " + showIntersSplash + " : " + showIntersSplashInt);
        Log.i(TAG, "show_inters_exit: " + showIntersExit + " : " + showIntersExitInt);
        Log.i(TAG, "show_inters_creation: " + showIntersCreation + " : " + showIntersCreationInt);
        Log.i(TAG, "inters_splash_after: " + intersSplashAfter);
        Log.i(TAG, "inters_create_freq_in_session: " + intersCreateFreqInSession);

        if (dbHandler.existAdsRules()) {
            dbHandler.updateAdsRules(showIntersSplashInt, showIntersExitInt, showIntersCreationInt, intersSplashAfter, intersCreateFreqInSession);
        } else {
            dbHandler.insertAdsRules(showIntersSplashInt, showIntersExitInt, showIntersCreationInt, intersSplashAfter, intersCreateFreqInSession);
        }

    }

    private void getValuesFromDb() {
        boolean showIntersSplash = dbHandler.showIntesSplash();
        boolean showIntersExit = dbHandler.showIntersExit();
        boolean showIntersCreation = dbHandler.showIntersCreation();
        int intersSplashAfter = dbHandler.intersSplashAfter();
        int intersCreateFreqInSession = dbHandler.intersCreateFreqInSession();

        Log.i(TAG, "from DB: show_inters_splash: " + showIntersSplash);
        Log.i(TAG, "from DB: show_inters_exit: " + showIntersExit);
        Log.i(TAG, "from DB: show_inters_creation: " + showIntersCreation);
        Log.i(TAG, "from DB: inters_splash_after: " + intersSplashAfter);
        Log.i(TAG, "from DB: inters_create_freq_in_session: " + intersCreateFreqInSession);
    }*/
    override fun onAdClosed() {
        createPDF()
    }


    override fun onAdLoadedOrFailed(isLoaded: Boolean) {
        createPDF()
    }

    private fun createPDF() {
        // create PDF after showing interstitial Ad
        if (selectedFileModel != null) {
            if (Constants.IS_CREATE_PDF_DIRECT) {
                selectedFileModel!!.pdfFileName = selectedFileModel!!.name
                handlePdfCreation(selectedFileModel!!, PDF_BY_DIRECT)
            } else {
                showAskPdfNameDialog(selectedFileModel!!, PDF_BY_DIRECT)
            }
        }
    }

    private fun checkDriveSignIn(fileModel: FileModel, position: Int) {
        if (flashScanUtil!!.isDriveSignedIn) {
            Log.e(TAG, "Drive signed in " + flashScanUtil!!.isDriveSignedIn)
            if (fileModel.isSavedOnGoogleDrive) {
                //showDeleteDialogGoogleDrive(fileModel);
                Toast.makeText(
                    this, resources.getString(R.string.doc_already_synced), Toast.LENGTH_LONG
                ).show()
            } else {
                if (fileModel.size > 0) {
                    flashScanUtil!!.saveFileInGoogleDrive(
                        this,
                        Constants.ROOT_FOLDER_NAME,
                        fileModel,
                        false,
                        resources.getString(R.string.uploading_files_to_drive)
                    ) { folderId: String? ->
                        fileModel.googleDriveFolderId = folderId
                        fileModel.isSavedOnGoogleDrive = true
                        finalFileModelList[position] = fileModel
                        fileModelAdapter!!.notifyItemChanged(position)
                    }
                } else {
                    Toast.makeText(
                        this, resources.getString(R.string.empty_folder), Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil!!.isDriveSignedIn)
            binding.homeLayout.progressLay.root.visibility = View.VISIBLE
            mFileModelForSaveToDrive = fileModel
            positionForSaveToDrive = position
            startActivityForResult(
                flashScanUtil!!.requestSignIn(this).signInIntent, REQUEST_CODE_DRIVE_SIGN_IN
            )
        }
    }

    /*private fun processGoogleSignIn(){
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(flashScanUtil!!.googleIdOption)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val credentialManager = CredentialManager.create(this@HomeActivity)
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@HomeActivity,
                )
//                flashScanUtil!!.handleCredentialResult(result)
            } catch (e: GetCredentialException) {
//                flashScanUtil!!.handleCredentialResult(e)
            }
        }
    }*/

    private val allAppUserDataFromGoogleDrive: Unit
        get() {
            if (flashScanUtil!!.isDriveSignedIn) {
                Log.e(TAG, "Drive signed in " + flashScanUtil!!.isDriveSignedIn)

                //Show progress loader dialog
                /*ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(this.getResources().getString(R.string.downloading_files_from_drive));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();*/
                prefManager!!.saveGoogleDriveDataList(ArrayList())
                flashScanUtil!!.queryFolderOnGoogleDrive(
                    this,
                    getString(R.string.fetching_drive_documents),
                    Constants.ROOT_FOLDER_NAME,
                    FlashScanUtil.getDocProcessingPath(this)
                ) {
                    //progressDialog.dismiss();
                    flashScanUtil!!.queryFolderOnGoogleDrive(
                        this,
                        getString(R.string.fetching_drive_ocr),
                        Constants.FLASH_SCAN_OCR,
                        FlashScanUtil.getOcrProcessingPath(this),
                        GoogleDriveDataDownloadListener {
                            //progressDialog.dismiss();
                            fetchFiles()
                        })
                }
            } else {
                Log.e(TAG, "Drive not signed in " + flashScanUtil!!.isDriveSignedIn)
                binding.homeLayout.progressLay.root.visibility = View.VISIBLE
                isGetAllDataFromDrive = true
                startActivityForResult(
                    flashScanUtil!!.requestSignIn(this).signInIntent, REQUEST_CODE_DRIVE_SIGN_IN
                )
            }
        }

    private fun deleteFromGoogleDrive(folderName: String) {
        if (flashScanUtil!!.isDriveSignedIn) {
            Log.e(TAG, "Drive signed in " + flashScanUtil!!.isDriveSignedIn)
            flashScanUtil!!.deleteFolderFromGoogleDrive(this, folderName)
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil!!.isDriveSignedIn)
            binding.homeLayout.progressLay.root.visibility = View.VISIBLE
            startActivityForResult(
                flashScanUtil!!.requestSignIn(this).signInIntent, REQUEST_CODE_DRIVE_SIGN_IN
            )
        }
    }

    private fun deleteFromGoogleDriveById(fileModel: FileModel) {
        if (flashScanUtil!!.isDriveSignedIn) {
            Log.e(TAG, "Drive signed in " + flashScanUtil!!.isDriveSignedIn)
            flashScanUtil!!.deleteFolderByIdFromGoogleDrive(
                this,
                fileModel.googleDriveFolderId,
                resources.getString(R.string.delete_files_from_drive)
            ) {
                prefManager!!.deleteFolderFromGoogleDriveDataList(fileModel.googleDriveFolderId)
                fileModel.isSavedOnGoogleDrive = false
                fileModel.googleDriveFolderId = ""
            }
        } else {
            Log.e(TAG, "Drive not signed in " + flashScanUtil!!.isDriveSignedIn)
            binding.homeLayout.progressLay.root.visibility = View.VISIBLE
            startActivityForResult(
                flashScanUtil!!.requestSignIn(this).signInIntent, REQUEST_CODE_DRIVE_SIGN_IN
            )
        }
    }

    companion object {
        const val TAKE_PHOTO: Int = 458
        private const val REQUEST_CODE_DRIVE_OPEN_DOCUMENT = 2
        private const val REQUEST_CODE_FOR_SINGLE_DOCUMENT = 201
        private const val REQUEST_CODE_FOR_MULTIPLE_DOCUMENT = 202
        private const val REQUEST_CODE_FOR_SHARE_SINGLE_DOCUMENT = 203
        private const val REQUEST_CODE_FOR_SHARE_MULTIPLE_DOCUMENTS = 204
        private const val REQUEST_IMAGE_GET = 11
        private const val REQUEST_CODE_FETCH_FAVORITE_DOCUMENTS = 301
        private const val REQUEST_CODE_FETCH_ALL_DOCUMENTS = 302
        private const val PDF_BY_DIRECT = 1
        private const val PDF_VIA_SHARE = 2
        const val REQUEST_GET_IMAGES_USING_LIBRARY: Int = 501
        private const val BARCODE_READER_ACTIVITY_REQUEST = 100
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val PERMISSIONS_SETTING_REQUEST_CODE = 101
        private val TAG: String = HomeActivity::class.java.simpleName

        private const val REQUEST_CODE_AD_FREE = 115

        //    public FirebaseRemoteConfig mFirebaseRemoteConfig;
        var isMonthlyOcrExpired: Boolean = false

        var isYearlyPlanExpired: Boolean = false
        fun deleteDir(dir: File): Boolean {
            if (dir.isDirectory) {
                val children = dir.list()
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }

            // The directory is now empty so delete it
            return dir.delete()
        }
    }

    private fun setupRecyclerViewLayoutManager() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.homeLayout.rvScannerFiles.layoutManager = linearLayoutManager
        binding.homeLayout.rvScannerFiles.setHasFixedSize(true)

        binding.homeLayout.rvFavorites.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.homeLayout.rvFavorites.setHasFixedSize(true)
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        permissionUtils?.onRequestPermissionResult(false)
    }

    override fun requestPermissions(permissionList: MutableList<String>, requestCode: Int) {
        requestMultiplePermissions.launch(permissionList.toTypedArray())
        val permissionPreference = PermissionPreference(this)

        for (permission in permissionList) {
            permissionPreference.setPermissionRequested(permission)
        }
    }

    override fun getPermissionResult(isPermissionGiven: Boolean) {
        isPermissionGranted = isPermissionGiven
        Log.i(TAG, "getPermissionResult: $isPermissionGiven")
        Log.e(TAG, "getPermissionResult: $fileModel")
        Log.e(TAG, "getPermissionResult: $click")

        if (isPermissionGiven) {
            if (click != null) {
                click?.performClick()
            } else {
                createPDF()
            }
        }

        /*if (isPermissionGiven) {
            fetchFiles()
        }*/
    }

    private fun processForPermissions(checkStatusOnly: Boolean = false) {
        Log.i(TAG, "getPermissionResult:checkStatusOnly:: $checkStatusOnly")
        var permissionName = Constants.Storage_and_Camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionName = Constants.Camera
        }
        val list = PermissionsListSealedClass.from(
            permissionName
        ).permissionsList

        permissionList?.addAll(list)
        permissionUtils?.addPermissionsToList(list)

        permissionUtils?.setPermissionName(
            PermissionsListSealedClass.from(
                permissionName
            ).permissionName
        )
        if (checkStatusOnly) {
            val result = permissionUtils?.checkAndRequestPermissions(true)
            isPermissionGranted = result?.finalStatus == PermissionStatus.ALLOWED
        } else {
            permissionUtils?.checkAndRequestMultiplePermissions()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return true
            } else {
                CommonMethods(this).showPermissionDialog("All_Files_Access", this, false)
                return false
            }
        } else {
            return true
        }
    }

    override fun onPermissionClickOkay(isAllFilesAccess: Boolean, context: Context) {
        CommonMethods(this).processPermission(isAllFilesAccess, context)
    }

    override fun onPermissionClickNotNow(context: Context) {

    }

    private fun fetchOcrFiles() {
        val defaultPathForOcr = FlashScanUtil.getOcrProcessingPath(this).absolutePath
        if (!TextUtils.isEmpty(defaultPathForOcr)) {
            ocrDirectoryPath = FlashScanUtil.getOcrProcessingPath(this).absolutePath
            if (!TextUtils.isEmpty(ocrDirectoryPath)) {
                fetchOcrDocuments(ocrDirectoryPath)
            } else {
                /*Toast.makeText(
                    this,
                    "" + getString(R.string.unable_to_fetch_documents),
                    Toast.LENGTH_SHORT
                ).show()*/
            }
        } else {
            /*Toast.makeText(
                this,
                "" + getString(R.string.unable_to_fetch_documents),
                Toast.LENGTH_SHORT
            ).show()*/
        }
    }

    private fun fetchOcrDocuments(directoryPath: String) {
        Log.d(TAG, "fetchOcrDocuments: $directoryPath")
        GetOcrDocuments(this, directoryPath, this, prefManager!!.appSortingOrder).execute()
    }

}
