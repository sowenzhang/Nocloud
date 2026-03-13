package com.nocloudchat.network

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FileNameUtilsTest {

    private lateinit var tempDir: File

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("nocloudchat-test").toFile()
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `returns original name when no conflict exists`() {
        val result = uniqueFile(tempDir, "document.pdf")
        assertEquals(File(tempDir, "document.pdf"), result)
    }

    @Test
    fun `appends (1) when original file already exists`() {
        File(tempDir, "photo.png").createNewFile()

        val result = uniqueFile(tempDir, "photo.png")
        assertEquals(File(tempDir, "photo (1).png"), result)
    }

    @Test
    fun `appends (2) when both original and (1) exist`() {
        File(tempDir, "photo.png").createNewFile()
        File(tempDir, "photo (1).png").createNewFile()

        val result = uniqueFile(tempDir, "photo.png")
        assertEquals(File(tempDir, "photo (2).png"), result)
    }

    @Test
    fun `handles files with no extension`() {
        File(tempDir, "README").createNewFile()

        val result = uniqueFile(tempDir, "README")
        assertEquals(File(tempDir, "README (1)"), result)
    }

    @Test
    fun `handles files with multiple dots in name`() {
        File(tempDir, "archive.tar.gz").createNewFile()

        val result = uniqueFile(tempDir, "archive.tar.gz")
        assertEquals(File(tempDir, "archive.tar (1).gz"), result)
    }

    @Test
    fun `returns first available slot when several copies exist`() {
        File(tempDir, "log.txt").createNewFile()
        File(tempDir, "log (1).txt").createNewFile()
        File(tempDir, "log (2).txt").createNewFile()

        val result = uniqueFile(tempDir, "log.txt")
        assertEquals(File(tempDir, "log (3).txt"), result)
    }

    @Test
    fun `returns original when empty directory`() {
        val result = uniqueFile(tempDir, "newfile.zip")
        assertEquals(File(tempDir, "newfile.zip"), result)
    }
}
