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

### 설정 항목 설명
| 설정 항목         | 설명 |
|------------------|------|
| `service-name`   | 해당 서비스를 식별하기 위한 이름입니다. APM 대시보드 상에서 서비스 이름으로 표시됩니다. |
| `base-package`   | 트레이싱할 대상 클래스들이 위치한 최상위 패키지 경로입니다. 트레이스 추적에 사용됩니다. |
| `project-key`    | 프로젝트를 구분하는 고유 키입니다. TrancedIn 프로젝트 생성 시 발급 받을 수 있습니다. |
| `sampling`       | 요청 샘플링 비율을 설정합니다. 예: `1`은 모든 요청을 수집, `0.5`는 50%의 요청만 수집합니다. |
| `metric-interval`| 메트릭 데이터를 APM 서버로 전송하는 주기(초)입니다. 예: `10`은 10초마다 전송됩니다. |
| `host`           | APM 데이터를 수집하는 **tracedIn 메인 서버의 주소**입니다. 기본값은 메인 서버로 설정되어 있으며, **로컬 환경에서 테스트할 경우 필요에 따라 `localhost` 등으로 변경**할 수 있습니다. |
