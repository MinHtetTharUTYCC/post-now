/**
 * API Client Configuration and Initialization
 * This file sets up the generated OpenAPI client with authentication support
 * 
 * Usage in components:
 * import { apiClient } from '@/api/client';
 * 
 * const posts = await apiClient.posts.getAllPosts();
 */

import {
    Configuration,
    PostsApi,
    UsersApi,
    AuthApi,
    CommentsApi,
    LikesApi,
    NotificationApi,
    DefaultApi,
} from '@/generated/api';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8090/api';

/**
 * Get current Authentication Configuration
 * Reads token from localStorage
 */
const getAuthConfig = (): Configuration => {
    const token = localStorage.getItem('authToken');

    return new Configuration({
        basePath: API_BASE_URL,
        accessToken: token || undefined,
        headers: {
            'Content-Type': 'application/json',
        },
    });
};

/**
 * API Client - Use this to make API calls
 * 
 * Example:
 * const posts = await apiClient.posts.getAllPosts(0, 20);
 * const user = await apiClient.users.getCurrentUser();
 */
export const apiClient = {
    // Authentication endpoints (login, register, validate token)
    auth: new AuthApi(getAuthConfig()),

    // Posts endpoints (CRUD, search, images)
    posts: new PostsApi(getAuthConfig()),

    // Users endpoints (profile, search, follow info)
    users: new UsersApi(getAuthConfig()),

    // Comments endpoints
    comments: new CommentsApi(getAuthConfig()),

    // Likes endpoints
    likes: new LikesApi(getAuthConfig()),

    // Notifications endpoints
    notifications: new NotificationApi(getAuthConfig()),

    // Follow endpoints
    follow: new DefaultApi(getAuthConfig()),
};

/**
 * Update authentication token and refresh all API clients
 * Call this after successful login
 * 
 * @param token JWT token from login response
 */
export const setAuthToken = (token: string) => {
    localStorage.setItem('authToken', token);
    // Recreate configuration for all clients to pick up new token
    const newConfig = getAuthConfig();
    Object.keys(apiClient).forEach((key) => {
        const client = apiClient[key as keyof typeof apiClient];
        if (client && 'configuration' in client) {
            (client as any).configuration = newConfig;
        }
    });
};

/**
 * Clear authentication token
 * Call this on logout
 */
export const clearAuthToken = () => {
    localStorage.removeItem('authToken');
    // Clear token from all clients
    Object.keys(apiClient).forEach((key) => {
        const client = apiClient[key as keyof typeof apiClient];
        if (client && 'configuration' in client) {
            const config = (client as any).configuration as Configuration;
            config.accessToken = undefined;
        }
    });
};

/**
 * Get current stored token
 */
export const getAuthToken = (): string | null => {
    return localStorage.getItem('authToken');
};

export default apiClient;
