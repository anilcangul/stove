package com.trendyol.stove.testing.e2e.elasticsearch

import arrow.core.*
import co.elastic.clients.elasticsearch.ElasticsearchClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.trendyol.stove.testing.e2e.containers.*
import com.trendyol.stove.testing.e2e.database.migrations.*
import com.trendyol.stove.testing.e2e.serialization.StoveObjectMapper
import com.trendyol.stove.testing.e2e.system.abstractions.*
import com.trendyol.stove.testing.e2e.system.annotations.StoveDsl
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.testcontainers.elasticsearch.ElasticsearchContainer
import kotlin.time.Duration.Companion.minutes

@StoveDsl
data class ElasticsearchSystemOptions(
  val defaultIndex: DefaultIndex,
  val clientConfigurer: ElasticClientConfigurer = ElasticClientConfigurer(),
  val container: ElasticContainerOptions = ElasticContainerOptions(),
  val objectMapper: ObjectMapper = StoveObjectMapper.Default,
  override val configureExposedConfiguration: (ElasticSearchExposedConfiguration) -> List<String> = { _ -> listOf() }
) : SystemOptions, ConfiguresExposedConfiguration<ElasticSearchExposedConfiguration> {
  internal val migrationCollection: MigrationCollection<ElasticsearchClient> = MigrationCollection()

  /**
   * Helps for registering migrations before the tests run.
   * @see MigrationCollection
   * @see DatabaseMigration
   */
  fun migrations(migration: MigrationCollection<ElasticsearchClient>.() -> Unit): ElasticsearchSystemOptions =
    migration(
      migrationCollection
    ).let {
      this
    }
}

data class ElasticSearchExposedConfiguration(
  val host: String,
  val port: Int,
  val password: String,
  val certificate: Option<ElasticsearchExposedCertificate>
) : ExposedConfiguration

data class ElasticsearchContext(
  val index: String,
  val container: ElasticsearchContainer,
  val options: ElasticsearchSystemOptions
)

data class ElasticContainerOptions(
  override val registry: String = "docker.elastic.co/",
  override val tag: String = "8.6.1",
  override val image: String = "elasticsearch/elasticsearch",
  override val compatibleSubstitute: String? = null,
  val exposedPorts: List<Int> = listOf(DEFAULT_ELASTIC_PORT),
  val password: String = "password",
  val disableSecurity: Boolean = true,
  override val containerFn: ContainerFn<ElasticsearchContainer> = {}
) : ContainerOptions {
  companion object {
    const val DEFAULT_ELASTIC_PORT = 9200
  }
}

data class ElasticClientConfigurer(
  val httpClientBuilder: HttpAsyncClientBuilder.() -> Unit = {
    setDefaultRequestConfig(
      RequestConfig.custom()
        .setSocketTimeout(5.minutes.inWholeMilliseconds.toInt())
        .setConnectTimeout(5.minutes.inWholeMilliseconds.toInt())
        .setConnectionRequestTimeout(5.minutes.inWholeMilliseconds.toInt())
        .build()
    )
  },
  val restClientOverrideFn: Option<(cfg: ElasticSearchExposedConfiguration) -> RestClient> = none()
)
