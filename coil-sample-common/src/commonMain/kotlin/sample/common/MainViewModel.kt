package sample.common

import coil.Extras
import coil.PlatformContext
import kotlin.js.JsName
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okio.buffer
import okio.use

@JsName("newMainViewModel")
fun MainViewModel(context: PlatformContext): MainViewModel {
    return RealMainViewModel(context)
}

interface MainViewModel {
    val images: StateFlow<List<Image>>
    val assetType: MutableStateFlow<AssetType>
    val screen: MutableStateFlow<Screen>

    suspend fun start()
    fun onBackPressed()
}

private class RealMainViewModel(
    private val context: PlatformContext,
) : MainViewModel {

    private val _images: MutableStateFlow<List<Image>> = MutableStateFlow(emptyList())
    override val images: StateFlow<List<Image>> get() = _images
    override val assetType: MutableStateFlow<AssetType> = MutableStateFlow(AssetType.JPG)
    override val screen: MutableStateFlow<Screen> = MutableStateFlow(Screen.List)

    override suspend fun start() {
        assetType.collect { _images.value = loadImagesAsync(it) }
    }

    override fun onBackPressed() {
        // Always navigate to the top-level list if this method is called.
        screen.value = Screen.List
    }

    private suspend fun loadImagesAsync(assetType: AssetType) = withContext(Dispatchers.Default) {
        if (assetType == AssetType.MP4) {
            loadVideoFrames()
        } else {
            loadImages(assetType)
        }
    }

    private fun loadVideoFrames(): List<Image> {
        return List(200) {
            val videoFrameMicros = Random.nextLong(62_000_000L)
            val extras = Extras.Builder()
                .set(Extras.Key.videoFrameMicros, videoFrameMicros)
                .build()

            Image(
                uri = "file:///android_asset/${AssetType.MP4.fileName}",
                color = randomColor(),
                width = 1280,
                height = 720,
                extras = extras,
            )
        }
    }

    private fun loadImages(assetType: AssetType): List<Image> {
        val json = context.openResource(assetType.fileName).buffer().use {
            Json.parseToJsonElement(it.readUtf8()).jsonArray
        }
        return List(json.size) { index ->
            val image = json[index].jsonObject

            val url: String
            val color: Int
            if (assetType == AssetType.JPG) {
                url = image.getValue("urls").jsonObject.getValue("regular").toString()
                color = image.getValue("color").toString().toColorInt()
            } else {
                url = image.getValue("url").toString()
                color = randomColor()
            }

            Image(
                uri = url,
                color = color,
                width = image.getValue("width").toString().toInt(),
                height = image.getValue("height").toString().toInt()
            )
        }
    }
}
