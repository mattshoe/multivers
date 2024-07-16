package io.github.mattshoe.shoebox

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.net.URI

class DependencyInspector {

    fun allAvailableVersions(
        project: Project,
        group: String,
        artifact: String
    ): List<String> {
        val versions = mutableListOf<String>()
        var pomFile: String? = null

        getRepositoryUrls(project).forEach {
            pomFile = loadMetaDataFile(it, group, artifact)

            if (pomFile != null)
                return@forEach
        }

        pomFile?.let {
            versions.addAll(
                parseVersionsFromMetadata(it)
            )
        }

        return versions
    }

    private fun isWebUrl(url: String): Boolean {
        return try {
            val uri = URI(url)
            uri.scheme == "http" || uri.scheme == "https"
        } catch (e: Exception) {
            false
        }
    }

    private fun loadMetaDataFile(repoUrl: String, group: String, artifact: String): String? {
        return if (isWebUrl(repoUrl))
            downloadMetaDataFileFromWeb(repoUrl, group, artifact)
        else
            downloadMetaDataFileLocal(repoUrl, group, artifact)
    }

    private fun downloadMetaDataFileFromWeb(repoUrl: String, group: String, artifact: String): String? {
        val groupPath = group.replace('.', '/')

        val url = buildString {
            append(repoUrl.removeSuffix("/"))
            append("/$groupPath")
            append("/$artifact")
            append("/maven-metadata.xml")
        }

        return try {
            runBlocking {
                val client = HttpClient(CIO)
                val response: HttpResponse = client.get(url)
                if (response.status.value == 200) {
                    response.bodyAsText()
                } else {
                    println("Failed to download POM file from $url: ${response.status.value}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Failed to download POM file from $url: ${e.message}")
            null
        }
    }

    private fun downloadMetaDataFileLocal(repoUrl: String, group: String, artifact: String): String? {
        val groupPath = group.replace('.', '/')

        val url = buildString {
            append(repoUrl.removeSuffix("/"))
            append("/$groupPath")
            append("/$artifact")
            append("/maven-metadata-local.xml")
        }

        return try {
            val file = File(URI(url))
            if (file.exists()) {
                file.readText()
            } else {
                println("File not found at $url")
                null
            }
        } catch (e: Throwable) {
            println(e)
            null
        }
    }

    private fun parseVersionsFromMetadata(metadataXml: String): List<String> {
        val doc: Document = Jsoup.parse(metadataXml)
        return doc.select("versioning > versions > version").map { it.text() }
    }

    private fun getRepositoryUrls(project: Project): List<String> {
        return project.repositories.mapNotNull { repository ->
            when (repository) {
                is org.gradle.api.artifacts.repositories.MavenArtifactRepository -> repository.url.toString()
                else -> null
            }
        }
    }
}