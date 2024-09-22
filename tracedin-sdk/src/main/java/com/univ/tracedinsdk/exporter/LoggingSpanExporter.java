package com.univ.tracedinsdk.exporter;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSpanExporter implements SpanExporter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSpanExporter.class);

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        for (SpanData span : spans) {
            // 스팬 데이터를 로그로 출력
            logger.info("Exporting span: {}", spanToString(span));
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        // 필요 시 flush 로직 구현
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        // 필요 시 shutdown 로직 구현
        return CompletableResultCode.ofSuccess();
    }

    private String spanToString(SpanData spanData) {
        // 스팬 데이터를 문자열로 변환하여 로그에 출력
        // 필요한 필드만 선택적으로 포함할 수 있습니다.
        StringBuilder sb = new StringBuilder();
        sb.append("Span {")
                .append("TraceId=").append(spanData.getTraceId()).append(", ")
                .append("SpanId=").append(spanData.getSpanId()).append(", ")
                .append("ParentSpanId=").append(spanData.getParentSpanId()).append(", ")
                .append("Name=").append(spanData.getName()).append(", ")
                .append("Kind=").append(spanData.getKind()).append(", ")
                .append("StartEpochNanos=").append(spanData.getStartEpochNanos()).append(", ")
                .append("EndEpochNanos=").append(spanData.getEndEpochNanos()).append(", ")
                .append("Attributes=").append(spanData.getAttributes())
                .append("}");
        return sb.toString();
    }

}
