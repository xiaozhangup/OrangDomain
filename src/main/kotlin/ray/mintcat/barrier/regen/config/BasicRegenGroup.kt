package ray.mintcat.barrier.regen.config

import org.bukkit.Material

data class BasicRegenGroup(
    val materials: List<Material>,
    val replace: Material,
    val delay: Long,
    val regions: List<String>
)
