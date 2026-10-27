package com.example.util

import com.example.data.entity.Transaction
import com.example.data.entity.TransactionItem
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelExporter {

    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    private val rupiahFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))

    /**
     * Exports transactions and item details into a professional Microsoft Excel (.xlsx) file.
     */
    fun exportToXlsx(
        transactions: List<Transaction>,
        itemsMap: Map<Int, List<TransactionItem>>,
        storeName: String,
        storeAddress: String,
        cashierName: String,
        periodLabel: String,
        outputStream: OutputStream
    ) {
        val zip = ZipOutputStream(outputStream)

        // 1. [Content_Types].xml
        zip.putNextEntry(ZipEntry("[Content_Types].xml"))
        zip.write(buildContentTypesXml().toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        // 2. _rels/.rels
        zip.putNextEntry(ZipEntry("_rels/.rels"))
        zip.write(buildRootRelsXml().toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        // 3. xl/_rels/workbook.xml.rels
        zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
        zip.write(buildWorkbookRelsXml().toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        // 4. xl/workbook.xml
        zip.putNextEntry(ZipEntry("xl/workbook.xml"))
        zip.write(buildWorkbookXml().toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        // 5. xl/styles.xml
        zip.putNextEntry(ZipEntry("xl/styles.xml"))
        zip.write(buildStylesXml().toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        // 6. xl/worksheets/sheet1.xml (Transactions & Summary)
        zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
        val sheet1Xml = buildSheet1Xml(
            transactions = transactions,
            itemsMap = itemsMap,
            storeName = storeName,
            storeAddress = storeAddress,
            cashierName = cashierName,
            periodLabel = periodLabel
        )
        zip.write(sheet1Xml.toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        // 7. xl/worksheets/sheet2.xml (Item Details)
        zip.putNextEntry(ZipEntry("xl/worksheets/sheet2.xml"))
        val sheet2Xml = buildSheet2Xml(
            transactions = transactions,
            itemsMap = itemsMap
        )
        zip.write(sheet2Xml.toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.finish()
    }

    /**
     * Fallback or alternative CSV export with UTF-8 BOM.
     */
    fun exportToCsv(
        transactions: List<Transaction>,
        itemsMap: Map<Int, List<TransactionItem>>,
        storeName: String,
        periodLabel: String,
        outputStream: OutputStream
    ) {
        val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
        // Write UTF-8 BOM for Microsoft Excel auto-detection
        outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

        writer.write("LAPORAN TRANSAKSI PENJUALAN - $storeName\n")
        writer.write("Periode: $periodLabel\n")
        writer.write("Waktu Unduh: ${dateTimeFormat.format(Date())}\n\n")

        writer.write("No,ID Transaksi,Waktu,Kasir,Rincian Produk,Total (Rp),Tunai (Rp),Kembalian (Rp)\n")

        var totalRevenue = 0.0
        transactions.forEachIndexed { index, tx ->
            totalRevenue += tx.totalAmount
            val items = itemsMap[tx.id] ?: emptyList()
            val itemsSummary = items.joinToString(" | ") { item ->
                val mod = if (item.selectedModifier.isNotEmpty()) " (${item.selectedModifier})" else ""
                "${item.productName}$mod (${item.quantity}x)"
            }.replace("\"", "\"\"")

            val line = listOf(
                "${index + 1}",
                "\"#EC-${tx.id}\"",
                "\"${dateTimeFormat.format(Date(tx.timestamp))}\"",
                "\"${tx.cashierName}\"",
                "\"$itemsSummary\"",
                "${tx.totalAmount.toLong()}",
                "${tx.cashPaid.toLong()}",
                "${tx.changeAmount.toLong()}"
            ).joinToString(",")
            writer.write(line + "\n")
        }

        writer.write("\n,,,TOTAL,$totalRevenue\n")
        writer.flush()
    }

    // --- OPENXML BUILDERS ---

    private fun buildContentTypesXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private fun buildRootRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun buildWorkbookRelsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun buildWorkbookXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Ringkasan &amp; Transaksi" sheetId="1" r:id="rId1"/>
    <sheet name="Rincian Item" sheetId="2" r:id="rId2"/>
  </sheets>
</workbook>"""

    private fun buildStylesXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="1">
    <numFmt numFmtId="164" formatCode="#,##0"/>
  </numFmts>
  <fonts count="5">
    <font><name val="Calibri"/><sz val="11"/></font>
    <font><b/><name val="Calibri"/><sz val="11"/></font>
    <font><b/><name val="Calibri"/><sz val="14"/><color rgb="FF1D6F42"/></font>
    <font><b/><name val="Calibri"/><sz val="11"/><color rgb="FFFFFFFF"/></font>
    <font><b/><name val="Calibri"/><sz val="11"/><color rgb="FF1D6F42"/></font>
  </fonts>
  <fills count="5">
    <fill><patternFill fillType="none"/></fill>
    <fill><patternFill fillType="gray125"/></fill>
    <fill><patternFill fillType="solid"><fgColor rgb="FF1D6F42"/></patternFill></fill>
    <fill><patternFill fillType="solid"><fgColor rgb="FFF2F4F7"/></patternFill></fill>
    <fill><patternFill fillType="solid"><fgColor rgb="FFE2EFDA"/></patternFill></fill>
  </fills>
  <borders count="3">
    <border><left/><right/><top/><bottom/></border>
    <border>
      <left style="thin"><color rgb="FFD0D5DD"/></left>
      <right style="thin"><color rgb="FFD0D5DD"/></right>
      <top style="thin"><color rgb="FFD0D5DD"/></top>
      <bottom style="thin"><color rgb="FFD0D5DD"/></bottom>
    </border>
    <border>
      <left style="thin"><color rgb="FFD0D5DD"/></left>
      <right style="thin"><color rgb="FFD0D5DD"/></right>
      <top style="thin"><color rgb="FF1D6F42"/></top>
      <bottom style="double"><color rgb="FF1D6F42"/></bottom>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="10">
    <!-- 0: Normal standard -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <!-- 1: Big Title -->
    <xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
    <!-- 2: Table Header (Excel Green fill, white bold text, centered) -->
    <xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center" wrapText="1"/>
    </xf>
    <!-- 3: Data text left (bordered) -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" applyAlignment="1">
      <alignment vertical="center"/>
    </xf>
    <!-- 4: Data numeric/currency right (bordered, formatted #,##0) -->
    <xf numFmtId="164" fontId="0" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="right" vertical="center"/>
    </xf>
    <!-- 5: KPI Card Header (gray fill, bold text, centered) -->
    <xf numFmtId="0" fontId="1" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center"/>
    </xf>
    <!-- 6: KPI Card Value (gray fill, green bold text, centered) -->
    <xf numFmtId="164" fontId="4" fillId="3" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center"/>
    </xf>
    <!-- 7: Total Row text (light green fill, bold text) -->
    <xf numFmtId="0" fontId="1" fillId="4" borderId="2" xfId="0" applyFont="1" applyFill="1" applyBorder="2" applyAlignment="1">
      <alignment horizontal="right" vertical="center"/>
    </xf>
    <!-- 8: Total Row number (light green fill, bold text, formatted #,##0) -->
    <xf numFmtId="164" fontId="1" fillId="4" borderId="2" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="2" applyAlignment="1">
      <alignment horizontal="right" vertical="center"/>
    </xf>
    <!-- 9: Data text center (bordered, centered) -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center"/>
    </xf>
  </cellXfs>
</styleSheet>"""

    private fun buildSheet1Xml(
        transactions: List<Transaction>,
        itemsMap: Map<Int, List<TransactionItem>>,
        storeName: String,
        storeAddress: String,
        cashierName: String,
        periodLabel: String
    ): String {
        val totalRevenue = transactions.sumOf { it.totalAmount }
        val totalCount = transactions.size
        val avgRevenue = if (totalCount > 0) totalRevenue / totalCount else 0.0

        val totalCash = transactions.sumOf { it.cashPaid }
        val totalChange = transactions.sumOf { it.changeAmount }

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <cols>
    <col min="1" max="1" width="7" customWidth="1"/>
    <col min="2" max="2" width="16" customWidth="1"/>
    <col min="3" max="3" width="24" customWidth="1"/>
    <col min="4" max="4" width="20" customWidth="1"/>
    <col min="5" max="5" width="42" customWidth="1"/>
    <col min="6" max="6" width="18" customWidth="1"/>
    <col min="7" max="7" width="18" customWidth="1"/>
    <col min="8" max="8" width="18" customWidth="1"/>
  </cols>
  <sheetData>
""")

        // Row 1: App & Title
        sb.append("""    <row r="1">
      <c r="A1" s="1" t="inlineStr"><is><t>${escapeXml(storeName.ifEmpty { "Easy Cashier" })} - LAPORAN PENJUALAN</t></is></c>
    </row>
""")

        // Row 2: Alamat
        sb.append("""    <row r="2">
      <c r="A2" s="0" t="inlineStr"><is><t>Alamat: ${escapeXml(storeAddress.ifEmpty { "-" })}</t></is></c>
    </row>
""")

        // Row 3: Kasir
        sb.append("""    <row r="3">
      <c r="A3" s="0" t="inlineStr"><is><t>Kasir: ${escapeXml(cashierName.ifEmpty { "-" })}</t></is></c>
    </row>
""")

        // Row 4: Periode
        sb.append("""    <row r="4">
      <c r="A4" s="0" t="inlineStr"><is><t>Periode: ${escapeXml(periodLabel)}</t></is></c>
    </row>
""")

        // Row 5: Waktu Cetak
        sb.append("""    <row r="5">
      <c r="A5" s="0" t="inlineStr"><is><t>Waktu Unduh: ${escapeXml(dateTimeFormat.format(Date()))}</t></is></c>
    </row>
    <row r="6"/>
""")

        // Row 7 & 8: Metric Highlights
        sb.append("""    <row r="7">
      <c r="B7" s="5" t="inlineStr"><is><t>Total Transaksi</t></is></c>
      <c r="C7" s="5" t="inlineStr"><is><t>Total Omset (Rp)</t></is></c>
      <c r="D7" s="5" t="inlineStr"><is><t>Rata-rata Transaksi (Rp)</t></is></c>
    </row>
    <row r="8">
      <c r="B8" s="6"><v>$totalCount</v></c>
      <c r="C8" s="6"><v>${totalRevenue.toLong()}</v></c>
      <c r="D8" s="6"><v>${avgRevenue.toLong()}</v></c>
    </row>
    <row r="9"/>
""")

        // Row 10: Table Headers
        val headerRow = 10
        sb.append("""    <row r="$headerRow">
      <c r="A$headerRow" s="2" t="inlineStr"><is><t>No</t></is></c>
      <c r="B$headerRow" s="2" t="inlineStr"><is><t>ID Transaksi</t></is></c>
      <c r="C$headerRow" s="2" t="inlineStr"><is><t>Tanggal &amp; Waktu</t></is></c>
      <c r="D$headerRow" s="2" t="inlineStr"><is><t>Kasir</t></is></c>
      <c r="E$headerRow" s="2" t="inlineStr"><is><t>Rincian Produk</t></is></c>
      <c r="F$headerRow" s="2" t="inlineStr"><is><t>Total Penjualan (Rp)</t></is></c>
      <c r="G$headerRow" s="2" t="inlineStr"><is><t>Bayar Tunai (Rp)</t></is></c>
      <c r="H$headerRow" s="2" t="inlineStr"><is><t>Kembalian (Rp)</t></is></c>
    </row>
""")

        var currentRow = headerRow + 1
        transactions.forEachIndexed { index, tx ->
            val items = itemsMap[tx.id] ?: emptyList()
            val itemsSummary = if (items.isEmpty()) {
                "-"
            } else {
                items.joinToString(", ") { item ->
                    val mod = if (item.selectedModifier.isNotEmpty()) " (${item.selectedModifier})" else ""
                    "${item.productName}$mod x${item.quantity}"
                }
            }

            val r = currentRow
            sb.append("""    <row r="$r">
      <c r="A$r" s="9"><v>${index + 1}</v></c>
      <c r="B$r" s="9" t="inlineStr"><is><t>#EC-${tx.id}</t></is></c>
      <c r="C$r" s="9" t="inlineStr"><is><t>${escapeXml(dateTimeFormat.format(Date(tx.timestamp)))}</t></is></c>
      <c r="D$r" s="3" t="inlineStr"><is><t>${escapeXml(tx.cashierName.ifEmpty { cashierName })}</t></is></c>
      <c r="E$r" s="3" t="inlineStr"><is><t>${escapeXml(itemsSummary)}</t></is></c>
      <c r="F$r" s="4"><v>${tx.totalAmount.toLong()}</v></c>
      <c r="G$r" s="4"><v>${tx.cashPaid.toLong()}</v></c>
      <c r="H$r" s="4"><v>${tx.changeAmount.toLong()}</v></c>
    </row>
""")
            currentRow++
        }

        // Summary Total Row
        val totalRow = currentRow
        sb.append("""    <row r="$totalRow">
      <c r="A$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="B$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="C$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="D$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="E$totalRow" s="7" t="inlineStr"><is><t>TOTAL KESELURUHAN:</t></is></c>
      <c r="F$totalRow" s="8"><v>${totalRevenue.toLong()}</v></c>
      <c r="G$totalRow" s="8"><v>${totalCash.toLong()}</v></c>
      <c r="H$totalRow" s="8"><v>${totalChange.toLong()}</v></c>
    </row>
""")

        sb.append("""  </sheetData>
</worksheet>""")
        return sb.toString()
    }

    private fun buildSheet2Xml(
        transactions: List<Transaction>,
        itemsMap: Map<Int, List<TransactionItem>>
    ): String {
        val txDateMap = transactions.associate { it.id to it.timestamp }

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <cols>
    <col min="1" max="1" width="7" customWidth="1"/>
    <col min="2" max="2" width="16" customWidth="1"/>
    <col min="3" max="3" width="24" customWidth="1"/>
    <col min="4" max="4" width="30" customWidth="1"/>
    <col min="5" max="5" width="22" customWidth="1"/>
    <col min="6" max="6" width="12" customWidth="1"/>
    <col min="7" max="7" width="18" customWidth="1"/>
    <col min="8" max="8" width="18" customWidth="1"/>
  </cols>
  <sheetData>
    <row r="1">
      <c r="A1" s="1" t="inlineStr"><is><t>RINCIAN ITEM TRANSAKSI PENJUALAN</t></is></c>
    </row>
    <row r="2"/>
    <row r="3">
      <c r="A3" s="2" t="inlineStr"><is><t>No</t></is></c>
      <c r="B3" s="2" t="inlineStr"><is><t>ID Transaksi</t></is></c>
      <c r="C3" s="2" t="inlineStr"><is><t>Tanggal &amp; Waktu</t></is></c>
      <c r="D3" s="2" t="inlineStr"><is><t>Nama Produk</t></is></c>
      <c r="E3" s="2" t="inlineStr"><is><t>Varian / Opsi</t></is></c>
      <c r="F3" s="2" t="inlineStr"><is><t>Qty</t></is></c>
      <c r="G3" s="2" t="inlineStr"><is><t>Harga Satuan (Rp)</t></is></c>
      <c r="H3" s="2" t="inlineStr"><is><t>Subtotal (Rp)</t></is></c>
    </row>
""")

        var currentRow = 4
        var itemNumber = 1
        var totalQty = 0
        var totalItemSubtotal = 0.0

        transactions.forEach { tx ->
            val items = itemsMap[tx.id] ?: emptyList()
            val timeString = dateTimeFormat.format(Date(tx.timestamp))

            items.forEach { item ->
                val subtotal = item.productPrice * item.quantity
                totalQty += item.quantity
                totalItemSubtotal += subtotal

                val r = currentRow
                sb.append("""    <row r="$r">
      <c r="A$r" s="9"><v>$itemNumber</v></c>
      <c r="B$r" s="9" t="inlineStr"><is><t>#EC-${item.transactionId}</t></is></c>
      <c r="C$r" s="9" t="inlineStr"><is><t>${escapeXml(timeString)}</t></is></c>
      <c r="D$r" s="3" t="inlineStr"><is><t>${escapeXml(item.productName)}</t></is></c>
      <c r="E$r" s="3" t="inlineStr"><is><t>${escapeXml(item.selectedModifier.ifEmpty { "-" })}</t></is></c>
      <c r="F$r" s="9"><v>${item.quantity}</v></c>
      <c r="G$r" s="4"><v>${item.productPrice.toLong()}</v></c>
      <c r="H$r" s="4"><v>${subtotal.toLong()}</v></c>
    </row>
""")
                currentRow++
                itemNumber++
            }
        }

        // Summary Total Row for Sheet 2
        val totalRow = currentRow
        sb.append("""    <row r="$totalRow">
      <c r="A$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="B$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="C$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="D$totalRow" s="7" t="inlineStr"><is><t></t></is></c>
      <c r="E$totalRow" s="7" t="inlineStr"><is><t>TOTAL KESELURUHAN:</t></is></c>
      <c r="F$totalRow" s="8"><v>$totalQty</v></c>
      <c r="G$totalRow" s="7" t="inlineStr"><is><t>-</t></is></c>
      <c r="H$totalRow" s="8"><v>${totalItemSubtotal.toLong()}</v></c>
    </row>
""")

        sb.append("""  </sheetData>
</worksheet>""")
        return sb.toString()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
