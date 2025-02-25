package com.papp.sample

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.papp.imagepicker.features.ImagePickerConfig
import com.papp.imagepicker.features.ImagePickerFragment
import com.papp.imagepicker.features.ImagePickerInteractionListener
import com.papp.imagepicker.features.cameraonly.CameraOnlyConfig
import com.papp.imagepicker.helper.ConfigUtils
import com.papp.imagepicker.helper.IpLogger
import com.papp.imagepicker.helper.LocaleManager
import com.papp.imagepicker.helper.ViewUtils
import com.papp.imagepicker.model.Image
import kotlinx.android.synthetic.main.activity_custom_ui.*

/**
 * This custom UI for ImagePicker puts the picker in the bottom half of the screen, and a preview of
 * the last selected image in the top half.
 */
class CustomUIActivity : AppCompatActivity() {

    private lateinit var actionBar: ActionBar
    private lateinit var imagePickerFragment: ImagePickerFragment

    private var cameraOnlyConfig: CameraOnlyConfig? = null
    private var config: ImagePickerConfig? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.updateResources(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        config = intent.extras?.getParcelable(ImagePickerConfig::class.java.simpleName)
        cameraOnlyConfig = intent.extras?.getParcelable(CameraOnlyConfig::class.java.simpleName)

        val theme = config?.theme
        if (theme != null) {
            setTheme(theme)
        }

        setContentView(R.layout.activity_custom_ui)
        setupView()

        if (savedInstanceState != null) {
            // The fragment has been restored.
            IpLogger.getInstance().e("Fragment has been restored")
            imagePickerFragment = supportFragmentManager
                .findFragmentById(R.id.ef_imagepicker_fragment_placeholder) as ImagePickerFragment
        } else {
            IpLogger.getInstance().e("Making fragment")
            imagePickerFragment = ImagePickerFragment.newInstance(config, cameraOnlyConfig)
            supportFragmentManager.beginTransaction()
                .replace(R.id.ef_imagepicker_fragment_placeholder, imagePickerFragment)
                .commit()
        }

        // For demonstration purposes, we're using a custom ImagePickerInteractionListener. Instead
        // of calling setInteractionListener, though, we could simply implement
        // ImagePickerInteractionListener in this class.
        imagePickerFragment.setInteractionListener(CustomInteractionListener())
    }

    /**
     * Create options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.papp.imagepicker.R.menu.ef_image_picker_menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuCamera = menu.findItem(com.papp.imagepicker.R.id.menu_camera)
        if (menuCamera != null) {
            if (config != null) {
                menuCamera.isVisible = config!!.isShowCamera
            }
        }
        val menuDone = menu.findItem(com.papp.imagepicker.R.id.menu_done)
        if (menuDone != null) {
            menuDone.title = ConfigUtils.getDoneButtonText(this, config)
            menuDone.isVisible = imagePickerFragment.isShowDoneButton
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Handle options menu's click event.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        if (id == com.papp.imagepicker.R.id.menu_done) {
            imagePickerFragment.onDone()
            return true
        }
        if (id == com.papp.imagepicker.R.id.menu_camera) {
            imagePickerFragment.captureImageWithPermission()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * If you're content for the back button to take you back, without consulting the state of the
     * fragment, you don't need to override this. On the other hand, the ImagePickerFragment might
     * want to handle the back button. For example, if it's in folder mode and a folder has been
     * selected, the fragment will go back to the folder list if you call its handleBack().
     */
    override fun onBackPressed() {
        if (!imagePickerFragment.handleBack()) {
            super.onBackPressed()
        }
    }

    private fun setupView() {
        setSupportActionBar(toolbar as Toolbar)
        checkNotNull(supportActionBar)

        actionBar = supportActionBar!!
        val arrowDrawable = ViewUtils.getArrowIcon(this)
        val arrowColor = config!!.arrowColor
        if (arrowColor != ImagePickerConfig.NO_COLOR && arrowDrawable != null) {
            arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_ATOP)
        }

        actionBar.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(arrowDrawable)
            setDisplayShowTitleEnabled(true)
        }
    }

    internal inner class CustomInteractionListener : ImagePickerInteractionListener {
        override fun setTitle(title: String) {
            actionBar.title = title
            invalidateOptionsMenu()
        }

        override fun cancel() {
            finish()
        }

        override fun selectionChanged(imageList: List<Image>) {
            if (imageList.isEmpty()) {
                photo_preview.setImageDrawable(null)
            } else {
                val imageUri = imageList[imageList.size - 1].uri
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                }
                photo_preview.setImageBitmap(bitmap)
            }
        }

        override fun finishPickImages(result: Intent) {
            setResult(RESULT_OK, result)
            finish()
        }
    }
}