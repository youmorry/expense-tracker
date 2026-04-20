type Period = { from: string; to: string } | null;

interface PeriodSelectorProps {
  onChange: (period: Period) => void;
}

export function PeriodSelector(props: PeriodSelectorProps) {
  void props;
  return null;
}
