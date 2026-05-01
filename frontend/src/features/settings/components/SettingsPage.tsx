import { useNavigate } from "react-router";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Spinner } from "@/components/ui/spinner";

import { clearToken } from "../../../lib/auth";
import { useUser } from "../api/useUser";

export default function SettingsPage() {
  const navigate = useNavigate();
  const { data: user, isLoading } = useUser();

  const handleLogout = (): void => {
    clearToken();
    void navigate("/login", { replace: true });
  };

  return (
    <div className="min-h-screen px-4 py-4">
      <h1 className="font-heading mb-4 text-xl font-semibold">Settings</h1>

      <section aria-label="Account" className="mb-4">
        <Card>
          <CardHeader>
            <h2 className="text-base font-semibold">Account</h2>
          </CardHeader>
          <CardContent>
            {isLoading || !user ? (
              <div className="flex justify-center py-4">
                <Spinner aria-label="Loading account" />
              </div>
            ) : (
              <dl className="flex flex-col gap-3">
                <div>
                  <dt className="text-muted-foreground text-xs">Email</dt>
                  <dd className="text-foreground text-sm">{user.email}</dd>
                </div>
                <div>
                  <dt className="text-muted-foreground text-xs">Display Name</dt>
                  <dd className="text-foreground text-sm">{user.displayName}</dd>
                </div>
              </dl>
            )}
          </CardContent>
        </Card>
      </section>

      <section aria-label="Danger Zone">
        <Card>
          <CardHeader>
            <h2 className="text-base font-semibold">Danger Zone</h2>
          </CardHeader>
          <CardContent>
            <Button variant="outline" onClick={handleLogout}>
              Log out
            </Button>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
