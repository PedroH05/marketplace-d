import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token_desapego');

  if (token && !tokenExpirado(token)) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(authReq);
  }

  if (token) {
    localStorage.removeItem('token_desapego');
  }

  return next(req);
};

function tokenExpirado(token: string): boolean {
  try {
    const payloadBase64 = token.split('.')[1];
    const payloadNormalizado = payloadBase64
      .replace(/-/g, '+')
      .replace(/_/g, '/')
      .padEnd(Math.ceil(payloadBase64.length / 4) * 4, '=');

    const payload = JSON.parse(atob(payloadNormalizado));
    return payload.exp ? Date.now() >= payload.exp * 1000 : false;
  } catch {
    return true;
  }
}
