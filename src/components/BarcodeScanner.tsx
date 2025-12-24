import { useState, useRef, useEffect } from "react";
import { Scan, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { BrowserMultiFormatReader, IScannerControls } from "@zxing/browser";

interface BarcodeScannerProps {
  onScan: (barcode: string) => void;
}

const BarcodeScanner = ({ onScan }: BarcodeScannerProps) => {
  const [open, setOpen] = useState(false);
  const [scanning, setScanning] = useState(false);
  const videoRef = useRef<HTMLVideoElement>(null);
  const controlsRef = useRef<IScannerControls | null>(null);

  useEffect(() => {
    if (open) {
      startScanning();
    } else {
      stopScanning();
    }

    return () => {
      stopScanning();
    };
  }, [open]);

  const startScanning = async () => {
    setScanning(true);
    try {
      const codeReader = new BrowserMultiFormatReader();
      
      const videoInputDevices = await BrowserMultiFormatReader.listVideoInputDevices();
      
      if (videoInputDevices.length === 0) {
        toast.error("No camera found");
        setOpen(false);
        return;
      }

      // Prefer back camera
      const backCamera = videoInputDevices.find(
        (device) => device.label.toLowerCase().includes("back") || device.label.toLowerCase().includes("rear")
      );
      const selectedDeviceId = backCamera?.deviceId || videoInputDevices[0].deviceId;

      if (videoRef.current) {
        controlsRef.current = await codeReader.decodeFromVideoDevice(
          selectedDeviceId,
          videoRef.current,
          (result) => {
            if (result) {
              const barcode = result.getText();
              toast.success(`Barcode scanned: ${barcode}`);
              onScan(barcode);
              setOpen(false);
            }
          }
        );
      }
    } catch (error) {
      console.error("Error starting barcode scanner:", error);
      toast.error("Could not access camera. Please check permissions.");
      setOpen(false);
    }
  };

  const stopScanning = () => {
    if (controlsRef.current) {
      controlsRef.current.stop();
      controlsRef.current = null;
    }
    setScanning(false);
  };

  return (
    <>
      <Button
        type="button"
        variant="secondary"
        className="flex-1 h-20 flex-col gap-2"
        onClick={() => setOpen(true)}
      >
        <Scan size={20} />
        <span className="text-xs">Scan Barcode</span>
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="bg-popover border-border max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-foreground">
              <Scan className="text-primary" size={20} />
              Scan Barcode
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            <div className="relative rounded-lg overflow-hidden bg-black aspect-video">
              {scanning && (
                <div className="absolute inset-0 flex items-center justify-center z-10">
                  <div className="w-48 h-48 border-2 border-primary rounded-lg animate-pulse" />
                </div>
              )}
              <video
                ref={videoRef}
                className="w-full h-full object-cover"
                autoPlay
                playsInline
                muted
              />
            </div>

            <p className="text-sm text-muted-foreground text-center">
              Point your camera at a barcode to scan
            </p>

            <Button
              variant="secondary"
              className="w-full"
              onClick={() => setOpen(false)}
            >
              <X size={16} className="mr-2" />
              Cancel
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
};

export default BarcodeScanner;
