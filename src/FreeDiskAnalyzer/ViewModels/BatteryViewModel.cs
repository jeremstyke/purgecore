using System.Diagnostics;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using FreeDiskAnalyzer.Services;

namespace FreeDiskAnalyzer.ViewModels;

public sealed partial class BatteryViewModel : ObservableObject
{
    private readonly IBatteryReportService _batteryReportService;

    [ObservableProperty]
    private bool hasBattery;

    [ObservableProperty]
    private bool isGeneratingReport;

    [ObservableProperty]
    private bool reportFailed;

    public BatteryViewModel(IBatteryReportService batteryReportService)
    {
        _batteryReportService = batteryReportService;
        _ = CheckBatteryAsync();
    }

    private async Task CheckBatteryAsync() => HasBattery = await _batteryReportService.HasBatteryAsync();

    [RelayCommand]
    private async Task GenerateReportAsync()
    {
        IsGeneratingReport = true;
        ReportFailed = false;

        try
        {
            var reportPath = await _batteryReportService.GenerateReportAsync();
            if (reportPath is not null)
            {
                Process.Start(new ProcessStartInfo(reportPath) { UseShellExecute = true });
            }
            else
            {
                ReportFailed = true;
            }
        }
        finally
        {
            IsGeneratingReport = false;
        }
    }
}
