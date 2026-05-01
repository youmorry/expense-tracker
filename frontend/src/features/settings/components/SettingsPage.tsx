import { useQueryClient } from "@tanstack/react-query";
import { useEffect } from "react";
import { useNavigate } from "react-router";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Spinner } from "@/components/ui/spinner";

import { clearToken } from "../../../lib/auth";
import { useUser } from "../api/useUser";

export default function SettingsPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { data: user, isLoading, isError } = useUser();

  // useUser が失敗するのは主にトークン期限切れ（401）。AuthGuard はマウント時にしか
  // トークンを検査しないため、ここでクリアしてログイン画面へ送らないと
  // スピナー表示のまま画面から抜けられなくなる。
  useEffect(() => {
    if (isError) {
      clearToken();
      queryClient.clear();
      void navigate("/login", { replace: true });
    }
  }, [isError, navigate, queryClient]);

  const handleLogout = (): void => {
    clearToken();
    queryClient.clear();
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
