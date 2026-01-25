
# Database Schema Recreation Plan

## Overview

When you remixed the project, a new backend instance was created without the original database schema. The web and Android apps have code that expects specific tables, which is why you're seeing TypeScript errors and the apps can't fetch/store data.

This plan will recreate the complete database schema to match what the code expects.

---

## Tables to Create

Based on the codebase analysis, the following tables are required:

### 1. **profiles** - User profile information
- `id` (UUID, references auth.users)
- `email` (text)
- `username` (text, nullable)
- `avatar_url` (text, nullable)
- `admin_verified` (boolean)
- `verified_by` (UUID, nullable)
- `verified_at` (timestamp, nullable)
- `created_at` (timestamp)

### 2. **items** - Inventory items
- `id` (UUID)
- `name` (text)
- `category` (text)
- `category_id` (UUID, nullable)
- `details` (text, nullable)
- `photo_url` (text, nullable)
- `buying_price` (numeric)
- `selling_price` (numeric)
- `quantity` (integer)
- `low_stock_threshold` (integer, default 5)
- `is_deleted` (boolean, default false)
- `created_by` (UUID, nullable)
- `deleted_at` (timestamp, nullable)
- `deleted_by` (UUID, nullable)
- `created_at`, `updated_at` (timestamps)

### 3. **categories** - Item categories
- `id` (UUID)
- `name` (text)
- `created_by` (UUID, nullable)
- `created_at` (timestamp)

### 4. **notifications** - User notifications
- `id` (UUID)
- `user_id` (UUID)
- `action_user_id` (UUID, nullable) - for Android
- `action_user_email` (text) - for web
- `action` (text)
- `item_name` (text)
- `details` (text, nullable)
- `is_read` (boolean, default false)
- `created_at` (timestamp)

### 5. **activity_logs** - Activity tracking
- `id` (UUID)
- `user_id` (UUID)
- `user_email` (text)
- `action` (text)
- `item_name` (text)
- `details` (text, nullable)
- `created_at` (timestamp)

### 6. **user_pins** - PIN lock feature
- `id` (UUID)
- `user_id` (UUID, unique)
- `pin_hash` (text)
- `is_enabled` (boolean, default true)
- `biometric_enabled` (boolean, default false)
- `created_at`, `updated_at` (timestamps)

### 7. **user_roles** - Role-based access control
- `id` (UUID)
- `user_id` (UUID)
- `role` (app_role enum: admin, moderator, user)
- Unique constraint on (user_id, role)

### 8. **app_releases** - Android APK releases
- `id` (UUID)
- `version` (text, unique)
- `release_date` (date)
- `release_notes` (text)
- `download_url` (text)
- `min_android_version` (text, nullable)
- `file_size_bytes` (bigint, nullable)
- `is_published` (boolean, default false)
- `created_at` (timestamp)

---

## Security Configuration

### Row Level Security (RLS) Policies

All tables will have RLS enabled with appropriate policies:

- **profiles**: Users can read all profiles (for username display), update only their own
- **items**: Authenticated users can CRUD (soft delete pattern)
- **categories**: Authenticated users can read/create
- **notifications**: Users can only read/update/delete their own notifications
- **activity_logs**: Authenticated users can read all, insert their own
- **user_pins**: Users can only access their own PIN data
- **user_roles**: Secured with security definer function to prevent privilege escalation
- **app_releases**: Public read for published releases, admin-only write

### Helper Functions

1. **has_role(user_id, role)** - Security definer function to check user roles without RLS recursion
2. **admin_verify_user(target_user_id)** - Function for admins to verify users
3. **cleanup_old_activity_logs()** - Function to remove logs older than 7 days

### Triggers

1. **handle_new_user** - Creates profile automatically when user signs up
2. **updated_at triggers** - Auto-update timestamps on modification

---

## Storage Buckets

### apk-files bucket
- For storing Android APK releases
- Public read access for downloads
- Authenticated write for admins

### item-photos bucket  
- For storing item images
- Public read access
- Authenticated write access

---

## Technical Implementation

```text
┌─────────────────────────────────────────────────────────────────┐
│                    Database Schema Structure                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  auth.users ──┬──> profiles (1:1)                               │
│               │                                                  │
│               ├──> items (1:many via created_by)                │
│               │                                                  │
│               ├──> notifications (1:many via user_id)           │
│               │                                                  │
│               ├──> activity_logs (1:many via user_id)           │
│               │                                                  │
│               ├──> user_pins (1:1)                              │
│               │                                                  │
│               └──> user_roles (1:many)                          │
│                                                                  │
│  items ──────────> categories (many:1 via category_id)          │
│                                                                  │
│  app_releases (standalone - admin managed)                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Execution Steps

1. Create the `app_role` enum type
2. Create all 8 tables with proper columns and constraints
3. Create the `has_role` security definer function
4. Create the `admin_verify_user` RPC function
5. Create the `cleanup_old_activity_logs` function
6. Enable RLS on all tables
7. Create RLS policies for each table
8. Create triggers for automatic timestamps and profile creation
9. Enable realtime for `items`, `notifications`, and `activity_logs` tables
10. Create storage buckets (`apk-files`, `item-photos`)
11. Configure storage bucket RLS policies

---

## Important Notes

- **Your existing data**: Unfortunately, the data from the original database cannot be automatically migrated since it's in a different instance. You would need to re-add your inventory items.
- **Admin user**: After the schema is created, you'll need to manually add yourself as an admin in the `user_roles` table (I can help with this after setup).
- **Auth configuration**: Email auto-confirm will be enabled so new users don't need to verify email.

---

## Result

After implementation:
- Web app will build without TypeScript errors
- Both web and Android apps will connect to the same database
- All features (items, notifications, activity logs, PIN lock, admin tools) will work
- You can start adding your inventory items again
