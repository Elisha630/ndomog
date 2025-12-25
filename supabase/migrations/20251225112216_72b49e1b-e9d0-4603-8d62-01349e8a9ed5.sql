-- Create a table to store user PIN settings
CREATE TABLE public.user_pins (
  id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE UNIQUE,
  pin_hash TEXT NOT NULL,
  is_enabled BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.user_pins ENABLE ROW LEVEL SECURITY;

-- Users can only access their own PIN settings
CREATE POLICY "Users can view their own PIN settings"
ON public.user_pins
FOR SELECT
USING (auth.uid() = user_id);

CREATE POLICY "Users can create their own PIN settings"
ON public.user_pins
FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own PIN settings"
ON public.user_pins
FOR UPDATE
USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own PIN settings"
ON public.user_pins
FOR DELETE
USING (auth.uid() = user_id);

-- Create trigger for updated_at
CREATE TRIGGER update_user_pins_updated_at
BEFORE UPDATE ON public.user_pins
FOR EACH ROW
EXECUTE FUNCTION public.update_updated_at_column();