import { useState } from "react";
import { Navigate } from "react-router";

import { Card, CardContent, CardHeader } from "@/components/ui/card";

import { getToken } from "../../../lib/auth";
import { useGoogleLogin } from "../api/useGoogleLogin";
import { GoogleSignInButton } from "./GoogleSignInButton";

export default function LoginPage() {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const googleLogin = useGoogleLogin();

  if (getToken()) {
    return <Navigate to="/transactions" replace />;
  }

  return (
    <div className="bg-muted flex min-h-screen items-center justify-center px-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="text-center">
          <h1 className="font-heading text-2xl leading-snug font-medium">Expense Tracker</h1>
        </CardHeader>
        <CardContent className="flex flex-col items-center gap-4">
          <GoogleSignInButton
            onSuccess={(response) => {
              if (response.credential) {
                setErrorMessage(null);
                googleLogin.mutate(response.credential);
              }
            }}
            onError={() => {
              setErrorMessage("Google認証に失敗しました。もう一度お試しください。");
            }}
          />

          {errorMessage && (
            <p role="alert" className="text-destructive text-sm">
              {errorMessage}
            </p>
          )}

          {googleLogin.isError && (
            <p role="alert" className="text-destructive text-sm">
              ログインに失敗しました。もう一度お試しください。
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
