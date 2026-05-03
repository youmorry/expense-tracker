import { useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router";

import { ConfirmDialog } from "@/components/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Spinner } from "@/components/ui/spinner";

import { clearToken } from "../../../lib/auth";
import { useDeleteAccount } from "../api/useDeleteAccount";
import { useUser } from "../api/useUser";
import { CurrencySelector } from "./CurrencySelector";

export default function SettingsPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { data: user, isLoading, isError } = useUser();
  const deleteAccount = useDeleteAccount();
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const clearSessionAndRedirectToLogin = useCallback((): void => {
    clearToken();
    queryClient.clear();
    void navigate("/login", { replace: true });
  }, [navigate, queryClient]);

  // useUser が失敗するのは主にトークン期限切れ（401）。AuthGuard はマウント時にしか
  // トークンを検査しないため、ここでクリアしてログイン画面へ送らないと
  // スピナー表示のまま画面から抜けられなくなる。
  useEffect(() => {
    if (isError) {
      clearSessionAndRedirectToLogin();
    }
  }, [isError, clearSessionAndRedirectToLogin]);

  const handleConfirmDelete = (): void => {
    deleteAccount.mutate(undefined, {
      onSuccess: clearSessionAndRedirectToLogin,
    });
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

      <section aria-label="Preferences" className="mb-4">
        <Card>
          <CardHeader>
            <h2 className="text-base font-semibold">Preferences</h2>
          </CardHeader>
          <CardContent>
            <CurrencySelector />
          </CardContent>
        </Card>
      </section>

      <section aria-label="Danger Zone">
        <Card>
          <CardHeader>
            <h2 className="text-base font-semibold">Danger Zone</h2>
          </CardHeader>
          <CardContent className="flex flex-col gap-2">
            <Button variant="outline" onClick={clearSessionAndRedirectToLogin}>
              Log out
            </Button>
            <Button
              variant="destructive"
              onClick={() => {
                setIsDeleteDialogOpen(true);
              }}
            >
              Delete Account
            </Button>
          </CardContent>
        </Card>
      </section>

      <ConfirmDialog
        open={isDeleteDialogOpen}
        title="Delete account?"
        message="This will permanently delete your account and all transactions. This action cannot be undone."
        confirmLabel="Delete"
        cancelLabel="Cancel"
        variant="destructive"
        onConfirm={handleConfirmDelete}
        onCancel={() => {
          setIsDeleteDialogOpen(false);
        }}
      />
    </div>
  );
}
