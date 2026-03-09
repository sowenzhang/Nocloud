package com.nocloudchat.tools

import java.awt.*
import java.awt.geom.*
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO

fun main() {
    val outDir = File("icons").also { it.mkdirs() }
    val resDir = File("src/main/resources").also { it.mkdirs() }

    val sizes  = intArrayOf(16, 32, 48, 256)
    val images = sizes.associateWith { renderIcon(it) }

    // Debug PNGs
    for ((sz, img) in images) ImageIO.write(img, "PNG", File(outDir, "icon_${sz}.png"))

    // Main PNG (256×256) → window decoration + Linux installer
    val main256 = images.getValue(256)
    ImageIO.write(main256, "PNG", File(outDir,  "icon.png"))
    ImageIO.write(main256, "PNG", File(resDir,  "icon.png"))

    // Multi-resolution ICO → Windows installer
    writeIco(File(outDir, "icon.ico"), sizes.map { images.getValue(it) })

    println("✓ Icons written to   ${outDir.absolutePath}")
    println("✓ icon.png copied to ${resDir.absolutePath}")
}

// ─── Icon rendering ───────────────────────────────────────────────────────────

private fun renderIcon(size: Int): BufferedImage {
    val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g   = img.g2d()
    val s   = size.toFloat()

    // Rounded-square background: brand gradient red
    g.paint = GradientPaint(s * 0.5f, 0f, Color(0xE9, 0x45, 0x60),
                            s * 0.5f, s,  Color(0x9E, 0x18, 0x34))
    g.fill(RoundRectangle2D.Float(0f, 0f, s, s, s * 0.44f, s * 0.44f))

    // Draw house on a separate transparent layer so the DST_OUT door
    // punch-through reveals the red background (not just transparency).
    val layer = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val gl    = layer.g2d()
    drawHouse(gl, s)
    gl.dispose()

    g.drawImage(layer, 0, 0, null)
    g.dispose()
    return img
}

private fun drawHouse(g: Graphics2D, s: Float) {
    val cx      = s * 0.50f
    val apexY   = s * 0.27f   // tip of roof
    val eaveY   = s * 0.51f   // roof-to-wall junction
    val bottomY = s * 0.82f   // floor
    val roofHW  = s * 0.33f   // half-width at eave (wider = shallower pitch)
    val wallHW  = s * 0.26f   // half-width of rectangular body

    g.color = Color.WHITE

    // 1. Chimney — drawn first so the roof triangle covers its lower half,
    //    leaving only the bit that pokes above the roof line visible.
    if (s >= 48f) {
        val chW   = s * 0.062f
        val chX   = cx + roofHW * 0.42f
        val chTop = apexY - s * 0.09f
        g.fill(Rectangle2D.Float(
            chX - chW / 2f, chTop, chW,
            eaveY - chTop + s * 0.04f   // extends past eave; covered by roof + wall
        ))
    }

    // 2. Roof triangle
    val roof = Path2D.Float()
    roof.moveTo(cx, apexY)
    roof.lineTo(cx + roofHW, eaveY)
    roof.lineTo(cx - roofHW, eaveY)
    roof.closePath()
    g.fill(roof)

    // 3. Wall body
    val wallH = bottomY - eaveY
    g.fill(RoundRectangle2D.Float(
        cx - wallHW, eaveY, wallHW * 2f, wallH, s * 0.03f, s * 0.03f
    ))

    // 4. Arched door — DST_OUT erases house pixels, revealing the red background
    if (s >= 32f) {
        val dw    = wallHW * 0.56f
        val dh    = wallH  * 0.50f
        val dx    = cx - dw / 2f
        val dy    = bottomY - dh
        val dr    = dw * 0.40f          // radius of the door arch
        val saved = g.composite
        g.composite = AlphaComposite.DstOut
        val door = Path2D.Float()
        door.moveTo(dx, bottomY + 1f)
        door.lineTo(dx, dy + dr)
        // Arc: start=180° (left), extent=-180° → CW on screen → left→top→right (upward arch)
        door.append(Arc2D.Float(dx, dy, dw, dr * 2f, 180f, -180f, Arc2D.OPEN), true)
        door.lineTo(dx + dw, bottomY + 1f)
        door.closePath()
        g.fill(door)
        g.composite = saved
    }

    // 5. WiFi arcs above the roof apex (only at ≥ 96 px for visual clarity)
    if (s >= 96f) {
        val acx         = cx
        val acy         = apexY
        val savedColor  = g.color
        val savedStroke = g.stroke
        g.color = Color(255, 255, 255, 210)
        for (i in 1..2) {
            val rad = s * (0.060f + i * 0.052f)
            g.stroke = BasicStroke(s * 0.030f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            // Arc centred at apex, opening upward (30°–150° = upper 120° of circle)
            g.draw(Arc2D.Float(acx - rad, acy - rad, rad * 2f, rad * 2f, 30f, 120f, Arc2D.OPEN))
        }
        // Centre dot
        g.stroke = savedStroke
        g.color  = Color.WHITE
        val dr = s * 0.026f
        g.fill(Ellipse2D.Float(acx - dr, acy - dr, dr * 2f, dr * 2f))
        g.color = savedColor
    }
}

private fun BufferedImage.g2d(): Graphics2D = createGraphics().apply {
    setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON)
    setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY)
    setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
}

// ─── ICO writer (PNG-in-ICO, Windows Vista+ format) ───────────────────────────

private fun writeIco(file: File, images: List<BufferedImage>) {
    val blobs = images.map { img ->
        ByteArrayOutputStream().also { ImageIO.write(img, "PNG", it) }.toByteArray()
    }

    val buf = ByteArrayOutputStream()
    fun sh(v: Int) { buf.write(v and 0xFF); buf.write((v shr 8)  and 0xFF) }
    fun i4(v: Int) {
        buf.write(v and 0xFF);         buf.write((v shr 8)  and 0xFF)
        buf.write((v shr 16) and 0xFF); buf.write((v shr 24) and 0xFF)
    }

    // ICO header (6 bytes, little-endian)
    sh(0); sh(1); sh(images.size)

    // Directory entries (16 bytes each)
    var offset = 6 + 16 * images.size
    for (k in images.indices) {
        val dim = images[k].width
        buf.write(if (dim == 256) 0 else dim)   // width  (0 = 256)
        buf.write(if (dim == 256) 0 else dim)   // height (0 = 256)
        buf.write(0); buf.write(0)              // color count, reserved
        sh(1); sh(32)                           // planes, bit depth
        i4(blobs[k].size)                       // image data size
        i4(offset)                              // offset from start of file
        offset += blobs[k].size
    }

    // Image data (PNG blobs)
    for (blob in blobs) buf.write(blob)

    file.writeBytes(buf.toByteArray())
}
