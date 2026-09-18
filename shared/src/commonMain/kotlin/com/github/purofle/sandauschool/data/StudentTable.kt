package com.github.purofle.sandauschool.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
data class StudentTable(
    val studentTableVms: List<StudentTableVm>
)

@Serializable
data class StudentTableVm(
    val activities: List<RemoteCourse>,
)

@Serializable
data class RemoteCourse(
    @SerialName("courseName") val name: String,
    val weekIndexes: List<Int>,
    @Serializable(with = RoomSerializer::class)
    val room: String,
    val teachers: List<String>,
    val startTime: String,
    val endTime: String,
    val startUnit: Int,
    val endUnit: Int,
    val weekday: Int,
)

object RoomSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("room", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        return try {
            decoder.decodeString()
        } catch (e: SerializationException) {
            decoder.decodeInt().toString()
        }
    }
}