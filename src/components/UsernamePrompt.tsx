import { useState } from "react";
import { User } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { supabase } from "@/integrations/supabase/client";
import { toast } from "sonner";

interface UsernamePromptProps {
  open: boolean;
  onComplete: (username: string) => void;
}

const UsernamePrompt = ({ open, onComplete }: UsernamePromptProps) => {
  const [username, setUsername] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const validateUsername = (value: string): string | null => {
    if (value.length < 3) {
      return "Username must be at least 3 characters";
    }
    if (value.length > 20) {
      return "Username must be less than 20 characters";
    }
    if (!/^[a-zA-Z0-9_]+$/.test(value)) {
      return "Username can only contain letters, numbers, and underscores";
    }
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const validationError = validateUsername(username);
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const { data: { user } } = await supabase.auth.getUser();
      if (!user) {
        throw new Error("Not authenticated");
      }

      // Check if username is already taken
      const { data: existing } = await supabase
        .from("profiles")
        .select("id")
        .eq("username", username.toLowerCase())
        .maybeSingle();

      if (existing) {
        setError("Username is already taken");
        setLoading(false);
        return;
      }

      // Update the user's profile with the new username
      const { error: updateError } = await supabase
        .from("profiles")
        .update({ username: username.toLowerCase() })
        .eq("id", user.id);

      if (updateError) {
        throw updateError;
      }

      toast.success("Username set successfully!");
      onComplete(username.toLowerCase());
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to set username";
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={() => {}}>
      <DialogContent className="bg-popover border-border max-w-sm" onPointerDownOutside={(e) => e.preventDefault()}>
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-foreground">
            <User className="text-primary" size={20} />
            Create Your Username
          </DialogTitle>
          <DialogDescription>
            Choose a username to protect your email. This will be shown in activity logs and notifications instead of your email.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              placeholder="e.g., john_doe"
              value={username}
              onChange={(e) => {
                setUsername(e.target.value);
                setError("");
              }}
              className="bg-secondary border-border"
              autoFocus
            />
            {error && (
              <p className="text-sm text-destructive">{error}</p>
            )}
            <p className="text-xs text-muted-foreground">
              3-20 characters, letters, numbers, and underscores only
            </p>
          </div>

          <Button 
            type="submit" 
            className="w-full bg-primary text-primary-foreground hover:bg-primary/90"
            disabled={loading || !username.trim()}
          >
            {loading ? "Setting..." : "Set Username"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default UsernamePrompt;
