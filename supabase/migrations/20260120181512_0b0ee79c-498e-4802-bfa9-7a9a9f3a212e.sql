-- Add admin_verified column to profiles table
ALTER TABLE public.profiles 
ADD COLUMN admin_verified boolean NOT NULL DEFAULT false;

-- Add verified_by column to track which admin verified
ALTER TABLE public.profiles 
ADD COLUMN verified_by uuid REFERENCES public.profiles(id);

-- Add verified_at timestamp
ALTER TABLE public.profiles 
ADD COLUMN verified_at timestamp with time zone;

-- Create function for admin to verify users (security definer to bypass RLS)
CREATE OR REPLACE FUNCTION public.admin_verify_user(_target_user_id uuid)
RETURNS boolean
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  -- Check if caller is admin
  IF NOT public.has_role(auth.uid(), 'admin') THEN
    RAISE EXCEPTION 'Only admins can verify users';
  END IF;
  
  -- Update the target user's profile
  UPDATE public.profiles
  SET 
    admin_verified = true,
    verified_by = auth.uid(),
    verified_at = now()
  WHERE id = _target_user_id;
  
  RETURN true;
END;
$$;