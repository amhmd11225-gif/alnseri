package com.example.ui.screens.haqeebat

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.haqeebat.PrayerTimesCalculator
import com.example.ui.components.AlAnsariGoldCard
import com.example.ui.components.AlAnsariSubTopBar
import com.example.ui.components.CrimsonPillBadge
import com.example.ui.components.GoldOutlineChip
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.ElevatedDark
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDarkBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSpecular
import com.example.ui.theme.MutedGray
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCream
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class PrayerRowItem(
    val id: String,
    val label: String,
    val minutesFromMidnight: Int,
)

@Composable
fun PrayerTimesScreen(
    selectedCityId: String,
    hijriOffsetDays: Int,
    onSelectCity: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val city = remember(selectedCityId) { PrayerTimesCalculator.cityById(selectedCityId) }
    val schedule = remember(city) { PrayerTimesCalculator.calculateForDate(city) }
    val hijriInfo = remember(hijriOffsetDays) { PrayerTimesCalculator.getCurrentHijriDate(hijriOffsetDays) }

    var nowSecondsOfDay by remember {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Baghdad"))
        mutableIntStateOf(
            cal.get(Calendar.HOUR_OF_DAY) * 3600 +
                cal.get(Calendar.MINUTE) * 60 +
                cal.get(Calendar.SECOND),
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Baghdad"))
            nowSecondsOfDay = cal.get(Calendar.HOUR_OF_DAY) * 3600 +
                cal.get(Calendar.MINUTE) * 60 +
                cal.get(Calendar.SECOND)
        }
    }

    val rows = remember(schedule) {
        listOf(
            PrayerRowItem("imsaak", "الإمساك", schedule.imsaakMinutes),
            PrayerRowItem("fajr", "صلاة الفجر", schedule.fajrMinutes),
            PrayerRowItem("sunrise", "الشروق", schedule.sunriseMinutes),
            PrayerRowItem("dhuhr", "صلاة الظهر", schedule.dhuhrMinutes),
            PrayerRowItem("asr", "صلاة العصر", schedule.asrMinutes),
            PrayerRowItem("sunset", "الغروب", schedule.sunsetMinutes),
            PrayerRowItem("maghrib", "صلاة المغرب", schedule.maghribMinutes),
            PrayerRowItem("isha", "صلاة العشاء", schedule.ishaMinutes),
            PrayerRowItem("midnight", "منتصف الليل الشرعي", schedule.midnightMinutes),
        )
    }

    val nextPrayerInfo = remember(rows, nowSecondsOfDay) {
        val obligatory = rows.filter { it.id in setOf("fajr", "dhuhr", "asr", "maghrib", "isha") }
        val found = obligatory.firstOrNull { it.minutesFromMidnight * 60 > nowSecondsOfDay }
        val target = found ?: obligatory.first()
        val diffSec = if (found != null) {
            target.minutesFromMidnight * 60 - nowSecondsOfDay
        } else {
            (target.minutesFromMidnight * 60 + 86400) - nowSecondsOfDay
        }
        val h = diffSec / 3600
        val m = (diffSec % 3600) / 60
        val s = diffSec % 60
        target to "%02d:%02d:%02d".format(h, m, s)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "أوقات الصلاة الشرعية",
            badgeText = "${city.nameAr} • توقيت الفقه الجعفري",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // City Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrayerTimesCalculator.iraqiCities.forEach { item ->
                    GoldOutlineChip(
                        text = item.nameAr,
                        selected = item.id == city.id,
                        onClick = { onSelectCity(item.id) },
                        testTag = "prayer_city_${item.id}",
                    )
                }
            }

            // Next Prayer Hero Countdown 3D Card
            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "prayer_next_countdown_card",
                contentPadding = PaddingValues(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${hijriInfo.weekdayAr} ${hijriInfo.day} ${hijriInfo.monthNameAr} ${hijriInfo.year} هـ",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = SoftCream,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CrimsonPillBadge(
                        text = "المتبقي على ${nextPrayerInfo.first.label}",
                        horizontalPadding = 16.dp,
                        verticalPadding = 4.dp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = nextPrayerInfo.second,
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = GoldBright,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الأذان القادم الساعة ${PrayerTimesCalculator.formatMinutesAr(nextPrayerInfo.first.minutesFromMidnight)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite),
                    )
                }
            }

            // Full 9-Row Schedule
            rows.forEach { row ->
                val isNext = row.id == nextPrayerInfo.first.id
                AlAnsariGoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentBorderColor = if (isNext) CrimsonRedBright else GoldBright,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = row.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isNext) GoldSpecular else PureWhite,
                                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
                                ),
                            )
                            if (isNext) {
                                CrimsonPillBadge(
                                    text = "التالي",
                                    horizontalPadding = 8.dp,
                                    verticalPadding = 1.dp,
                                )
                            }
                        }
                        Text(
                            text = PrayerTimesCalculator.formatMinutesAr(row.minutesFromMidnight),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isNext) GoldSpecular else GoldBright,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        )
                    }
                }
            }

            Text(
                text = "الحساب الفلكي وفق أفق ${city.nameAr} (زاوية الفجر 16°، المغرب 4°، العشاء 14°)",
                style = MaterialTheme.typography.bodySmall.copy(color = MutedGray),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun QiblaScreen(
    selectedCityId: String,
    onSelectCity: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val city = remember(selectedCityId) { PrayerTimesCalculator.cityById(selectedCityId) }
    val qiblaBearing = remember(city) {
        PrayerTimesCalculator.calculateQiblaBearing(city.lat, city.lon)
    }

    var headingDeg by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (sensorManager == null || (rotationSensor == null && (accelSensor == null || magSensor == null))) {
            hasSensor = false
            onDispose { }
        } else {
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            var hasGravity = false
            var hasMag = false

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val rotationMatrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            SensorManager.getOrientation(rotationMatrix, orientation)
                            val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                            headingDeg = headingDeg + 0.25f * (((deg - headingDeg + 540f) % 360f) - 180f)
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, 3)
                            hasGravity = true
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                            hasMag = true
                        }
                    }
                    if (rotationSensor == null && hasGravity && hasMag) {
                        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(rotationMatrix, orientation)
                            val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                            headingDeg = headingDeg + 0.25f * (((deg - headingDeg + 540f) % 360f) - 180f)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            runCatching {
                if (rotationSensor != null) {
                    sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
                } else {
                    sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                    sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
                }
            }.onFailure { hasSensor = false }

            onDispose {
                runCatching { sensorManager.unregisterListener(listener) }
            }
        }
    }

    val animatedHeading by animateFloatAsState(
        targetValue = headingDeg,
        animationSpec = tween(durationMillis = 160),
        label = "compass_heading",
    )

    val deltaAngle = ((qiblaBearing - animatedHeading + 540f) % 360f) - 180f
    val isAligned = hasSensor && abs(deltaAngle) <= 5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlAnsariSubTopBar(
            title = "اتجاه القبلة الشريفة",
            badgeText = "${city.nameAr} • ${qiblaBearing.roundToInt()}° من الشمال",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrayerTimesCalculator.iraqiCities.forEach { item ->
                    GoldOutlineChip(
                        text = item.nameAr,
                        selected = item.id == city.id,
                        onClick = { onSelectCity(item.id) },
                    )
                }
            }

            AlAnsariGoldCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorderColor = if (isAligned) EmeraldAccent else GoldBright,
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CrimsonPillBadge(
                        text = when {
                            !hasSensor -> "زاوية القبلة الدقيقة: ${qiblaBearing.roundToInt()}°"
                            isAligned -> "أنت الآن متجه نحو القبلة الشريفة"
                            else -> "قم بتدوير الهاتف باتجاه السهم الذهبي"
                        },
                        gradientColors = if (isAligned) {
                            listOf(EmeraldAccent, EmeraldAccent, EmeraldAccent)
                        } else {
                            listOf(CrimsonRed, CrimsonRedBright, CrimsonRed)
                        },
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3D Metallic Compass Dial
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .testTag("qibla_compass_dial"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val outerR = size.minDimension / 2f - 6.dp.toPx()

                            // Outer 3D metallic gold rim
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(ElevatedDark, ObsidianBlack),
                                ),
                                radius = outerR,
                                center = Offset(cx, cy),
                            )
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(GoldSpecular, GoldBright, GoldDarkBorder),
                                ),
                                radius = outerR,
                                center = Offset(cx, cy),
                                style = Stroke(width = 6.dp.toPx()),
                            )

                            // Rotating dial relative to magnetic north
                            rotate(degrees = -animatedHeading, pivot = Offset(cx, cy)) {
                                // Degree ticks every 15 deg
                                for (deg in 0 until 360 step 15) {
                                    val rad = Math.toRadians(deg.toDouble() - 90.0)
                                    val innerR = if (deg % 90 == 0) outerR - 18.dp.toPx() else outerR - 10.dp.toPx()
                                    drawLine(
                                        color = if (deg == 0) CrimsonRedBright else GoldPrimary.copy(alpha = 0.7f),
                                        start = Offset(
                                            cx + (innerR * cos(rad)).toFloat(),
                                            cy + (innerR * sin(rad)).toFloat(),
                                        ),
                                        end = Offset(
                                            cx + ((outerR - 4.dp.toPx()) * cos(rad)).toFloat(),
                                            cy + ((outerR - 4.dp.toPx()) * sin(rad)).toFloat(),
                                        ),
                                        strokeWidth = if (deg % 90 == 0) 3.dp.toPx() else 1.4.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                }

                                // Kaaba Qibla pointer rotated to exact qiblaBearing
                                rotate(degrees = qiblaBearing, pivot = Offset(cx, cy)) {
                                    val tipY = cy - outerR + 22.dp.toPx()
                                    val qiblaPath = Path().apply {
                                        moveTo(cx, tipY)
                                        lineTo(cx + 15.dp.toPx(), cy + 16.dp.toPx())
                                        lineTo(cx, cy + 4.dp.toPx())
                                        lineTo(cx - 15.dp.toPx(), cy + 16.dp.toPx())
                                        close()
                                    }
                                    drawPath(
                                        path = qiblaPath,
                                        brush = Brush.verticalGradient(
                                            colors = if (isAligned) {
                                                listOf(EmeraldAccent, GoldBright)
                                            } else {
                                                listOf(GoldSpecular, GoldBright, GoldPrimary)
                                            },
                                        ),
                                    )
                                    // Kaaba black/gold cube symbol near tip
                                    drawCircle(
                                        color = ObsidianBlack,
                                        radius = 12.dp.toPx(),
                                        center = Offset(cx, tipY - 12.dp.toPx()),
                                    )
                                    drawCircle(
                                        color = GoldBright,
                                        radius = 12.dp.toPx(),
                                        center = Offset(cx, tipY - 12.dp.toPx()),
                                        style = Stroke(width = 2.dp.toPx()),
                                    )
                                }
                            }

                            // Center metallic jewel pivot
                            drawCircle(
                                brush = Brush.radialGradient(listOf(GoldSpecular, GoldPrimary)),
                                radius = 9.dp.toPx(),
                                center = Offset(cx, cy),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("زاوية القبلة", style = MaterialTheme.typography.labelMedium.copy(color = SoftCream))
                            Text(
                                "${qiblaBearing.roundToInt()}°",
                                style = MaterialTheme.typography.headlineSmall.copy(color = GoldBright, fontWeight = FontWeight.ExtraBold),
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("اتجاه الهاتف", style = MaterialTheme.typography.labelMedium.copy(color = SoftCream))
                            Text(
                                if (hasSensor) "${animatedHeading.roundToInt()}°" else "غير متاح",
                                style = MaterialTheme.typography.headlineSmall.copy(color = PureWhite, fontWeight = FontWeight.ExtraBold),
                            )
                        }
                    }
                }
            }

            if (!hasSensor) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElevatedDark)
                        .border(1.dp, GoldDarkBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Text(
                        text = "مستشعر البوصلة غير متوفر على هذا الجهاز أو المحاكي. يمكنك التوجه بزاوية ${qiblaBearing.roundToInt()}° شرقاً من الشمال الجغرافي لمدينة ${city.nameAr}.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftCream),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
