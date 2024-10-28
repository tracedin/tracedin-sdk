package io.github.tracedin.exporter.dto;

import io.github.tracedin.exporter.grpc.ServiceMetricsProto;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record MetricRequest(
        String name,
        String description,
        String unit,
        String type,
        Double value,     // GAUGE 및 SUM 타입의 단일 값
        Long count,       // HISTOGRAM 타입의 카운트
        Double sum,       // HISTOGRAM 타입의 합계
        Double min,       // HISTOGRAM 타입의 최소값
        Double max,       // HISTOGRAM 타입의 최대값
        Map<String, String> attributes
) {
    public static MetricRequest from(MetricData metricData) {
        String name = metricData.getName();
        String description = metricData.getDescription();
        String unit = metricData.getUnit();
        String type = metricData.getType().name();
        Map<String, String> attributes = metricData.getResource().getAttributes().asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getKey(),
                        entry -> entry.getValue().toString()
                ));

        Double value = null;
        Long count = null;
        Double sum = null;
        Double min = null;
        Double max = null;

        switch (metricData.getType()) {
            case LONG_GAUGE:
                value =  metricData.getLongGaugeData().getPoints().stream()
                        .mapToLong(LongPointData::getValue)
                        .average()
                        .orElse(0L);
                break;
            case DOUBLE_GAUGE:
                value = metricData.getDoubleGaugeData().getPoints().stream()
                        .mapToDouble(DoublePointData::getValue)
                        .average()
                        .orElse(0.0);
                break;
            case LONG_SUM:
                value = (double) metricData.getLongSumData().getPoints().stream()
                        .mapToLong(LongPointData::getValue)
                        .sum();
                break;
            case DOUBLE_SUM:
                value = metricData.getDoubleSumData().getPoints().stream()
                        .mapToDouble(DoublePointData::getValue)
                        .sum();
                break;
            case HISTOGRAM:
                var histogramData = metricData.getHistogramData().getPoints().stream().findFirst().orElse(null);
                if (histogramData != null) {
                    count = histogramData.getCount();
                    sum = histogramData.getSum();
                    min = histogramData.getMin();
                    max = histogramData.getMax();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported metric type: " + metricData.getType());
        }

        return new MetricRequest(
                name,
                description,
                unit,
                type,
                value,
                count,
                sum,
                min,
                max,
                attributes
        );
    }

    public ServiceMetricsProto.MetricRequest toGrpcMetric() {
        return ServiceMetricsProto.MetricRequest.newBuilder()
                .setName(Optional.ofNullable(name).orElse(""))
                .setDescription(Optional.ofNullable(description).orElse(""))
                .setUnit(Optional.ofNullable(unit).orElse(""))
                .setType(Optional.ofNullable(type).orElse(""))
                .setValue(Optional.ofNullable(value).orElse(0.0))
                .setCount(Optional.ofNullable(count).orElse(0L))
                .setSum(Optional.ofNullable(sum).orElse(0.0))
                .setMin(Optional.ofNullable(min).orElse(0.0))
                .setMax(Optional.ofNullable(max).orElse(0.0))
                .putAllAttributes(attributes)
                .build();
    }
}
