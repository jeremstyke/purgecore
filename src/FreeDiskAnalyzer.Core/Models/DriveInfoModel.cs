namespace FreeDiskAnalyzer.Core.Models;

/// <summary>
/// A ready, available drive as shown on the Dashboard.
/// </summary>
public sealed record DriveInfoModel(
    string Name,
    string RootPath,
    long TotalBytes,
    long FreeBytes,
    string DriveFormat,
    string DriveType)
{
    public long UsedBytes => TotalBytes - FreeBytes;

    public double UsedPercentage => TotalBytes == 0
        ? 0
        : Math.Round(UsedBytes / (double)TotalBytes * 100, 1);

    /// <summary>
    /// A single readable label for this drive, instead of showing the same
    /// path twice. Most drives (especially C:) have no custom volume label,
    /// Name is the Windows volume label ("Windows", "Data") when the drive
    /// has one, empty otherwise, so falling back to just the path avoids a
    /// redundant "C:\ - C:\" style display.
    /// </summary>
    public string DisplayName => string.IsNullOrWhiteSpace(Name)
        ? RootPath
        : $"{Name} ({RootPath})";
}
