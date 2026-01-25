import { getFormattedVersion } from "@/lib/version";

interface AppVersionProps {
  className?: string;
  showAppName?: boolean;
}

const AppVersion = ({ className = "", showAppName = true }: AppVersionProps) => {
  const version = getFormattedVersion();
  
  return (
    <p className={`text-xs text-muted-foreground text-center ${className}`}>
      {showAppName ? `Ndomog Investment ${version}` : version}
    </p>
  );
};

export default AppVersion;
