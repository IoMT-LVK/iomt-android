package com.iomt.android

import com.iomt.android.config.ConfigParser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File


class ConfigParserTest {
    private val sep: String = File.separator
    private val resourcesPath = listOf(
        System.getProperty("user.dir"),
        "src",
        "test",
        "resources",
        "ConfigParser").joinToString (separator = sep)
    private val configParser: ConfigParser = ConfigParser("$resourcesPath${sep}config.toml")

    @Test
    fun `should prase general section from file`() {
        val generalConfig = configParser.parseGeneralConfig()
        assertTrue(generalConfig.name == "Test")
        assertTrue(generalConfig.nameRegex.toRegex().matches(generalConfig.name))
        assertTrue(generalConfig.characteristicNames.size == 2)
        assertTrue(generalConfig.type == null)
    }

    @Test
    fun `should prase all characteristics`() {
        val deviceConfig = configParser.parse()
        assertTrue(deviceConfig.characteristics.size == 2)

        val heartRate = deviceConfig.characteristics["heartRate"]
        heartRate.let {
            assertNotNull(it)
            assertTrue(it!!.serviceUUID == "0x180D")
            assertTrue(it.characteristicUUID == "0x2A37")
            assertNull(it.descriptorUUID)
        }

        val batteryInfo = deviceConfig.characteristics["batteryInfo"]
        batteryInfo.let {
            assertNotNull(it)
            assertTrue(it!!.serviceUUID == "0x180F")
            assertTrue(it.characteristicUUID == "0x2A19")
            assertNull(it.descriptorUUID)
        }
    }

    @Test
    fun `should read toml from url`() {
        val url = ""
    }
}
