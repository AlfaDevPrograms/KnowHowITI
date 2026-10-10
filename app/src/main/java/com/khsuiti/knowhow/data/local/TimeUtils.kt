package com.khsuiti.knowhow.data.local

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun parseScheduleDateTime(isoString: String): LocalDateTime? {
	if (isoString.isBlank()) return null
	return runCatching {
		LocalDateTime.parse(isoString)
	}.recoverCatching {
		OffsetDateTime.parse(isoString).toLocalDateTime()
	}.getOrNull()
}

fun formatScheduleTime(isoString: String): String {
	if (isoString.isBlank()) return ""
	return runCatching {
		val dt = parseScheduleDateTime(isoString) ?: LocalDateTime.parse(isoString.removeSuffix("Z"))
		dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm"))
	}.getOrDefault(isoString)
}
