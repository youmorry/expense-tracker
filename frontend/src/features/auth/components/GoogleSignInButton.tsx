import { GoogleLogin, type CredentialResponse } from "@react-oauth/google";

interface GoogleSignInButtonProps {
  onSuccess: (response: CredentialResponse) => void;
  onError: () => void;
}

// E2E (`vite build --mode e2e`) では実 GSI iframe が Google サーバーの origin 検証で
// hidden にされ、ボタンを描画できない。E2E で OAuth フロー自体は MSW が
// `/api/v1/auth/google` を返すため、ここでは credential を素通しするスタブに差し替える。
export function GoogleSignInButton({ onSuccess, onError }: GoogleSignInButtonProps) {
  if (import.meta.env.MODE === "e2e") {
    return (
      <button
        type="button"
        onClick={() => {
          onSuccess({ credential: "e2e-fake-credential", select_by: "btn" });
        }}
        className="bg-primary text-primary-foreground hover:bg-primary/90 inline-flex h-10 items-center justify-center rounded-md px-4 text-sm font-medium"
      >
        Sign in with Google
      </button>
    );
  }

  return (
    <GoogleLogin
      onSuccess={onSuccess}
      onError={onError}
      size="large"
      text="signin_with"
      shape="rectangular"
      width="300"
    />
  );
}
