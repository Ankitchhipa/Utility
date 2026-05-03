package com.itl.commonres.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min


class ApkInstaller(private val context: Context) {

    private val TAG = "ApkInstaller"

    fun installApkFromBundle(bundleFile: File, outputDir: File) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Starting APK installation process in coroutine...")
            try {
                val apkFiles = extractBundle(bundleFile, outputDir)
                //val apkFiles = findApkFiles(tempDir)
                installApks(apkFiles)
            } catch (e: IOException) {
                Log.e(TAG, "Error installing APKs from zip", e)
            } finally {
                deleteRecursive(outputDir)
            }
        }
    }

    fun installApkFromFolder(folderPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Starting APK installation process in coroutine...")
            var folder: File? = null
            try {
                folder = File(folderPath)
                val apkFiles = findApkFiles(folder)
                installApks(apkFiles)
            } catch (e: IOException) {
                Log.e(TAG, "Error installing APKs from zip", e)
            }
        }
    }

    private fun findApkFiles(directory: File): List<File> {
        val apkFiles = mutableListOf<File>()
        directory.walk().forEach { file ->
            if (file.isFile && file.name.endsWith(".apk")) {
                apkFiles.add(file)
            }
        }
        return apkFiles
    }

    fun installApks(apkFiles: List<File>) {
        Log.d(TAG, "Starting APK installation process...")

        if (apkFiles.isEmpty()) {
            Log.e(TAG, "No APK files provided for installation.")
            return
        }

        val packageInstaller = context.packageManager.packageInstaller
        var sessionId = -1
        try {
            val params =
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            params.setInstallLocation(PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY) // Optional: Set install location
            sessionId = packageInstaller.createSession(params)
            Log.d(TAG, "Installation session created with ID: $sessionId")

            val session = packageInstaller.openSession(sessionId)

            // Write APKs to the session
            apkFiles.forEachIndexed { index, apkFile ->
                val apkName = "split$index"
                Log.d(TAG, "Writing APK: ${apkFile.absolutePath} to session as $apkName")
                writeApkToSession(session, apkFile, apkName)
            }

            // Commit the session
            val intent = Intent(context, InstallationReceiver::class.java)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            val statusReceiver = pendingIntent.intentSender
            session.commit(statusReceiver)
            Log.d(TAG, "Installation session committed.")

        } catch (e: Exception) {
            Log.e(TAG, "Error during APK installation (Session ID: $sessionId)", e)
            // Clean up the session if an error occurs
            if (sessionId != -1) {
                packageInstaller.abandonSession(sessionId)
                Log.w(TAG, "Installation session abandoned due to error.")
            }
        }
    }

    private fun writeApkToSession(
        session: PackageInstaller.Session,
        apkFile: File,
        apkName: String
    ) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val length = apkFile.length()
            outputStream = session.openWrite(apkName, 0, length)
            inputStream = FileInputStream(apkFile)
            inputStream.copyTo(outputStream)
            session.fsync(outputStream)
        } catch (e: IOException) {
            Log.e(TAG, "Error writing APK: ${apkFile.absolutePath} to session", e)
            throw e // Re-throw the exception to be handled in the main try-catch
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach {
                deleteRecursive(it)
            }
        }
        fileOrDirectory.delete()
    }

    private fun extractBundle(inputBundle: File?, outputDir: File): List<File> {
        val extractedFiles: MutableList<File> = ArrayList()

        try {
            val dis = DataInputStream(FileInputStream(inputBundle))
            // Read the number of files
            val numFiles = dis.readInt()

            // Read metadata and file data for each file
            for (i in 0 until numFiles) {
                // Read file name
                val fileName = dis.readUTF()

                // Validate file name
                if (fileName == null || fileName.isEmpty()) {
                    println("Invalid file name: $fileName");
                    continue; // Skip this file
                }


                // Read file size
                val fileSize = dis.readLong()

                // Create the output file
                val outputFile = File(outputDir, fileName)
                println(outputFile.isFile)

                // Log the file path
                println("Extracting file: $fileName")
                println("Output file path: " + outputFile.absolutePath)

                try {
                    val fos = FileOutputStream(outputFile, true)
                    val buffer = ByteArray(4096)
                    var remaining = fileSize
                    while (remaining > 0) {
                        val bytesRead = dis.read(
                            buffer, 0,
                            min(buffer.size.toDouble(), remaining.toDouble()).toInt()
                        )
                        fos.write(buffer, 0, bytesRead)
                        remaining -= bytesRead.toLong()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                extractedFiles.add(outputFile)
                println("File extracted: " + outputFile.absolutePath)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return extractedFiles
    }
}