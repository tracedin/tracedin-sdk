package io.github.tracedin.exporter.grpc;

import io.github.tracedin.exporter.grpc.ServiceMetricsGrpcAppenderGrpc.ServiceMetricsGrpcAppenderBlockingStub;
import io.github.tracedin.exporter.grpc.ServiceMetricsProto.AppendServiceMetricsRequest;
import io.github.tracedin.exporter.grpc.ServiceMetricsProto.AppendServiceMetricsResponse;
import io.github.tracedin.exporter.grpc.SpanGrpcAppenderGrpc.SpanGrpcAppenderBlockingStub;
import io.github.tracedin.exporter.grpc.SpanProto.AppendSpanResponse;
import io.github.tracedin.exporter.grpc.SpanProto.AppendSpansRequest;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GrpcClient {

    private final SpanGrpcAppenderBlockingStub spanBlockingStub;
    private final ServiceMetricsGrpcAppenderBlockingStub serviceMetricsBlockingStub;

    public GrpcClient(String host) {
        log.info("Connecting to gRPC server at {}", host);
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, 443)
                .useTransportSecurity()
                .build();

        spanBlockingStub = SpanGrpcAppenderGrpc.newBlockingStub(channel);
        serviceMetricsBlockingStub = ServiceMetricsGrpcAppenderGrpc.newBlockingStub(channel);
    }

    public AppendSpanResponse appendSpans(AppendSpansRequest request) {
        return spanBlockingStub.appendSpans(request);
    }

    public AppendServiceMetricsResponse appendServiceMetrics(AppendServiceMetricsRequest request) {
        return serviceMetricsBlockingStub.appendServiceMetrics(request);
    }

}
