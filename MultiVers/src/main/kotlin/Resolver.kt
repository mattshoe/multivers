package io.github.mattshoe.shoebox

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.gradle.api.Project
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Resolver(
    private val project: Project
) {
    private val repositoryUrls by lazy { getRepositoryUrls(project) }

    suspend fun allAvailableVersions(group: String, artifact: String): List<String> {
        val versions = mutableListOf<String>()
        var pomFile: String? = null

        withContext(Dispatchers.IO) {
            repositoryUrls.forEach {
                yield()
                pomFile = downloadPomFile(it, group, artifact)

                if (pomFile != null)
                    return@forEach
            }
        }

        pomFile?.let {
            versions.addAll(
                parseVersionsFromMetadata(it)
            )
        }

        return versions
    }

    private suspend fun downloadPomFile(repoUrl: String, group: String, artifact: String): String? {
        val groupPath = group.replace('.', '/')
        val url = "$repoUrl/$groupPath/$artifact/maven-metadata.xml"

        return try {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.get(url)
            if (response.status.value == 200) {
                response.bodyAsText()
            } else {
                println("Failed to download POM file from $url: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            println("Failed to download POM file from $url: ${e.message}")
            null
        }
    }

    private suspend fun parseVersionsFromMetadata(metadataXml: String): List<String> = withContext(Dispatchers.Default) {
        val doc: Document = Jsoup.parse(metadataXml)
        return@withContext doc.select("versioning > versions > version").map { it.text() }
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