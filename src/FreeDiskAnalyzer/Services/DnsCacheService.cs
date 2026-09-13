using System.Diagnostics;

namespace FreeDiskAnalyzer.Services;

public sealed class DnsCacheService : IDnsCacheService
{
    public async Task<bool> FlushAsync(CancellationToken cancellationToken = default)
    {
        try
        {
            // Not redirecting stdout/stderr: never read here, and doing so
            // without draining the pipe risks a deadlock if output ever
            // filled the OS buffer while this awaits WaitForExitAsync.
            var startInfo = new ProcessStartInfo("ipconfig", "/flushdns")
            {
                UseShellExecute = false,
                CreateNoWindow = true
            };

            using var process = Process.Start(startInfo);
            if (process is null) return false;

            await process.WaitForExitAsync(cancellationToken);
            return process.ExitCode == 0;
        }
        catch (Exception ex) when (ex is InvalidOperationException or System.ComponentModel.Win32Exception)
        {
            return false;
        }
    }
}
