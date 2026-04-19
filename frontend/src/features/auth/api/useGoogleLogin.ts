import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router";

import { apiClient } from "../../../lib/api/client";
import { setToken } from "../../../lib/auth";
import { AuthResponseSchema } from "../../../types/api";

export function useGoogleLogin() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: async (idToken: string) => {
      const data = await apiClient.post("/api/v1/auth/google", { idToken }, { skipAuth: true });
      return AuthResponseSchema.parse(data);
    },
    onSuccess: (data) => {
      setToken(data.accessToken);
      navigate("/transactions", { replace: true });
    },
  });
}
