package me.xiaozhangup.domain.utils.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.xiaozhangup.domain.utils.fromLocation
import me.xiaozhangup.domain.utils.toLocation
import org.bukkit.Location

object LocationSerializer : KSerializer<Location> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Location", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeString(fromLocation(value))
    }

    override fun deserialize(decoder: Decoder): Location {
        val string = decoder.decodeString()
        return toLocation(string)
    }
}