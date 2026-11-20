package com.example.data.haqeebat

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class IraqiCity(
    val id: String,
    val nameAr: String,
    val lat: Double,
    val lon: Double,
    val timeZoneHours: Double = 3.0,
)

data class DailyPrayerSchedule(
    val city: IraqiCity,
    val imsaakMinutes: Int,
    val fajrMinutes: Int,
    val sunriseMinutes: Int,
    val dhuhrMinutes: Int,
    val asrMinutes: Int,
    val sunsetMinutes: Int,
    val maghribMinutes: Int,
    val ishaMinutes: Int,
    val midnightMinutes: Int,
)

data class HijriDateInfo(
    val day: Int,
    val month: Int, // 1..12
    val monthNameAr: String,
    val year: Int,
    val weekdayAr: String,
    val gregorianFormatted: String,
)

object PrayerTimesCalculator {

    const val KAABA_LAT = 21.422487
    const val KAABA_LON = 39.826206

    val iraqiCities: List<IraqiCity> = listOf(
        IraqiCity("najaf", "النجف الأشرف", 32.0000, 44.3360),
        IraqiCity("karbala", "كربلاء المقدسة", 32.6160, 44.0249),
        IraqiCity("baghdad", "بغداد – الكاظمية", 33.3152, 44.3661),
        IraqiCity("dhiqar", "ذي قار – الناصرية", 31.0439, 46.2573),
        IraqiCity("suq", "ذي قار – سوق الشيوخ", 30.8927, 46.4633),
        IraqiCity("basra", "البصرة", 30.5085, 47.7804),
        IraqiCity("babil", "بابل – الحلة", 32.4637, 44.4196),
        IraqiCity("diwaniyah", "القادسية – الديوانية", 31.9929, 44.9255),
        IraqiCity("muthanna", "المثنى – السماوة", 31.3320, 45.2944),
        IraqiCity("wasit", "واسط – الكوت", 32.5128, 45.8182),
        IraqiCity("maysan", "ميسان – العمارة", 31.8356, 47.1448),
        IraqiCity("samarra", "صلاح الدين – سامراء", 34.1983, 43.8742),
        IraqiCity("diyala", "ديالى – بعقوبة", 33.7481, 44.6436),
        IraqiCity("kirkuk", "كركوك", 35.4681, 44.3922),
        IraqiCity("mosul", "نينوى – الموصل", 36.3350, 43.1189),
        IraqiCity("anbar", "الأنبار – الرمادي", 33.4206, 43.3078),
        IraqiCity("erbil", "أربيل", 36.1911, 44.0092),
        IraqiCity("sulaymaniyah", "السليمانية", 35.5613, 45.4304),
        IraqiCity("duhok", "دهوك", 36.8669, 42.9503),
    )

    val hijriMonthsArabic = listOf(
        "محرم الحرام",
        "صفر الخير",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب الأصب",
        "شعبان المعظم",
        "شهر رمضان المبارك",
        "شوال المكرم",
        "ذو القعدة",
        "ذو الحجة الحرام",
    )

    fun cityById(id: String): IraqiCity = iraqiCities.firstOrNull { it.id == id } ?: iraqiCities.first()

    /**
     * Calculates prayer times using Shia Ithna-Ashari (Ja'fari / Najaf-Karbala) astronomical standard:
     * Fajr: 16 deg, Maghrib: 4 deg after sunset, Isha: 14 deg, Midnight: midpoint between Sunset & Fajr.
     */
    fun calculateForDate(
        city: IraqiCity,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Baghdad")),
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val jd = julianDate(year, month, day) - city.lon / (15.0 * 24.0)
        val fajrHours = computeSunAngleTime(jd, city.lat, 16.0, isMorning = true, tz = city.timeZoneHours, lon = city.lon)
        val sunriseHours = computeSunAngleTime(jd, city.lat, 0.833, isMorning = true, tz = city.timeZoneHours, lon = city.lon)
        val dhuhrHours = computeMidDay(jd, city.timeZoneHours, city.lon)
        val asrHours = computeAsrTime(jd, city.lat, 1.0, tz = city.timeZoneHours, lon = city.lon)
        val sunsetHours = computeSunAngleTime(jd, city.lat, 0.833, isMorning = false, tz = city.timeZoneHours, lon = city.lon)
        val maghribHours = computeSunAngleTime(jd, city.lat, 4.0, isMorning = false, tz = city.timeZoneHours, lon = city.lon)
        val ishaHours = computeSunAngleTime(jd, city.lat, 14.0, isMorning = false, tz = city.timeZoneHours, lon = city.lon)

        val fajrMin = toMinutes(fajrHours)
        val sunsetMin = toMinutes(sunsetHours)
        val nightDuration = ((fajrMin + 1440) - sunsetMin) % 1440
        val midnightMin = (sunsetMin + nightDuration / 2) % 1440

        return DailyPrayerSchedule(
            city = city,
            imsaakMinutes = (fajrMin - 10 + 1440) % 1440,
            fajrMinutes = fajrMin,
            sunriseMinutes = toMinutes(sunriseHours),
            dhuhrMinutes = toMinutes(dhuhrHours),
            asrMinutes = toMinutes(asrHours),
            sunsetMinutes = sunsetMin,
            maghribMinutes = toMinutes(maghribHours),
            ishaMinutes = toMinutes(ishaHours),
            midnightMinutes = midnightMin,
        )
    }

    fun formatMinutesAr(minutesFromMidnight: Int): String {
        val normalized = ((minutesFromMidnight % 1440) + 1440) % 1440
        val h24 = normalized / 60
        val m = normalized % 60
        val suffix = if (h24 < 12) "ص" else "م"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return "%02d:%02d %s".format(h12, m, suffix)
    }

    fun calculateQiblaBearing(lat: Double, lon: Double): Float {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LON - lon)
        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = Math.toDegrees(atan2(y, x))
        return ((theta + 360.0) % 360.0).toFloat()
    }

    fun getCurrentHijriDate(offsetDays: Int = 0): HijriDateInfo {
        val gregCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Baghdad"))
        val weekdays = arrayOf(
            "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت",
        )
        val weekdayAr = weekdays[(gregCal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]
        val gregText = "%04d/%02d/%02d".format(
            gregCal.get(Calendar.YEAR),
            gregCal.get(Calendar.MONTH) + 1,
            gregCal.get(Calendar.DAY_OF_MONTH),
        )

        val hijriCal = IslamicCalendar(ULocale("en_US@calendar=islamic-umalqura"))
        hijriCal.timeInMillis = System.currentTimeMillis() + offsetDays * 86_400_000L
        val hDay = hijriCal.get(IslamicCalendar.DAY_OF_MONTH)
        val hMonth = hijriCal.get(IslamicCalendar.MONTH) + 1 // 1..12
        val hYear = hijriCal.get(IslamicCalendar.YEAR)
        val monthName = hijriMonthsArabic[(hMonth - 1).coerceIn(0, 11)]

        return HijriDateInfo(
            day = hDay,
            month = hMonth,
            monthNameAr = monthName,
            year = hYear,
            weekdayAr = weekdayAr,
            gregorianFormatted = gregText,
        )
    }

    private fun toMinutes(hours: Double): Int {
        val norm = ((hours % 24.0) + 24.0) % 24.0
        return floor(norm * 60.0 + 0.5).toInt() % 1440
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2.0 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l)) / 15.0
        val decl = asinDeg(sinDeg(e) * sinDeg(l))
        val eqTime = q / 15.0 - fixHour(ra)
        return decl to eqTime
    }

    private fun computeMidDay(jd: Double, tz: Double, lon: Double): Double {
        val (_, eqTime) = sunPosition(jd + 0.5)
        val transit = fixHour(12.0 - eqTime)
        return transit + tz - lon / 15.0
    }

    private fun computeSunAngleTime(
        jd: Double,
        lat: Double,
        angle: Double,
        isMorning: Boolean,
        tz: Double,
        lon: Double,
    ): Double {
        val offset = if (isMorning) 5.0 / 24.0 else 18.0 / 24.0
        val (decl, eqTime) = sunPosition(jd + offset)
        val transit = fixHour(12.0 - eqTime)
        val num = -sinDeg(angle) - sinDeg(lat) * sinDeg(decl)
        val den = cosDeg(lat) * cosDeg(decl)
        val ratio = (num / den).coerceIn(-1.0, 1.0)
        val t = acosDeg(ratio) / 15.0
        val local = transit + if (isMorning) -t else t
        return local + tz - lon / 15.0
    }

    private fun computeAsrTime(
        jd: Double,
        lat: Double,
        factor: Double,
        tz: Double,
        lon: Double,
    ): Double {
        val (decl, eqTime) = sunPosition(jd + 15.0 / 24.0)
        val transit = fixHour(12.0 - eqTime)
        val angle = atanDeg(1.0 / (factor + tanDeg(kotlin.math.abs(lat - decl))))
        val num = sinDeg(angle) - sinDeg(lat) * sinDeg(decl)
        val den = cosDeg(lat) * cosDeg(decl)
        val ratio = (num / den).coerceIn(-1.0, 1.0)
        val t = acosDeg(ratio) / 15.0
        return (transit + t) + tz - lon / 15.0
    }

    private fun fixAngle(a: Double): Double = ((a % 360.0) + 360.0) % 360.0
    private fun fixHour(a: Double): Double = ((a % 24.0) + 24.0) % 24.0
    private fun sinDeg(d: Double): Double = sin(Math.toRadians(d))
    private fun cosDeg(d: Double): Double = cos(Math.toRadians(d))
    private fun tanDeg(d: Double): Double = tan(Math.toRadians(d))
    private fun asinDeg(x: Double): Double = Math.toDegrees(asin(x))
    private fun acosDeg(x: Double): Double = Math.toDegrees(acos(x))
    private fun atanDeg(x: Double): Double = Math.toDegrees(atan(x))
    private fun atan2Deg(y: Double, x: Double): Double = Math.toDegrees(atan2(y, x))
}
