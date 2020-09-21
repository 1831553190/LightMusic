package com.mymusic.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.ColorUtils.calculateLuminance
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import kotlin.math.abs


class AlbumUtils {
    /**
     * The fraction below which we select the vibrant instead of the light/dark vibrant color
     */
    private val POPULATION_FRACTION_FOR_MORE_VIBRANT = 1.0f

    /**
     * Minimum saturation that a muted color must have if there exists if deciding between two
     * colors
     */
    private val MIN_SATURATION_WHEN_DECIDING = 0.19f

    /**
     * Minimum fraction that any color must have to be picked up as a text color
     */
    private val MINIMUM_IMAGE_FRACTION = 0.002

    /**
     * The population fraction to select the dominant color as the text color over a the colored
     * ones.
     */
    private val POPULATION_FRACTION_FOR_DOMINANT = 0.01f

    /**
     * The population fraction to select a white or black color as the background over a color.
     */
    private val POPULATION_FRACTION_FOR_WHITE_OR_BLACK = 2.5f
    private val BLACK_MAX_LIGHTNESS = 0.08f
    private val WHITE_MIN_LIGHTNESS = 0.90f
    private val RESIZE_BITMAP_AREA = 150 * 150
    private var mFilteredBackgroundHsl: FloatArray? = null
    private val mBlackWhiteFilter =
            Palette.Filter { rgb: Int, hsl: FloatArray -> !isWhiteOrBlack(hsl) }


    /**
     * Processes a builder of a media notification and calculates the appropriate colors that should
     * be used.
     *
     * @param notification the notification that is being processed
     * @param builder the recovered builder for the notification. this will be modified
     */
    fun processNotification(bitmap: Bitmap):Pair<Int,Int> {
// rebuilt!
        var backgroundColor = 0
        // for the background we only take the left side of the image to ensure
// a smooth transition
        val paletteBuilder = Palette.from(bitmap)
                .setRegion(0, 0, bitmap.width / 2, bitmap.height)
                .clearFilters() // we want all colors, red / white / black ones too!
                .resizeBitmapArea(RESIZE_BITMAP_AREA)
        var palette = paletteBuilder.generate()
        backgroundColor = findBackgroundColorAndFilter(palette)
        // we want most of the full region again, slightly shifted to the right
        val textColorStartWidthFraction = 0.4f
        paletteBuilder.setRegion(
                (bitmap.width * textColorStartWidthFraction).toInt(), 0,
                bitmap.width,
                bitmap.height
        )
        if (mFilteredBackgroundHsl != null) {
            paletteBuilder.addFilter { rgb: Int, hsl: FloatArray ->
                // at least 10 degrees hue difference
                val diff =
                        abs(hsl[0] - mFilteredBackgroundHsl!![0])
                diff > 10 && diff < 350
            }
        }
        paletteBuilder.addFilter(mBlackWhiteFilter)
        palette = paletteBuilder.generate()
        val foregroundColor = selectForegroundColor(backgroundColor, palette)
        return backgroundColor to foregroundColor
    }

    private fun selectForegroundColor(backgroundColor: Int, palette: Palette): Int {
        return if (isColorLight(backgroundColor)) {
            selectForegroundColorForSwatches(
                    palette.darkMutedSwatch,
                    palette.darkVibrantSwatch,
                    palette.vibrantSwatch,
                    palette.mutedSwatch,
                    palette.dominantSwatch,
                    Color.BLACK
            )
        } else {
            selectForegroundColorForSwatches(
                    palette.lightMutedSwatch,
                    palette.lightVibrantSwatch,
                    palette.vibrantSwatch,
                    palette.mutedSwatch,
                    palette.dominantSwatch,
                    Color.WHITE
            )
        }
    }

    private fun selectForegroundColorForSwatches(
            moreVibrant: Swatch?,
            vibrant: Swatch?, moreMutedSwatch: Swatch?, mutedSwatch: Swatch?,
            dominantSwatch: Swatch?, fallbackColor: Int
    ): Int {
        var coloredCandidate = selectVibrantCandidate(moreVibrant, vibrant)
        if (coloredCandidate == null) {
            coloredCandidate = selectMutedCandidate(mutedSwatch, moreMutedSwatch)
        }
        return if (coloredCandidate != null) {
            if (dominantSwatch === coloredCandidate) {
                coloredCandidate.rgb
            } else if (coloredCandidate.population.toFloat() / dominantSwatch!!.population
                    < POPULATION_FRACTION_FOR_DOMINANT
                    && dominantSwatch.hsl[1] > MIN_SATURATION_WHEN_DECIDING
            ) {
                dominantSwatch.rgb
            } else {
                coloredCandidate.rgb
            }
        } else if (hasEnoughPopulation(dominantSwatch)) {
            dominantSwatch!!.rgb
        } else {
            fallbackColor
        }
    }

    private fun selectMutedCandidate(
            first: Swatch?,
            second: Swatch?
    ): Swatch? {
        val firstValid = hasEnoughPopulation(first)
        val secondValid = hasEnoughPopulation(second)
        if (firstValid && secondValid) {
            val firstSaturation = first!!.hsl[1]
            val secondSaturation = second!!.hsl[1]
            val populationFraction =
                    first.population / second.population.toFloat()
            return if (firstSaturation * populationFraction > secondSaturation) {
                first
            } else {
                second
            }
        } else if (firstValid) {
            return first
        } else if (secondValid) {
            return second
        }
        return null
    }

    private fun selectVibrantCandidate(first: Swatch?, second: Swatch?): Swatch? {
        val firstValid = hasEnoughPopulation(first)
        val secondValid = hasEnoughPopulation(second)
        POPULATION_FRACTION_FOR_MORE_VIBRANT
        if (firstValid && secondValid) {
            val firstPopulation = first!!.population
            val secondPopulation = second!!.population
            return if (firstPopulation / secondPopulation.toFloat() < POPULATION_FRACTION_FOR_MORE_VIBRANT ) {
                second
            } else {
                first
            }
        } else if (firstValid) {
            return first
        } else if (secondValid) {
            return second
        }
        return null
    }

    private fun hasEnoughPopulation(swatch: Swatch?): Boolean { // We want a fraction that is at least 1% of the image
        return (swatch != null
                && swatch.population / RESIZE_BITMAP_AREA.toFloat() > MINIMUM_IMAGE_FRACTION)
    }

    private fun findBackgroundColorAndFilter(palette: Palette): Int { // by default we use the dominant palette
        val dominantSwatch = palette.dominantSwatch
        if (dominantSwatch == null) { // We're not filtering on white or black
            mFilteredBackgroundHsl = null
            return Color.WHITE
        }
        if (!isWhiteOrBlack(dominantSwatch.hsl)) {
            mFilteredBackgroundHsl = dominantSwatch.hsl
            return dominantSwatch.rgb
        }
        // Oh well, we selected black or white. Lets look at the second color!
        val swatches = palette.swatches
        var highestNonWhitePopulation = -1f
        var second: Swatch? = null
        for (swatch in swatches) {
            if (swatch !== dominantSwatch && swatch.population > highestNonWhitePopulation && !isWhiteOrBlack(
                            swatch.hsl
                    )
            ) {
                second = swatch
                highestNonWhitePopulation = swatch.population.toFloat()
            }
        }
        if (second == null) { // We're not filtering on white or black
            mFilteredBackgroundHsl = null
            return dominantSwatch.rgb
        }
        return if (dominantSwatch.population / highestNonWhitePopulation
                > POPULATION_FRACTION_FOR_WHITE_OR_BLACK
        ) { // The dominant swatch is very dominant, lets take it!
            // We're not filtering on white or black
            mFilteredBackgroundHsl = null
            dominantSwatch.rgb
        } else {
            mFilteredBackgroundHsl = second.hsl
            second.rgb
        }
    }

    private fun isWhiteOrBlack(hsl: FloatArray): Boolean {
        return isBlack(hsl) || isWhite(hsl)
    }


    /**
     * @return true if the color represents a color which is close to black.
     */
    private fun isBlack(hslColor: FloatArray): Boolean {
        return hslColor[2] <= BLACK_MAX_LIGHTNESS
    }

    /**
     * @return true if the color represents a color which is close to white.
     */
    private fun isWhite(hslColor: FloatArray): Boolean {
        return hslColor[2] >= WHITE_MIN_LIGHTNESS
    }

    fun isColorLight(backgroundColor: Int): Boolean {
        return calculateLuminance(backgroundColor) > 0.5f
    }
}