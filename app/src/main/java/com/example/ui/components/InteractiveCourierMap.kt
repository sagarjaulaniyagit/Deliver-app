package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeliveryOrder
import com.example.model.DeliveryStatus
import com.example.model.VehicleType
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun InteractiveCourierMap(
    order: DeliveryOrder?,
    modifier: Modifier = Modifier,
    isDriverView: Boolean = false,
    onMapClicked: (() -> Unit)? = null
) {
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    val textMeasurer = rememberTextMeasurer()

    // Pulse animation for markers
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Dash offset animation for moving route
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashPhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MapAsphalt)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    panOffset += pan
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 2.5f)
                }
            }
            .testTag("interactive_map_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f) + panOffset

            // Draw stylized dark city map base
            drawCityMapGrid(center, zoomScale, textMeasurer)

            // If active order exists, draw route, pickup, dropoff, and courier
            if (order != null) {
                val pX = center.x + (order.pickupCoords.x - 300f) * zoomScale
                val pY = center.y + (order.pickupCoords.y - 400f) * zoomScale
                val pickupTransformed = Offset(pX, pY)

                val dX = center.x + (order.dropoffCoords.x - 300f) * zoomScale
                val dY = center.y + (order.dropoffCoords.y - 400f) * zoomScale
                val dropoffTransformed = Offset(dX, dY)

                val cX = center.x + (order.courierCoords.x - 300f) * zoomScale
                val cY = center.y + (order.courierCoords.y - 400f) * zoomScale
                val courierTransformed = Offset(cX, cY)

                // Road-aligned polyline waypoints
                val midWay1 = Offset(pickupTransformed.x, (pickupTransformed.y + dropoffTransformed.y) / 2f)
                val midWay2 = Offset(dropoffTransformed.x, (pickupTransformed.y + dropoffTransformed.y) / 2f)

                val fullRoutePath = Path().apply {
                    moveTo(pickupTransformed.x, pickupTransformed.y)
                    lineTo(midWay1.x, midWay1.y)
                    lineTo(midWay2.x, midWay2.y)
                    lineTo(dropoffTransformed.x, dropoffTransformed.y)
                }

                // Draw route background glow
                drawPath(
                    path = fullRoutePath,
                    color = ElectricBlue.copy(alpha = 0.25f),
                    style = Stroke(width = 18f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw route dashed line with animation
                drawPath(
                    path = fullRoutePath,
                    brush = Brush.linearGradient(
                        colors = listOf(EmeraldPrimary, ElectricBlue)
                    ),
                    style = Stroke(
                        width = 8f * zoomScale,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(24f * zoomScale, 16f * zoomScale),
                            dashPhase
                        )
                    )
                )

                // Draw Pickup Pin (Green)
                drawCircle(
                    color = EmeraldPrimary.copy(alpha = pulseAlpha),
                    radius = 28f * zoomScale * pulseScale,
                    center = pickupTransformed
                )
                drawCircle(
                    color = Color.White,
                    radius = 14f * zoomScale,
                    center = pickupTransformed
                )
                drawCircle(
                    color = EmeraldDark,
                    radius = 11f * zoomScale,
                    center = pickupTransformed
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "A",
                    topLeft = pickupTransformed - Offset(6f * zoomScale, 10f * zoomScale),
                    style = TextStyle(color = Color.White, fontSize = (12 * zoomScale).sp, fontWeight = FontWeight.Bold)
                )

                // Draw Dropoff Pin (Red)
                drawCircle(
                    color = RoseError.copy(alpha = pulseAlpha),
                    radius = 28f * zoomScale * pulseScale,
                    center = dropoffTransformed
                )
                drawCircle(
                    color = Color.White,
                    radius = 14f * zoomScale,
                    center = dropoffTransformed
                )
                drawCircle(
                    color = RoseError,
                    radius = 11f * zoomScale,
                    center = dropoffTransformed
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "B",
                    topLeft = dropoffTransformed - Offset(6f * zoomScale, 10f * zoomScale),
                    style = TextStyle(color = Color.White, fontSize = (12 * zoomScale).sp, fontWeight = FontWeight.Bold)
                )

                // Draw Animated Courier Vehicle Marker
                if (order.status != DeliveryStatus.DELIVERED && order.status != DeliveryStatus.CANCELLED) {
                    drawCourierMarker(
                        coords = courierTransformed,
                        bearing = order.courierBearingDegrees,
                        vehicleType = order.vehicleType,
                        zoomScale = zoomScale,
                        pulseScale = pulseScale
                    )
                }
            } else {
                // Default center marker when no active order
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.2f),
                    radius = 40f * zoomScale,
                    center = center
                )
                drawCircle(
                    color = ElectricBlue,
                    radius = 12f * zoomScale,
                    center = center
                )
            }
        }

        // Floating controls (Zoom +, Zoom -, Center)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 72.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f) },
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .testTag("map_zoom_in_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Slate900.copy(alpha = 0.9f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }

            FilledTonalIconButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .testTag("map_zoom_out_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Slate900.copy(alpha = 0.9f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FilledTonalIconButton(
                onClick = {
                    panOffset = Offset.Zero
                    zoomScale = 1.0f
                },
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .testTag("map_recenter_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Slate900.copy(alpha = 0.9f),
                    contentColor = EmeraldPrimary
                )
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map")
            }
        }

        // Floating Live Telemetry Badge (Speed & ETA)
        if (order != null && order.status != DeliveryStatus.DELIVERED && order.status != DeliveryStatus.CANCELLED) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 72.dp, start = 16.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Slate900.copy(alpha = 0.92f),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Column {
                        Text(
                            text = if (order.status == DeliveryStatus.HEADING_TO_PICKUP) "Heading to Pickup" else "In Transit to Destination",
                            color = Slate200,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${order.estimatedMinutes} min ETA",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                            Text(
                                text = String.format("%.1f km", order.distanceKm),
                                color = EmeraldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCityMapGrid(center: Offset, zoom: Float, textMeasurer: TextMeasurer) {
    val step = 140f * zoom
    val width = size.width
    val height = size.height

    // Draw City Green Parks
    val parkRect1 = androidx.compose.ui.geometry.Rect(
        center.x - 350f * zoom,
        center.y - 280f * zoom,
        center.x - 180f * zoom,
        center.y - 80f * zoom
    )
    drawRoundRect(
        color = MapGreenery,
        topLeft = parkRect1.topLeft,
        size = parkRect1.size,
        cornerRadius = CornerRadius(16f * zoom, 16f * zoom)
    )

    val parkRect2 = androidx.compose.ui.geometry.Rect(
        center.x + 120f * zoom,
        center.y + 150f * zoom,
        center.x + 320f * zoom,
        center.y + 360f * zoom
    )
    drawRoundRect(
        color = MapGreenery,
        topLeft = parkRect2.topLeft,
        size = parkRect2.size,
        cornerRadius = CornerRadius(16f * zoom, 16f * zoom)
    )

    // River on right edge
    val riverPath = Path().apply {
        moveTo(center.x + 380f * zoom, 0f)
        cubicTo(
            center.x + 340f * zoom, height * 0.35f,
            center.x + 420f * zoom, height * 0.7f,
            center.x + 360f * zoom, height
        )
        lineTo(width, height)
        lineTo(width, 0f)
        close()
    }
    drawPath(path = riverPath, color = MapWater)

    // Street Blocks (Buildings)
    val startX = (center.x % step) - step
    val startY = (center.y % step) - step

    var y = startY
    var row = 0
    while (y < height + step) {
        var x = startX
        var col = 0
        while (x < width + step) {
            val blockSize = step - 24f * zoom
            if (blockSize > 10f) {
                drawRoundRect(
                    color = MapBuilding,
                    topLeft = Offset(x + 12f * zoom, y + 12f * zoom),
                    size = Size(blockSize, blockSize),
                    cornerRadius = CornerRadius(8f * zoom, 8f * zoom)
                )
            }
            x += step
            col++
        }
        y += step
        row++
    }

    // Road Grid lines
    y = startY
    while (y < height + step) {
        drawLine(
            color = MapRoadPrimary,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 14f * zoom
        )
        y += step
    }

    var x = startX
    while (x < width + step) {
        drawLine(
            color = MapRoadPrimary,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 14f * zoom
        )
        x += step
    }

    // Street Labels
    val streetNames = listOf("Broadway Ave", "5th Avenue", "Grand Street", "Canal St", "Lexington", "Houston St")
    var nameIdx = 0
    y = startY + 20f
    while (y < height && nameIdx < streetNames.size) {
        drawText(
            textMeasurer = textMeasurer,
            text = streetNames[nameIdx % streetNames.size],
            topLeft = Offset(24f, y + 2f),
            style = TextStyle(
                color = Slate400.copy(alpha = 0.45f),
                fontSize = (10 * zoom.coerceIn(0.8f, 1.3f)).sp,
                fontWeight = FontWeight.Medium
            )
        )
        y += step * 1.5f
        nameIdx++
    }
}

private fun DrawScope.drawCourierMarker(
    coords: Offset,
    bearing: Float,
    vehicleType: VehicleType,
    zoomScale: Float,
    pulseScale: Float
) {
    // Pulse ring
    drawCircle(
        color = EmeraldPrimary.copy(alpha = 0.25f),
        radius = 36f * zoomScale * pulseScale,
        center = coords
    )

    // Outer circle
    drawCircle(
        color = Slate950,
        radius = 22f * zoomScale,
        center = coords
    )
    drawCircle(
        color = EmeraldPrimary,
        radius = 18f * zoomScale,
        center = coords
    )

    // Directional chevron indicator
    rotate(degrees = bearing, pivot = coords) {
        val arrowPath = Path().apply {
            moveTo(coords.x, coords.y - 28f * zoomScale)
            lineTo(coords.x - 8f * zoomScale, coords.y - 18f * zoomScale)
            lineTo(coords.x + 8f * zoomScale, coords.y - 18f * zoomScale)
            close()
        }
        drawPath(path = arrowPath, color = EmeraldPrimary)

        // Vehicle badge icon inner
        drawCircle(
            color = Color.White,
            radius = 10f * zoomScale,
            center = coords
        )
        drawCircle(
            color = Slate950,
            radius = 6f * zoomScale,
            center = coords
        )
    }
}
