namespace FreeDiskAnalyzer.Core.Models;

/// <summary>Result of checking GitHub Releases for a newer version.</summary>
public sealed record UpdateInfo(bool IsUpdateAvailable, string? LatestVersion, string? DownloadUrl, string? ReleaseUrl)
{
    public static UpdateInfo NoUpdate { get; } = new(false, null, null, null);

    /// <summary>
    /// The website's release notes article for this version, so someone
    /// updating can see what's actually changing first, not just download
    /// blind. Follows the site's naming convention (dots to dashes, e.g.
    /// "1.9.1" -> "v1-9-1-release-notes.html"), matches every article
    /// published so far but isn't guaranteed to exist for every release.
    /// </summary>
    public string? BlogArticleUrl => LatestVersion is { } version
        ? $"https://jeremstyke.github.io/purgecore/blog/v{version.Replace('.', '-')}-release-notes.html"
        : null;
}
