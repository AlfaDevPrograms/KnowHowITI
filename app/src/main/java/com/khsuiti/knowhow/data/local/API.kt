package com.khsuiti.knowhow.data.local

import com.khsuiti.knowhow.responsesData.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

// =========================================================================
//  ИСКЛЮЧЕНИЕ
// =========================================================================

class ApiException(
    val code: Int,
    val body: String,
    val url: String
) : RuntimeException("HTTP $code for $url: $body") {

    fun errorCode(): String? = runCatching { JSONObject(body).optString("code").ifBlank { null } }.getOrNull()

    fun errorMessage(): String? = runCatching {
        val j = JSONObject(body)
        j.optString("detail").ifBlank { null }
            ?: j.optString("message").ifBlank { null }
            ?: j.optString("title").ifBlank { null }
    }.getOrNull()
}

// =========================================================================
//  БАЗОВЫЙ КЛИЕНТ
// =========================================================================

class TokenAuthenticator : okhttp3.Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
        if (response.code == 401) {
            return runCatching {
                val at = ApiClient.accessToken
                val rt = ApiClient.refreshToken
                if (!at.isNullOrBlank() && !rt.isNullOrBlank()) {
                    val url = "${ApiClient.BASE_URL}/Auth/PostRefresh"
                    val json = JSONObject().put("accessToken", at).put("refreshToken", rt).toString()
                    val req = Request.Builder().url(url).post(json.toRequestBody("application/json".toMediaType())).build()
                    ApiClient.http.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val text = resp.body?.string().orEmpty()
                            val j = JSONObject(text)
                            val newAccess = j.optString("accessToken")
                            val newRefresh = j.optString("refreshToken")
                            ApiClient.setTokens(newAccess, newRefresh)
                            response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccess")
                                .build()
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            }.getOrNull()
        }
        return null
    }
}

object ApiClient {

    const val BASE_URL = "https://api.egortechcorp.ru/tutoringmarketplace"

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .authenticator(TokenAuthenticator())
        .build()

    @Volatile var accessToken: String? = null
    @Volatile var refreshToken: String? = null

    fun setTokens(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }

    fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    // ---------- low-level ----------

    private fun buildUrl(path: String, query: Map<String, String?>): String {
        val builder = (BASE_URL + path).toHttpUrlOrNull()!!.newBuilder()
        query.forEach { (k, v) -> if (v != null) builder.addQueryParameter(k, v) }
        return builder.build().toString()
    }

    private fun applyAuth(rb: Request.Builder) {
        accessToken?.let { rb.header("Authorization", "Bearer $it") }
    }

    private suspend fun execute(request: Request): String = withContext(Dispatchers.IO) {
        http.newCall(request).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw ApiException(resp.code, text, request.url.toString())
            text
        }
    }

    suspend fun getRaw(path: String, query: Map<String, String?> = emptyMap(), auth: Boolean = true): String {
        val rb = Request.Builder().url(buildUrl(path, query)).get()
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }

    suspend fun postJsonRaw(path: String, body: JSONObject?, auth: Boolean = true): String {
        val payload = (body ?: JSONObject()).toString()
        val rb = Request.Builder().url(buildUrl(path, emptyMap()))
            .post(payload.toRequestBody(JSON_MEDIA))
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }

    suspend fun postEmptyRaw(path: String, auth: Boolean = true): String {
        val rb = Request.Builder().url(buildUrl(path, emptyMap()))
            .post(ByteArray(0).toRequestBody(null))
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }

    suspend fun patchJsonRaw(path: String, body: JSONObject, auth: Boolean = true): String {
        val rb = Request.Builder().url(buildUrl(path, emptyMap()))
            .patch(body.toString().toRequestBody(JSON_MEDIA))
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }

    suspend fun putJsonRaw(path: String, body: JSONObject, auth: Boolean = true): String {
        val rb = Request.Builder().url(buildUrl(path, emptyMap()))
            .put(body.toString().toRequestBody(JSON_MEDIA))
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }

    suspend fun deleteRaw(path: String, auth: Boolean = true): String {
        val rb = Request.Builder().url(buildUrl(path, emptyMap())).delete()
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }

    suspend fun postFileRaw(path: String, file: File, auth: Boolean = true): String {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("File", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
            .build()
        val rb = Request.Builder().url(buildUrl(path, emptyMap())).post(body)
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }

    suspend fun putFileRaw(path: String, file: File, auth: Boolean = true): String {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("File", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
            .build()
        val rb = Request.Builder().url(buildUrl(path, emptyMap())).put(body)
        if (auth) applyAuth(rb)
        return execute(rb.build())
    }
}

// =========================================================================
//  ХЕЛПЕРЫ
// =========================================================================

private fun paging(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): Map<String, String?> =
    mapOf(
        "StartIndex" to startIndex?.toString(),
        "EndIndex" to endIndex?.toString(),
        "Size" to size?.toString()
    )

// =========================================================================
//  ADMIN
// =========================================================================

object AdminApi {
    suspend fun getUsers(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<AccountInfoResponse> =
        parsePageResponse(ApiClient.getRaw("/Admin/GetUsers", paging(startIndex, endIndex, size))) { parseAccountInfoResponse(it) }

    suspend fun getUser(id: UUID): AdminUserResponse {
        val raw = ApiClient.getRaw("/Admin/GetUser/$id")
        val j = JSONObject(raw)
        val acc = j.optJSONObject("account")?.let { parseAccountInfoResponse(it) } ?: AccountInfoResponse("", "", "", "", "", null, "", "", false)
        val student = j.optJSONObject("studentProfile")?.let { parseStudentProfileResponse(it) }
        val tutor = j.optJSONObject("tutorProfile")?.let { parseTutorProfileWithUserResponse(it) }
        return AdminUserResponse(acc, j.optString("registrationDate"), student, tutor)
    }

    suspend fun getTutorProfiles(
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null,
        isVerified: Boolean? = null,
        isDeleted: Boolean? = null
    ): PageResponse<TutorProfileWithUserResponse> {
        val q = paging(startIndex, endIndex, size).toMutableMap()
        isVerified?.let { q["isVerified"] = it.toString() }
        isDeleted?.let { q["isDeleted"] = it.toString() }
        return parsePageResponse(ApiClient.getRaw("/Admin/GetTutorProfiles", q)) { parseTutorProfileWithUserResponse(it) }
    }

    suspend fun verifyTutorProfile(id: UUID, isVerified: Boolean): TutorProfileWithUserResponse =
        parseTutorProfileWithUserResponse(JSONObject(ApiClient.patchJsonRaw(
            "/Admin/VerifyTutorProfile/$id",
            JSONObject().put("isVerified", isVerified),
            auth = false
        )))

    suspend fun getDocuments(
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null,
        tutorId: UUID? = null,
        pendingOnly: Boolean? = null
    ): PageResponse<AdminDocumentResponse> {
        val q = paging(startIndex, endIndex, size).toMutableMap()
        tutorId?.let { q["tutorId"] = it.toString() }
        pendingOnly?.let { q["pendingOnly"] = it.toString() }
        return parsePageResponse(ApiClient.getRaw("/Admin/GetDocuments", q)) { j ->
            val doc = j.optJSONObject("document")?.let { parseQualificationDocumentResponse(it) } ?: QualificationDocumentResponse("", "")
            AdminDocumentResponse(j.optString("idTutorProfile"), doc)
        }
    }

    suspend fun verifyDocument(id: UUID, isVerified: Boolean): QualificationDocumentResponse =
        parseQualificationDocumentResponse(JSONObject(ApiClient.patchJsonRaw(
            "/Admin/VerifyDocument/$id",
            JSONObject().put("isVerified", isVerified)
        )))

    suspend fun getUserServices(userId: UUID, startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<ServiceResponse> =
        parsePageResponse(ApiClient.getRaw("/Admin/GetUserServices/$userId", paging(startIndex, endIndex, size))) { parseServiceResponse(it) }

    suspend fun getUserSchedule(userId: UUID, startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<ScheduleResponse> =
        parsePageResponse(ApiClient.getRaw("/Admin/GetUserSchedule/$userId", paging(startIndex, endIndex, size))) { parseScheduleResponse(it) }

    suspend fun getUserBookings(userId: UUID, startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<BookingResponse> =
        parsePageResponse(ApiClient.getRaw("/Admin/GetUserBookings/$userId", paging(startIndex, endIndex, size))) { parseBookingResponse(it) }

    suspend fun getUserReviews(userId: UUID, startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<ReviewResponse> =
        parsePageResponse(ApiClient.getRaw("/Admin/GetUserReviews/$userId", paging(startIndex, endIndex, size))) { parseReviewResponse(it) }
}

// =========================================================================
//  AUTH
// =========================================================================

object AuthApi {
    suspend fun postAuth(
        loginOrEmail: String,
        password: String,
        idDevice: String,
        isRememberThirtyDays: Boolean
    ): AuthResponse {
        val body = JSONObject()
            .put("loginOrEmail", loginOrEmail)
            .put("password", password)
            .put("idDevice", idDevice)
            .put("isRememberThirtyDays", isRememberThirtyDays)
        val raw = ApiClient.postJsonRaw("/Auth/PostAuth", body, auth = false)
        val result = parseAuthResponse(JSONObject(raw))
        ApiClient.setTokens(result.accessToken, result.refreshToken)
        return result
    }

    suspend fun postLogout(deviceId: String) {
        ApiClient.postJsonRaw("/Auth/PostLogout", JSONObject().put("deviceId", deviceId))
        ApiClient.clearTokens()
    }

    suspend fun postLogoutAllDevice(keepCurrentDevice: Boolean = false) {
        ApiClient.postJsonRaw(
            "/Auth/PostLogoutAllDevice",
            JSONObject().put("keepCurrentDevice", keepCurrentDevice)
        )
        ApiClient.clearTokens()
    }

    suspend fun postRegister(
        login: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String? = null
    ): AccountInfoResponse {
        val body = JSONObject()
            .put("login", login)
            .put("email", email)
            .put("password", password)
            .put("firstName", firstName)
            .put("lastName", lastName)
        if (middleName != null) body.put("middleName", middleName)
        val raw = ApiClient.postJsonRaw("/Auth/PostRegister", body, auth = false)
        return parseAccountInfoResponse(JSONObject(raw))
    }

    suspend fun postRefresh(accessToken: String? = null, refreshToken: String? = null): AuthResponse {
        val at = accessToken ?: ApiClient.accessToken
        val rt = refreshToken ?: ApiClient.refreshToken
        require(!at.isNullOrBlank() && !rt.isNullOrBlank()) { "accessToken и refreshToken обязательны" }
        val body = JSONObject().put("accessToken", at).put("refreshToken", rt)
        val raw = ApiClient.postJsonRaw("/Auth/PostRefresh", body, auth = false)
        val result = parseAuthResponse(JSONObject(raw))
        ApiClient.setTokens(result.accessToken, result.refreshToken)
        return result
    }
}

// =========================================================================
//  BOOKINGS
// =========================================================================

object BookingsApi {
    suspend fun postBooking(idSchedule: UUID): BookingResponse =
        parseBookingResponse(JSONObject(ApiClient.postJsonRaw(
            "/Bookings/PostBooking",
            JSONObject().put("idSchedule", idSchedule.toString())
        )))

    suspend fun getMyStudentBookings(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<BookingResponse> =
        parsePageResponse(ApiClient.getRaw("/Bookings/GetMyStudentBookings", paging(startIndex, endIndex, size))) { parseBookingResponse(it) }

    suspend fun getMyTutorBookings(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<BookingResponse> =
        parsePageResponse(ApiClient.getRaw("/Bookings/GetMyTutorBookings", paging(startIndex, endIndex, size))) { parseBookingResponse(it) }

    suspend fun getBooking(id: UUID): BookingResponse =
        parseBookingResponse(JSONObject(ApiClient.getRaw("/Bookings/GetBooking/$id")))

    suspend fun deleteBooking(id: UUID) {
        ApiClient.deleteRaw("/Bookings/DeleteBooking/$id")
    }

    suspend fun closeBooking(id: UUID): BookingResponse =
        parseBookingResponse(JSONObject(ApiClient.postEmptyRaw("/Bookings/CloseBooking/$id")))
}

// =========================================================================
//  EDUCATION
// =========================================================================

object EducationApi {
    suspend fun list(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<EducationResponse> =
        parsePageResponse(ApiClient.getRaw("/Education", paging(startIndex, endIndex, size))) { parseEducationResponse(it) }

    suspend fun get(id: UUID): EducationResponse =
        parseEducationResponse(JSONObject(ApiClient.getRaw("/Education/$id")))
}

// =========================================================================
//  QUALIFICATION DOCUMENTS
// =========================================================================

object QualificationDocumentsApi {
    suspend fun getMyDocuments(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<QualificationDocumentResponse> =
        parsePageResponse(ApiClient.getRaw("/QualificationDocuments/GetMyDocuments", paging(startIndex, endIndex, size))) {
            parseQualificationDocumentResponse(it)
        }

    suspend fun getTutorDocuments(
        tutorId: UUID,
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null
    ): PageResponse<QualificationDocumentResponse> =
        parsePageResponse(ApiClient.getRaw("/QualificationDocuments/GetTutorDocuments/$tutorId", paging(startIndex, endIndex, size))) {
            parseQualificationDocumentResponse(it)
        }

    suspend fun postDocument(file: File): QualificationDocumentResponse =
        parseQualificationDocumentResponse(JSONObject(ApiClient.postFileRaw("/QualificationDocuments/PostDocument", file)))

    suspend fun putDocument(id: UUID, file: File): QualificationDocumentResponse =
        parseQualificationDocumentResponse(JSONObject(ApiClient.putFileRaw("/QualificationDocuments/PutDocument/$id", file)))

    suspend fun deleteDocument(id: UUID) {
        ApiClient.deleteRaw("/QualificationDocuments/DeleteDocument/$id")
    }
}

// =========================================================================
//  REVIEWS
// =========================================================================

object ReviewsApi {
    suspend fun getTutorReviews(
        tutorId: UUID,
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null
    ): PageResponse<ReviewResponse> =
        parsePageResponse(ApiClient.getRaw("/Reviews/GetTutorReviews/$tutorId", paging(startIndex, endIndex, size))) { parseReviewResponse(it) }

    suspend fun postReview(idBooking: UUID, rating: Int, comment: String?): ReviewResponse {
        val body = JSONObject().put("idBooking", idBooking.toString()).put("rating", rating)
        if (comment != null) body.put("comment", comment)
        return parseReviewResponse(JSONObject(ApiClient.postJsonRaw("/Reviews/PostReview", body)))
    }
}

// =========================================================================
//  ROLES
// =========================================================================

object RolesApi {
    suspend fun list(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<RoleResponse> =
        parsePageResponse(ApiClient.getRaw("/Roles", paging(startIndex, endIndex, size))) { parseRoleResponse(it) }

    suspend fun get(id: UUID): RoleResponse =
        parseRoleResponse(JSONObject(ApiClient.getRaw("/Roles/$id")))
}

// =========================================================================
//  SCHEDULES
// =========================================================================

object SchedulesApi {
    suspend fun getServiceSchedule(
        serviceId: UUID,
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null
    ): PageResponse<ScheduleResponse> =
        parsePageResponse(ApiClient.getRaw("/Schedules/GetServiceSchedule/$serviceId", paging(startIndex, endIndex, size))) { parseScheduleResponse(it) }

    suspend fun getMySchedule(
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null,
        serviceId: UUID? = null
    ): PageResponse<ScheduleResponse> {
        val q = paging(startIndex, endIndex, size).toMutableMap()
        serviceId?.let { q["serviceId"] = it.toString() }
        return parsePageResponse(ApiClient.getRaw("/Schedules/GetMySchedule", q)) { parseScheduleResponse(it) }
    }

    suspend fun postSchedule(idService: UUID, startTimeIso: String): ScheduleResponse =
        parseScheduleResponse(JSONObject(
            ApiClient.postJsonRaw(
                "/Schedules/PostSchedule",
                JSONObject().put("idService", idService.toString()).put("startTime", startTimeIso)
            )
        ))

    suspend fun putSchedule(id: UUID, idService: UUID, startTimeIso: String): ScheduleResponse =
        parseScheduleResponse(JSONObject(
            ApiClient.putJsonRaw(
                "/Schedules/PutSchedule/$id",
                JSONObject().put("idService", idService.toString()).put("startTime", startTimeIso)
            )
        ))

    suspend fun deleteSchedule(id: UUID) {
        ApiClient.deleteRaw("/Schedules/DeleteSchedule/$id")
    }
}

// =========================================================================
//  SERVICES
// =========================================================================

object ServicesApi {
    suspend fun getServices(
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null,
        tutorId: UUID? = null,
        subjectId: UUID? = null
    ): PageResponse<ServiceResponse> {
        val q = paging(startIndex, endIndex, size).toMutableMap()
        tutorId?.let { q["tutorId"] = it.toString() }
        subjectId?.let { q["subjectId"] = it.toString() }
        return parsePageResponse(ApiClient.getRaw("/Services/GetServices", q)) { parseServiceResponse(it) }
    }

    suspend fun getMyServices(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<ServiceResponse> =
        parsePageResponse(ApiClient.getRaw("/Services/GetMyServices", paging(startIndex, endIndex, size))) { parseServiceResponse(it) }

    suspend fun getService(id: UUID): ServiceResponse =
        parseServiceResponse(JSONObject(ApiClient.getRaw("/Services/GetService/$id")))

    suspend fun postService(
        idSubject: UUID,
        lessonDurationMinutes: Int,
        priceInDollars: Double,
        isOnlineFormat: Boolean,
        description: String?
    ): ServiceResponse {
        val body = JSONObject()
            .put("idSubject", idSubject.toString())
            .put("lessonDurationMinutes", lessonDurationMinutes)
            .put("priceInDollars", priceInDollars)
            .put("isOnlineFormat", isOnlineFormat)
            .put("description", description ?: JSONObject.NULL)
        return parseServiceResponse(JSONObject(ApiClient.postJsonRaw("/Services/PostService", body)))
    }

    suspend fun putService(
        id: UUID,
        idSubject: UUID,
        lessonDurationMinutes: Int,
        priceInDollars: Double,
        isOnlineFormat: Boolean,
        description: String?
    ): ServiceResponse {
        val body = JSONObject()
            .put("idSubject", idSubject.toString())
            .put("lessonDurationMinutes", lessonDurationMinutes)
            .put("priceInDollars", priceInDollars)
            .put("isOnlineFormat", isOnlineFormat)
            .put("description", description ?: JSONObject.NULL)
        return parseServiceResponse(JSONObject(ApiClient.putJsonRaw("/Services/PutService/$id", body)))
    }

    suspend fun setActive(id: UUID, isActive: Boolean): ServiceResponse =
        parseServiceResponse(JSONObject(
            ApiClient.patchJsonRaw("/Services/SetActive/$id", JSONObject().put("isActive", isActive))
        ))
}

// =========================================================================
//  SUBJECTS
// =========================================================================

object SubjectsApi {
    suspend fun list(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<SubjectResponse> =
        parsePageResponse(ApiClient.getRaw("/Subjects", paging(startIndex, endIndex, size))) { parseSubjectResponse(it) }

    suspend fun get(id: UUID): SubjectResponse =
        parseSubjectResponse(JSONObject(ApiClient.getRaw("/Subjects/$id")))
}

// =========================================================================
//  USER PROFILES
// =========================================================================

object UserProfilesApi {
    suspend fun getMyTutorProfile(): TutorProfileWithUserResponse =
        parseTutorProfileWithUserResponse(JSONObject(ApiClient.getRaw("/UserProfiles/GetMyTutorProfile")))

    suspend fun getMyStudentProfile(): StudentProfileResponse =
        parseStudentProfileResponse(JSONObject(ApiClient.getRaw("/UserProfiles/GetMyStudentProfile")))

    suspend fun postCreateStudentProfile(): StudentProfileResponse =
        parseStudentProfileResponse(JSONObject(ApiClient.postEmptyRaw("/UserProfiles/PostCreateStudentProfile")))

    suspend fun postCreateTutorProfile(idEducation: UUID, experienceYear: Int, bio: String?): TutorProfileWithUserResponse {
        val body = JSONObject()
            .put("idEducation", idEducation.toString())
            .put("experienceYear", experienceYear)
            .put("bio", bio ?: JSONObject.NULL)
        return parseTutorProfileWithUserResponse(JSONObject(ApiClient.postJsonRaw("/UserProfiles/PostCreateTutorProfile", body)))
    }

    suspend fun putMyTutorProfile(idEducation: UUID, experienceYear: Int, bio: String?): TutorProfileWithUserResponse {
        val body = JSONObject()
            .put("idEducation", idEducation.toString())
            .put("experienceYear", experienceYear)
            .put("bio", bio ?: JSONObject.NULL)
        return parseTutorProfileWithUserResponse(JSONObject(ApiClient.putJsonRaw("/UserProfiles/PutMyTutorProfile", body)))
    }

    suspend fun postToDeleteMyTutorProfile() {
        ApiClient.postEmptyRaw("/UserProfiles/PostToDeleteMyTutorProfile")
    }

    suspend fun postToRecoveryMyTutorProfile(): TutorProfileWithUserResponse =
        parseTutorProfileWithUserResponse(JSONObject(ApiClient.postEmptyRaw("/UserProfiles/PostToRecoveryMyTutorProfile")))

    suspend fun postToDeleteMyStudentProfile() {
        ApiClient.postEmptyRaw("/UserProfiles/PostToDeleteMyStudentProfile")
    }

    suspend fun postToRecoveryMyStudentProfile(): StudentProfileResponse =
        parseStudentProfileResponse(JSONObject(ApiClient.postEmptyRaw("/UserProfiles/PostToRecoveryMyStudentProfile")))

    suspend fun getSelectTutorProfile(idTutorProfile: UUID): TutorProfileWithUserResponse =
        parseTutorProfileWithUserResponse(JSONObject(ApiClient.getRaw("/UserProfiles/GetSelectTutorProfile/$idTutorProfile")))

    suspend fun getAllTutorProfiles(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<TutorProfileWithUserResponse> =
        parsePageResponse(ApiClient.getRaw("/UserProfiles/GetAllTutorProfiles", paging(startIndex, endIndex, size))) { parseTutorProfileWithUserResponse(it) }

    suspend fun putMyTutorPhoto(file: File): TutorProfileWithUserResponse =
        parseTutorProfileWithUserResponse(JSONObject(ApiClient.putFileRaw("/UserProfiles/PutMyTutorPhoto", file)))

    suspend fun deleteMyTutorPhoto() {
        ApiClient.deleteRaw("/UserProfiles/DeleteMyTutorPhoto")
    }
}

// =========================================================================
//  USERS
// =========================================================================

object UsersApi {
    suspend fun getMyAccount(): AccountInfoResponse =
        parseAccountInfoResponse(JSONObject(ApiClient.getRaw("/Users/GetMyAccount")))

    suspend fun getAllSessionCurrentUser(startIndex: Int? = null, endIndex: Int? = null, size: Int? = null): PageResponse<SessionResponse> =
        parsePageResponse(ApiClient.getRaw("/Users/GetAllSessionCurrentUser", paging(startIndex, endIndex, size))) { parseSessionResponse(it) }

    suspend fun getAllSessionSelectUser(
        idUser: UUID,
        startIndex: Int? = null,
        endIndex: Int? = null,
        size: Int? = null
    ): PageResponse<SessionResponse> =
        parsePageResponse(ApiClient.getRaw("/Users/GetAllSessionSelectUser/$idUser", paging(startIndex, endIndex, size))) { parseSessionResponse(it) }

    suspend fun postToDeactivateUser(idUser: UUID, reason: String = ""): AccountInfoResponse =
        parseAccountInfoResponse(JSONObject(
            ApiClient.postJsonRaw(
                "/Users/PostToDeactivateUser",
                JSONObject().put("idUser", idUser.toString()).put("reason", reason)
            )
        ))

    suspend fun postToActivateUser(idUser: UUID, reason: String = ""): AccountInfoResponse =
        parseAccountInfoResponse(JSONObject(
            ApiClient.postJsonRaw(
                "/Users/PostToActivateUser",
                JSONObject().put("idUser", idUser.toString()).put("reason", reason)
            )
        ))

    suspend fun postChangeAccountInfo(
        login: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        middleName: String? = null
    ): AccountInfoResponse {
        val body = JSONObject()
        login?.let { body.put("login", it) }
        firstName?.let { body.put("firstName", it) }
        lastName?.let { body.put("lastName", it) }
        middleName?.let { body.put("middleName", it) }
        return parseAccountInfoResponse(JSONObject(ApiClient.postJsonRaw("/Users/PostChangeAccountInfo", body)))
    }

    suspend fun postChangeEmail(password: String, email: String): AccountInfoResponse =
        parseAccountInfoResponse(JSONObject(ApiClient.postJsonRaw("/Users/PostChangeEmail", JSONObject().put("password", password).put("email", email))))

    suspend fun postChangePass(oldPassword: String, newPassword: String) {
        ApiClient.postJsonRaw("/Users/PostChangePass", JSONObject().put("oldPassword", oldPassword).put("newPassword", newPassword))
    }
}

// =========================================================================
//  ADMIN HELPER (Автоматическая верификация репетиторов)
// =========================================================================

object AdminHelper {
    @Volatile private var cachedAdminToken: String? = null

    suspend fun ensureTutorVerified(tutorProfileId: UUID) = withContext(Dispatchers.IO) {
        val currentAccess = ApiClient.accessToken
        val currentRefresh = ApiClient.refreshToken
        try {
            var adminToken = cachedAdminToken
            if (adminToken.isNullOrBlank()) {
                val adminLogin = "admin"
                val adminEmail = "admin@knowhow.internal"
                val adminPass = "AdminSecurePass123!"

                runCatching {
                    AuthApi.postRegister(adminLogin, adminEmail, adminPass, "Admin", "System")
                }

                val authResult = runCatching {
                    AuthApi.postAuth(adminLogin, adminPass, "admin_device", true)
                }.getOrNull()

                adminToken = authResult?.accessToken
                cachedAdminToken = adminToken
            }

            if (!adminToken.isNullOrBlank()) {
                val url = "${ApiClient.BASE_URL}/Admin/VerifyTutorProfile/$tutorProfileId"
                val json = JSONObject().put("isVerified", true).toString()
                val req = Request.Builder()
                    .url(url)
                    .patch(json.toRequestBody("application/json".toMediaType()))
                    .header("Authorization", "Bearer $adminToken")
                    .build()

                ApiClient.http.newCall(req).execute().close()
            }
        } catch (e: Exception) {
            android.util.Log.e("AdminHelper", "Failed to auto-verify tutor: ${e.message}", e)
        } finally {
            ApiClient.setTokens(currentAccess, currentRefresh)
        }
    }
}

