package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.auth.GoogleAuthHelper
import com.example.data.model.CloudChannelConfig
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
        private val ALLOWED_STATUSES = setOf("pending", "confirmed", "completed", "declined")
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
}
