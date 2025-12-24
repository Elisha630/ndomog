import { useState, useEffect, useRef } from "react";
import { Bell } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { supabase } from "@/integrations/supabase/client";
import { formatDistanceToNow } from "date-fns";

interface Notification {
  id: string;
  user_id: string | null;
  action_user_email: string;
  action: string;
  item_name: string;
  details: string | null;
  is_read: boolean;
  created_at: string;
}

// Notification sound as a base64 encoded short beep
const NOTIFICATION_SOUND = "data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodHfvHFMXYqy2vGwb0M8YIGWq62xvMjc8/z0287BxMvY4e/0+f/84s62ppSKfXByeIGHi4uIe3RwdYSXqLvJ2O35/PXm1r+nlIB0eH6Bjop+bV9bboGVqLnG1eX2/Pns2rqjjnlub3V/hYeBdGpka3qHkpulr7rE0d/t+f/+8+DCq5R/b2hsdn+JlaCpsLnCzdnm8fj8+/Xr4NTHvLSvq6uusbW5vsXM1d3l7PP3+fv7+Pb18/Dv7u7v7/Dw8fHy8/P09fX29/f4+Pn5+fr6+vr6+vr6+vr6+fn5+Pj39/b29fX09PPz8vLx8fDw8O/v7u7t7ezr6+rp6ejn5uXk4+Lg397d3NvZ2NbV09LQzszKyMbEwL68uri1s7CuqqellpCHf3ZtY1lRSUA3MC0qKCcmJygrLjM5QEhRWmRtdn+HkJqjrLO5vsLGycvMzMvJyMbEwr+9uri1sq+sqKSgm5aRjIeCfnh0cG1qaGZlZGVmaWxwdHl9goeKjpGTlpeYmZqam5ucnJ2dnZycm5uampqZmJiXlpWUk5KRkI+OjYyLiomIh4aFhIOCgYCAf359fHt6eXl4eHh4eHl5enp7fH1+f4CCg4SFhoeIiYqLjI2OkJGSlJWXmJmam5ydnp+goaKjpKWmp6ipqqutr7CxsrO0tba3uLm6u7y9vsDBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=";

const NotificationBell = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [open, setOpen] = useState(false);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    // Initialize audio element
    audioRef.current = new Audio(NOTIFICATION_SOUND);
    audioRef.current.volume = 0.5;

    fetchNotifications();
    const unsubscribe = subscribeToNotifications();

    return () => {
      unsubscribe();
    };
  }, []);

  // Auto-mark notifications as read when popover opens
  useEffect(() => {
    if (open && unreadCount > 0) {
      markAllAsRead();
    }
  }, [open]);

  const playNotificationSound = () => {
    if (audioRef.current) {
      audioRef.current.currentTime = 0;
      audioRef.current.play().catch((err) => {
        console.log("Could not play notification sound:", err);
      });
    }
  };

  const fetchNotifications = async () => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return;

    const { data, error } = await supabase
      .from("notifications")
      .select("*")
      .eq("user_id", user.id)
      .order("created_at", { ascending: false })
      .limit(20);

    if (!error && data) {
      setNotifications(data);
      setUnreadCount(data.filter((n) => !n.is_read).length);
    }
  };

  const subscribeToNotifications = () => {
    const channel = supabase
      .channel("notifications-changes")
      .on(
        "postgres_changes",
        { event: "INSERT", schema: "public", table: "notifications" },
        async (payload) => {
          const { data: { user } } = await supabase.auth.getUser();
          const newNotification = payload.new as Notification;
          
          // Only add if it's for the current user
          if (user && newNotification.user_id === user.id) {
            setNotifications((prev) => [newNotification, ...prev].slice(0, 20));
            setUnreadCount((prev) => prev + 1);
            playNotificationSound();
          }
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  };

  const markAllAsRead = async () => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return;

    await supabase
      .from("notifications")
      .update({ is_read: true })
      .eq("user_id", user.id)
      .eq("is_read", false);

    setNotifications((prev) => prev.map((n) => ({ ...n, is_read: true })));
    setUnreadCount(0);
  };

  const getActionColor = (action: string) => {
    switch (action) {
      case "added":
        return "text-success";
      case "removed":
        return "text-destructive";
      case "updated":
        return "text-primary";
      default:
        return "text-foreground";
    }
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon" className="relative text-muted-foreground hover:text-foreground">
          <Bell size={20} />
          {unreadCount > 0 && (
            <span className="absolute -top-1 -right-1 bg-destructive text-destructive-foreground text-xs w-5 h-5 rounded-full flex items-center justify-center">
              {unreadCount > 9 ? "9+" : unreadCount}
            </span>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-80 p-0 bg-popover border-border" align="end">
        <div className="flex items-center justify-between p-3 border-b border-border">
          <h4 className="font-semibold text-foreground">Notifications</h4>
        </div>
        <div className="max-h-80 overflow-y-auto">
          {notifications.length === 0 ? (
            <div className="p-4 text-center text-muted-foreground text-sm">
              No notifications yet
            </div>
          ) : (
            notifications.map((notification) => (
              <div
                key={notification.id}
                className={`p-3 border-b border-border last:border-0 ${
                  !notification.is_read ? "bg-secondary/50" : ""
                }`}
              >
                <p className="text-sm text-foreground">
                  <span className="font-medium">{notification.action_user_email}</span>{" "}
                  <span className={getActionColor(notification.action)}>
                    {notification.action}
                  </span>{" "}
                  <span className="font-medium">{notification.item_name}</span>
                </p>
                {notification.details && (
                  <p className="text-xs text-muted-foreground mt-1">
                    {notification.details}
                  </p>
                )}
                <p className="text-xs text-muted-foreground mt-1">
                  {formatDistanceToNow(new Date(notification.created_at), { addSuffix: true })}
                </p>
              </div>
            ))
          )}
        </div>
      </PopoverContent>
    </Popover>
  );
};

export default NotificationBell;