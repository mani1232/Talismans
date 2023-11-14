package com.willfp.talismans.display

import com.rexcantor64.triton.api.TritonAPI
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.display.Display
import com.willfp.eco.core.display.DisplayModule
import com.willfp.eco.core.display.DisplayPriority
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.formatEco
import com.willfp.libreforge.ItemProvidedHolder
import com.willfp.talismans.talismans.util.TalismanChecks
import com.willfp.talismans.talismans.util.TalismanUtils
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("DEPRECATION")
class TalismanDisplay(plugin: EcoPlugin) : DisplayModule(plugin, DisplayPriority.LOWEST) {
    override fun display(
        itemStack: ItemStack,
        player: Player?,
        vararg args: Any
    ) {
        if (!TalismanUtils.isTalismanMaterial(itemStack.type)) {
            return
        }

        val meta = itemStack.itemMeta ?: return
        val itemLore = meta.lore ?: mutableListOf()

        val talisman = TalismanChecks.getTalismanOnItem(itemStack) ?: return

        val placeholderContext = placeholderContext(
            player = player,
            item = itemStack
        )

        if (player != null && talisman.name.isNotEmpty()) {
            meta.setDisplayName(TritonAPI.getInstance().languageParser.parseString(TritonAPI.getInstance().playerManager.get(player.uniqueId).getLang().getName(), TritonAPI.getInstance().config.itemsSyntax, talisman.name.formatEco(placeholderContext)))
        }

        if (talisman.itemStack.itemMeta?.hasCustomModelData() == true) {
            meta.setCustomModelData(talisman.itemStack.itemMeta?.customModelData)
        }

        val lore = mutableListOf<String>()

        lore.addAll(
            talisman.description
                .map { Display.PREFIX + it.formatEco(placeholderContext) }
        )

        lore.addAll(itemLore)

        if (player != null) {
            val provided = ItemProvidedHolder(talisman, itemStack)
            val lines = provided.getNotMetLines(player).map { Display.PREFIX + it }

            if (lines.isNotEmpty()) {
                lore.add(Display.PREFIX)
                lore.addAll(lines)
            }
        }
        val parsed = arrayListOf<String>()
        lore.forEach {
            if (player != null && it.isNotEmpty()) {
                // Triton v3
                parsed.add(TritonAPI.getInstance().languageParser.parseString(TritonAPI.getInstance().playerManager.get(player.uniqueId).getLang().getName(), TritonAPI.getInstance().config.itemsSyntax, it))
                // Triton v4
                //val result = TritonAPI.getInstance().messageParser.translateString(it, TritonAPI.getInstance().playerManager.get(player.uniqueId).language, TritonAPI.getInstance().config.itemsSyntax).result
                //if (result.isPresent) {
                //    parsed.add(result.get())
                //} else {
                //    parsed.add(it)
                //}
            } else {
                parsed.add(it)
            }
        }
        meta.lore = parsed
        itemStack.itemMeta = meta
    }
}