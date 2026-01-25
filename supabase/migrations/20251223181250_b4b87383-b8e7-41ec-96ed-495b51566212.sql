-- Create storage bucket for item photos
INSERT INTO storage.buckets (id, name, public) VALUES ('item-photos', 'item-photos', true);

-- Allow authenticated users to upload files
CREATE POLICY "Authenticated users can upload item photos"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'item-photos' AND auth.role() = 'authenticated');

-- Allow public read access to item photos
CREATE POLICY "Anyone can view item photos"
ON storage.objects FOR SELECT
USING (bucket_id = 'item-photos');

-- Allow authenticated users to update their uploads
CREATE POLICY "Authenticated users can update item photos"
ON storage.objects FOR UPDATE
USING (bucket_id = 'item-photos' AND auth.role() = 'authenticated');

-- Allow authenticated users to delete item photos
CREATE POLICY "Authenticated users can delete item photos"
ON storage.objects FOR DELETE
USING (bucket_id = 'item-photos' AND auth.role() = 'authenticated');

-- Add low_stock_threshold column to items table
ALTER TABLE public.items ADD COLUMN low_stock_threshold integer NOT NULL DEFAULT 5;