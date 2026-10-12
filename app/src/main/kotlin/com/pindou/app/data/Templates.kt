package com.pindou.app.data

import com.pindou.core.model.Rgb

/**
 * A built-in starter pattern. [sprite] is a pixel grid (one string per row); each
 * character maps to a color via [colors] and '.' is transparent. Tapping one
 * creates a blank project whose grid is this sprite, with every pixel mapped to
 * the nearest color of the chosen bead palette.
 */
data class PindouTemplate(
    val id: String,
    val name: String,
    val sprite: List<String>,
    val colors: Map<Char, Rgb>,
) {
    val width: Int get() = sprite.maxOf { it.length }
    val height: Int get() = sprite.size
}

private val RED = Rgb(229, 57, 53)
private val AMBER = Rgb(255, 193, 7)
private val SUN = Rgb(253, 216, 53)
private val DARK = Rgb(38, 38, 38)
private val BLUE = Rgb(30, 136, 229)
private val ORANGE = Rgb(251, 140, 0)
private val GREEN = Rgb(67, 160, 71)
private val WHITE = Rgb(255, 255, 255)
private val SKIN = Rgb(255, 205, 150)
private val TAN = Rgb(214, 177, 120)

private val HEART = PindouTemplate(
    id = "heart",
    name = "爱心",
    colors = mapOf('R' to RED),
    sprite = listOf(
        "...............",
        "...RRR...RRR...",
        "..RRRRR.RRRRR..",
        ".RRRRRRRRRRRRR.",
        ".RRRRRRRRRRRRR.",
        "RRRRRRRRRRRRRRR",
        "RRRRRRRRRRRRRRR",
        "RRRRRRRRRRRRRRR",
        ".RRRRRRRRRRRRR.",
        ".RRRRRRRRRRRRR.",
        "..RRRRRRRRRRR..",
        "...RRRRRRRRR...",
        "....RRRRRRR....",
        ".....RRRRR.....",
        "......RRR......",
    ),
)

private val STAR = PindouTemplate(
    id = "star",
    name = "星星",
    colors = mapOf('Y' to AMBER),
    sprite = listOf(
        ".......Y.......",
        ".......Y.......",
        "......YYY......",
        "......YYY......",
        ".....YYYYY.....",
        "YYYYYYYYYYYYYYY",
        ".YYYYYYYYYYYYY.",
        "..YYYYYYYYYYY..",
        "...YYYYYYYYY...",
        "....YYYYYYY....",
        "...YYYYYYYYY...",
        "...YYYYYYYYY...",
        "..YYYYY.YYYYY..",
        "..YYY.....YYY..",
        "...............",
    ),
)

private val SMILEY = PindouTemplate(
    id = "smiley",
    name = "笑脸",
    colors = mapOf('Y' to SUN, 'K' to DARK),
    sprite = listOf(
        ".....YYYYY.....",
        "...YYYYYYYYY...",
        "..YYYYYYYYYYY..",
        ".YYYYYYYYYYYYY.",
        ".YYYYYYYYYYYYY.",
        "YYYKKYYYYYKKYYY",
        "YYYKKYYYYYKKYYY",
        "YYYYYYYYYYYYYYY",
        "YYYYYYYYYYYYYYY",
        "YYYKYYYYYYYKYYY",
        "YYYKKKKKKKKKYYY",
        ".YYYYYYYYYYYYY.",
        "..YYYYYYYYYYY..",
        "...YYYYYYYYY...",
        ".....YYYYY.....",
    ),
)

private val LETTER_A = PindouTemplate(
    id = "letter_a",
    name = "字母A",
    colors = mapOf('B' to BLUE),
    sprite = listOf(
        ".......B.......",
        "......BBB......",
        "......BBB......",
        ".....B...B.....",
        ".....B...B.....",
        "....B.....B....",
        "....BBBBBBB....",
        "....B.....B....",
        "...B.......B...",
        "...B.......B...",
        "..B.........B..",
        "..B.........B..",
        ".B...........B.",
        ".B...........B.",
        "B.............B",
    ),
)

private val SUN_TEMPLATE = PindouTemplate(
    id = "sun",
    name = "太阳",
    colors = mapOf('Y' to SUN),
    sprite = listOf(
        ".......Y.......",
        ".......Y.......",
        "...Y...Y...Y...",
        "....Y..Y..Y....",
        ".....Y.Y.Y.....",
        "......YYY......",
        "..Y..YYYYY..Y..",
        ".YY.YYYYYYY.YY.",
        "..Y..YYYYY..Y..",
        "......YYY......",
        ".....Y.Y.Y.....",
        "....Y..Y..Y....",
        "...Y...Y...Y...",
        ".......Y.......",
        ".......Y.......",
    ),
)

private val RAINBOW = PindouTemplate(
    id = "rainbow",
    name = "彩虹",
    colors = mapOf('R' to RED, 'O' to ORANGE, 'Y' to SUN, 'G' to GREEN, 'B' to BLUE),
    sprite = listOf(
        "RRRRRRRRRRRRRRR",
        "RRRRRRRRRRRRRRR",
        "RRRRRRRRRRRRRRR",
        "OOOOOOOOOOOOOOO",
        "OOOOOOOOOOOOOOO",
        "OOOOOOOOOOOOOOO",
        "YYYYYYYYYYYYYYY",
        "YYYYYYYYYYYYYYY",
        "YYYYYYYYYYYYYYY",
        "GGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGG",
        "BBBBBBBBBBBBBBB",
        "BBBBBBBBBBBBBBB",
        "BBBBBBBBBBBBBBB",
    ),
)

private val LAUGH_CRY = PindouTemplate(
    id = "laugh_cry",
    name = "笑哭",
    colors = mapOf('Y' to SUN, 'K' to DARK, 'W' to WHITE, 'R' to RED, 'B' to BLUE),
    sprite = listOf(
        ".....YYYYY.....",
        "...YYYYYYYYY...",
        "..YYYYYYYYYYY..",
        ".YYYYYYYYYYYYY.",
        ".YYKKYYYYYKKYY.",
        "BYYYYYYYYYYYYYB",
        "BYYYYYYYYYYYYYB",
        ".YYYYKKKKKYYYY.",
        ".YYYKWWWWWKYYY.",
        ".YYYKRRRRRKYYY.",
        ".YYYYKKKKKYYYY.",
        "..YYYYYYYYYYY..",
        "...YYYYYYYYY...",
        "....YYYYYYY....",
        ".....YYYYY.....",
    ),
)

private val BIG_LAUGH = PindouTemplate(
    id = "big_laugh",
    name = "大笑",
    colors = mapOf('Y' to SUN, 'K' to DARK, 'W' to WHITE, 'R' to RED),
    sprite = listOf(
        ".....YYYYY.....",
        "...YYYYYYYYY...",
        "..YYYYYYYYYYY..",
        ".YYYYYYYYYYYYY.",
        ".YYKKYYYYYKKYY.",
        "YYYYYYYYYYYYYYY",
        "YYYYYYYYYYYYYYY",
        "YKKKKKKKKKKKKKY",
        "YKWWWWWWWWWWWKY",
        ".YKWWWWWWWWWKY.",
        ".YKRRRRRRRRRKY.",
        "..YKKKKKKKKKY..",
        "...YYYYYYYYY...",
        "....YYYYYYY....",
        ".....YYYYY.....",
    ),
)

private val FACEPALM = PindouTemplate(
    id = "facepalm",
    name = "捂脸",
    colors = mapOf('Y' to SUN, 'S' to SKIN, 'D' to DARK),
    sprite = listOf(
        "..SS.SS.SS.SS..",
        "..SSYSSYSSYSS..",
        "..SSYSSYSSYSS..",
        ".YSSYSSYSSYSSY.",
        ".YSSSSSSSSSSSY.",
        "YYSSSSSSSSSSSYY",
        "YYYYYYYYYYYYYYY",
        "YYYYYYYYYYYYYYY",
        "YYYYYYYYYYYYYYY",
        ".YYYYDDDDDYYYY.",
        ".YYYDDDDDDDYYY.",
        "..YYYYYYYYYYY..",
        "...YYYYYYYYY...",
        "....YYYYYYY....",
        ".....YYYYY.....",
    ),
)

private val DOGE = PindouTemplate(
    id = "doge",
    name = "狗头",
    colors = mapOf('T' to TAN, 'K' to DARK),
    sprite = listOf(
        "..TT.......TT..",
        ".TTTT.....TTTT.",
        ".TTTTTTTTTTTTT.",
        "TTTTTTTTTTTTTTT",
        "TTTTKTTTTTKTTTT",
        "TTTTTTTTTTTTTTT",
        "TTTTTTTTTTTTTTT",
        "TTTTTTTTTTTTTTT",
        "TTTTTTDDDTTTTTT",
        "TTTTTTDDDTTTTTT",
        "TTTTTTTTTTTTTTT",
        "TTTTTDDDDDTTTTT",
        ".TTTTTTTTTTTTT.",
        "..TTTTTTTTTTT..",
        "...TTTTTTTTT...",
    ),
)

private val THUMBS_UP = PindouTemplate(
    id = "thumbs_up",
    name = "点赞",
    colors = mapOf('S' to SKIN, 'B' to BLUE),
    sprite = listOf(
        "....SSS........",
        "...SSSSS.......",
        "...SSSSS.......",
        "...SSSSS.......",
        "...SSSSSSSSS...",
        "..SSSSSSSSSSS..",
        "..SSSSSSSSSSS..",
        "..SSSSSSSSSSS..",
        "..SSSSSSSSSSS..",
        "..SSSSSSSSSSS..",
        "..SSSSSSSSSSS..",
        "...SSSSSSSSS...",
        "..BBBBBBBBBBB..",
        "...............",
        "...............",
    ),
)

/** Built-in quick-start templates shown on the home screen. */
val TEMPLATES: List<PindouTemplate> = listOf(
    HEART, STAR, SMILEY, LETTER_A, SUN_TEMPLATE, RAINBOW,
    LAUGH_CRY, BIG_LAUGH, FACEPALM, DOGE, THUMBS_UP,
)
