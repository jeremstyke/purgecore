using System.Diagnostics;
using System.IO;
using System.Management;

namespace FreeDiskAnalyzer.Services;

public sealed class BatteryReportService : IBatteryReportService
{
    public Task<bool> HasBatteryAsync(CancellationToken cancellationToken = default)
    {
        return Task.Run(() =>
        {
            try
            {
                using var searcher = new ManagementObjectSearcher("SELECT * FROM Win32_Battery");
                using var results = searcher.Get();
                return results.Count > 0;
            }
            catch (Exception ex) when (ex is ManagementException or UnauthorizedAccessException)
            {
                // Can't tell, default to not showing the feature rather than
                // showing something that might fail on a desktop with no battery.
                return false;
            }
        }, cancellationToken);
    }

    public async Task<string?> GenerateReportAsync(CancellationToken cancellationToken = default)
    {
        var outputPath = Path.Combine(Path.GetTempPath(), $"PurgeCore-BatteryReport-{Guid.NewGuid():N}.html");

        try
        {
            var startInfo = new ProcessStartInfo("powercfg", $"/batteryreport /output \"{outputPath}\"")
            {
                UseShellExecute = false,
                CreateNoWindow = true,
                RedirectStandardOutput = true,
                RedirectStandardError = true
            };

            using var process = Process.Start(startInfo);
            if (process is null) return null;

            await process.WaitForExitAsync(cancellationToken);

            return process.ExitCode == 0 && File.Exists(outputPath) ? outputPath : null;
        }
        catch (Exception ex) when (ex is InvalidOperationException or System.ComponentModel.Win32Exception or IOException)
        {
            return null;
        }
    }
}
