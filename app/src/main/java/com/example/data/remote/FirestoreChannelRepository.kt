package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.auth.GoogleAuthHelper
import com.example.data.model.CloudChannelConfig
import com.example.data.model.CloudCoverageDoc
import com.example.data.model.CloudEventDoc
import com.example.data.model.CloudNewsDoc
import com.example.data.model.CloudVideoDoc
import com.example.data.model.CoverageBookingRequest
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreChannelRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id),
        ),
    )

    companion object {
        private const val COLLECTION_CHANNEL_CONFIG = "channel_config"
        private const val DOC_LIVE_CONFIG = "live"
        private const val COLLECTION_COVERAGE_REQUESTS = "coverage_requests"
        private const val COLLECTION_EVENTS = "channel_events"
        private const val COLLECTION_NEWS = "channel_news"
        private const val COLLECTION_VIDEOS = "channel_videos"
        private const val COLLECTION_COVERAGES = "channel_coverages"
        private val ALLOWED_STATUSES = setOf("pending", "confirmed", "completed", "declined")
        private val ALLOWED_EVENT_STATUSES = setOf("ONGOING", "UPCOMING", "ARCHIVED")
    }

    fun observeLiveChannelConfig(): Flow<CloudChannelConfig?> = flow {
        val path = "$COLLECTION_CHANNEL_CONFIG/$DOC_LIVE_CONFIG"
        emitAll(
            db.collection(COLLECTION_CHANNEL_CONFIG)
                .document(DOC_LIVE_CONFIG)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) {
                        null
                    } else {
                        snapshot.toObject(
                            CloudChannelConfig::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                        )
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    emit(null)
                },
        )
    }

    suspend fun publishChannelConfig(config: CloudChannelConfig): Result<Unit> {
        val docPath = "$COLLECTION_CHANNEL_CONFIG/$DOC_LIVE_CONFIG"
        return try {
            val user = auth.currentUser
                ?: throw IllegalStateException("يجب تسجيل الدخول أولاً بحساب مالك القناة.")
            if (!GoogleAuthHelper.isVerifiedChannelOwner(user)) {
                throw SecurityException("هذا الحساب غير مخوّل لتعديل إعدادات قناة الأنصاري.")
            }

            val cleanPrimary = config.primaryLiveUrl.trim().take(1000)
            val cleanBackup = config.backupLiveUrl.trim().take(1000)
            val cleanTitle = config.liveTitle.trim().ifBlank { "البث المباشر لقناة الأنصاري" }.take(150)
            val cleanHandle = config.youtubeHandle.trim().ifBlank { "@alansarie1" }.take(80)
            val cleanChannelId = config.youtubeChannelId.trim().ifBlank { "UCbZnqtO8B64mMhTzTXIxqUA" }.take(80)
            val cleanFb = config.facebookUrl.trim().take(300)
            val cleanTg = config.telegramUrl.trim().take(300)
            val cleanTt = config.tiktokUrl.trim().take(300)
            val cleanIg = config.instagramUrl.trim().take(300)
            val cleanFreq = config.satelliteFrequency.trim().take(150)

            val payload = mapOf(
                "primaryLiveUrl" to cleanPrimary,
                "backupLiveUrl" to cleanBackup,
                "liveTitle" to cleanTitle,
                "youtubeHandle" to cleanHandle,
                "youtubeChannelId" to cleanChannelId,
                "facebookUrl" to cleanFb,
                "telegramUrl" to cleanTg,
                "tiktokUrl" to cleanTt,
                "instagramUrl" to cleanIg,
                "satelliteFrequency" to cleanFreq,
                "updatedByUid" to user.uid,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            db.collection(COLLECTION_CHANNEL_CONFIG).document(DOC_LIVE_CONFIG).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun submitCoverageBooking(
        senderName: String,
        phone: String,
        governorate: String,
        serviceType: String,
        eventDate: String,
        topic: String,
        details: String,
    ): Result<Unit> {
        val docPath = COLLECTION_COVERAGE_REQUESTS
        return try {
            val uid = auth.currentUser?.uid
                ?: throw IllegalStateException("يرجى تسجيل الدخول بحساب Google لإرسال وحفظ طلب الحجز.")

            val nameTrim = senderName.trim()
            val phoneTrim = phone.trim()
            val govTrim = governorate.trim()
            val typeTrim = serviceType.trim()
            val dateTrim = eventDate.trim()
            val topicTrim = topic.trim()
            val detailsTrim = details.trim()

            require(nameTrim.length in 2..100) { "يرجى كتابة الاسم أو اسم الجهة (حرفان على الأقل)." }
            require(phoneTrim.length in 5..30) { "يرجى كتابة رقم هاتف صحيح للتواصل." }
            require(govTrim.length in 2..80) { "يرجى تحديد المحافظة والمنطقة." }
            require(typeTrim.length in 2..80) { "يرجى اختيار نوع التغطية." }
            require(dateTrim.length in 2..80) { "يرجى تحديد موعد الفعالية." }
            require(topicTrim.length in 2..150) { "يرجى كتابة عنوان الفعالية أو المجلس." }

            val docRef = db.collection(COLLECTION_COVERAGE_REQUESTS).document()
            val payload = linkedMapOf<String, Any?>(
                "userId" to uid,
                "senderName" to nameTrim,
                "phone" to phoneTrim,
                "governorate" to govTrim,
                "serviceType" to typeTrim,
                "eventDate" to dateTrim,
                "topic" to topicTrim,
                "details" to detailsTrim.take(1000).ifBlank { null },
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    fun observeMyBookings(userId: String): Flow<List<CoverageBookingRequest>> = flow {
        if (userId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = COLLECTION_COVERAGE_REQUESTS
        emitAll(
            db.collection(COLLECTION_COVERAGE_REQUESTS)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CoverageBookingRequest::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    fun observeAllBookingsForOwner(): Flow<List<CoverageBookingRequest>> = flow {
        val currentUser = auth.currentUser
        if (!GoogleAuthHelper.isVerifiedChannelOwner(currentUser)) {
            emit(emptyList())
            return@flow
        }
        val path = COLLECTION_COVERAGE_REQUESTS
        emitAll(
            db.collection(COLLECTION_COVERAGE_REQUESTS)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CoverageBookingRequest::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && GoogleAuthHelper.isVerifiedChannelOwner(auth.currentUser)) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    suspend fun updateBookingStatus(
        requestId: String,
        newStatus: String,
        adminNote: String? = null,
    ): Result<Unit> {
        val docPath = "$COLLECTION_COVERAGE_REQUESTS/$requestId"
        return try {
            val user = auth.currentUser
                ?: throw IllegalStateException("يجب تسجيل الدخول بحساب مالك القناة.")
            if (!GoogleAuthHelper.isVerifiedChannelOwner(user)) {
                throw SecurityException("هذا الإجراء مخصص لمالك القناة فقط.")
            }
            require(requestId.isNotBlank()) { "معرّف الطلب غير صالح" }
            require(newStatus in ALLOWED_STATUSES) { "حالة الطلب غير صالحة" }

            val updates = linkedMapOf<String, Any?>(
                "status" to newStatus,
                "adminNote" to adminNote?.trim()?.take(300)?.ifBlank { null },
                "updatedAt" to FieldValue.serverTimestamp(),
            ).filterValues { it != null }

            db.collection(COLLECTION_COVERAGE_REQUESTS)
                .document(requestId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    private fun requireVerifiedOwnerUid(): String {
        val user = auth.currentUser
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً بحساب مالك القناة.")
        if (!GoogleAuthHelper.isVerifiedChannelOwner(user)) {
            throw SecurityException("هذا الإجراء مخصص لمالك القناة فقط.")
        }
        return user.uid
    }

    fun observeCloudEvents(): Flow<List<CloudEventDoc>> = flow {
        emitAll(
            db.collection(COLLECTION_EVENTS)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudEventDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    (firestoreError ?: (error as? Exception))?.let {
                        handleFirestoreError(it, OperationType.LIST, COLLECTION_EVENTS)
                    }
                    emit(emptyList())
                },
        )
    }

    suspend fun publishChannelEvent(
        title: String,
        hijriDate: String,
        gregorianDate: String,
        timeLabel: String,
        location: String,
        status: String,
        description: String,
    ): Result<Unit> = try {
        val uid = requireVerifiedOwnerUid()
        val cleanTitle = title.trim().take(180)
        val cleanHijri = hijriDate.trim().ifBlank { "1448 هـ" }.take(80)
        val cleanGreg = gregorianDate.trim().ifBlank { "2026" }.take(80)
        val cleanTime = timeLabel.trim().ifBlank { "08:00 مساءً" }.take(80)
        val cleanLocation = location.trim().take(140)
        val cleanStatus = if (status in ALLOWED_EVENT_STATUSES) status else "UPCOMING"
        val cleanDesc = description.trim().take(1500)

        require(cleanTitle.length >= 2) { "يرجى كتابة عنوان الفعالية (حرفان على الأقل)." }
        require(cleanLocation.length >= 2) { "يرجى تحديد مكان الفعالية." }

        val payload = mapOf(
            "title" to cleanTitle,
            "hijriDate" to cleanHijri,
            "gregorianDate" to cleanGreg,
            "timeLabel" to cleanTime,
            "location" to cleanLocation,
            "status" to cleanStatus,
            "description" to cleanDesc,
            "createdByUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        db.collection(COLLECTION_EVENTS).document().set(payload).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, COLLECTION_EVENTS)
        Result.failure(e)
    }

    suspend fun updateCloudEventStatus(eventId: String, newStatus: String): Result<Unit> = try {
        requireVerifiedOwnerUid()
        require(eventId.isNotBlank() && newStatus in ALLOWED_EVENT_STATUSES)
        db.collection(COLLECTION_EVENTS).document(eventId).update(
            mapOf(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.UPDATE, "$COLLECTION_EVENTS/$eventId")
        Result.failure(e)
    }

    suspend fun deleteCloudEvent(eventId: String): Result<Unit> = try {
        requireVerifiedOwnerUid()
        db.collection(COLLECTION_EVENTS).document(eventId).delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "$COLLECTION_EVENTS/$eventId")
        Result.failure(e)
    }

    fun observeCloudNews(): Flow<List<CloudNewsDoc>> = flow {
        emitAll(
            db.collection(COLLECTION_NEWS)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudNewsDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    (firestoreError ?: (error as? Exception))?.let {
                        handleFirestoreError(it, OperationType.LIST, COLLECTION_NEWS)
                    }
                    emit(emptyList())
                },
        )
    }

    suspend fun publishChannelNews(
        title: String,
        category: String,
        summary: String,
        fullBody: String,
        author: String,
        isBreaking: Boolean,
    ): Result<Unit> = try {
        val uid = requireVerifiedOwnerUid()
        val cleanTitle = title.trim().take(200)
        val cleanCat = category.trim().ifBlank { "أخبار القناة" }.take(80)
        val cleanSummary = summary.trim().take(500)
        val cleanBody = fullBody.trim().ifBlank { cleanSummary }.take(4000)
        val cleanAuthor = author.trim().ifBlank { "إدارة قناة الأنصاري" }.take(100)

        require(cleanTitle.length >= 2) { "يرجى كتابة عنوان الخبر." }
        require(cleanSummary.length >= 2) { "يرجى كتابة ملخص الخبر." }

        val payload = mapOf(
            "title" to cleanTitle,
            "category" to cleanCat,
            "summary" to cleanSummary,
            "fullBody" to cleanBody,
            "publishedAt" to "الآن",
            "readingTime" to "دقيقتان",
            "author" to cleanAuthor,
            "breaking" to isBreaking,
            "createdByUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        db.collection(COLLECTION_NEWS).document().set(payload).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, COLLECTION_NEWS)
        Result.failure(e)
    }

    suspend fun deleteCloudNews(newsId: String): Result<Unit> = try {
        requireVerifiedOwnerUid()
        db.collection(COLLECTION_NEWS).document(newsId).delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "$COLLECTION_NEWS/$newsId")
        Result.failure(e)
    }

    fun observeCloudVideos(): Flow<List<CloudVideoDoc>> = flow {
        emitAll(
            db.collection(COLLECTION_VIDEOS)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudVideoDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    (firestoreError ?: (error as? Exception))?.let {
                        handleFirestoreError(it, OperationType.LIST, COLLECTION_VIDEOS)
                    }
                    emit(emptyList())
                },
        )
    }

    suspend fun publishChannelVideo(
        title: String,
        category: String,
        presenter: String,
        duration: String,
        description: String,
        streamOrYoutubeUrl: String,
        youtubeVideoId: String?,
    ): Result<Unit> = try {
        val uid = requireVerifiedOwnerUid()
        val cleanTitle = title.trim().take(200)
        val cleanCat = category.trim().ifBlank { "تغطيات خاصة" }.take(80)
        val cleanPresenter = presenter.trim().ifBlank { "قناة الأنصاري للإنتاج الفني" }.take(120)
        val cleanDuration = duration.trim().ifBlank { if (youtubeVideoId != null) "يوتيوب" else "فيديو" }.take(40)
        val cleanDesc = description.trim().take(1500)
        val rawUrl = streamOrYoutubeUrl.trim().take(1000)

        require(cleanTitle.length >= 2) { "يرجى كتابة عنوان الفيديو." }
        require(rawUrl.isNotBlank()) { "يرجى إدخال رابط يوتيوب أو رابط الفيديو المباشر." }

        val payload = linkedMapOf<String, Any?>(
            "title" to cleanTitle,
            "category" to cleanCat,
            "duration" to cleanDuration,
            "publishedAt" to "جديد",
            "description" to cleanDesc,
            "videoUrl" to if (youtubeVideoId != null) "" else rawUrl,
            "presenter" to cleanPresenter,
            "youtubeVideoId" to youtubeVideoId?.take(64),
            "youtubeWatchUrl" to if (youtubeVideoId != null) rawUrl else null,
            "featured" to true,
            "createdByUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        ).filterValues { it != null }

        db.collection(COLLECTION_VIDEOS).document().set(payload).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, COLLECTION_VIDEOS)
        Result.failure(e)
    }

    suspend fun deleteCloudVideo(videoId: String): Result<Unit> = try {
        requireVerifiedOwnerUid()
        db.collection(COLLECTION_VIDEOS).document(videoId).delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "$COLLECTION_VIDEOS/$videoId")
        Result.failure(e)
    }

    fun observeCloudCoverages(): Flow<List<CloudCoverageDoc>> = flow {
        emitAll(
            db.collection(COLLECTION_COVERAGES)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudCoverageDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    (firestoreError ?: (error as? Exception))?.let {
                        handleFirestoreError(it, OperationType.LIST, COLLECTION_COVERAGES)
                    }
                    emit(emptyList())
                },
        )
    }

    suspend fun publishChannelCoverage(
        title: String,
        location: String,
        reporter: String,
        statusLabel: String,
        summary: String,
        videoUrl: String,
        youtubeUrl: String?,
        isLiveField: Boolean,
    ): Result<Unit> = try {
        val uid = requireVerifiedOwnerUid()
        val cleanTitle = title.trim().take(200)
        val cleanLocation = location.trim().take(120)
        val cleanReporter = reporter.trim().ifBlank { "وحدة البث الخارجي SNG1080" }.take(120)
        val cleanStatus = statusLabel.trim().ifBlank { if (isLiveField) "بث ميداني" else "تغطية مصورة" }.take(60)
        val cleanSummary = summary.trim().take(1500)
        val cleanVideoUrl = videoUrl.trim().take(1000)
        val cleanYoutube = youtubeUrl?.trim()?.take(1000)?.ifBlank { null }

        require(cleanTitle.length >= 2) { "يرجى كتابة عنوان التغطية." }
        require(cleanLocation.length >= 2) { "يرجى كتابة موقع التغطية." }

        val payload = linkedMapOf<String, Any?>(
            "title" to cleanTitle,
            "location" to cleanLocation,
            "reporter" to cleanReporter,
            "timestampLabel" to if (isLiveField) "مباشر الآن" else "حديث",
            "statusLabel" to cleanStatus,
            "summary" to cleanSummary,
            "videoUrl" to cleanVideoUrl,
            "youtubeUrl" to cleanYoutube,
            "liveField" to isLiveField,
            "createdByUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
        ).filterValues { it != null }

        db.collection(COLLECTION_COVERAGES).document().set(payload).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.CREATE, COLLECTION_COVERAGES)
        Result.failure(e)
    }

    suspend fun deleteCloudCoverage(coverageId: String): Result<Unit> = try {
        requireVerifiedOwnerUid()
        db.collection(COLLECTION_COVERAGES).document(coverageId).delete().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, "$COLLECTION_COVERAGES/$coverageId")
        Result.failure(e)
    }
}
