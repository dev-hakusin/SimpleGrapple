package io.github.daruma256.extension

import com.sun.org.apache.xpath.internal.operations.Bool
import io.github.daruma256.SimpleGrapple
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

private val plugin = SimpleGrapple.PLUGIN

var ItemStack.displayName: String
    get() {
        return try {
            itemMeta!!.displayName
        }catch (e: Exception) {
            ""
        }
    }
    set(value) {
        itemMeta = itemMeta?.also { meta ->
            meta.setDisplayName(value)
        }
    }

var ItemStack.customId: String?
    get() {
        return getStringNBT("customId")
    }
    set(value) {
        setNBTTag("customId", value ?: "unknown")
    }

var ItemStack.isUnBreakable: Boolean
    get() {
        return itemMeta!!.isUnbreakable
    }
    set(value) {
        itemMeta = itemMeta?.also { meta ->
            meta.isUnbreakable = value
        }
    }

fun ItemStack.setNBTTag(key: String, value: Any) {
    when (value) {
        is String -> itemMeta = itemMeta?.also { meta ->
            meta.persistentDataContainer.set(NamespacedKey(plugin, key), PersistentDataType.STRING, value)
        }
        else -> return
    }
}

private fun getNBT(itemStack: ItemStack, key: String, persistentDataType: PersistentDataType<out Any, out Any>): Any? {
    return itemStack.itemMeta?.persistentDataContainer?.get(NamespacedKey(plugin, key), persistentDataType)
}

fun ItemStack.getStringNBT(key: String): String? {
    return (getNBT(this, key, PersistentDataType.STRING) ?: return null) as String
}