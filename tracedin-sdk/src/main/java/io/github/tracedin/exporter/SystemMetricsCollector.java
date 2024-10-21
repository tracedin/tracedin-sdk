package io.github.tracedin.exporter;

import com.sun.management.OperatingSystemMXBean;
import io.github.tracedin.OpenTelemetryInitializer;
import io.github.tracedin.config.TracedInProperties;
import io.opentelemetry.api.metrics.Meter;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class SystemMetricsCollector {

    private final Meter meter;

    public SystemMetricsCollector(TracedInProperties properties) {
        this.meter = OpenTelemetryInitializer.getOpenTelemetry().getMeter(properties.getBasePackage());

        meter.gaugeBuilder("system.cpu.usage")
                .setDescription("Reports the CPU usage")
                .setUnit("percent")
                .buildWithCallback(measurement -> measurement.record(getCpuUsage()));

        meter.gaugeBuilder("system.memory.usage")
                .setDescription("Reports the memory usage")
                .setUnit("bytes")
                .buildWithCallback(measurement -> measurement.record(getMemoryUsage()));

        meter.gaugeBuilder("system.heap.usage")
                .setDescription("Reports the heap memory usage")
                .setUnit("percent")
                .buildWithCallback(measurement -> measurement.record(getHeapMemoryUsage()));

        meter.gaugeBuilder("system.swap.usage")
                .setDescription("Reports Swap Memory usage")
                .setUnit("percent")
                .buildWithCallback(measurement -> measurement.record(getSwapMemoryUsage()));

    }

    private double getCpuUsage() {
        double cpuLoad = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getCpuLoad();
        return cpuLoad * 100;
    }

    private double getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    private double getHeapMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

        long usedHeapMemory = heapMemoryUsage.getUsed();
        long maxHeapMemory = heapMemoryUsage.getMax();

        if (maxHeapMemory == 0) return 0;

        return (double) usedHeapMemory / maxHeapMemory * 100;
    }

    private double getSwapMemoryUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long totalSwap = osBean.getTotalSwapSpaceSize();
        long freeSwap = osBean.getFreeSwapSpaceSize();
        long usedSwap = totalSwap - freeSwap;

        return (double) usedSwap / totalSwap * 100;
    }
}
