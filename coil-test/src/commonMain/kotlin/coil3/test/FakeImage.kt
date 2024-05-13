package coil3.test

import coil3.Image
import coil3.annotation.ExperimentalCoilApi

@ExperimentalCoilApi
expect class FakeImage(
    width: Int = 100,
    height: Int = 100,
    size: Int = 4 * width * height,
    shareable: Boolean = true,
    color: Int = 0x000000,
) : Image {
    override val width: Int
    override val height: Int
    override val size: Int
    override val shareable: Boolean
    val color: Int
}
