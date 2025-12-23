import { Package } from "lucide-react";

const Logo = ({ size = "md" }: { size?: "sm" | "md" | "lg" }) => {
  const sizeClasses = {
    sm: "w-8 h-8",
    md: "w-12 h-12",
    lg: "w-16 h-16",
  };

  const iconSizes = {
    sm: 16,
    md: 24,
    lg: 32,
  };

  return (
    <div className={`${sizeClasses[size]} bg-primary rounded-xl flex items-center justify-center`}>
      <Package size={iconSizes[size]} className="text-primary-foreground" />
    </div>
  );
};

export default Logo;
