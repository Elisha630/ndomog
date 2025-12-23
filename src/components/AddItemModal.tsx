import { useState, useEffect } from "react";
import { Package, Upload, Camera, X } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import type { Item } from "./ItemsList";

interface AddItemModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (item: Omit<Item, "id" | "created_by" | "created_at" | "updated_at">) => void;
  editItem?: Item | null;
}

const AddItemModal = ({ open, onClose, onSubmit, editItem }: AddItemModalProps) => {
  const [name, setName] = useState("");
  const [category, setCategory] = useState("");
  const [details, setDetails] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");
  const [buyingPrice, setBuyingPrice] = useState(0);
  const [sellingPrice, setSellingPrice] = useState(0);
  const [quantity, setQuantity] = useState(0);

  useEffect(() => {
    if (editItem) {
      setName(editItem.name);
      setCategory(editItem.category);
      setDetails(editItem.details || "");
      setPhotoUrl(editItem.photo_url || "");
      setBuyingPrice(editItem.buying_price);
      setSellingPrice(editItem.selling_price);
      setQuantity(editItem.quantity);
    } else {
      resetForm();
    }
  }, [editItem, open]);

  const resetForm = () => {
    setName("");
    setCategory("");
    setDetails("");
    setPhotoUrl("");
    setBuyingPrice(0);
    setSellingPrice(0);
    setQuantity(0);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      name,
      category,
      details: details || null,
      photo_url: photoUrl || null,
      buying_price: buyingPrice,
      selling_price: sellingPrice,
      quantity,
    });
    resetForm();
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="bg-popover border-border max-w-md max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-foreground">
            <Package className="text-primary" size={20} />
            {editItem ? "Edit Item" : "Add New Item"}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Item Name *</Label>
            <Input
              id="name"
              placeholder="e.g., Wireless Mouse"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="bg-secondary border-border"
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="category">Type / Category *</Label>
            <Input
              id="category"
              placeholder="e.g., Electronics"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="bg-secondary border-border"
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="details">Details (optional)</Label>
            <Textarea
              id="details"
              placeholder="Additional details about the item..."
              value={details}
              onChange={(e) => setDetails(e.target.value)}
              className="bg-secondary border-border resize-none"
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label>Photo (optional)</Label>
            <div className="grid grid-cols-2 gap-2">
              <Button type="button" variant="secondary" className="h-20 flex-col gap-2">
                <Upload size={20} />
                <span className="text-xs">Upload</span>
              </Button>
              <Button type="button" variant="secondary" className="h-20 flex-col gap-2">
                <Camera size={20} />
                <span className="text-xs">Camera</span>
              </Button>
            </div>
            {photoUrl && (
              <div className="relative mt-2">
                <img src={photoUrl} alt="Preview" className="w-full h-32 object-cover rounded-lg" />
                <Button
                  type="button"
                  variant="destructive"
                  size="icon"
                  className="absolute top-2 right-2 h-6 w-6"
                  onClick={() => setPhotoUrl("")}
                >
                  <X size={12} />
                </Button>
              </div>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="buyingPrice">Buying Price (KES)</Label>
              <Input
                id="buyingPrice"
                type="number"
                min="0"
                value={buyingPrice}
                onChange={(e) => setBuyingPrice(parseInt(e.target.value) || 0)}
                className="bg-secondary border-border"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="sellingPrice">Selling Price (KES)</Label>
              <Input
                id="sellingPrice"
                type="number"
                min="0"
                value={sellingPrice}
                onChange={(e) => setSellingPrice(parseInt(e.target.value) || 0)}
                className="bg-secondary border-border"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="quantity">Quantity</Label>
            <Input
              id="quantity"
              type="number"
              min="0"
              value={quantity}
              onChange={(e) => setQuantity(parseInt(e.target.value) || 0)}
              className="bg-secondary border-border"
            />
          </div>

          <div className="grid grid-cols-2 gap-4 pt-4">
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" className="bg-primary text-primary-foreground hover:bg-primary/90">
              {editItem ? "Save Changes" : "Add Item"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AddItemModal;
