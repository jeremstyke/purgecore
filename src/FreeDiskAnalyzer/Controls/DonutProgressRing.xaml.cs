using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Shapes;

namespace FreeDiskAnalyzer.Controls;

/// <summary>One colored slice of the ring, sized by its share of TotalBytes.</summary>
public readonly record struct RingSegment(long Bytes, Brush Color);

/// <summary>
/// Multi-colored used/free donut ring. Each segment is its own Ellipse,
/// drawn with a StrokeDashArray sized to just that segment's share and
/// rotated to start exactly where the previous segment ended, the same
/// "fake an arc" trick as the original single-color version, just stacked
/// per category instead of a single accent-colored arc.
/// </summary>
public partial class DonutProgressRing : UserControl
{
    private const double Diameter = 140;
    private const double RingStrokeThickness = 14;

    public static readonly DependencyProperty UsedPercentageProperty = DependencyProperty.Register(
        nameof(UsedPercentage),
        typeof(double),
        typeof(DonutProgressRing),
        new PropertyMetadata(0.0, OnVisualPropertyChanged));

    public double UsedPercentage
    {
        get => (double)GetValue(UsedPercentageProperty);
        set => SetValue(UsedPercentageProperty, value);
    }

    public static readonly DependencyProperty SegmentsProperty = DependencyProperty.Register(
        nameof(Segments),
        typeof(IReadOnlyList<RingSegment>),
        typeof(DonutProgressRing),
        new PropertyMetadata(null, OnVisualPropertyChanged));

    /// <summary>
    /// Optional. When set (and non-empty), the ring is drawn as one colored
    /// arc per segment instead of a single accent-colored arc, segments are
    /// sized relative to the sum of every segment's Bytes, not to
    /// UsedPercentage, so this should add up to the same used space that
    /// UsedPercentage represents, or the two will visually disagree.
    /// </summary>
    public IReadOnlyList<RingSegment>? Segments
    {
        get => (IReadOnlyList<RingSegment>?)GetValue(SegmentsProperty);
        set => SetValue(SegmentsProperty, value);
    }

    public DonutProgressRing()
    {
        InitializeComponent();
        Loaded += (_, _) => UpdateVisual();
    }

    private static void OnVisualPropertyChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        if (d is DonutProgressRing ring && ring.IsLoaded)
        {
            ring.UpdateVisual();
        }
    }

    private void UpdateVisual()
    {
        PercentageText.Text = $"{Math.Clamp(UsedPercentage, 0, 100):0.#}%";

        SegmentsCanvas.Children.Clear();

        var segments = Segments;
        var totalBytes = segments?.Sum(s => s.Bytes) ?? 0;

        if (segments is null || segments.Count == 0 || totalBytes <= 0)
        {
            // No category data, same single accent-colored arc as before.
            var percentage = Math.Clamp(UsedPercentage, 0, 100);
            AddArc(AccentBrush, startAngle: 0, sweepFraction: percentage / 100.0);
            return;
        }

        double startAngle = 0;
        foreach (var segment in segments)
        {
            if (segment.Bytes <= 0) continue;
            var fraction = segment.Bytes / (double)totalBytes;
            AddArc(segment.Color, startAngle, fraction);
            startAngle += fraction * 360.0;
        }
    }

    private static Brush AccentBrush => (Brush)Application.Current.Resources["AccentBrush"];

    /// <summary>
    /// One Ellipse per arc, rotated so it starts at startAngle degrees
    /// clockwise from the top, with a StrokeDashArray covering exactly
    /// sweepFraction of the circle and a gap for the rest, the classic WPF
    /// "fake an arc with a dashed circle" approach, just applied once per
    /// segment instead of once for the whole ring.
    /// </summary>
    private void AddArc(Brush color, double startAngle, double sweepFraction)
    {
        sweepFraction = Math.Clamp(sweepFraction, 0, 1);
        if (sweepFraction <= 0) return;

        var circumference = Math.PI * Diameter;
        var totalDashUnits = circumference / RingStrokeThickness;
        var arcUnits = totalDashUnits * sweepFraction;
        var gapUnits = Math.Max(totalDashUnits - arcUnits, 0);

        var ellipse = new Ellipse
        {
            Width = Diameter,
            Height = Diameter,
            StrokeThickness = RingStrokeThickness,
            Stroke = color,
            StrokeStartLineCap = PenLineCap.Round,
            StrokeEndLineCap = PenLineCap.Round,
            StrokeDashArray = new DoubleCollection { arcUnits, gapUnits },
            HorizontalAlignment = HorizontalAlignment.Center,
            VerticalAlignment = VerticalAlignment.Center,
            RenderTransformOrigin = new Point(0.5, 0.5),
            RenderTransform = new RotateTransform(startAngle - 90)
        };

        SegmentsCanvas.Children.Add(ellipse);
    }
}
