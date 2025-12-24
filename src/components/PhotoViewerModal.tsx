import { X } from "lucide-react";
import {
  Dialog,
  DialogContent,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface PhotoViewerModalProps {
  open: boolean;
  onClose: () => void;
  photoUrl: string;
  itemName: string;
}

const PhotoViewerModal = ({ open, onClose, photoUrl, itemName }: PhotoViewerModalProps) => {
  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="bg-popover border-border max-w-3xl p-0 overflow-hidden">
        <div className="relative">
          <Button
            variant="ghost"
            size="icon"
            className="absolute top-2 right-2 z-10 bg-background/80 hover:bg-background text-foreground"
            onClick={onClose}
          >
            <X size={20} />
          </Button>
          <img
            src={photoUrl}
            alt={itemName}
            className="w-full h-auto max-h-[80vh] object-contain"
          />
          <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-background/90 to-transparent">
            <h3 className="text-lg font-semibold text-foreground">{itemName}</h3>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default PhotoViewerModal;
