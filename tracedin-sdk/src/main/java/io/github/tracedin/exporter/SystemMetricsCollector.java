package io.github.tracedin.exporter;

import com.sun.management.OperatingSystemMXBean;
import io.github.tracedin.OpenTelemetryInitializer;
import io.github.tracedin.config.TracedInProperties;
import io.opentelemetry.api.metrics.Meter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class SystemMetricsCollector {

    private final Meter meter;

    public SystemMetricsCollector(TracedInProperties properties) {
        this.meter = OpenTelemetryInitializer.getOpenTelemetry().getMeter(properties.getBasePackage());

        // CPU 사용률 게이지 추가
        meter.gaugeBuilder("system.cpu.usage")
                .setDescription("Reports the CPU usage")
                .setUnit("percent")
                .buildWithCallback(result -> {
                    double cpuUsage = getCpuUsage();  // CPU 사용률 가져오기
                    result.record(cpuUsage);
                });

        // 메모리 사용률 게이지 추가
        meter.gaugeBuilder("system.memory.usage")
                .setDescription("Reports the memory usage")
                .setUnit("bytes")
                .buildWithCallback(result -> {
                    long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    result.record(memoryUsage);
                });

        // GC 횟수 측정
        meter.counterBuilder("jvm.gc.count")
                .setDescription("Counts the number of garbage collection events")
                .setUnit("1")
                .buildWithCallback(result -> {
                    long gcCount = getGcCount();  // GC 횟수 가져오기
                    result.record(gcCount);
                });

        if (isUbuntu()) {
            meter.gaugeBuilder("system.io.usage")
                    .setDescription("Reports the I/O usage")
                    .setUnit("bytes")
                    .buildWithCallback(result -> {
                        double ioUsage = getIoUsage();  // I/O 사용량 가져오기
                        result.record(ioUsage);
                    });
        }

    }

    // CPU 사용률 측정 로직 (플랫폼에 따라 달라질 수 있음)
    private double getCpuUsage() {
        double cpuLoad = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class).getCpuLoad();
        return cpuLoad * 100;
    }

    private long getGcCount() {
        long totalGcCount = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGcCount += gcBean.getCollectionCount();
        }
        return totalGcCount;
    }

    public double getIoUsage() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/diskstats"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("sda")) {  // 특정 디스크 장치에 대한 통계 (sda)
                    String[] tokens = line.trim().split("\\s+");
                    double readSectors = Double.parseDouble(tokens[5]);
                    double writeSectors = Double.parseDouble(tokens[9]);
                    return (readSectors + writeSectors) * 512 / (1024 * 1024);  // MB 단위로 변환
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Ubuntu 운영체제 확인 메서드
    private boolean isUbuntu() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("linux") && checkForUbuntu();
    }

    // Ubuntu 운영체제 세부 확인
    private boolean checkForUbuntu() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/etc/os-release"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("ubuntu")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
