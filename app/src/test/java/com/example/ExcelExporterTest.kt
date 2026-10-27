package com.example

import com.example.data.entity.Transaction
import com.example.data.entity.TransactionItem
import com.example.util.ExcelExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class ExcelExporterTest {

    @Test
    fun testExportToXlsx_generatesValidZipAndSheets() {
        val transactions = listOf(
            Transaction(
                id = 1,
                timestamp = 1770000000000L,
                totalAmount = 50000.0,
                cashPaid = 100000.0,
                changeAmount = 50000.0,
                storeName = "Kedai Kopi Santai",
                storeAddress = "Jl. Merdeka No. 10",
                cashierName = "Budi"
            )
        )

        val itemsMap = mapOf(
            1 to listOf(
                TransactionItem(
                    id = 101,
                    transactionId = 1,
                    productId = 1,
                    productName = "Kopi Susu Gula Aren",
                    productPrice = 25000.0,
                    quantity = 2,
                    selectedModifier = "Less Sugar"
                )
            )
        )

        val out = ByteArrayOutputStream()
        ExcelExporter.exportToXlsx(
            transactions = transactions,
            itemsMap = itemsMap,
            storeName = "Kedai Kopi Santai",
            storeAddress = "Jl. Merdeka No. 10",
            cashierName = "Budi",
            periodLabel = "Hari Ini",
            outputStream = out
        )

        val bytes = out.toByteArray()
        assertTrue("XLSX output should not be empty", bytes.isNotEmpty())

        // Verify ZIP entries
        val entryNames = mutableListOf<String>()
        val sheet1Content = StringBuilder()
        val sheet2Content = StringBuilder()

        ZipInputStream(bytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            val buffer = ByteArray(1024)
            while (entry != null) {
                entryNames.add(entry.name)
                val entryContent = StringBuilder()
                var read = zip.read(buffer)
                while (read != -1) {
                    entryContent.append(String(buffer, 0, read, Charsets.UTF_8))
                    read = zip.read(buffer)
                }

                if (entry.name == "xl/worksheets/sheet1.xml") {
                    sheet1Content.append(entryContent)
                } else if (entry.name == "xl/worksheets/sheet2.xml") {
                    sheet2Content.append(entryContent)
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        assertTrue("Must contain [Content_Types].xml", entryNames.contains("[Content_Types].xml"))
        assertTrue("Must contain _rels/.rels", entryNames.contains("_rels/.rels"))
        assertTrue("Must contain xl/workbook.xml", entryNames.contains("xl/workbook.xml"))
        assertTrue("Must contain xl/styles.xml", entryNames.contains("xl/styles.xml"))
        assertTrue("Must contain xl/worksheets/sheet1.xml", entryNames.contains("xl/worksheets/sheet1.xml"))
        assertTrue("Must contain xl/worksheets/sheet2.xml", entryNames.contains("xl/worksheets/sheet2.xml"))

        // Verify Sheet 1 content
        val s1 = sheet1Content.toString()
        assertTrue("Sheet 1 should contain store name", s1.contains("Kedai Kopi Santai"))
        assertTrue("Sheet 1 should contain transaction code", s1.contains("#EC-1"))
        assertTrue("Sheet 1 should contain total amount 50000", s1.contains("50000"))

        // Verify Sheet 2 content
        val s2 = sheet2Content.toString()
        assertTrue("Sheet 2 should contain product name", s2.contains("Kopi Susu Gula Aren"))
        assertTrue("Sheet 2 should contain modifier", s2.contains("Less Sugar"))
        assertTrue("Sheet 2 should contain price 25000", s2.contains("25000"))
    }

    @Test
    fun testExportToCsv_generatesUtf8CsvWithBom() {
        val transactions = listOf(
            Transaction(
                id = 2,
                timestamp = 1770000000000L,
                totalAmount = 30000.0,
                cashPaid = 50000.0,
                changeAmount = 20000.0,
                storeName = "Toko Berkah",
                cashierName = "Siti"
            )
        )

        val itemsMap = mapOf(
            2 to listOf(
                TransactionItem(
                    id = 102,
                    transactionId = 2,
                    productId = 2,
                    productName = "Roti Coklat",
                    productPrice = 15000.0,
                    quantity = 2,
                    selectedModifier = ""
                )
            )
        )

        val out = ByteArrayOutputStream()
        ExcelExporter.exportToCsv(
            transactions = transactions,
            itemsMap = itemsMap,
            storeName = "Toko Berkah",
            periodLabel = "Bulan Ini",
            outputStream = out
        )

        val bytes = out.toByteArray()
        assertTrue("CSV should have UTF-8 BOM", bytes.size >= 3)
        assertEquals(0xEF.toByte(), bytes[0])
        assertEquals(0xBB.toByte(), bytes[1])
        assertEquals(0xBF.toByte(), bytes[2])

        val text = String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        assertTrue("CSV should contain store name", text.contains("Toko Berkah"))
        assertTrue("CSV should contain transaction code", text.contains("#EC-2"))
        assertTrue("CSV should contain product name", text.contains("Roti Coklat"))
    }
}
