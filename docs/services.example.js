/**
 * auth.js - Servicio de Autenticación
 * Ejemplo de cómo manejar login, logout y almacenamiento de tokens
 */

import api from './AxiosConfig';

export const authService = {
  /**
   * Registrar nuevo usuario
   */
  register: async (userData) => {
    try {
      const response = await api.post('/api/auth/register', {
        email: userData.email,
        password: userData.password,
        name: userData.name,
        phone: userData.phone,
        address: userData.address,
        city: userData.city,
        state: userData.state,
        postalCode: userData.postalCode
      });
      return response.data.data;
    } catch (error) {
      console.error('Error en registro:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Login y obtener JWT tokens
   */
  login: async (email, password) => {
    try {
      const response = await api.post('/api/auth/login', {
        email,
        password
      });

      const { data } = response.data; // response.data.data contiene los tokens

      // IMPORTANTE: Guardar los tokens en localStorage
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      
      // Guardar datos del usuario
      localStorage.setItem('user', JSON.stringify({
        id: data.id,
        email: data.email,
        role: data.role,
        username: data.username,
        expiresIn: data.expiresIn
      }));

      console.log('Login exitoso:', data.email, 'Rol:', data.role);
      return data;
    } catch (error) {
      console.error('Error en login:', error.response?.data?.message || error.message);
      throw error;
    }
  },

  /**
   * Logout
   */
  logout: async () => {
    try {
      const accessToken = localStorage.getItem('accessToken');
      const refreshToken = localStorage.getItem('refreshToken');

      // Notificar al backend que estamos haciendo logout
      if (accessToken || refreshToken) {
        await api.post('/api/auth/logout', null, {
          params: {
            accessToken: accessToken || '',
            refreshToken: refreshToken || ''
          }
        });
      }

      // Limpiar localStorage
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      console.log('Logout exitoso');
      return true;
    } catch (error) {
      console.error('Error en logout:', error);
      // Aún así limpiar tokens locales
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      return false;
    }
  },

  /**
   * Verificar si el usuario está autenticado
   */
  isAuthenticated: () => {
    const token = localStorage.getItem('accessToken');
    return !!token;
  },

  /**
   * Obtener datos del usuario actual
   */
  getCurrentUser: () => {
    const userJson = localStorage.getItem('user');
    return userJson ? JSON.parse(userJson) : null;
  },

  /**
   * Cambiar contraseña
   */
  changePassword: async (userId, oldPassword, newPassword, confirmPassword) => {
    try {
      const response = await api.post('/api/auth/change-password', 
        {
          oldPassword,
          newPassword,
          confirmPassword
        },
        {
          params: { userId }
        }
      );
      return response.data;
    } catch (error) {
      console.error('Error cambiando contraseña:', error.response?.data?.message);
      throw error;
    }
  }
};

/**
 * products.js - Servicio de Productos
 */

export const productService = {
  /**
   * Obtener todos los productos
   */
  getAll: async () => {
    try {
      const response = await api.get('/api/products');
      return response.data.data; // response.data.data contiene el array de productos
    } catch (error) {
      console.error('Error obteniendo productos:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Obtener un producto por ID
   */
  getById: async (id) => {
    try {
      const response = await api.get(`/api/products/${id}`);
      return response.data.data;
    } catch (error) {
      console.error('Error obteniendo producto:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Obtener productos con stock bajo (ADMIN)
   */
  getLowStock: async () => {
    try {
      const response = await api.get('/api/products/low-stock');
      return response.data.data;
    } catch (error) {
      console.error('Error obteniendo productos con stock bajo:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Obtener productos relacionados
   */
  getRelated: async (id, limit = 5) => {
    try {
      const response = await api.get(`/api/products/${id}/related`, {
        params: { limit }
      });
      return response.data.data;
    } catch (error) {
      console.error('Error obteniendo productos relacionados:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Crear nuevo producto (ADMIN)
   */
  create: async (productData) => {
    try {
      const response = await api.post('/api/products', {
        name: productData.name,
        description: productData.description,
        price: productData.price,
        costPrice: productData.costPrice,
        stock: productData.stock,
        minStock: productData.minStock,
        imageUrl: productData.imageUrl,
        categoryId: productData.categoryId,
        brandId: productData.brandId
      });
      return response.data.data;
    } catch (error) {
      console.error('Error creando producto:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Actualizar producto (ADMIN)
   */
  update: async (id, productData) => {
    try {
      const response = await api.put(`/api/products/${id}`, productData);
      return response.data.data;
    } catch (error) {
      console.error('Error actualizando producto:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Eliminar producto (ADMIN)
   */
  delete: async (id) => {
    try {
      const response = await api.delete(`/api/products/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error eliminando producto:', error.response?.data?.message);
      throw error;
    }
  }
};

/**
 * categories.js - Servicio de Categorías
 * 
 * NOTA: No hay endpoint específico de categorías en el backend actual.
 * Las categorías están asociadas a productos.
 * Para crear un endpoint de categorías, necesitarías:
 * 1. Crear una entidad Category (si no existe)
 * 2. Crear un repositorio CategoryRepository
 * 3. Crear un controlador CategoryController con endpoints:
 *    - GET /api/categories
 *    - POST /api/categories (ADMIN)
 *    - PUT /api/categories/{id} (ADMIN)
 *    - DELETE /api/categories/{id} (ADMIN)
 */

export const categoryService = {
  /**
   * PLACEHOLDER: Esta es una función de ejemplo
   * El backend no tiene endpoint de categorías aún
   */
  getAll: async () => {
    // TODO: Implementar endpoint /api/categories en el backend
    console.warn('Endpoint de categorías no disponible aún. Necesita ser creado en el backend.');
    throw new Error('Endpoint /api/categories no disponible');
  }
};

/**
 * inventory.js - Servicio de Inventario (ADMIN)
 */

export const inventoryService = {
  /**
   * Obtener movimientos de inventario
   */
  getMovements: async (productId) => {
    try {
      const response = await api.get(`/api/inventory/movements/${productId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error obteniendo movimientos:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Obtener stock disponible
   */
  getAvailableStock: async (productId) => {
    try {
      const response = await api.get(`/api/inventory/available-stock/${productId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error obteniendo stock:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Ajustar stock
   */
  adjustStock: async (productId, newQuantity, reason) => {
    try {
      const response = await api.post('/api/inventory/adjust', null, {
        params: {
          productId,
          newQuantity,
          reason
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error ajustando stock:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Agregar stock
   */
  addStock: async (productId, quantity, referenceType = 'MANUAL', notes = '') => {
    try {
      const response = await api.post('/api/inventory/add-stock', null, {
        params: {
          productId,
          quantity,
          referenceType,
          notes
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error agregando stock:', error.response?.data?.message);
      throw error;
    }
  },

  /**
   * Remover stock
   */
  removeStock: async (productId, quantity, reason) => {
    try {
      const response = await api.post('/api/inventory/remove-stock', null, {
        params: {
          productId,
          quantity,
          reason
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error removiendo stock:', error.response?.data?.message);
      throw error;
    }
  }
};

