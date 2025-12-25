import { Activity, User, Plus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import Logo from "@/components/Logo";
import NotificationBell from "@/components/NotificationBell";

interface HeaderProps {
  showActivity: boolean;
  setShowActivity: (show: boolean) => void;
  onAddItem: () => void;
}

const Header = ({ showActivity, setShowActivity, onAddItem }: HeaderProps) => {
  const navigate = useNavigate();

  return (
    <header className="flex items-center justify-between px-4 py-3 border-b border-border">
      <div className="flex items-center gap-3">
        <Logo size="sm" />
        <span className="font-semibold text-lg text-foreground">Ndomog Investment</span>
      </div>

      <nav className="flex items-center gap-2">
        <NotificationBell />

        <button
          onClick={() => setShowActivity(!showActivity)}
          className={`nav-button ${showActivity ? 'nav-button-active' : 'nav-button-default'}`}
        >
          <Activity size={18} />
          <span className="hidden sm:inline">Activity</span>
        </button>

        <Button onClick={onAddItem} size="sm" className="nav-button-primary">
          <Plus size={18} />
          <span className="hidden sm:inline">Add Item</span>
        </Button>

        <button onClick={() => navigate("/profile")} className="nav-button nav-button-default">
          <User size={18} />
        </button>
      </nav>
    </header>
  );
};

export default Header;
