import { GoogleLogin } from "@react-oauth/google";
import { useState } from "react";
import { Navigate } from "react-router";

import { getToken } from "../../../lib/auth";
import { useGoogleLogin } from "../api/useGoogleLogin";

export default function LoginPage() {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const googleLogin = useGoogleLogin();

  if (getToken()) {
    return <Navigate to="/transactions" replace />;
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-sm text-center">
        <h1 className="mb-8 text-3xl font-bold text-gray-900">Expense Tracker</h1>

        <div className="flex justify-center">
          <GoogleLogin
            onSuccess={(response) => {
              if (response.credential) {
                setErrorMessage(null);
                googleLogin.mutate(response.credential);
              }
            }}
            onError={() => {
              setErrorMessage("Google認証に失敗しました。もう一度お試しください。");
            }}
            size="large"
            text="signin_with"
            shape="rectangular"
            width="300"
          />
        </div>

        {errorMessage && (
          <p role="alert" className="mt-4 text-sm text-red-600">
            {errorMessage}
          </p>
        )}

        {googleLogin.isError && (
          <p role="alert" className="mt-4 text-sm text-red-600">
            ログインに失敗しました。もう一度お試しください。
          </p>
        )}
      </div>
    </div>
  );
}
