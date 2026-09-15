using System.ComponentModel;
using System.IO;
using System.Threading;
using FreeDiskAnalyzer.Core.Utilities;
using Microsoft.VisualBasic.FileIO;

namespace FreeDiskAnalyzer.Services;

public sealed class SafeDeleteService : ISafeDeleteService
{
    public bool TryDeleteFile(string path)
    {
        if (PathSafetyGuard.IsProtected(path)) return false;

        try
        {
            if (!File.Exists(path)) return true;

            // Microsoft.VisualBasic.FileIO's recycle-bin delete can only be
            // told to show error dialogs or not, there's no fully-silent
            // option, so a locked file (very common for Temp files while a
            // browser or another program is running) pops up a native
            // Windows dialog and blocks the whole cleanup waiting for a
            // click. Checking the lock ourselves first and skipping quietly
            // avoids that entirely.
            if (IsFileLocked(path)) return false;

            FileSystem.DeleteFile(path, UIOption.OnlyErrorDialogs, RecycleOption.SendToRecycleBin, UICancelOption.DoNothing);
            return true;
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException or Win32Exception or OperationCanceledException)
        {
            return false;
        }
    }

    public bool TryDeleteDirectory(string path)
    {
        if (PathSafetyGuard.IsProtected(path)) return false;

        try
        {
            if (!Directory.Exists(path)) return true;

            // Same reasoning as TryDeleteFile: if any file inside is locked,
            // deleting the whole folder through the recycle-bin API can
            // still trigger that native dialog partway through. Checking
            // every file first means we either delete the whole folder
            // cleanly or skip it, never get stuck showing a popup.
            foreach (var file in Directory.EnumerateFiles(path, "*", System.IO.SearchOption.AllDirectories))
            {
                if (IsFileLocked(file)) return false;
            }

            FileSystem.DeleteDirectory(path, UIOption.OnlyErrorDialogs, RecycleOption.SendToRecycleBin, UICancelOption.DoNothing);
            return true;
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException or Win32Exception or OperationCanceledException)
        {
            return false;
        }
    }

    /// <summary>
    /// Tries to open the file with no sharing allowed, the same test
    /// Windows itself would do. If another process has it open, this throws
    /// immediately instead of blocking, which is exactly what we want here:
    /// a quick, silent yes/no answer.
    ///
    /// A single attempt can misfire: Windows Search indexing, antivirus
    /// real-time scanning, or a browser that just closed but hasn't
    /// released its SQLite journal file yet can all hold a brief lock with
    /// nothing actually "open" from the user's point of view. A genuinely
    /// running browser holds its files locked continuously, a transient
    /// scan does not, so a couple of short retries tell them apart without
    /// meaningfully slowing down a scan that's actually clear.
    /// </summary>
    private static bool IsFileLocked(string path)
    {
        const int maxAttempts = 3;
        for (var attempt = 1; attempt <= maxAttempts; attempt++)
        {
            try
            {
                using var stream = File.Open(path, FileMode.Open, FileAccess.Read, FileShare.None);
                return false;
            }
            catch (IOException)
            {
                if (attempt == maxAttempts) return true;
                Thread.Sleep(150);
            }
            catch (Exception ex) when (ex is UnauthorizedAccessException or Win32Exception)
            {
                // Can't tell for sure, but also can't safely proceed, treat as
                // locked rather than risk the native dialog.
                return true;
            }
        }
        return true;
    }
}
