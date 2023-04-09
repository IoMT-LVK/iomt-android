package com.iomt.android.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfigParserTest {
    private val configLines = """
        |[general]
        |    name = "Test"
        |    nameRegex = "T.*"
        |    characteristicNames = ["heartRate", "batteryInfo"]
        |[heartRate]
        |    name = "Heart Rate"
        |    serviceUUID = "0x180D"
        |    characteristicUUID = "0x2A37"
        |[batteryInfo]
        |    name = "Battery"
        |    serviceUUID = "0x180F"
        |    characteristicUUID = "0x2A19"
    """.trimMargin()

    @Test
    fun `should prase general section from file`() {
        val generalConfig = parseConfig(configLines).general
        assertTrue(generalConfig.name == "Test")
        assertTrue(generalConfig.nameRegex.toRegex().matches(generalConfig.name))
        assertTrue(generalConfig.characteristicNames.size == 2)
        assertTrue(generalConfig.type == null)
    }

    @Test
    fun `should prase all characteristics`() {
        val deviceConfig = parseConfig(configLines)
        assertTrue(deviceConfig.characteristics.size == 2)

        val heartRate = deviceConfig.characteristics["heartRate"]
        heartRate.let {
            requireNotNull(it)
            assertTrue(it.serviceUuid == "0x180D")
            assertTrue(it.characteristicUuid == "0x2A37")
            assertNull(it.descriptorUuid)
        }

        val batteryInfo = deviceConfig.characteristics["batteryInfo"]
        batteryInfo.let {
            requireNotNull(it)
            assertTrue(it.serviceUuid == "0x180F")
            assertTrue(it.characteristicUuid == "0x2A19")
            assertNull(it.descriptorUuid)
        }
    }
}
