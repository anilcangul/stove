plugins {}
dependencies {
  api(projects.lib.stoveTestingE2e)
  api(libs.elastic)
  api(libs.testcontainers.elasticsearch)
  implementation(libs.jackson.databind)
}

dependencies {
  testImplementation(libs.slf4j.simple)
}
