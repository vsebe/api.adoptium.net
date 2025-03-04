package net.adoptopenjdk.api

import kotlinx.coroutines.runBlocking
import net.adoptopenjdk.api.testDoubles.InMemoryInternalDbStore
import net.adoptopenjdk.api.v3.AdoptRepositoryImpl
import net.adoptopenjdk.api.v3.V3Updater
import net.adoptopenjdk.api.v3.dataSources.DefaultUpdaterHtmlClient
import net.adoptopenjdk.api.v3.dataSources.HttpClientFactory
import net.adoptopenjdk.api.v3.dataSources.UpdaterHtmlClient
import net.adoptopenjdk.api.v3.dataSources.github.CachedGitHubHtmlClient
import net.adoptopenjdk.api.v3.dataSources.github.GitHubApi
import net.adoptopenjdk.api.v3.dataSources.github.GitHubHtmlClient
import net.adoptopenjdk.api.v3.dataSources.github.graphql.GraphQLGitHubClient
import net.adoptopenjdk.api.v3.dataSources.github.graphql.clients.GraphQLGitHubReleaseClient
import net.adoptopenjdk.api.v3.dataSources.github.graphql.clients.GraphQLGitHubRepositoryClient
import net.adoptopenjdk.api.v3.dataSources.github.graphql.clients.GraphQLGitHubSummaryClient
import net.adoptopenjdk.api.v3.dataSources.github.graphql.clients.GraphQLRequest
import net.adoptopenjdk.api.v3.dataSources.github.graphql.clients.GraphQLRequestImpl
import net.adoptopenjdk.api.v3.mapping.adopt.AdoptBinaryMapper
import net.adoptopenjdk.api.v3.mapping.adopt.AdoptReleaseMapperFactory
import org.awaitility.Awaitility
import org.jboss.weld.junit5.auto.AddPackages
import org.junit.Ignore
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@Ignore("For manual execution")
@AddPackages(value = [V3Updater::class, UpdaterHtmlClient::class])
class UpdateRunner {

    @Test
    @Ignore("For manual execution")
    fun run(updater: V3Updater) {
        System.clearProperty("GITHUB_TOKEN")
        updater.run(false)
        Awaitility.await().atMost(Long.MAX_VALUE, TimeUnit.NANOSECONDS).until({ 4 == 5 })
    }

    @Test
    @Ignore("For manual execution")
    fun runSingleUpdate() {
        runBlocking {

            val httpClientFactory = HttpClientFactory()
            val graphQLRequest: GraphQLRequest = GraphQLRequestImpl()
            val updaterHtmlClient: UpdaterHtmlClient = DefaultUpdaterHtmlClient(
                httpClientFactory.getHttpClient(),
                httpClientFactory.getNonRedirectHttpClient()
            )
            val client: GitHubApi = GraphQLGitHubClient(
                GraphQLGitHubSummaryClient(
                    graphQLRequest,
                    updaterHtmlClient
                ),
                GraphQLGitHubReleaseClient(
                    graphQLRequest,
                    updaterHtmlClient
                ),
                GraphQLGitHubRepositoryClient(
                    graphQLRequest,
                    updaterHtmlClient
                )
            )
            val gitHubHtmlClient: GitHubHtmlClient = CachedGitHubHtmlClient(
                InMemoryInternalDbStore(),
                updaterHtmlClient
            )

            val repo = AdoptRepositoryImpl(client, AdoptReleaseMapperFactory(AdoptBinaryMapper(gitHubHtmlClient), gitHubHtmlClient))
            repo.getRelease(8)
        }
    }
}
