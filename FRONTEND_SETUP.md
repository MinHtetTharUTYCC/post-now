# Frontend Setup - OpenAPI TypeScript Client Generation

This guide sets up your React + TypeScript frontend to auto-generate type-safe API client from the backend.

## 📋 Prerequisites

- Node.js 16+ installed
- React project created: `npx create-react-app my-app --template typescript`
- Backend running on `http://localhost:8090`

---

## 🚀 Step 1: Install OpenAPI Generator CLI

```bash
npm install --save-dev @openapitools/openapi-generator-cli
```

---

## 🛠 Step 2: Create OpenAPI Configuration

Copy this content to **`openapi-generator-config.json`** in your project root:

```json
{
  "packageName": "@post-now/api-client",
  "packageVersion": "1.0.0",
  "apiDocumentationUrl": "http://localhost:8090/v3/api-docs",
  "supportsES6": true,
  "npmVersion": "6",
  "generateApiDocumentation": false
}
```

---

## 📦 Step 3: Add npm Script

Add this to your **`package.json`** scripts section:

```json
{
  "scripts": {
    "generate-api": "openapi-generator-cli generate -i http://localhost:8090/v3/api-docs -g typescript-fetch -o src/generated/api --config openapi-generator-config.json --skip-validate-spec",
    "generate-api:watch": "npm run generate-api -- --serve"
  }
}
```

---

## 📂 Step 4: Add .gitignore Entry

Add to your **`.gitignore`**:

```
# Generated API client
src/generated/
```

---

## 🎯 Step 5: Generate API Client

Run this command **once the backend is running**:

```bash
npm run generate-api
```

This creates:
```
src/generated/api/
├── models/              # All your DTOs (Post, User, Comment, etc)
├── apis/                # Controller API clients
├── index.ts             # Barrel export
├── configuration.ts     # Base configuration
└── .openapi-generator/
```

---

## 💻 Step 6: Create API Client Wrapper

Create **`src/api/client.ts`**:

```typescript
import {
  Configuration,
  PostsApi,
  UsersApi,
  AuthApi,
  CommentsApi,
  LikesApi,
  NotificationApi,
  DefaultApi as FollowApi
} from '@/generated/api';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8090/api';

const getAuthConfig = (): Configuration => {
  return new Configuration({
    basePath: API_BASE_URL,
    accessToken: () => localStorage.getItem('authToken') || '',
    headers: {
      'Content-Type': 'application/json',
    },
  });
};

export const apiClient = {
  posts: new PostsApi(getAuthConfig()),
  users: new UsersApi(getAuthConfig()),
  auth: new AuthApi(getAuthConfig()),
  comments: new CommentsApi(getAuthConfig()),
  likes: new LikesApi(getAuthConfig()),
  notifications: new NotificationApi(getAuthConfig()),
  follow: new FollowApi(getAuthConfig()),
};

// Helper to refresh auth on token change
export const updateAuthToken = (token: string) => {
  localStorage.setItem('authToken', token);
  // Recreate clients with new token
  Object.values(apiClient).forEach((api) => {
    if (api instanceof Configuration) {
      api.accessToken = () => token;
    }
  });
};
```

---

## 🔄 Step 7: Use in React Components

```typescript
import { useEffect, useState } from 'react';
import { apiClient } from '@/api/client';
import type { Post } from '@/generated/api';

export const PostList = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        // Full TypeScript autocompletion ✨
        const response = await apiClient.posts.getAllPosts(0, 20);
        setPosts(response || []);
      } catch (error) {
        console.error('Failed to fetch posts:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      {posts.map((post) => (
        <div key={post.id}>
          <h3>{post.title}</h3>
          <p>{post.content}</p>
        </div>
      ))}
    </div>
  );
};
```

---

## 🔑 Step 8: Environment Setup

Create **`.env.local`**:

```
REACT_APP_API_URL=http://localhost:8090/api
```

---

## 📝 Workflow After Setup

**When backend API changes:**

1. Restart backend (if needed)
2. Run: `npm run generate-api`
3. New types auto-generated in `src/generated/api/`
4. TypeScript will show errors if you're using old API signatures
5. Update your component code accordingly
6. Commit generated code to git

---

## ✅ What You Get

- ✨ **100% Type-Safe** - All DTOs, request/response types generated
- 🔄 **Auto-Sync** - Types always match backend
- 🚀 **Zero Runtime Errors** - TypeScript catches API issues at compile time
- 📚 **Full IDE Autocompletion** - IntelliSense for all API methods
- 🛡️ **Industry Standard** - Used by Netflix, Uber, Twitter, etc.

---

## 🆘 Troubleshooting

**Q: Generator not finding backend?**
```bash
# Make sure backend is running on 8090
http://localhost:8090/v3/api-docs
```

**Q: Want to regenerate?**
```bash
rm -rf src/generated/api
npm run generate-api
```

**Q: Custom API methods needed?**

Extend `src/api/client.ts` with custom wrapper methods that use the generated clients.

---

## 📖 Resources

- [OpenAPI Generator Docs](https://openapi-generator.tech/)
- [TypeScript Fetch Client](https://openapi-generator.tech/docs/generators/typescript-fetch/)
- [Springdoc Docs](https://springdoc.org/)

