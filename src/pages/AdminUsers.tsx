import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, UserCheck, UserX, Shield, Mail, Calendar, CheckCircle } from "lucide-react";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { format } from "date-fns";

interface UserProfile {
  id: string;
  email: string;
  username: string | null;
  created_at: string;
  admin_verified: boolean;
  verified_by: string | null;
  verified_at: string | null;
  email_confirmed_at: string | null;
}

const AdminUsers = () => {
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);
  const [verifyingUser, setVerifyingUser] = useState<string | null>(null);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    checkAdminAndFetch();
  }, []);

  const checkAdminAndFetch = async () => {
    const { data: { session } } = await supabase.auth.getSession();
    if (!session) {
      toast({
        title: "Access denied",
        description: "Please sign in to access this page",
        variant: "destructive",
      });
      navigate("/auth");
      return;
    }

    const { data: roleData } = await supabase
      .from("user_roles")
      .select("role")
      .eq("user_id", session.user.id)
      .eq("role", "admin")
      .maybeSingle();

    if (!roleData) {
      toast({
        title: "Access denied",
        description: "You don't have permission to access this page",
        variant: "destructive",
      });
      navigate("/");
      return;
    }

    setIsAdmin(true);
    fetchUsers();
  };

  const fetchUsers = async () => {
    setLoading(true);
    try {
      // Fetch profiles with admin_verified status
      const { data: profiles, error } = await supabase
        .from("profiles")
        .select("id, email, username, created_at, admin_verified, verified_by, verified_at")
        .order("created_at", { ascending: false });

      if (error) throw error;

      // We can't directly access auth.users, so we'll show what we have
      // Email confirmation status would need to be inferred from login ability
      const usersWithStatus: UserProfile[] = (profiles || []).map(p => ({
        ...p,
        email_confirmed_at: null, // We can't access this directly
      }));

      setUsers(usersWithStatus);
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyUser = async (userId: string) => {
    setVerifyingUser(userId);
    try {
      const { error } = await supabase.rpc("admin_verify_user", {
        _target_user_id: userId,
      });

      if (error) throw error;

      toast({
        title: "User verified!",
        description: "The user can now access the inventory.",
      });

      fetchUsers();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setVerifyingUser(null);
    }
  };

  const handleRevokeVerification = async (userId: string) => {
    setVerifyingUser(userId);
    try {
      const { error } = await supabase
        .from("profiles")
        .update({
          admin_verified: false,
          verified_by: null,
          verified_at: null,
        })
        .eq("id", userId);

      if (error) throw error;

      toast({
        title: "Verification revoked",
        description: "The user's admin verification has been removed.",
      });

      fetchUsers();
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    } finally {
      setVerifyingUser(null);
    }
  };

  if (!isAdmin) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="animate-pulse text-muted-foreground">Checking permissions...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container max-w-4xl mx-auto p-4 space-y-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-foreground">User Management</h1>
            <p className="text-muted-foreground">Verify user accounts manually</p>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5" />
              User Accounts
            </CardTitle>
            <CardDescription>
              Admin verification allows users to access the inventory even if they haven't verified their email.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <div className="animate-pulse text-muted-foreground">Loading users...</div>
              </div>
            ) : users.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                No users found
              </div>
            ) : (
              <div className="space-y-4">
                {users.map((user) => (
                  <div
                    key={user.id}
                    className="flex items-center justify-between p-4 rounded-lg border bg-card"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <Mail className="h-4 w-4 text-muted-foreground" />
                        <span className="font-medium">{user.email}</span>
                        {user.username && (
                          <span className="text-muted-foreground">({user.username})</span>
                        )}
                      </div>
                      <div className="flex items-center gap-4 text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Joined {format(new Date(user.created_at), "MMM d, yyyy")}
                        </div>
                        {user.verified_at && (
                          <div className="flex items-center gap-1">
                            <CheckCircle className="h-3 w-3 text-green-500" />
                            Verified {format(new Date(user.verified_at), "MMM d, yyyy")}
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      {user.admin_verified ? (
                        <>
                          <Badge variant="default" className="bg-green-500/20 text-green-600 border-green-500/30">
                            <UserCheck className="h-3 w-3 mr-1" />
                            Verified
                          </Badge>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleRevokeVerification(user.id)}
                            disabled={verifyingUser === user.id}
                          >
                            <UserX className="h-4 w-4 mr-1" />
                            Revoke
                          </Button>
                        </>
                      ) : (
                        <>
                          <Badge variant="secondary" className="text-muted-foreground">
                            <UserX className="h-3 w-3 mr-1" />
                            Not Verified
                          </Badge>
                          <Button
                            variant="default"
                            size="sm"
                            onClick={() => handleVerifyUser(user.id)}
                            disabled={verifyingUser === user.id}
                          >
                            <UserCheck className="h-4 w-4 mr-1" />
                            {verifyingUser === user.id ? "Verifying..." : "Verify"}
                          </Button>
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminUsers;
