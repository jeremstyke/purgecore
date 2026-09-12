using System.ComponentModel;
using System.IO;
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
            foreach (var file in Directory.EnumerateFiles(path, "*", SearchOption.AllDirectories))
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
    /// </summary>
    private static bool IsFileLocked(string path)
    {
        try
        {
            using var stream = File.Open(path, FileMode.Open, FileAccess.Read, FileShare.None);
            return false;
        }
        catch (IOException)
        {
            return true;
        }
        catch (Exception ex) when (ex is UnauthorizedAccessException or Win32Exception)
        {
            // Can't tell for sure, but also can't safely proceed, treat as
            // locked rather than risk the native dialog.
            return true;
        }
    }
}
