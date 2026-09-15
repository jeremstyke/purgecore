using System.Net.Http;
using System.Text.Json;
using FreeDiskAnalyzer.Core.Models;
using FreeDiskAnalyzer.Core.Utilities;

namespace FreeDiskAnalyzer.Core.Services;

public sealed class UpdateChecker : IUpdateChecker
{
    // This repository also hosts PurgeCore Mobile releases (tagged
    // mobile-v*.*.*), so /releases/latest can return a mobile release
    // instead of the Windows one if it happens to be more recent. The full
    // list is fetched instead, and the first entry NOT tagged mobile-* is
    // used, that's the actual latest Windows release.
    private const string ReleasesApiUrl = "https://api.github.com/repos/jeremstyke/purgecore/releases";
    private const string InstallerAssetName = "PurgeCore-Setup.exe";
    private const string MobileTagPrefix = "mobile-";

    public async Task<UpdateInfo> CheckForUpdateAsync(string currentVersion, CancellationToken cancellationToken = default)
    {
        try
        {
            using var client = new HttpClient { Timeout = TimeSpan.FromSeconds(10) };
            client.DefaultRequestHeaders.UserAgent.ParseAdd("FreeDiskAnalyzer-UpdateChecker");

            using var response = await client.GetAsync(ReleasesApiUrl, cancellationToken);
            if (!response.IsSuccessStatusCode)
            {
                return UpdateInfo.NoUpdate;
            }

            var stream = await response.Content.ReadAsStreamAsync(cancellationToken);
            using var doc = await JsonDocument.ParseAsync(stream, cancellationToken: cancellationToken);

            if (doc.RootElement.ValueKind != JsonValueKind.Array)
            {
                return UpdateInfo.NoUpdate;
            }

            JsonElement? windowsRelease = null;
            foreach (var release in doc.RootElement.EnumerateArray())
            {
                if (release.TryGetProperty("tag_name", out var tagProp) &&
                    tagProp.GetString() is { } tag &&
                    !tag.StartsWith(MobileTagPrefix, StringComparison.OrdinalIgnoreCase))
                {
                    windowsRelease = release;
                    break;
                }
            }

            if (windowsRelease is not { } root)
            {
                return UpdateInfo.NoUpdate;
            }

            if (!root.TryGetProperty("tag_name", out var tagNameProp))
            {
                return UpdateInfo.NoUpdate;
            }

            var tagName = tagNameProp.GetString();
            if (string.IsNullOrWhiteSpace(tagName))
            {
                return UpdateInfo.NoUpdate;
            }

            var releaseUrl = root.TryGetProperty("html_url", out var htmlUrlProp) ? htmlUrlProp.GetString() : null;

            string? downloadUrl = null;
            if (root.TryGetProperty("assets", out var assets))
            {
                foreach (var asset in assets.EnumerateArray())
                {
                    if (asset.TryGetProperty("name", out var nameProp) &&
                        nameProp.GetString() == InstallerAssetName &&
                        asset.TryGetProperty("browser_download_url", out var urlProp))
                    {
                        downloadUrl = urlProp.GetString();
                        break;
                    }
                }
            }

            if (downloadUrl is null)
            {
                return UpdateInfo.NoUpdate;
            }

            var latestVersion = tagName.TrimStart('v', 'V');
            var isNewer = ReleaseVersionComparer.IsNewer(latestVersion, currentVersion);

            return new UpdateInfo(isNewer, latestVersion, downloadUrl, releaseUrl);
        }
        catch (Exception ex) when (ex is HttpRequestException or TaskCanceledException or JsonException)
        {
            // No internet, GitHub unreachable, unexpected response shape:
            // fail quietly rather than bother the user about it.
            return UpdateInfo.NoUpdate;
        }
    }
}
