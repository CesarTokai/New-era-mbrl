import axios from "axios";
import Swal from "sweetalert2";

const instance = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

const ToastError = (title, message) =>
  Swal.fire({ icon: "error", title, text: message });

const ToastWarning = (title, message) =>
  Swal.fire({ icon: "warning", title, text: message });

const ToastSuccess = (title, message) =>
  Swal.fire({ icon: "success", title, text: message, timer: 2000, showConfirmButton: false });

// ✅ REQUEST INTERCEPTOR - Agregar token a TODAS las peticiones
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`✅ TOKEN ENVIADO: ${config.method.toUpperCase()} ${config.url}`);
    } else {
      console.warn(`⚠️  SIN TOKEN: ${config.method.toUpperCase()} ${config.url}`);
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ✅ RESPONSE INTERCEPTOR - Manejo de errores
instance.interceptors.response.use(
  (response) => {
    console.log(`✅ RESPUESTA ${response.status}: ${response.config.url}`);
    return response;
  },
  (error) => {
    if (!error.response) {
      console.error("❌ SIN RESPUESTA DEL SERVIDOR:", error.message);
      ToastError("Error de conexión", "No se pudo conectar con el servidor.");
      return Promise.reject(error);
    }

    const { status } = error.response;
    const url = error.config?.url || '';
    
    console.error(`❌ ERROR ${status}: ${url}`);

    // 🔐 CASO 401: Token inválido, expirado o no enviado
    if (status === 401) {
      // ✅ IMPORTANTE: NO redirigir si estamos en endpoints de AUTH
      // (login, register, forgot-password, reset-password)
      const isAuthEndpoint = url.includes('/api/auth/');
      
      if (isAuthEndpoint) {
        console.warn("⚠️  Error 401 en endpoint de auth - devolviendo error sin redirigir");
        return Promise.reject(error);
      }
      
      // ❌ Si llegamos aquí, el token expiró o es inválido en un endpoint PROTEGIDO
      console.error("❌ TOKEN EXPIRADO - LIMPIANDO Y REDIRIGIENDO A LOGIN");
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");
      
      ToastError("Sesión expirada", "Tu token ha expirado. Por favor, inicia sesión de nuevo.");
      
      // Redirige después de mostrar el toast
      setTimeout(() => {
        window.location.href = "/login";
      }, 1500);
      
      return Promise.reject(error);
    }

    // 🚫 CASO 403: Acceso denegado (permisos insuficientes)
    if (status === 403) {
      ToastWarning("Acceso denegado", "No tienes permisos para realizar esta acción.");
    } 
    // ❌ CASO 400: Datos inválidos
    else if (status === 400) {
      const errorData = error.response.data;
      if (errorData && typeof errorData === "object") {
        const firstKey = Object.keys(errorData)[0];
        const firstValue = errorData[firstKey];
        ToastWarning("Datos incorrectos", Array.isArray(firstValue) ? firstValue[0] : String(firstValue));
      } else {
        ToastWarning("Datos incorrectos", "Verifica la información ingresada.");
      }
    } 
    // 🔍 CASO 404: No encontrado
    else if (status === 404) {
      ToastWarning("No encontrado", "El recurso solicitado no existe.");
    } 
    // 💥 CASO 500: Error del servidor
    else if (status === 500) {
      ToastError("Error del servidor", "Ocurrió un error en el servidor. Inténtalo más tarde.");
    }

    return Promise.reject(error);
  }
);

export default {
  async doDelete(url, data) {
    return await instance.delete(url, { data });
  },
  async doPost(url, data, config = {}) {
    return await instance.post(url, data, config);
  },
  async doGet(url, config = {}) {
    return await instance.get(url, config);
  },
  async doPut(url, data, config = {}) {
    return await instance.put(url, data, config);
  },
  async doPostFile(url, data) {
    return await instance.post(url, data, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  async doPutFile(url, data) {
    return await instance.put(url, data, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  ToastSuccess,
  ToastError,
  ToastWarning,
};

