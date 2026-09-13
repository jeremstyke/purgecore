namespace FreeDiskAnalyzer.Services;

/// <summary>
/// Generates Windows' own battery health report (design capacity vs current
/// full-charge capacity, usage history) rather than reimplementing battery
/// diagnostics. PurgeCore just triggers it and opens the result, the same
/// report Windows itself would produce via "powercfg /batteryreport".
/// </summary>
public interface IBatteryReportService
{
    /// <summary>True if this device reports having a battery at all, so the feature can stay hidden on desktop PCs.</summary>
    Task<bool> HasBatteryAsync(CancellationToken cancellationToken = default);

    /// <summary>Generates the report to a temp file and returns its path, or null on failure.</summary>
    Task<string?> GenerateReportAsync(CancellationToken cancellationToken = default);
}
