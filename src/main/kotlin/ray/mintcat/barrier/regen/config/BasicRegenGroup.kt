package ray.mintcat.barrier.regen.config

import org.bukkit.Material
import taboolib.common5.RandomList

data class BasicRegenGroup(
    val materials: List<Material>,
    val replace: Material,
    val delay: Long,
    val regions: List<String>,
    val fallback: Boolean,
    val check: Boolean,
    val random: RandomList<Material>?
)
