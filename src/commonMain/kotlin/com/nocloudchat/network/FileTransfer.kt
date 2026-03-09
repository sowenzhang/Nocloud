package com.nocloudchat.network

import kotlinx.coroutines.*
import java.io.*
import java.net.*

private const val BUFFER_SIZE = 65_536          // 64 KB chunks
private const val ACCEPT_TIMEOUT_MS = 60_000   // 1 min for receiver to accept

/**
 * Handles file transfer over a dedicated TCP connection.
 *
 * Protocol: sender opens a ServerSocket, sends the port in the FILE_OFFER message.
 * Receiver connects to that port. First 8 bytes = file size (long), then raw bytes.
 */
class FileTransfer(private val scope: CoroutineScope) {

    /**
     * Opens a one-shot TCP server for the file.
     * Returns the port immediately; actual transfer runs in the background.
     */
    fun prepareSend(
        transferId: String,
        file: File,
        onProgress: (id: String, bytes: Long) -> Unit,
        onComplete: (id: String, localPath: String) -> Unit,
        onFailed: (id: String, error: String) -> Unit,
    ): Int {
        val serverSocket = ServerSocket(0)
        val port = serverSocket.localPort

        scope.launch(Dispatchers.IO) {
            try {
                serverSocket.soTimeout = ACCEPT_TIMEOUT_MS
                val socket = serverSocket.accept()
                serverSocket.close()

                socket.use {
                    val out = DataOutputStream(BufferedOutputStream(it.getOutputStream()))
                    out.writeLong(file.length())   // header: total size
                    val buf = ByteArray(BUFFER_SIZE)
                    var sent = 0L
                    FileInputStream(file).buffered(BUFFER_SIZE).use { fin ->
                        var n: Int
                        while (fin.read(buf).also { n = it } >= 0) {
                            out.write(buf, 0, n)
                            sent += n
                            onProgress(transferId, sent)
                        }
                    }
                    out.flush()
                }
                onComplete(transferId, file.absolutePath)
            } catch (e: Exception) {
                onFailed(transferId, e.message ?: "Send failed")
            } finally {
                try { serverSocket.close() } catch (_: Exception) {}
            }
        }

        return port
    }

    /** Connects to the sender and downloads the file into destDir. */
    fun receive(
        transferId: String,
        senderIp: String,
        senderPort: Int,
        filename: String,
        destDir: File,
        onProgress: (id: String, bytes: Long) -> Unit,
        onComplete: (id: String, localPath: String) -> Unit,
        onFailed: (id: String, error: String) -> Unit,
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                destDir.mkdirs()
                val destFile = uniqueFile(destDir, filename)

                Socket().use { socket ->
                    socket.connect(InetSocketAddress(senderIp, senderPort), 15_000)
                    val din = DataInputStream(BufferedInputStream(socket.getInputStream()))
                    val totalSize = din.readLong()

                    FileOutputStream(destFile).buffered(BUFFER_SIZE).use { fout ->
                        val buf = ByteArray(BUFFER_SIZE)
                        var received = 0L
                        while (received < totalSize) {
                            val toRead = minOf(BUFFER_SIZE.toLong(), totalSize - received).toInt()
                            val n = din.read(buf, 0, toRead)
                            if (n < 0) break
                            fout.write(buf, 0, n)
                            received += n
                            onProgress(transferId, received)
                        }
                    }
                }
                onComplete(transferId, destFile.absolutePath)
            } catch (e: Exception) {
                onFailed(transferId, e.message ?: "Receive failed")
            }
        }
    }

    /** Returns a File that doesn't already exist, appending (1), (2) etc. as needed. */
    private fun uniqueFile(dir: File, filename: String): File {
        val candidate = File(dir, filename)
        if (!candidate.exists()) return candidate

        val dot = filename.lastIndexOf('.')
        val base = if (dot >= 0) filename.substring(0, dot) else filename
        val ext  = if (dot >= 0) filename.substring(dot) else ""
        var n = 1
        while (true) {
            val f = File(dir, "$base ($n)$ext")
            if (!f.exists()) return f
            n++
        }
    }
}
