package dev.whaltermc.veyra.data.db.converters

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeFormatter
import kotlinx.datetime.UTC
import dev.whaltermc.veyra.data.model.AuthType
import dev.whaltermc.veyra.data.model.ModloaderType
import dev.whaltermc.veyra.data.model.RenderBackend

class InstantConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun toTimestamp(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}

class AuthTypeConverter {
    @TypeConverter
    fun fromString(value: String?): AuthType? = value?.let { AuthType.valueOf(it) }

    @TypeConverter
    fun toString(authType: AuthType?): String? = authType?.name
}

class ModloaderTypeConverter {
    @TypeConverter
    fun fromString(value: String?): ModloaderType? = value?.let { ModloaderType.valueOf(it) }

    @TypeConverter
    fun toString(type: ModloaderType?): String? = type?.name
}

class RenderBackendConverter {
    @TypeConverter
    fun fromString(value: String?): RenderBackend? = value?.let { RenderBackend.valueOf(it) }

    @TypeConverter
    fun toString(backend: RenderBackend?): String? = backend?.name
}