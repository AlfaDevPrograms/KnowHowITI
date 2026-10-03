package com.khsuiti.knowhow.responsesData

import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

internal fun JSONObject.optStringOrNull(key: String): String? =
	if (has(key) && !isNull(key)) optString(key).ifBlank { null } else null

fun parseAccountInfoResponse(j: JSONObject) = AccountInfoResponse(
	idUser = j.optString("idUser"),
	login = j.optString("login"),
	email = j.optString("email"),
	firstName = j.optString("firstName"),
	lastName = j.optString("lastName"),
	middleName = j.optStringOrNull("middleName"),
	roleID = j.optString("roleID"),
	roleName = j.optString("roleName"),
	isDiactivated = j.optBoolean("isDiactivated", false),
	diactivatedDate = j.optStringOrNull("diactivatedDate"),
	diactivatedReason = j.optStringOrNull("diactivatedReason")
)

fun parseAuthResponse(j: JSONObject) = AuthResponse(
	accessToken = j.optString("accessToken"),
	refreshToken = j.optString("refreshToken"),
	accountInfo = j.optJSONObject("accountInfo")?.let { parseAccountInfoResponse(it) } ?: AccountInfoResponse("", "", "", "", "", null, "", "", false)
)

fun parseUserResponse(j: JSONObject) = UserResponse(
	firstName = j.optString("firstName"),
	lastName = j.optString("lastName"),
	middleName = j.optStringOrNull("middleName")
)

fun <T> parsePageResponse(json: String, itemParser: (JSONObject) -> T): PageResponse<T> {
	val j = JSONObject(json)
	val arr = j.optJSONArray("items") ?: JSONArray()
	val list = ArrayList<T>(arr.length())
	for (i in 0 until arr.length()) {
		arr.optJSONObject(i)?.let { list.add(itemParser(it)) }
	}
	return PageResponse(
		startIndex = j.optInt("startIndex"),
		endIndex = j.optInt("endIndex"),
		totalCount = j.optInt("totalCount"),
		items = list
	)
}

fun parseSubjectResponse(j: JSONObject) = SubjectResponse(
	idSubject = j.optString("idSubject"),
	name = j.optString("name")
)

fun parseEducationResponse(j: JSONObject) = EducationResponse(
	idEducation = j.optString("idEducation"),
	educationName = j.optString("educationName")
)

fun parseRoleResponse(j: JSONObject) = RoleResponse(
	idRole = j.optString("idRole"),
	roleName = j.optString("roleName")
)

fun parseSessionResponse(j: JSONObject) = SessionResponse(
	createdAt = j.optString("createdAt"),
	clientInfo = j.optString("clientInfo")
)

fun parseQualificationDocumentResponse(j: JSONObject) = QualificationDocumentResponse(
	idQualificationDocuments = j.optString("idQualificationDocuments"),
	fileUrl = j.optString("fileUrl"),
	isVerified = if (j.has("isVerified") && !j.isNull("isVerified")) j.optBoolean("isVerified") else null
)

fun parseStudentProfileResponse(j: JSONObject) = StudentProfileResponse(
	idStudentProfile = j.optString("idStudentProfile"),
	createDate = j.optString("createDate"),
	isDeleted = j.optBoolean("isDeleted")
)

fun parseTutorProfileWithUserResponse(j: JSONObject): TutorProfileWithUserResponse {
	val userObj = j.optJSONObject("user")
	val eduObj = j.optJSONObject("education")
	val reviewsObj = j.optJSONObject("reviews")
	val reviewsPage = if (reviewsObj != null) {
		parsePageResponse(reviewsObj.toString()) { parseReviewResponse(it) }
	} else {
		PageResponse<ReviewResponse>(0, 0, 0, emptyList())
	}
	return TutorProfileWithUserResponse(
		idTutorProfile = j.optString("idTutorProfile"),
		experienceYear = j.optInt("experienceYear"),
		createdDate = j.optString("createdDate"),
		isVerified = j.optBoolean("isVerified"),
		isDeleted = j.optBoolean("isDeleted"),
		user = userObj?.let { parseUserResponse(it) } ?: UserResponse("Репетитор", "", null),
		bio = j.optStringOrNull("bio"),
		photoURL = j.optStringOrNull("photoURL"),
		education = eduObj?.let { parseEducationResponse(it) } ?: EducationResponse("e1", "Высшее образование"),
		averageRating = if (j.has("averageRating") && !j.isNull("averageRating")) j.optDouble("averageRating") else null,
		reviewCount = j.optInt("reviewCount"),
		reviews = reviewsPage
	)
}

fun parseServiceResponse(j: JSONObject): ServiceResponse {
	val tutorObj = j.optJSONObject("tutor")
	val defaultTutor = TutorProfileWithUserResponse(
		idTutorProfile = "t1",
		experienceYear = 5,
		createdDate = "2023-01-01",
		isVerified = true,
		isDeleted = false,
		user = UserResponse("Иван", "Иванов", null),
		bio = null,
		photoURL = null,
		education = EducationResponse("e1", "Высшее образование"),
		averageRating = 5.0,
		reviewCount = 10,
		reviews = PageResponse<ReviewResponse>(0, 0, 0, emptyList())
	)
	return ServiceResponse(
		idService = j.optString("idService"),
		idSubject = j.optString("idSubject"),
		subjectName = j.optString("subjectName"),
		description = j.optStringOrNull("description"),
		lessonDurationMinutes = j.optInt("lessonDurationMinutes"),
		priceInDollars = runCatching { BigDecimal(j.optDouble("priceInDollars").toString()) }.getOrDefault(BigDecimal("39.30")),
		isOnlineFormat = j.optBoolean("isOnlineFormat"),
		isActive = j.optBoolean("isActive"),
		tutor = tutorObj?.let { parseTutorProfileWithUserResponse(it) } ?: defaultTutor
	)
}

fun parseScheduleResponse(j: JSONObject) = ScheduleResponse(
	idSchedule = j.optString("idSchedule"),
	idService = j.optString("idService"),
	startTime = j.optString("startTime"),
	endTime = j.optString("endTime"),
	isBooked = j.optBoolean("isBooked")
)

fun parseBookingResponse(j: JSONObject): BookingResponse {
	val studentObj = j.optJSONObject("student")
	val scheduleObj = j.optJSONObject("schedule")
	val serviceObj = j.optJSONObject("service")
	return BookingResponse(
		idBooking = j.optString("idBooking"),
		idStudentProfile = j.optString("idStudentProfile"),
		student = studentObj?.let { parseUserResponse(it) } ?: UserResponse("Ученик", "", null),
		isClose = j.optBoolean("isClose"),
		dateBooking = j.optString("dateBooking"),
		schedule = scheduleObj?.let { parseScheduleResponse(it) } ?: ScheduleResponse("", "", "", "", false),
		service = serviceObj?.let { parseServiceResponse(it) } ?: ServiceResponse("", "", "", null, 60, BigDecimal("39.30"), true, true, TutorProfileWithUserResponse("", 0, "", false, false, UserResponse("", "", null), null, null, EducationResponse("", ""), 0.0, 0, PageResponse(0,0,0,emptyList())))
	)
}

fun parseReviewResponse(j: JSONObject): ReviewResponse {
	val studentObj = j.optJSONObject("student")
	return ReviewResponse(
		idReview = j.optString("idReview"),
		idBooking = j.optString("idBooking"),
		rating = j.optInt("rating"),
		comment = j.optStringOrNull("comment"),
		student = studentObj?.let { parseUserResponse(it) } ?: UserResponse("Ученик", "", null)
	)
}
