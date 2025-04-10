# Tracedin SDK for Java

![GitHub](https://img.shields.io/github/license/tracedin/tracedin-sdk)
![Maven Central](https://img.shields.io/maven-central/v/io.github.tracedin/tracedin-sdk)
## Setup

### Java Version

The current release of SDK Java supports Java 17+.

### Add a dependency on the Tracedin Java SDK to your project

If you're using Maven, then edit your project's "pom.xml" and add this to the `<dependencies>` section:

```xml
<dependency>
    <groupId>io.github.tracedin</groupId>
    <artifactId>tracedin-sdk</artifactId>
    <version>0.0.4</version>
</dependency>
```

If you are using Gradle, then edit your project's "build.gradle" and add this to the `dependencies` section:

```groovy
dependencies {
    // ...
    implementation 'io.github.tracedin:tracedin-sdk:0.0.4'
}
```

## Usage

```yaml
traced-in:
  service-name: tracedin-service
  base-package: com.univ.tracedinservice
  project-key: 1206887328-a7863a66-528e-4f37-b805-04e1314fb924
  sampling: 1
  metric-interval: 10
  host: localhost
```

### Configuration Property Description

| Property          | Description |
|------------------|-------------|
| `service-name`   | The name used to identify the service. It will be displayed as the service name on the APM dashboard. |
| `base-package`   | The root package containing the classes to be traced. Used for trace tracking. |
| `project-key`    | A unique key to distinguish your project. This key can be issued when creating a project in TracedIn. |
| `sampling`       | Sets the sampling rate for requests. For example, `1` collects all requests, `0.5` collects 50% of requests. |
| `metric-interval`| Interval (in seconds) at which metric data is sent to the APM server. For example, `10` means data is sent every 10 seconds. |
| `host`           | The **address of the main TracedIn APM server** that receives trace data. By default, it points to the main server, but **you can change it (e.g., to `localhost`) for local development or testing.** |
