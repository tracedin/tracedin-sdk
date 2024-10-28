package io.github.tracedin.exporter;

import com.sun.management.OperatingSystemMXBean;
import io.github.tracedin.OpenTelemetryInitializer;
import io.github.tracedin.config.TracedInProperties;
import io.opentelemetry.api.metrics.Meter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class SystemMetricsCollector {

    private long previousIoTime = 0;
    private long previousTotalTime = 0;

    private final Meter meter;

    public SystemMetricsCollector(TracedInProperties properties) {
        this.meter = OpenTelemetryInitializer.getOpenTelemetry().getMeter(properties.getBasePackage());

        meter.gaugeBuilder("system.cpu.usage")
                .setDescription("Reports the CPU usage")
                .setUnit("percent")
                .buildWithCallback(measurement -> measurement.record(getCpuUsage()));

        meter.gaugeBuilder("system.memory.usage")
                .setDescription("Reports the memory usage")
                .setUnit("percent")
                .buildWithCallback(measurement -> measurement.record(getMemoryUsage()));

        meter.gaugeBuilder("system.heap.usage")
                .setDescription("Reports the heap memory usage")
                .setUnit("percent")
                .buildWithCallback(measurement -> measurement.record(getHeapMemoryUsage()));

        meter.gaugeBuilder("system.swap.usage")
                .setDescription("Reports Swap Memory usage")
                .setUnit("percent")
                .buildWithCallback(measurement -> measurement.record(getSwapMemoryUsage()));

//        meter.gaugeBuilder("system.disk.usage")
//                .setDescription("Reports the disk usage percentage")
//                .setUnit("percent")
//                .buildWithCallback(result -> result.record(getDiskIOUsage()));

    }

    private double getCpuUsage() {
        double cpuLoad = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getCpuLoad();
        return cpuLoad * 100;
    }

    private double getMemoryUsage() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
        return ((double) usedMemory / totalMemory) * 100;
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

    private double getDiskIOUsage() {
        String diskName = "vda"; // 모니터링하려는 디스크 이름을 지정하세요.
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/diskstats"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(diskName)) {
                    String[] tokens = line.trim().split("\\s+");
                    long currentIoTime = Long.parseLong(tokens[13]);

                    long currentTotalTime = getSystemUptime() * 1000; // 시스템 업타임(ms)

                    // 이전 값이 0이면 초기화
                    if (previousIoTime == 0 && previousTotalTime == 0) {
                        previousIoTime = currentIoTime;
                        previousTotalTime = currentTotalTime;
                        return 0;
                    }

                    long ioTimeDiff = currentIoTime - previousIoTime;
                    long totalTimeDiff = currentTotalTime - previousTotalTime;

                    previousIoTime = currentIoTime;
                    previousTotalTime = currentTotalTime;

                    if (totalTimeDiff > 0) {
                        return ((double) ioTimeDiff / totalTimeDiff) * 100;
                    } else {
                        return 0;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getSystemUptime() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/uptime"))) {
            String line = reader.readLine();
            if (line != null) {
                String[] tokens = line.split("\\s+");
                double uptimeSeconds = Double.parseDouble(tokens[0]);
                return (long) uptimeSeconds;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
